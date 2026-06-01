require('dotenv').config();
const express = require('express');
const Anthropic = require('@anthropic-ai/sdk');
const path = require('path');
const { buildBaseSystemPrompt, buildEnhancedSystemPrompt, loadTemplateContent } = require('./lib/skill-loader');
const { parseFiles, detectStep } = require('./lib/code-parser');
const { generateZip } = require('./lib/zip-generator');

const app = express();
app.use(express.json());
app.use(express.static(path.join(__dirname, 'public')));

// API 客户端
const client = new Anthropic({
  apiKey: process.env.ANTHROPIC_API_KEY,
  baseURL: process.env.ANTHROPIC_BASE_URL,
});
const MODEL = process.env.ANTHROPIC_MODEL || 'glm-5.1';

// 会话存储（内存，重启丢失）
const sessions = new Map();

function getSession(id) {
  if (!sessions.has(id)) {
    sessions.set(id, {
      messages: [],
      currentStep: null,
      projectName: 'generated-project',
    });
  }
  return sessions.get(id);
}

// 聊天接口（SSE 流式）
app.post('/api/chat', async (req, res) => {
  const { message, sessionId } = req.body;
  if (!message || !sessionId) {
    return res.status(400).json({ error: '缺少 message 或 sessionId' });
  }

  const session = getSession(sessionId);
  session.messages.push({ role: 'user', content: message });

  // 构建 System Prompt
  const detectedStep = detectStep(message);
  if (detectedStep) {
    session.currentStep = detectedStep;
  }

  let systemPrompt;
  if (session.currentStep && session.currentStep !== 'step1') {
    systemPrompt = buildEnhancedSystemPrompt(session.currentStep);
  } else {
    systemPrompt = buildBaseSystemPrompt();
  }

  // SSE 响应
  res.setHeader('Content-Type', 'text/event-stream');
  res.setHeader('Cache-Control', 'no-cache');
  res.setHeader('Connection', 'keep-alive');

  try {
    const stream = client.messages.stream({
      model: MODEL,
      max_tokens: 8192,
      system: systemPrompt,
      messages: session.messages,
    });

    let fullResponse = '';

    stream.on('text', (text) => {
      fullResponse += text;
      res.write(`data: ${JSON.stringify({ type: 'text', content: text })}\n\n`);
    });

    stream.on('end', () => {
      session.messages.push({ role: 'assistant', content: fullResponse });

      // 检测回复中的步骤
      const replyStep = detectStep(fullResponse);
      if (replyStep) {
        session.currentStep = replyStep;
      }

      // 检测回复中是否包含文件（用于显示下载按钮）
      const files = parseFiles(fullResponse);
      if (files.length > 0) {
        res.write(`data: ${JSON.stringify({ type: 'files', count: files.length })}\n\n`);
      }

      res.write(`data: ${JSON.stringify({ type: 'done' })}\n\n`);
      res.end();
    });

    stream.on('error', (err) => {
      console.error('Stream error:', err);
      res.write(`data: ${JSON.stringify({ type: 'error', content: err.message })}\n\n`);
      res.end();
    });

    req.on('close', () => {
      stream.stop();
    });
  } catch (err) {
    console.error('API error:', err);
    res.write(`data: ${JSON.stringify({ type: 'error', content: err.message })}\n\n`);
    res.end();
  }
});

// 生成代码并返回 ZIP
app.post('/api/generate', async (req, res) => {
  const { sessionId, projectName } = req.body;
  if (!sessionId) {
    return res.status(400).json({ error: '缺少 sessionId' });
  }

  const session = getSession(sessionId);
  if (projectName) {
    session.projectName = projectName;
  }

  // 构建 Step 5 增强版 System Prompt（带模板内容）
  let systemPrompt = buildEnhancedSystemPrompt('step5');

  // 加载关键模板内容注入 System Prompt
  const keyTemplates = [
    'entity.java', 'service-impl.java', 'controller.java',
    'mapper.java', 'dto-request.java', 'dto-response.java',
    'converter.java', 'service.java', 'pom.xml', 'application.yml',
    'test-service.java', 'test-controller.java', 'test-mapper.java'
  ];

  systemPrompt += '\n\n## 关键模板内容（生成代码时参考以下格式）\n\n';
  for (const t of keyTemplates) {
    const content = loadTemplateContent(t);
    if (content) {
      systemPrompt += `### ${t}\n\`\`\`\n${content}\n\`\`\`\n\n`;
    }
  }

  const generateMessage = `现在请执行 Step 5：根据以上所有确认的需求和设计，一次性生成完整的工业级代码。

要求：
1. 使用 "===== 文件: 路径 =====" 格式输出每个文件
2. 包含所有层：Entity、DTO、Mapper、Service、Controller、Converter、配置、测试、设计文档、Postman集合
3. 按照 skill.md 的规范生成，注意版权头、类注释、方法注释、并发控制、SQL 性能
4. 单元测试使用 JUnit 4，所有类和方法加 public
5. 项目名使用: ${session.projectName}`;

  // SSE 流式返回生成过程
  res.setHeader('Content-Type', 'text/event-stream');
  res.setHeader('Cache-Control', 'no-cache');
  res.setHeader('Connection', 'keep-alive');

  const messages = [...session.messages, { role: 'user', content: generateMessage }];

  try {
    const stream = client.messages.stream({
      model: MODEL,
      max_tokens: 16384,
      system: systemPrompt,
      messages: messages,
    });

    let fullResponse = '';

    stream.on('text', (text) => {
      fullResponse += text;
      res.write(`data: ${JSON.stringify({ type: 'text', content: text })}\n\n`);
    });

    stream.on('end', async () => {
      session.messages.push({ role: 'user', content: generateMessage });
      session.messages.push({ role: 'assistant', content: fullResponse });

      const files = parseFiles(fullResponse);
      if (files.length > 0) {
        // 将文件列表存入会话，供后续下载
        session.generatedFiles = files;
        res.write(`data: ${JSON.stringify({ type: 'files', count: files.length })}\n\n`);
      }
      res.write(`data: ${JSON.stringify({ type: 'done' })}\n\n`);
      res.end();
    });

    stream.on('error', (err) => {
      console.error('Generate stream error:', err);
      res.write(`data: ${JSON.stringify({ type: 'error', content: err.message })}\n\n`);
      res.end();
    });
  } catch (err) {
    console.error('Generate error:', err);
    res.write(`data: ${JSON.stringify({ type: 'error', content: err.message })}\n\n`);
    res.end();
  }
});

// 下载 ZIP
app.post('/api/download', async (req, res) => {
  const { sessionId } = req.body;
  const session = getSession(sessionId);

  if (!session.generatedFiles || session.generatedFiles.length === 0) {
    return res.status(400).json({ error: '没有可下载的文件，请先生成代码' });
  }

  try {
    const zipBuffer = await generateZip(session.generatedFiles, session.projectName);
    const filename = `${session.projectName}.zip`;

    res.setHeader('Content-Type', 'application/zip');
    res.setHeader('Content-Disposition', `attachment; filename="${filename}"`);
    res.send(zipBuffer);
  } catch (err) {
    console.error('ZIP error:', err);
    res.status(500).json({ error: 'ZIP 打包失败: ' + err.message });
  }
});

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
  console.log(`Java 代码生成 Agent 已启动: http://localhost:${PORT}`);
});

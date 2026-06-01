require('dotenv').config();
const express = require('express');
const https = require('https');
const path = require('path');
const { buildBaseSystemPrompt, buildEnhancedSystemPrompt, loadTemplateContent } = require('./lib/skill-loader');
const { parseFiles, detectStep } = require('./lib/code-parser');
const { generateZip } = require('./lib/zip-generator');

const app = express();
app.use(express.json());
app.use(express.static(path.join(__dirname, 'public')));

const ZHIPU_API_KEY = process.env.ZHIPU_API_KEY || '';
const MODEL = process.env.ZHIPU_MODEL || 'glm-4-flash';
console.log('使用模型:', MODEL, '| API Key前8位:', ZHIPU_API_KEY.slice(0, 8));

function zhipuStream(messages, systemPrompt, maxTokens, onText, onDone, onError) {
  const body = JSON.stringify({
    model: MODEL,
    max_tokens: maxTokens,
    stream: true,
    messages: [{ role: 'system', content: systemPrompt }, ...messages],
  });

  const options = {
    hostname: 'open.bigmodel.cn',
    path: '/api/paas/v4/chat/completions',
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': 'Bearer ' + ZHIPU_API_KEY,
      'Content-Length': Buffer.byteLength(body),
    },
  };

  const req = https.request(options, (res) => {
    console.log('智谱 API 状态码:', res.statusCode);
    let buffer = '';

    res.on('data', (chunk) => {
      buffer += chunk.toString();
      const lines = buffer.split('\n');
      buffer = lines.pop();

      for (const line of lines) {
        if (!line.startsWith('data: ')) continue;
        const data = line.slice(6).trim();
        if (data === '[DONE]') { onDone(); return; }
        try {
          const json = JSON.parse(data);
          const text = json.choices?.[0]?.delta?.content || '';
          if (text) onText(text);
        } catch (e) {
          console.error('parse error:', e.message, 'data:', data);
        }
      }
    });

    res.on('end', () => {
      if (buffer.startsWith('data: ')) {
        const data = buffer.slice(6).trim();
        if (data !== '[DONE]') {
          try {
            const json = JSON.parse(data);
            const text = json.choices?.[0]?.delta?.content || '';
            if (text) onText(text);
          } catch (e) {}
        }
      }
      onDone();
    });

    res.on('error', onError);
  });

  req.on('error', onError);
  req.write(body);
  req.end();
}

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

app.post('/api/chat', (req, res) => {
  const { message, sessionId } = req.body;
  if (!message || !sessionId) {
    return res.status(400).json({ error: '缺少 message 或 sessionId' });
  }

  const session = getSession(sessionId);
  session.messages.push({ role: 'user', content: message });

  const detectedStep = detectStep(message);
  if (detectedStep) session.currentStep = detectedStep;

  let systemPrompt;
  if (session.currentStep && session.currentStep !== 'step1') {
    systemPrompt = buildEnhancedSystemPrompt(session.currentStep);
  } else {
    systemPrompt = buildBaseSystemPrompt();
  }

  res.setHeader('Content-Type', 'text/event-stream');
  res.setHeader('Cache-Control', 'no-cache');
  res.setHeader('Connection', 'keep-alive');

  let fullResponse = '';

  zhipuStream(
    session.messages,
    systemPrompt,
    8192,
    (text) => {
      fullResponse += text;
      res.write('data: ' + JSON.stringify({ type: 'text', content: text }) + '\n\n');
    },
    () => {
      session.messages.push({ role: 'assistant', content: fullResponse });
      const replyStep = detectStep(fullResponse);
      if (replyStep) session.currentStep = replyStep;
      const files = parseFiles(fullResponse);
      if (files.length > 0) {
        res.write('data: ' + JSON.stringify({ type: 'files', count: files.length }) + '\n\n');
      }
      res.write('data: ' + JSON.stringify({ type: 'done' }) + '\n\n');
      res.end();
    },
    (err) => {
      console.error('API error:', err);
      res.write('data: ' + JSON.stringify({ type: 'error', content: err.message }) + '\n\n');
      res.end();
    }
  );

  req.on('close', () => {});
});

app.post('/api/generate', (req, res) => {
  const { sessionId, projectName } = req.body;
  if (!sessionId) {
    return res.status(400).json({ error: '缺少 sessionId' });
  }

  const session = getSession(sessionId);
  if (projectName) session.projectName = projectName;

  let systemPrompt = buildEnhancedSystemPrompt('step5');

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
      systemPrompt += '### ' + t + '\n```\n' + content + '\n```\n\n';
    }
  }

  const generateMessage = '现在请执行 Step 5：根据以上所有确认的需求和设计，一次性生成完整的工业级代码。\n\n要求：\n1. 使用 "===== 文件: 路径 =====" 格式输出每个文件\n2. 包含所有层：Entity、DTO、Mapper、Service、Controller、Converter、配置、测试、设计文档、Postman集合\n3. 按照 skill.md 的规范生成，注意版权头、类注释、方法注释、并发控制、SQL 性能\n4. 单元测试使用 JUnit 4，所有类和方法加 public\n5. 项目名使用: ' + session.projectName;

  res.setHeader('Content-Type', 'text/event-stream');
  res.setHeader('Cache-Control', 'no-cache');
  res.setHeader('Connection', 'keep-alive');

  const messages = [...session.messages, { role: 'user', content: generateMessage }];
  let fullResponse = '';

  zhipuStream(
    messages,
    systemPrompt,
    16384,
    (text) => {
      fullResponse += text;
      res.write('data: ' + JSON.stringify({ type: 'text', content: text }) + '\n\n');
    },
    () => {
      session.messages.push({ role: 'user', content: generateMessage });
      session.messages.push({ role: 'assistant', content: fullResponse });
      const files = parseFiles(fullResponse);
      if (files.length > 0) {
        session.generatedFiles = files;
        res.write('data: ' + JSON.stringify({ type: 'files', count: files.length }) + '\n\n');
      }
      res.write('data: ' + JSON.stringify({ type: 'done' }) + '\n\n');
      res.end();
    },
    (err) => {
      console.error('Generate error:', err);
      res.write('data: ' + JSON.stringify({ type: 'error', content: err.message }) + '\n\n');
      res.end();
    }
  );
});

app.post('/api/download', async (req, res) => {
  const { sessionId } = req.body;
  const session = getSession(sessionId);

  if (!session.generatedFiles || session.generatedFiles.length === 0) {
    return res.status(400).json({ error: '没有可下载的文件，请先生成代码' });
  }

  try {
    const zipBuffer = await generateZip(session.generatedFiles, session.projectName);
    const filename = session.projectName + '.zip';
    res.setHeader('Content-Type', 'application/zip');
    res.setHeader('Content-Disposition', 'attachment; filename="' + filename + '"');
    res.send(zipBuffer);
  } catch (err) {
    console.error('ZIP error:', err);
    res.status(500).json({ error: 'ZIP 打包失败: ' + err.message });
  }
});

app.get('/api/files', (req, res) => {
  const { sessionId } = req.query;
  const session = sessions.get(sessionId);
  if (!session || !session.generatedFiles || session.generatedFiles.length === 0) {
    return res.status(404).json({ error: '没有可用的文件，请先生成代码' });
  }
  res.json({ files: session.generatedFiles });
});

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
  console.log('Java 代码生成 Agent 已启动: http://localhost:' + PORT);
});

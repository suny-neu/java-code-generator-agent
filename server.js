require('dotenv').config();
  const express = require('express');
  const { OpenAI } = require('openai');
  const path = require('path');
  const { buildBaseSystemPrompt, buildEnhancedSystemPrompt, loadTemplateContent
  } = require('./lib/skill-loader');
  const { parseFiles, detectStep } = require('./lib/code-parser');
  const { generateZip } = require('./lib/zip-generator');

  const app = express();
  app.use(express.json());
  app.use(express.static(path.join(__dirname, 'public')));

  const client = new OpenAI({
    apiKey: process.env.ZHIPU_API_KEY,
    baseURL: 'https://open.bigmodel.cn/api/paas/v4/',
  });
  const MODEL = process.env.ZHIPU_MODEL || 'glm-4-flash';

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

  app.post('/api/chat', async (req, res) => {
    const { message, sessionId } = req.body;
    if (!message || !sessionId) {
      return res.status(400).json({ error: '缺少 message 或 sessionId' });
    }

    const session = getSession(sessionId);
    session.messages.push({ role: 'user', content: message });

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

    res.setHeader('Content-Type', 'text/event-stream');
    res.setHeader('Cache-Control', 'no-cache');
    res.setHeader('Connection', 'keep-alive');

    let aborted = false;
    req.on('close', () => { aborted = true; });

    try {
      const stream = await client.chat.completions.create({
        model: MODEL,
        max_tokens: 8192,
        messages: [{ role: 'system', content: systemPrompt },
  ...session.messages],
        stream: true,
      });

      let fullResponse = '';

      for await (const chunk of stream) {
        if (aborted) break;
        const text = chunk.choices[0]?.delta?.content || '';
        if (text) {
          fullResponse += text;
          res.write(`data: ${JSON.stringify({ type: 'text', content: text
  })}\n\n`);
        }
      }

      session.messages.push({ role: 'assistant', content: fullResponse });

      const replyStep = detectStep(fullResponse);
      if (replyStep) {
        session.currentStep = replyStep;
      }

      const files = parseFiles(fullResponse);
      if (files.length > 0) {
        res.write(`data: ${JSON.stringify({ type: 'files', count: files.length
  })}\n\n`);
      }

      res.write(`data: ${JSON.stringify({ type: 'done' })}\n\n`);
      res.end();
    } catch (err) {
      console.error('API error:', err);
      res.write(`data: ${JSON.stringify({ type: 'error', content: err.message
  })}\n\n`);
      res.end();
    }
  });

  app.post('/api/generate', async (req, res) => {
    const { sessionId, projectName } = req.body;
    if (!sessionId) {
      return res.status(400).json({ error: '缺少 sessionId' });
    }

    const session = getSession(sessionId);
    if (projectName) {
      session.projectName = projectName;
    }

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

    const generateMessage = `现在请执行 Step
  5：根据以上所有确认的需求和设计，一次性生成完整的工业级代码。

  要求：
  1. 使用 "===== 文件: 路径 =====" 格式输出每个文件
  2. 包含所有层：Entity、DTO、Mapper、Service、Controller、Converter、配置、测试
  、设计文档、Postman集合
  3. 按照 skill.md 的规范生成，注意版权头、类注释、方法注释、并发控制、SQL 性能
  4. 单元测试使用 JUnit 4，所有类和方法加 public
  5. 项目名使用: ${session.projectName}`;

    res.setHeader('Content-Type', 'text/event-stream');
    res.setHeader('Cache-Control', 'no-cache');
    res.setHeader('Connection', 'keep-alive');

    const messages = [...session.messages, { role: 'user', content:
  generateMessage }];

    try {
      const stream = await client.chat.completions.create({
        model: MODEL,
        max_tokens: 16384,
        messages: [{ role: 'system', content: systemPrompt }, ...messages],
        stream: true,
      });

      let fullResponse = '';

      for await (const chunk of stream) {
        const text = chunk.choices[0]?.delta?.content || '';
        if (text) {
          fullResponse += text;
          res.write(`data: ${JSON.stringify({ type: 'text', content: text
  })}\n\n`);
        }
      }

      session.messages.push({ role: 'user', content: generateMessage });
      session.messages.push({ role: 'assistant', content: fullResponse });

      const files = parseFiles(fullResponse);
      if (files.length > 0) {
        session.generatedFiles = files;
        res.write(`data: ${JSON.stringify({ type: 'files', count: files.length
  })}\n\n`);
      }
      res.write(`data: ${JSON.stringify({ type: 'done' })}\n\n`);
      res.end();
    } catch (err) {
      console.error('Generate error:', err);
      res.write(`data: ${JSON.stringify({ type: 'error', content: err.message
  })}\n\n`);
      res.end();
    }
  });

  app.post('/api/download', async (req, res) => {
    const { sessionId } = req.body;
    const session = getSession(sessionId);

    if (!session.generatedFiles || session.generatedFiles.length === 0) {
      return res.status(400).json({ error: '没有可下载的文件，请先生成代码' });
    }

    try {
      const zipBuffer = await generateZip(session.generatedFiles,
  session.projectName);
      const filename = `${session.projectName}.zip`;

      res.setHeader('Content-Type', 'application/zip');
      res.setHeader('Content-Disposition', `attachment;
  filename="${filename}"`);
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

  ---
  package.json — 全部替换成这个：

  {
    "name": "java-code-generator-agent",
    "version": "1.0.0",
    "description": "Java代码生成Web Agent - 基于SOP的工业级代码生成",
    "main": "server.js",
    "scripts": {
      "start": "node server.js",
      "dev": "node --watch server.js"
    },
    "dependencies": {
      "openai": "^4.0.0",
      "archiver": "^7.0.1",
      "dotenv": "^16.4.5",
      "express": "^4.21.0"
    }
  }

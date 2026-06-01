/**
 * 从 AI 回复中解析出代码文件
 *
 * 支持两种格式：
 * 格式1: ===== 文件: path/to/File.java =====
 * 格式2: ### 文件: path/to/File.java  (```代码块```)
 */

/**
 * 解析 AI 回复，提取所有文件
 * @param {string} text - AI 完整回复
 * @returns {Array<{path: string, content: string}>} 文件列表
 */
function parseFiles(text) {
  const files = [];

  // 格式1: ===== 文件: path ===== 模式
  const pattern1 = /={3,}\s*文件:\s*(.+?)\s*={3,}\n([\s\S]*?)(?==={3,}\s*文件:|$)/g;
  let match;
  while ((match = pattern1.exec(text)) !== null) {
    const filePath = match[1].trim();
    let content = match[2].trim();
    files.push({ path: filePath, content });
  }

  // 如果格式1没匹配到，尝试格式2: ### 文件: path + ```代码块```
  if (files.length === 0) {
    const pattern2 = /#{1,4}\s*文件:\s*(.+?)\s*\n```(?:\w+)?\n([\s\S]*?)```/g;
    while ((match = pattern2.exec(text)) !== null) {
      const filePath = match[1].trim();
      const content = match[2].trim();
      files.push({ path: filePath, content });
    }
  }

  // 如果还是没匹配到，尝试通用的 ```代码块``` + 前面有文件路径的模式
  if (files.length === 0) {
    const pattern3 = /(?:\/\/|<!--|#)\s*(?:文件|File):\s*(.+?)\s*\n```(?:\w+)?\n([\s\S]*?)```/g;
    while ((match = pattern3.exec(text)) !== null) {
      const filePath = match[1].trim();
      const content = match[2].trim();
      files.push({ path: filePath, content });
    }
  }

  return files;
}

/**
 * 检测当前处于 SOP 的哪个阶段
 * @param {string} text - AI 回复
 * @returns {string} 阶段标识 (step1 ~ step6)
 */
function detectStep(text) {
  const stepPatterns = [
    { step: 'step1', patterns: [/环境.*校验|Step\s*1|配置.*检测/i] },
    { step: 'step2', patterns: [/需求.*解构|Step\s*2|并发.*识别|数据量级/i] },
    { step: 'step3', patterns: [/领域模型|表结构|Step\s*3|DDL|实体.*设计/i] },
    { step: 'step3.5', patterns: [/设计文档|概要设计|HLD|LLD|Step\s*3\.5/i] },
    { step: 'step4', patterns: [/依赖.*审查|Step\s*4|pom\.xml|依赖.*审查/i] },
    { step: 'step5', patterns: [/代码.*落地|Step\s*5|生成.*代码|单元测试/i] },
    { step: 'step6', patterns: [/测试.*调优|Step\s*6|修复.*Bug/i] },
  ];

  for (const { step, patterns } of stepPatterns) {
    for (const p of patterns) {
      if (p.test(text)) return step;
    }
  }
  return null;
}

module.exports = { parseFiles, detectStep };

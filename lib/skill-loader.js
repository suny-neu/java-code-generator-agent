const fs = require('fs');
const path = require('path');

const SKILL_ROOT = path.resolve(__dirname, '..', 'skill-data');

/**
 * 按需加载参考文档的映射
 * key = SOP 阶段，value = 需要加载的文档列表
 */
const REFERENCE_MAP = {
  'step2': ['sql-performance.md'],
  'step3': ['mybatis-plus-guide.md', 'naming-conventions.md'],
  'step4': ['mapstruct-guide.md'],
  'step5': ['test-patterns.md', 'mapstruct-guide.md', 'mybatis-plus-guide.md', 'sql-performance.md'],
  'all': null // null 表示加载全部
};

/**
 * 加载 skill.md 核心内容（去除 frontmatter）
 */
function loadSkillCore() {
  const skillPath = path.join(SKILL_ROOT, 'skill.md');
  const content = fs.readFileSync(skillPath, 'utf-8');
  // 去掉 frontmatter（--- 之间的内容）
  return content.replace(/^---\n[\s\S]*?\n---\n/, '');
}

/**
 * 加载指定参考文档
 */
function loadReferences(docNames) {
  if (!docNames) {
    // 加载全部
    const refsDir = path.join(SKILL_ROOT, 'references');
    const files = fs.readdirSync(refsDir).filter(f => f.endsWith('.md'));
    return files.map(f => ({
      name: f,
      content: fs.readFileSync(path.join(refsDir, f), 'utf-8')
    }));
  }
  return docNames.map(name => {
    const filePath = path.join(SKILL_ROOT, 'references', name);
    if (fs.existsSync(filePath)) {
      return { name, content: fs.readFileSync(filePath, 'utf-8') };
    }
    return null;
  }).filter(Boolean);
}

/**
 * 加载代码模板文件
 */
function loadTemplates() {
  const templatesDir = path.join(SKILL_ROOT, 'assets', 'templates');
  const files = fs.readdirSync(templatesDir);
  return files.map(f => ({
    name: f,
    content: fs.readFileSync(path.join(templatesDir, f), 'utf-8')
  }));
}

/**
 * 加载公共组件模板
 */
function loadCommons() {
  const commonsDir = path.join(SKILL_ROOT, 'assets', 'commons');
  const files = fs.readdirSync(commonsDir);
  return files.map(f => ({
    name: f,
    content: fs.readFileSync(path.join(commonsDir, f), 'utf-8')
  }));
}

/**
 * 构建基础 System Prompt（精简版，约 10K tokens）
 */
function buildBaseSystemPrompt() {
  const skillCore = loadSkillCore();
  const templates = loadTemplates();

  let prompt = skillCore;

  // 追加模板文件列表（只列文件名，不加载内容，节省 token）
  prompt += '\n\n## 可用的代码模板文件\n\n';
  prompt += '生成代码时，你需要在回复中直接输出完整的文件内容。以下模板供你参考格式：\n\n';
  for (const t of templates) {
    prompt += `- \`${t.name}\`\n`;
  }

  // 追加输出格式约定
  prompt += `

## 输出格式约定（强制）

当你需要输出多个代码文件时，**必须**使用以下格式：

===== 文件: 相对路径/FileName.java =====
文件完整内容

===== 文件: 另一个路径/AnotherFile.java =====
另一个文件的完整内容

注意：
- 每个文件用 "===== 文件: 路径 =====" 开头
- 路径使用相对于项目根目录的相对路径
- 文件内容紧接着标记下方，直到下一个 "===== 文件:" 或回复结束
- 不要使用 markdown 代码块包裹整个文件内容
`;

  return prompt;
}

/**
 * 构建增强 System Prompt（带参考文档，用于特定阶段）
 */
function buildEnhancedSystemPrompt(step) {
  let prompt = buildBaseSystemPrompt();

  const docNames = REFERENCE_MAP[step];
  if (docNames) {
    const refs = loadReferences(docNames);
    prompt += '\n\n## 参考文档\n\n';
    for (const ref of refs) {
      prompt += `### ${ref.name}\n\n${ref.content}\n\n`;
    }
  }

  return prompt;
}

/**
 * 加载模板内容（用于生成代码时注入到上下文）
 */
function loadTemplateContent(templateName) {
  const templatesDir = path.join(SKILL_ROOT, 'assets', 'templates');
  const filePath = path.join(templatesDir, templateName);
  if (fs.existsSync(filePath)) {
    return fs.readFileSync(filePath, 'utf-8');
  }
  return null;
}

module.exports = {
  buildBaseSystemPrompt,
  buildEnhancedSystemPrompt,
  loadTemplates,
  loadCommons,
  loadTemplateContent,
  loadReferences,
  REFERENCE_MAP
};

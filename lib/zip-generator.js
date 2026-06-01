const archiver = require('archiver');

/**
 * 将文件列表打包成 ZIP
 * @param {Array<{path: string, content: string}>} files - 文件列表
 * @param {string} projectDir - 项目根目录名（ZIP 内的顶层目录）
 * @returns {Promise<Buffer>} ZIP Buffer
 */
function generateZip(files, projectDir = 'generated-project') {
  return new Promise((resolve, reject) => {
    const chunks = [];
    const archive = archiver('zip', { zlib: { level: 9 } });

    archive.on('data', chunk => chunks.push(chunk));
    archive.on('end', () => resolve(Buffer.concat(chunks)));
    archive.on('error', err => reject(err));

    for (const file of files) {
      // 去除路径开头的斜杠
      const cleanPath = file.path.replace(/^\/+/, '');
      archive.append(file.content, { name: `${projectDir}/${cleanPath}` });
    }

    archive.finalize();
  });
}

module.exports = { generateZip };

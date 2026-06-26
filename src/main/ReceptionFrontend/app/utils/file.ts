/**
 * 浏览器文件下载工具。
 *
 * 通过构造 Blob 并触发临时 <a download> 点击实现纯前端下载，
 * 不依赖服务端，在 HTTP / HTTPS 环境均可工作。
 */

/**
 * 将文本内容作为文件下载。
 * @param filename 下载文件名
 * @param content 文本内容
 * @param mimeType MIME 类型，默认纯文本（UTF-8）
 */
export function downloadTextFile(
  filename: string,
  content: string,
  mimeType = 'text/plain;charset=utf-8',
): void {
  const blob = new Blob([content], {type: `${mimeType}`})
  const url = URL.createObjectURL(blob)

  const anchor = document.createElement('a')
  anchor.href = url
  anchor.download = filename
  anchor.rel = 'noopener'
  // 需挂到 DOM 才能在部分浏览器触发点击下载
  document.body.appendChild(anchor)
  anchor.click()
  document.body.removeChild(anchor)

  // 释放对象 URL，避免内存泄漏
  URL.revokeObjectURL(url)
}

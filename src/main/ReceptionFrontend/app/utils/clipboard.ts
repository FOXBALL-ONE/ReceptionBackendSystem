/**
 * 剪贴板工具。
 *
 * 浏览器的 Clipboard API（navigator.clipboard.writeText）仅在安全上下文
 * （HTTPS 或 localhost）下可用，且对页面失焦/权限拒绝很敏感；在内网用 HTTP
 * 部署时会因 navigator.clipboard 为 undefined 而静默失败。这里在 Clipboard
 * API 不可用或抛错时回退到隐藏 textarea + document.execCommand('copy')，
 * 使「复制」在 HTTP 环境也能工作。
 */

/**
 * 将文本写入剪贴板。
 * @param text 待复制文本
 * @returns 是否复制成功
 */
export async function copyToClipboard(text: string): Promise<boolean> {
  if (!text) return false

  // 优先使用 Clipboard API（需安全上下文）
  if (window.isSecureContext && navigator.clipboard) {
    try {
      await navigator.clipboard.writeText(text)
      return true
    } catch {
      // 失焦或权限被拒时回退
    }
  }

  return legacyCopy(text)
}

/** 兜底实现：临时 textarea + execCommand('copy')，兼容非安全上下文。 */
function legacyCopy(text: string): boolean {
  const textarea = document.createElement('textarea')
  textarea.value = text
  textarea.setAttribute('readonly', '')
  // 放到屏幕外并隐藏，避免页面跳动和移动端键盘弹出
  textarea.style.position = 'fixed'
  textarea.style.top = '-9999px'
  textarea.style.left = '-9999px'
  textarea.style.opacity = '0'
  document.body.appendChild(textarea)

  // iOS 需先创建可编辑范围才能 select
  const selection = document.getSelection()
  const range = document.createRange()
  range.selectNodeContents(textarea)
  selection?.removeAllRanges()
  selection?.addRange(range)
  textarea.setSelectionRange(0, text.length)

  let ok = false
  try {
    ok = document.execCommand('copy')
  } catch {
    ok = false
  } finally {
    document.body.removeChild(textarea)
    selection?.removeAllRanges()
  }
  return ok
}

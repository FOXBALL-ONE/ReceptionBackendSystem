/**
 * 将各种时间格式转换为 ISO 8601 格式用于传输到后端
 */

/**
 * 将日期时间转换为 ISO 8601 格式字符串
 * @param value - 日期时间值（Date、时间戳、字符串）
 * @returns ISO 8601 格式的字符串，如果无效则返回 null
 */
export function toISO8601DateTime(value: unknown): string | null {
  if (!value) return null

  let date: Date | null = null

  if (value instanceof Date) {
    date = value
  } else if (typeof value === 'number') {
    date = new Date(value)
  } else if (typeof value === 'string') {
    const trimmed = value.trim()
    if (!trimmed) return null

    // 尝试解析各种格式的日期时间字符串
    // 支持: yyyy-MM-dd HH:mm, yyyy:MM:dd:HH:mm, yyyy/MM/dd HH:mm 等
    const dateTimeMatch = trimmed.match(
      /^(\d{4})[-/:](\d{2})[-/:](\d{2})(?:[ T:](\d{2}):(\d{2})(?::(\d{2}))?)?/
    )

    if (dateTimeMatch) {
      const [, year, month, day, hours = '00', minutes = '00', seconds = '00'] = dateTimeMatch
      date = new Date(
        Number(year),
        Number(month) - 1,
        Number(day),
        Number(hours),
        Number(minutes),
        Number(seconds)
      )
    } else {
      // 尝试直接解析
      date = new Date(trimmed)
    }
  }

  if (!date || Number.isNaN(date.getTime())) {
    return null
  }

  // 返回 ISO 8601 格式: YYYY-MM-DDTHH:mm:ss.sssZ
  return date.toISOString()
}

/**
 * 将日期转换为 ISO 8601 日期格式字符串（不含时间）
 * @param value - 日期值（Date、时间戳、字符串）
 * @returns ISO 8601 日期格式的字符串 (YYYY-MM-DD)，如果无效则返回 null
 */
export function toISO8601Date(value: unknown): string | null {
  if (!value) return null

  let date: Date | null = null

  if (value instanceof Date) {
    date = value
  } else if (typeof value === 'number') {
    date = new Date(value)
  } else if (typeof value === 'string') {
    const trimmed = value.trim()
    if (!trimmed) return null

    // 支持: yyyy-MM-dd, yyyy:MM:dd, yyyy/MM/dd 等
    const dateMatch = trimmed.match(/^(\d{4})[-/:](\d{2})[-/:](\d{2})/)

    if (dateMatch) {
      const [, year, month, day] = dateMatch
      date = new Date(Number(year), Number(month) - 1, Number(day))
    } else {
      date = new Date(trimmed)
    }
  }

  if (!date || Number.isNaN(date.getTime())) {
    return null
  }

  // 返回 ISO 8601 日期格式: YYYY-MM-DD
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')

  return `${year}-${month}-${day}`
}

/**
 * 将时间转换为 ISO 8601 时间格式字符串（不含日期）
 * @param value - 时间值（字符串格式如 "HH:mm" 或 "HH:mm:ss"）
 * @returns ISO 8601 时间格式的字符串 (HH:mm:ss)，如果无效则返回 null
 */
export function toISO8601Time(value: unknown): string | null {
  if (!value) return null

  if (typeof value === 'string') {
    const trimmed = value.trim()
    if (!trimmed) return null

    // 支持: HH:mm 或 HH:mm:ss
    const timeMatch = trimmed.match(/^(\d{1,2}):(\d{2})(?::(\d{2}))?/)

    if (timeMatch) {
      const [, hours, minutes, seconds = '00'] = timeMatch
      const h = Number(hours)
      const m = Number(minutes)
      const s = Number(seconds)

      if (h >= 0 && h <= 23 && m >= 0 && m <= 59 && s >= 0 && s <= 59) {
        return `${String(h).padStart(2, '0')}:${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`
      }
    }
  }

  return null
}

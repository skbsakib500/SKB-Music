package com.skb.music.lyrics

data class LyricLine(val timeMs: Long, val text: String)

object LrcParser {

    private val TAG_RE = Regex("""\[(\d{1,3}):(\d{1,2})(?:[.:](\d{1,3}))?]""")

    fun parse(content: String): List<LyricLine> {
        val lines = mutableListOf<LyricLine>()
        content.lineSequence().forEach { raw ->
            val matches = TAG_RE.findAll(raw).toList()
            if (matches.isEmpty()) return@forEach
            val text = raw.substringAfterLast(']').trim()
            matches.forEach { m ->
                val min = m.groupValues[1].toIntOrNull() ?: 0
                val sec = m.groupValues[2].toIntOrNull() ?: 0
                val msStr = m.groupValues[3]
                val ms = when (msStr.length) {
                    0 -> 0
                    1 -> msStr.toInt() * 100
                    2 -> msStr.toInt() * 10
                    3 -> msStr.toInt()
                    else -> msStr.take(3).toInt()
                }
                lines += LyricLine(min * 60_000L + sec * 1000L + ms, text)
            }
        }
        return lines.sortedBy { it.timeMs }
    }

    fun isLrc(content: String): Boolean =
        content.lineSequence().any { TAG_RE.containsMatchIn(it) }
}

package com.skb.music.lyrics

import android.content.Context
import com.skb.music.data.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class LyricsRepository(private val context: Context) {

    suspend fun load(song: Song): List<LyricLine> = withContext(Dispatchers.IO) {
        val path = song.dataPath
        if (path.isNullOrBlank()) return@withContext emptyList()

        try {
            // Try .lrc next to the audio file
            val lrcFile = File(path.substringBeforeLast('.') + ".lrc")
            if (lrcFile.exists() && lrcFile.canRead()) {
                val parsed = LrcParser.parse(lrcFile.readText())
                if (parsed.isNotEmpty()) return@withContext parsed
            }

            // Try .LRC (uppercase)
            val lrcUpper = File(path.substringBeforeLast('.') + ".LRC")
            if (lrcUpper.exists() && lrcUpper.canRead()) {
                val parsed = LrcParser.parse(lrcUpper.readText())
                if (parsed.isNotEmpty()) return@withContext parsed
            }

            // Try .txt (plain text or lrc content)
            val txtFile = File(path.substringBeforeLast('.') + ".txt")
            if (txtFile.exists() && txtFile.canRead()) {
                val raw = txtFile.readText()
                val parsed = LrcParser.parse(raw)
                if (parsed.isNotEmpty()) return@withContext parsed
                // Plain text fallback: each line at +3s interval
                if (raw.isNotBlank()) {
                    return@withContext raw.lineSequence()
                        .filter { it.isNotBlank() }
                        .mapIndexed { i, line ->
                            LyricLine(timeMs = i * 3000L, text = line.trim())
                        }.toList()
                }
            }
        } catch (_: Exception) {}

        emptyList()
    }
}

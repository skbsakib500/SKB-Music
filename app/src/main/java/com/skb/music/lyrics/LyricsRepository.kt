package com.skb.music.lyrics

import android.content.Context
import android.media.MediaMetadataRetriever
import com.skb.music.data.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class LyricsRepository(private val context: Context) {

    suspend fun load(song: Song): List<LyricLine> = withContext(Dispatchers.IO) {
        // 1) Try .lrc file sitting next to the audio file
        val path = song.dataPath
        if (!path.isNullOrBlank()) {
            try {
                val lrcFile = File(path.substringBeforeLast('.') + ".lrc")
                if (lrcFile.exists() && lrcFile.canRead()) {
                    val parsed = LrcParser.parse(lrcFile.readText())
                    if (parsed.isNotEmpty()) return@withContext parsed
                }
                val txtFile = File(path.substringBeforeLast('.') + ".txt")
                if (txtFile.exists() && txtFile.canRead()) {
                    val parsed = LrcParser.parse(txtFile.readText())
                    if (parsed.isNotEmpty()) return@withContext parsed
                }
            } catch (_: Exception) {}
        }

        // 2) Try embedded USLT / SYLT
        try {
            val mmr = MediaMetadataRetriever()
            mmr.setDataSource(context, song.uri)
            val raw = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_LYRICS)
            mmr.release()
            if (!raw.isNullOrBlank()) {
                val parsed = LrcParser.parse(raw)
                if (parsed.isNotEmpty()) return@withContext parsed
            }
        } catch (_: Exception) {}

        emptyList()
    }
}

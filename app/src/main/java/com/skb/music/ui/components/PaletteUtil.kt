package com.skb.music.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.compose.ui.graphics.Color
import androidx.palette.graphics.Palette
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object PaletteUtil {

    data class Gradient(val top: Color, val bottom: Color)

    suspend fun extract(context: Context, uri: Uri?): Gradient? = withContext(Dispatchers.IO) {
        if (uri == null) return@withContext null
        try {
            val input = context.contentResolver.openInputStream(uri) ?: return@withContext null
            val bmp = android.graphics.BitmapFactory.decodeStream(input)
            input.close()
            if (bmp == null) return@withContext null
            val palette = Palette.from(bmp).generate()

            val dark = palette.getDarkVibrantColor(
                palette.getDarkMutedColor(android.graphics.Color.parseColor("#0A0A0A"))
            )
            val vibrant = palette.getVibrantColor(
                palette.getMutedColor(android.graphics.Color.parseColor("#1DB954"))
            )
            val top = Color(vibrant).copy(alpha = 0.35f)
            val bottom = Color(dark).copy(alpha = 0.0f)
            Gradient(top = top, bottom = Color.Black)
        } catch (_: Exception) {
            null
        }
    }
}

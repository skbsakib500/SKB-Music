package com.skb.music.playback

import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import kotlin.math.log10
import kotlin.math.pow

object ReplayGainManager {

    fun apply(
        player: Player,
        item: MediaItem?,
        enabled: Boolean,
        targetDb: Float,
        maxBoostDb: Float
    ) {
        if (!enabled) {
            player.volume = 1f
            return
        }

        val extras = item?.mediaMetadata?.extras
        val trackGainDb = extras?.getFloat("rg_track_gain", Float.NaN) ?: Float.NaN
        val trackPeak = extras?.getFloat("rg_track_peak", Float.NaN) ?: Float.NaN

        val gainDb: Float = if (!trackGainDb.isNaN()) {
            (targetDb - (-14f)) + trackGainDb
        } else {
            0f
        }

        var clamped = gainDb.coerceAtMost(maxBoostDb)
        if (!trackPeak.isNaN() && trackPeak > 0f) {
            val peakDb = 20f * log10(trackPeak)
            clamped = clamped.coerceAtMost(-peakDb)
        }

        val linear = 10f.pow(clamped / 20f)
        player.volume = linear.coerceIn(0f, 1f)
    }
}

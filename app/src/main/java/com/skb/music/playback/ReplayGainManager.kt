package com.skb.music.playback

import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.audio.AudioProcessor
import androidx.media3.exoplayer.ExoPlayer
import kotlin.math.pow

/**
 * ReplayGain approximation.
 *
 * Real ReplayGain needs track/album gain tags analyzed offline.
 * Here we use MediaItem's volume (ExoPlayer volume) to apply a
 * user-selected target loudness; a true implementation would read
 * REPLAYGAIN_TRACK_GAIN from the file metadata and map it here.
 */
object ReplayGainManager {

    fun apply(player: ExoPlayer, item: MediaItem?, enabled: Boolean, targetDb: Float, maxBoostDb: Float) {
        if (!enabled) {
            player.volume = 1f
            return
        }

        // Read embedded gain from MediaMetadata extras (set by our mapper if present)
        val extras = item?.mediaMetadata?.extras
        val trackGainDb = extras?.getFloat("rg_track_gain", Float.NaN) ?: Float.NaN
        val trackPeak = extras?.getFloat("rg_track_peak", Float.NaN) ?: Float.NaN

        val target = targetDb
        val gainDb: Float = if (!trackGainDb.isNaN()) {
            // ReplayGain reference is -14 LUFS-ish; adjust to user target
            (target - (-14f)) + trackGainDb
        } else {
            // No tags: no change
            0f
        }

        var clamped = gainDb.coerceAtMost(maxBoostDb)
        if (!trackPeak.isNaN() && trackPeak > 0f) {
            val peakDb = 20f * kotlin.math.log10(trackPeak)
            val headroom = -peakDb
            clamped = clamped.coerceAtMost(headroom)
        }

        val linear = 10f.pow(clamped / 20f)
        player.volume = linear.coerceIn(0f, 1f)
    }
}

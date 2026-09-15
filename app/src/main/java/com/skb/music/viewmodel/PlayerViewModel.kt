package com.skb.music.viewmodel

import android.app.Application
import android.content.ComponentName
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.skb.music.SkbApplication
import com.skb.music.data.Song
import com.skb.music.data.toMediaItem
import com.skb.music.data.toSong
import com.skb.music.equalizer.EqPreferences
import com.skb.music.playback.PlaybackService
import com.skb.music.playback.ReplayGainManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PlayerViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = (app as SkbApplication).repository
    private val appCtx = app.applicationContext

    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var controller: MediaController? = null

    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _positionMs = MutableStateFlow(0L)
    val positionMs: StateFlow<Long> = _positionMs.asStateFlow()

    private val _durationMs = MutableStateFlow(0L)
    val durationMs: StateFlow<Long> = _durationMs.asStateFlow()

    private val _shuffle = MutableStateFlow(false)
    val shuffle: StateFlow<Boolean> = _shuffle.asStateFlow()

    private val _repeatMode = MutableStateFlow(Player.REPEAT_MODE_OFF)
    val repeatMode: StateFlow<Int> = _repeatMode.asStateFlow()

    private val _sleepRemainingMs = MutableStateFlow<Long?>(null)
    val sleepRemainingMs: StateFlow<Long?> = _sleepRemainingMs.asStateFlow()

    private val _playbackSpeed = MutableStateFlow(1f)
    val playbackSpeed: StateFlow<Float> = _playbackSpeed.asStateFlow()

    private val _bassLevel = MutableStateFlow(0f)
    val bassLevel: StateFlow<Float> = _bassLevel.asStateFlow()

    private val _midLevel = MutableStateFlow(0f)
    val midLevel: StateFlow<Float> = _midLevel.asStateFlow()

    private val _trebleLevel = MutableStateFlow(0f)
    val trebleLevel: StateFlow<Float> = _trebleLevel.asStateFlow()

    private var tickerJob: Job? = null
    private var sleepJob: Job? = null
    private var listenTrackJob: Job? = null
    private var visualizerJob: Job? = null

    private var lastListenedSongId: Long? = null
    private var lastPositionMs: Long = 0L

    init {
        val token = SessionToken(
            app,
            ComponentName(app, PlaybackService::class.java)
        )
        controllerFuture = MediaController.Builder(app, token).buildAsync()
        controllerFuture?.addListener({
            controller = controllerFuture?.get()
            controller?.addListener(playerListener)
            syncFromController()
            startTicker()
            startListenTracker()
        }, ContextCompat.getMainExecutor(app))
    }

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(playing: Boolean) {
            _isPlaying.value = playing
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            // Finalize previous
            flushListenTime()

            val c = controller
            val song = mediaItem?.toSong(c?.duration?.coerceAtLeast(0L) ?: 0L)
            _currentSong.value = song
            lastPositionMs = 0L

            if (song != null) {
                lastListenedSongId = song.id
                viewModelScope.launch { repo.markPlayed(song.id) }
                applyReplayGain(mediaItem)
                startVisualizerIfNeeded()
            }
            syncFromController()
        }

        override fun onPlaybackStateChanged(state: Int) {
            syncFromController()
            if (state == Player.STATE_ENDED || state == Player.STATE_IDLE) {
                flushListenTime()
                stopVisualizer()
            }
        }

        override fun onShuffleModeEnabledChanged(enabled: Boolean) {
            _shuffle.value = enabled
        }

        override fun onRepeatModeChanged(mode: Int) {
            _repeatMode.value = mode
        }

        override fun onPlaybackParametersChanged(pp: androidx.media3.common.PlaybackParameters) {
            _playbackSpeed.value = pp.speed
        }
    }

    private fun applyReplayGain(item: MediaItem?) {
        val c = controller ?: return
        val exo = c.player as? ExoPlayer ?: return
        viewModelScope.launch {
            val s = EqPreferences.load(appCtx)
            if (s.replayGainEnabled) {
                ReplayGainManager.apply(exo, item, true, s.replayGainTargetDb, s.replayGainMaxBoostDb)
            } else {
                exo.volume = 1f
            }
        }
    }

    private fun syncFromController() {
        val c = controller ?: return
        _durationMs.value = c.duration.coerceAtLeast(0L)
        _positionMs.value = c.currentPosition.coerceAtLeast(0L)
    }

    private fun startTicker() {
        tickerJob?.cancel()
        tickerJob = viewModelScope.launch {
            while (true) {
                val c = controller
                if (c != null) {
                    _positionMs.value = c.currentPosition.coerceAtLeast(0L)
                    _durationMs.value = c.duration.coerceAtLeast(0L)
                }
                delay(500)
            }
        }
    }

    private fun startListenTracker() {
        listenTrackJob?.cancel()
        listenTrackJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                val c = controller ?: continue
                if (c.isPlaying) {
                    val id = lastListenedSongId ?: continue
                    val current = c.currentPosition
                    val delta = current - lastPositionMs
                    if (delta in 1..3000) {
                        repo.addListenTime(id, delta)
                    }
                    lastPositionMs = current
                }
            }
        }
    }

    private fun flushListenTime() {
        val id = lastListenedSongId ?: return
        val c = controller ?: return
        val current = c.currentPosition
        val delta = current - lastPositionMs
        if (delta in 1..5000) {
            viewModelScope.launch { repo.addListenTime(id, delta) }
        }
        lastPositionMs = 0L
    }

    // ---------------- Visualizer ----------------
    private fun startVisualizerIfNeeded() {
        stopVisualizer()
        val c = controller ?: return
        val exo = c.player as? ExoPlayer ?: return
        val sessionId = exo.audioSessionId
        if (sessionId <= 0) return

        visualizerJob = viewModelScope.launch {
            val v = try {
                android.media.audiofx.Visualizer(sessionId)
            } catch (_: Exception) {
                null
            } ?: return@launch

            try {
                v.captureSize = android.media.audiofx.Visualizer.getCaptureSizeRange()[0]
                v.setDataCaptureListener(
                    object : android.media.audiofx.Visualizer.OnDataCaptureListener {
                        override fun onWaveFormDataCapture(
                            vis: android.media.audiofx.Visualizer?,
                            waveform: ByteArray?,
                            samplingRate: Int
                        ) {
                            if (waveform == null || waveform.isEmpty()) return
                            var sum = 0.0
                            for (b in waveform) sum += kotlin.math.abs(b.toInt())
                            val avg = (sum / waveform.size) / 128.0
                            val level = avg.coerceIn(0.0, 1.0).toFloat()
                            _bassLevel.value = level
                            _midLevel.value = level
                            _trebleLevel.value = level
                        }

                        override fun onFftDataCapture(
                            vis: android.media.audiofx.Visualizer?,
                            fft: ByteArray?,
                            samplingRate: Int
                        ) {
                            if (fft == null || fft.size < 6) return
                            // Approximate: split into 3 bands
                            val n = fft.size / 2
                            var low = 0.0
                            var mid = 0.0
                            var high = 0.0
                            var lowC = 0
                            var midC = 0
                            var highC = 0
                            var i = 2
                            while (i + 1 < fft.size) {
                                val re = fft[i].toInt()
                                val im = fft[i + 1].toInt()
                                val mag = kotlin.math.hypot(re.toDouble(), im.toDouble())
                                val idx = (i - 2) / 2
                                when {
                                    idx < n / 3 -> { low += mag; lowC++ }
                                    idx < 2 * n / 3 -> { mid += mag; midC++ }
                                    else -> { high += mag; highC++ }
                                }
                                i += 2
                            }
                            val maxMag = (n * 128.0).coerceAtLeast(1.0)
                            _bassLevel.value = ((low / lowC.coerceAtLeast(1)) / maxMag * 8).coerceIn(0.0, 1.0).toFloat()
                            _midLevel.value = ((mid / midC.coerceAtLeast(1)) / maxMag * 8).coerceIn(0.0, 1.0).toFloat()
                            _trebleLevel.value = ((high / highC.coerceAtLeast(1)) / maxMag * 8).coerceIn(0.0, 1.0).toFloat()
                        }
                    },
                    android.media.audiofx.Visualizer.getMaxCaptureRate() / 2,
                    true,
                    true
                )
                v.enabled = true
                while (true) {
                    delay(50)
                }
            } catch (_: Exception) {
            } finally {
                try { v.release() } catch (_: Exception) {}
            }
        }
    }

    private fun stopVisualizer() {
        visualizerJob?.cancel()
        visualizerJob = null
        _bassLevel.value = 0f
        _midLevel.value = 0f
        _trebleLevel.value = 0f
    }

    // ---------------- Actions ----------------
    fun playSongs(songs: List<Song>, startIndex: Int = 0) {
        if (songs.isEmpty()) return
        val items = songs.map { it.toMediaItem() }
        controller?.apply {
            setMediaItems(items, startIndex, 0L)
            prepare()
            play()
        }
        _currentSong.value = songs.getOrNull(startIndex)
    }

    fun togglePlayPause() {
        controller?.let { if (it.isPlaying) it.pause() else it.play() }
    }

    fun next() { controller?.seekToNextMediaItem() }
    fun previous() { controller?.seekToPreviousMediaItem() }
    fun seekTo(ms: Long) { controller?.seekTo(ms) }

    fun toggleShuffle() {
        controller?.let { it.shuffleModeEnabled = !it.shuffleModeEnabled }
    }

    fun cycleRepeat() {
        val c = controller ?: return
        c.repeatMode = when (c.repeatMode) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
    }

    fun playNext(song: Song) {
        val c = controller ?: return
        val insertAt = c.currentMediaItemIndex + 1
        c.addMediaItem(insertAt, song.toMediaItem())
    }

    fun addToQueue(song: Song) {
        controller?.addMediaItem(song.toMediaItem())
    }

    fun startSleepTimer(minutes: Int) {
        sleepJob?.cancel()
        if (minutes <= 0) {
            _sleepRemainingMs.value = null
            return
        }
        val totalMs = minutes * 60_000L
        sleepJob = viewModelScope.launch {
            var remaining = totalMs
            while (remaining > 0) {
                _sleepRemainingMs.value = remaining
                delay(1000)
                remaining -= 1000
            }
            controller?.pause()
            _sleepRemainingMs.value = null
        }
    }

    fun cancelSleepTimer() {
        sleepJob?.cancel()
        sleepJob = null
        _sleepRemainingMs.value = null
    }

    fun setPlaybackSpeed(speed: Float) {
        val s = speed.coerceIn(0.5f, 2.0f)
        controller?.setPlaybackSpeed(s)
        _playbackSpeed.value = s
    }

    override fun onCleared() {
        flushListenTime()
        tickerJob?.cancel()
        sleepJob?.cancel()
        listenTrackJob?.cancel()
        stopVisualizer()
        controllerFuture?.let { MediaController.releaseFuture(it) }
        controller = null
        super.onCleared()
    }
}

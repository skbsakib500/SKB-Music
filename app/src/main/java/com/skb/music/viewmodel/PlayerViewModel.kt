package com.skb.music.viewmodel

import android.app.Application
import android.content.ComponentName
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.skb.music.SkbApplication
import com.skb.music.data.Song
import com.skb.music.data.toMediaItem
import com.skb.music.data.toSong
import com.skb.music.equalizer.AudioEffectsManager
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

    // Visualizer flow driven by AudioEffectsManager (session lives there)
    val bassLevel: StateFlow<Float> = AudioEffectsManager.bassLevel
    val midLevel: StateFlow<Float> = AudioEffectsManager.midLevel
    val trebleLevel: StateFlow<Float> = AudioEffectsManager.trebleLevel

    private var tickerJob: Job? = null
    private var sleepJob: Job? = null
    private var listenTrackJob: Job? = null

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
            if (playing) AudioEffectsManager.startVisualizer() else AudioEffectsManager.stopVisualizer()
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            flushListenTime()

            val c = controller
            val song = mediaItem?.toSong(c?.duration?.coerceAtLeast(0L) ?: 0L)
            _currentSong.value = song
            lastPositionMs = 0L

            if (song != null) {
                lastListenedSongId = song.id
                viewModelScope.launch { repo.markPlayed(song.id) }
                applyReplayGain(mediaItem)
                AudioEffectsManager.startVisualizer()
            }
            syncFromController()
        }

        override fun onPlaybackStateChanged(state: Int) {
            syncFromController()
            if (state == Player.STATE_ENDED || state == Player.STATE_IDLE) {
                flushListenTime()
                AudioEffectsManager.stopVisualizer()
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
        viewModelScope.launch {
            val s = EqPreferences.load(appCtx)
            if (s.replayGainEnabled) {
                ReplayGainManager.apply(c, item, true, s.replayGainTargetDb, s.replayGainMaxBoostDb)
            } else {
                c.volume = 1f
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
        AudioEffectsManager.stopVisualizer()
        controllerFuture?.let { MediaController.releaseFuture(it) }
        controller = null
        super.onCleared()
    }
}

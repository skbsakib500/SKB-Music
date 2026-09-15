package com.skb.music.equalizer

import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.LoudnessEnhancer
import android.media.audiofx.Virtualizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object AudioEffectsManager {

    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var virtualizer: Virtualizer? = null
    private var loudness: LoudnessEnhancer? = null

    private var currentSessionId: Int = -1

    private val _available = MutableStateFlow(false)
    val available: StateFlow<Boolean> = _available.asStateFlow()

    private val _enabled = MutableStateFlow(false)
    val enabled: StateFlow<Boolean> = _enabled.asStateFlow()

    private val _bandLevels = MutableStateFlow<List<Short>>(emptyList())
    val bandLevels: StateFlow<List<Short>> = _bandLevels.asStateFlow()

    private val _bandFreqsHz = MutableStateFlow<List<Int>>(emptyList())
    val bandFreqsHz: StateFlow<List<Int>> = _bandFreqsHz.asStateFlow()

    private val _bandRangeMb = MutableStateFlow(-1500 to 1500)
    val bandRangeMb: StateFlow<Pair<Int, Int>> = _bandRangeMb.asStateFlow()

    private val _preset = MutableStateFlow(0)
    val preset: StateFlow<Int> = _preset.asStateFlow()

    private val _presetNames = MutableStateFlow<List<String>>(emptyList())
    val presetNames: StateFlow<List<String>> = _presetNames.asStateFlow()

    private val _bassStrength = MutableStateFlow(0)
    val bassStrength: StateFlow<Int> = _bassStrength.asStateFlow()

    private val _virtualizerStrength = MutableStateFlow(0)
    val virtualizerStrength: StateFlow<Int> = _virtualizerStrength.asStateFlow()

    private val _loudnessGainMb = MutableStateFlow(0)
    val loudnessGainMb: StateFlow<Int> = _loudnessGainMb.asStateFlow()

    fun attach(sessionId: Int) {
        if (sessionId <= 0) return
        if (sessionId == currentSessionId && equalizer != null) return
        release()
        currentSessionId = sessionId
        try {
            val eq = Equalizer(0, sessionId)
            eq.enabled = true
            equalizer = eq

            bassBoost = BassBoost(0, sessionId)
            virtualizer = Virtualizer(0, sessionId)
            loudness = LoudnessEnhancer(sessionId)

            val numBands = eq.numberOfBands.toInt()
            val freqs = (0 until numBands).map {
                eq.getCenterFreq(it.toShort()).toInt() / 1000
            }
            val range = eq.bandLevelRange
            _bandFreqsHz.value = freqs
            _bandRangeMb.value = range[0].toInt() to range[1].toInt()
            _bandLevels.value = (0 until numBands).map { eq.getBandLevel(it.toShort()) }
            _presetNames.value = (0 until eq.numberOfPresets.toInt()).map {
                eq.getPresetName(it.toShort()).toString()
            }
            _enabled.value = true
            _available.value = true
        } catch (e: Exception) {
            release()
        }
    }

    fun release() {
        try { equalizer?.release() } catch (_: Exception) {}
        try { bassBoost?.release() } catch (_: Exception) {}
        try { virtualizer?.release() } catch (_: Exception) {}
        try { loudness?.release() } catch (_: Exception) {}
        equalizer = null
        bassBoost = null
        virtualizer = null
        loudness = null
        currentSessionId = -1
        _enabled.value = false
        _available.value = false
    }

    fun setEnabled(on: Boolean) {
        equalizer?.enabled = on
        _enabled.value = on
    }

    fun setPreset(index: Int) {
        val eq = equalizer ?: return
        try {
            eq.usePreset(index.toShort())
            _preset.value = index
            val numBands = eq.numberOfBands.toInt()
            _bandLevels.value = (0 until numBands).map { eq.getBandLevel(it.toShort()) }
        } catch (_: Exception) {}
    }

    fun setBandLevel(band: Int, levelMb: Short) {
        val eq = equalizer ?: return
        try {
            val range = eq.bandLevelRange
            val clamped = levelMb
                .coerceAtLeast(range[0])
                .coerceAtMost(range[1])
            eq.setBandLevel(band.toShort(), clamped)
            val numBands = eq.numberOfBands.toInt()
            _bandLevels.value = (0 until numBands).map { eq.getBandLevel(it.toShort()) }
            _preset.value = -1
        } catch (_: Exception) {}
    }

    fun setBassBoost(strength: Int) {
        val bb = bassBoost ?: return
        val s = strength.coerceIn(0, 1000).toShort()
        try {
            bb.setStrength(s)
            bb.enabled = s > 0
            _bassStrength.value = s.toInt()
        } catch (_: Exception) {}
    }

    fun setVirtualizer(strength: Int) {
        val v = virtualizer ?: return
        val s = strength.coerceIn(0, 1000).toShort()
        try {
            v.setStrength(s)
            v.enabled = s > 0
            _virtualizerStrength.value = s.toInt()
        } catch (_: Exception) {}
    }

    fun setLoudness(gainMb: Int) {
        val l = loudness ?: return
        val g = gainMb.coerceIn(0, 2000)
        try {
            l.setTargetGain(g)
            l.enabled = g > 0
            _loudnessGainMb.value = g
        } catch (_: Exception) {}
    }

    fun resetAll() {
        val eq = equalizer ?: return
        try {
            eq.usePreset(0)
            _preset.value = 0
            val numBands = eq.numberOfBands.toInt()
            _bandLevels.value = (0 until numBands).map { eq.getBandLevel(it.toShort()) }
            bassBoost?.setStrength(0)
            bassBoost?.enabled = false
            _bassStrength.value = 0
            virtualizer?.setStrength(0)
            virtualizer?.enabled = false
            _virtualizerStrength.value = 0
            loudness?.setTargetGain(0)
            loudness?.enabled = false
            _loudnessGainMb.value = 0
        } catch (_: Exception) {}
    }
}

package com.skb.music.equalizer

import android.content.Context
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.LoudnessEnhancer
import android.media.audiofx.Virtualizer
import android.media.audiofx.Visualizer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

object AudioEffectsManager {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var appContext: Context? = null

    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var virtualizer: Virtualizer? = null
    private var loudness: LoudnessEnhancer? = null
    private var visualizer: Visualizer? = null

    private var currentSessionId: Int = -1
    private var pendingSettings: EqSettings? = null

    private var visualizerJob: Job? = null

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

    private val _ready = MutableStateFlow(false)
    val ready: StateFlow<Boolean> = _ready.asStateFlow()

    // Visualizer levels
    private val _bassLevel = MutableStateFlow(0f)
    val bassLevel: StateFlow<Float> = _bassLevel.asStateFlow()

    private val _midLevel = MutableStateFlow(0f)
    val midLevel: StateFlow<Float> = _midLevel.asStateFlow()

    private val _trebleLevel = MutableStateFlow(0f)
    val trebleLevel: StateFlow<Float> = _trebleLevel.asStateFlow()

    fun init(context: Context) {
        appContext = context.applicationContext
        scope.launch {
            pendingSettings = EqPreferences.load(context.applicationContext)
            _ready.value = true
            currentSessionId.takeIf { it > 0 }?.let { tryApplyPending() }
        }
    }

    fun attach(sessionId: Int) {
        if (sessionId <= 0) return
        if (sessionId == currentSessionId && equalizer != null) return
        release(clearSession = false)
        currentSessionId = sessionId
        try {
            val eq = Equalizer(0, sessionId)
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
            _available.value = true
            tryApplyPending()
            startVisualizer()
        } catch (_: Exception) {
            release(clearSession = false)
        }
    }

    private fun tryApplyPending() {
        val settings = pendingSettings ?: return
        val eq = equalizer ?: return
        try {
            if (settings.bandLevelsCsv.isNotBlank()) {
                val saved = EqPreferences.parseBands(settings.bandLevelsCsv)
                val numBands = eq.numberOfBands.toInt()
                if (saved.size == numBands) {
                    val range = eq.bandLevelRange
                    saved.forEachIndexed { i, lvl ->
                        val clamped = lvl.coerceAtLeast(range[0]).coerceAtMost(range[1])
                        eq.setBandLevel(i.toShort(), clamped)
                    }
                    _bandLevels.value = (0 until numBands).map { eq.getBandLevel(it.toShort()) }
                    _preset.value = settings.preset
                } else {
                    applyPreset(settings.preset)
                }
            } else {
                applyPreset(settings.preset)
            }
            bassBoost?.let {
                val s = settings.bass.coerceIn(0, 1000).toShort()
                it.setStrength(s); it.enabled = s > 0
                _bassStrength.value = s.toInt()
            }
            virtualizer?.let {
                val s = settings.virtualizer.coerceIn(0, 1000).toShort()
                it.setStrength(s); it.enabled = s > 0
                _virtualizerStrength.value = s.toInt()
            }
            loudness?.let {
                val g = settings.loudness.coerceIn(0, 2000)
                it.setTargetGain(g); it.enabled = g > 0
                _loudnessGainMb.value = g
            }
            eq.enabled = settings.enabled
            _enabled.value = settings.enabled
        } catch (_: Exception) {}
    }

    private fun applyPreset(index: Int) {
        val eq = equalizer ?: return
        try {
            eq.usePreset(index.toShort())
            _preset.value = index
            val numBands = eq.numberOfBands.toInt()
            _bandLevels.value = (0 until numBands).map { eq.getBandLevel(it.toShort()) }
        } catch (_: Exception) {}
    }

    fun release(clearSession: Boolean = true) {
        stopVisualizer()
        try { equalizer?.release() } catch (_: Exception) {}
        try { bassBoost?.release() } catch (_: Exception) {}
        try { virtualizer?.release() } catch (_: Exception) {}
        try { loudness?.release() } catch (_: Exception) {}
        equalizer = null; bassBoost = null; virtualizer = null; loudness = null
        if (clearSession) currentSessionId = -1
        _enabled.value = false
        _available.value = false
    }

    fun setEnabled(on: Boolean) {
        equalizer?.enabled = on
        _enabled.value = on
        persist { it.saveEnabled(appContext!!, on) }
    }

    fun setPreset(index: Int) {
        applyPreset(index)
        persist { it.savePreset(appContext!!, index) }
        val bands = _bandLevels.value
        if (bands.isNotEmpty()) persist { it.saveBands(appContext!!, bands) }
    }

    fun setBandLevel(band: Int, levelMb: Short) {
        val eq = equalizer ?: return
        try {
            val range = eq.bandLevelRange
            val clamped = levelMb.coerceAtLeast(range[0]).coerceAtMost(range[1])
            eq.setBandLevel(band.toShort(), clamped)
            val newBands = (0 until eq.numberOfBands.toInt()).map { eq.getBandLevel(it.toShort()) }
            _bandLevels.value = newBands
            _preset.value = -1
            persist { it.saveBands(appContext!!, newBands) }
            persist { it.savePreset(appContext!!, -1) }
        } catch (_: Exception) {}
    }

    fun setBassBoost(strength: Int) {
        val bb = bassBoost ?: return
        val s = strength.coerceIn(0, 1000).toShort()
        try {
            bb.setStrength(s); bb.enabled = s > 0
            _bassStrength.value = s.toInt()
            persist { it.saveBass(appContext!!, s.toInt()) }
        } catch (_: Exception) {}
    }

    fun setVirtualizer(strength: Int) {
        val v = virtualizer ?: return
        val s = strength.coerceIn(0, 1000).toShort()
        try {
            v.setStrength(s); v.enabled = s > 0
            _virtualizerStrength.value = s.toInt()
            persist { it.saveVirtualizer(appContext!!, s.toInt()) }
        } catch (_: Exception) {}
    }

    fun setLoudness(gainMb: Int) {
        val l = loudness ?: return
        val g = gainMb.coerceIn(0, 2000)
        try {
            l.setTargetGain(g); l.enabled = g > 0
            _loudnessGainMb.value = g
            persist { it.saveLoudness(appContext!!, g) }
        } catch (_: Exception) {}
    }

    fun resetAll() {
        val eq = equalizer ?: return
        try {
            eq.usePreset(0)
            _preset.value = 0
            val bands = (0 until eq.numberOfBands.toInt()).map { eq.getBandLevel(it.toShort()) }
            _bandLevels.value = bands
            bassBoost?.setStrength(0); bassBoost?.enabled = false
            _bassStrength.value = 0
            virtualizer?.setStrength(0); virtualizer?.enabled = false
            _virtualizerStrength.value = 0
            loudness?.setTargetGain(0); loudness?.enabled = false
            _loudnessGainMb.value = 0
            persist { it.savePreset(appContext!!, 0) }
            persist { it.saveBands(appContext!!, bands) }
            persist { it.saveBass(appContext!!, 0) }
            persist { it.saveVirtualizer(appContext!!, 0) }
            persist { it.saveLoudness(appContext!!, 0) }
        } catch (_: Exception) {}
    }

    private fun persist(block: suspend (EqPreferences) -> Unit) {
        if (appContext == null) return
        scope.launch { block(EqPreferences) }
    }

    // ---------------- Visualizer ----------------
    fun startVisualizer() {
        if (visualizer != null) return
        val session = currentSessionId
        if (session <= 0) return
        visualizerJob?.cancel()
        visualizerJob = scope.launch {
            val v = try { Visualizer(session) } catch (_: Exception) { null } ?: return@launch
            try {
                v.captureSize = Visualizer.getCaptureSizeRange()[0]
                v.setDataCaptureListener(
                    object : Visualizer.OnDataCaptureListener {
                        override fun onWaveFormDataCapture(
                            vis: Visualizer?, waveform: ByteArray?, samplingRate: Int
                        ) {
                            if (waveform == null || waveform.isEmpty()) return
                            var sum = 0.0
                            for (b in waveform) sum += kotlin.math.abs(b.toInt())
                            val level = ((sum / waveform.size) / 128.0).coerceIn(0.0, 1.0).toFloat()
                            _bassLevel.value = level
                            _midLevel.value = level
                            _trebleLevel.value = level
                        }

                        override fun onFftDataCapture(
                            vis: Visualizer?, fft: ByteArray?, samplingRate: Int
                        ) {
                            if (fft == null || fft.size < 6) return
                            val n = fft.size / 2
                            var low = 0.0; var mid = 0.0; var high = 0.0
                            var lowC = 0; var midC = 0; var highC = 0
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
                    Visualizer.getMaxCaptureRate() / 2,
                    true,
                    true
                )
                v.enabled = true
                visualizer = v
                while (true) delay(200)
            } catch (_: Exception) {
            } finally {
                try { v.enabled = false } catch (_: Exception) {}
                try { v.release() } catch (_: Exception) {}
                visualizer = null
            }
        }
    }

    fun stopVisualizer() {
        visualizerJob?.cancel()
        visualizerJob = null
        try { visualizer?.enabled = false } catch (_: Exception) {}
        try { visualizer?.release() } catch (_: Exception) {}
        visualizer = null
        _bassLevel.value = 0f
        _midLevel.value = 0f
        _trebleLevel.value = 0f
    }
}

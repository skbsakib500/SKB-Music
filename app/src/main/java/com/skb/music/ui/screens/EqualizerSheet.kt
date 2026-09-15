package com.skb.music.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.skb.music.equalizer.AudioEffectsManager
import com.skb.music.equalizer.EqPreferences
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EqualizerSheet(onDismiss: () -> Unit) {

    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val ready by AudioEffectsManager.ready.collectAsStateWithLifecycle()
    val available by AudioEffectsManager.available.collectAsStateWithLifecycle()
    val enabled by AudioEffectsManager.enabled.collectAsStateWithLifecycle()
    val bandLevels by AudioEffectsManager.bandLevels.collectAsStateWithLifecycle()
    val bandFreqs by AudioEffectsManager.bandFreqsHz.collectAsStateWithLifecycle()
    val bandRange by AudioEffectsManager.bandRangeMb.collectAsStateWithLifecycle()
    val preset by AudioEffectsManager.preset.collectAsStateWithLifecycle()
    val presetNames by AudioEffectsManager.presetNames.collectAsStateWithLifecycle()
    val bass by AudioEffectsManager.bassStrength.collectAsStateWithLifecycle()
    val virtual by AudioEffectsManager.virtualizerStrength.collectAsStateWithLifecycle()
    val loudness by AudioEffectsManager.loudnessGainMb.collectAsStateWithLifecycle()

    var rgEnabled by remember { mutableStateOf(false) }
    var rgTarget by remember { mutableStateOf(-14f) }
    var rgMaxBoost by remember { mutableStateOf(6f) }
    var rgPreventClip by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val s = EqPreferences.load(ctx)
        rgEnabled = s.replayGainEnabled
        rgTarget = s.replayGainTargetDb
        rgMaxBoost = s.replayGainMaxBoostDb
        rgPreventClip = s.replayGainPreventClip
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF0E0E0E)
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Equalizer",
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = enabled,
                    onCheckedChange = { AudioEffectsManager.setEnabled(it) },
                    enabled = available,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.Black,
                        checkedTrackColor = Color(0xFF1DB954)
                    )
                )
            }

            Spacer(Modifier.height(12.dp))

            if (!ready) {
                Text("Loading settings...", color = Color.Gray)
                return@Column
            }

            if (!available) {
                Text("Audio effects unavailable. Start playing a song.", color = Color.Gray)
            } else {
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = if (preset in presetNames.indices) presetNames[preset] else "Custom",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Preset") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF1DB954),
                            unfocusedBorderColor = Color.DarkGray,
                            focusedLabelColor = Color(0xFF1DB954),
                            unfocusedLabelColor = Color.Gray,
                            cursorColor = Color(0xFF1DB954)
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        containerColor = Color(0xFF141414)
                    ) {
                        presetNames.forEachIndexed { i, name ->
                            DropdownMenuItem(
                                text = { Text(name, color = Color.White) },
                                onClick = {
                                    AudioEffectsManager.setPreset(i)
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
                Text("Bands", color = Color.Gray, fontWeight = FontWeight.Bold)

                bandLevels.forEachIndexed { i, level ->
                    val freq = bandFreqs.getOrNull(i) ?: 0
                    val label = if (freq >= 1000) "${freq / 1000} kHz" else "$freq Hz"
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(label, color = Color.Gray, modifier = Modifier.width(56.dp))
                        Slider(
                            value = level.toFloat(),
                            onValueChange = { AudioEffectsManager.setBandLevel(i, it.toInt().toShort()) },
                            valueRange = bandRange.first.toFloat()..bandRange.second.toFloat(),
                            enabled = enabled,
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFF1DB954),
                                activeTrackColor = Color(0xFF1DB954),
                                inactiveTrackColor = Color(0xFF262626)
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        Text("%.1f dB".format(level / 100f), color = Color.Gray, modifier = Modifier.width(60.dp))
                    }
                }

                Spacer(Modifier.height(16.dp))
                Text("Effects", color = Color.Gray, fontWeight = FontWeight.Bold)

                EffectSlider("Bass Boost", bass.toFloat(), 1000f, "%", 10f, enabled) {
                    AudioEffectsManager.setBassBoost(it.toInt())
                }
                EffectSlider("Virtualizer", virtual.toFloat(), 1000f, "%", 10f, enabled) {
                    AudioEffectsManager.setVirtualizer(it.toInt())
                }
                EffectSlider("Loudness", loudness.toFloat(), 2000f, " dB", 100f, enabled) {
                    AudioEffectsManager.setLoudness(it.toInt())
                }

                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { AudioEffectsManager.resetAll() },
                    enabled = enabled,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF1DB954))
                ) { Text("Reset All") }
            }

            Spacer(Modifier.height(24.dp))
            HorizontalDivider(color = Color(0xFF262626))
            Spacer(Modifier.height(16.dp))

            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("ReplayGain", color = Color.White, fontWeight = FontWeight.Bold)
                    Text(
                        "Volume normalization using track tags",
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Switch(
                    checked = rgEnabled,
                    onCheckedChange = {
                        rgEnabled = it
                        scope.launch { EqPreferences.saveReplayGainEnabled(ctx, it) }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.Black,
                        checkedTrackColor = Color(0xFF1DB954)
                    )
                )
            }

            if (rgEnabled) {
                Spacer(Modifier.height(8.dp))
                Text("Target: %.1f dB".format(rgTarget), color = Color.Gray)
                Slider(
                    value = rgTarget,
                    onValueChange = {
                        rgTarget = it
                        scope.launch { EqPreferences.saveReplayGainTarget(ctx, it) }
                    },
                    valueRange = -24f..-6f,
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFF1DB954),
                        activeTrackColor = Color(0xFF1DB954),
                        inactiveTrackColor = Color(0xFF262626)
                    )
                )
                Text("Max Boost: +%.1f dB".format(rgMaxBoost), color = Color.Gray)
                Slider(
                    value = rgMaxBoost,
                    onValueChange = {
                        rgMaxBoost = it
                        scope.launch { EqPreferences.saveReplayGainMaxBoost(ctx, it) }
                    },
                    valueRange = 0f..12f,
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFF1DB954),
                        activeTrackColor = Color(0xFF1DB954),
                        inactiveTrackColor = Color(0xFF262626)
                    )
                )
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("Prevent Clipping", color = Color.White, modifier = Modifier.weight(1f))
                    Switch(
                        checked = rgPreventClip,
                        onCheckedChange = {
                            rgPreventClip = it
                            scope.launch { EqPreferences.saveReplayGainPreventClip(ctx, it) }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = Color(0xFF1DB954)
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun EffectSlider(
    label: String,
    value: Float,
    max: Float,
    suffix: String,
    divisor: Float,
    enabled: Boolean,
    onChange: (Float) -> Unit
) {
    Column(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(Modifier.fillMaxWidth()) {
            Text(label, color = Color.White)
            Spacer(Modifier.weight(1f))
            Text("%.1f%s".format(value / divisor, suffix), color = Color.Gray)
        }
        Slider(
            value = value,
            onValueChange = onChange,
            valueRange = 0f..max,
            enabled = enabled,
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFF1DB954),
                activeTrackColor = Color(0xFF1DB954),
                inactiveTrackColor = Color(0xFF262626)
            )
        )
    }
}

package com.skb.music.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.skb.music.equalizer.AudioEffectsManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EqualizerSheet(onDismiss: () -> Unit) {

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
                Text(
                    "Loading settings...",
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodyMedium
                )
                return@Column
            }

            if (!available) {
                Text(
                    "Audio effects are not available yet. Start playing a song first.",
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodyMedium
                )
                return@Column
            }

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
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
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

            Spacer(Modifier.height(20.dp))

            Text(
                "Bands",
                color = Color.Gray,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))

            bandLevels.forEachIndexed { i, level ->
                val freq = bandFreqs.getOrNull(i) ?: 0
                val freqLabel = if (freq >= 1000) "${freq / 1000} kHz" else "$freq Hz"
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        freqLabel,
                        color = Color.Gray,
                        modifier = Modifier.width(56.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                    Slider(
                        value = level.toFloat(),
                        onValueChange = {
                            AudioEffectsManager.setBandLevel(i, it.toInt().toShort())
                        },
                        valueRange = bandRange.first.toFloat()..bandRange.second.toFloat(),
                        enabled = enabled,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFF1DB954),
                            activeTrackColor = Color(0xFF1DB954),
                            inactiveTrackColor = Color(0xFF262626)
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        "%.1f dB".format(level / 100f),
                        color = Color.Gray,
                        modifier = Modifier.width(60.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Text(
                "Effects",
                color = Color.Gray,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))

            EffectSlider(
                label = "Bass Boost",
                value = bass.toFloat(),
                max = 1000f,
                suffix = "%",
                divisor = 10f,
                enabled = enabled,
                onChange = { AudioEffectsManager.setBassBoost(it.toInt()) }
            )
            EffectSlider(
                label = "Virtualizer",
                value = virtual.toFloat(),
                max = 1000f,
                suffix = "%",
                divisor = 10f,
                enabled = enabled,
                onChange = { AudioEffectsManager.setVirtualizer(it.toInt()) }
            )
            EffectSlider(
                label = "Loudness",
                value = loudness.toFloat(),
                max = 2000f,
                suffix = " dB",
                divisor = 100f,
                enabled = enabled,
                onChange = { AudioEffectsManager.setLoudness(it.toInt()) }
            )

            Spacer(Modifier.height(12.dp))

            OutlinedButton(
                onClick = { AudioEffectsManager.resetAll() },
                enabled = enabled,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFF1DB954)
                )
            ) {
                Text("Reset All")
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
            Text(label, color = Color.White, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.weight(1f))
            Text(
                "%.1f%s".format(value / divisor, suffix),
                color = Color.Gray,
                style = MaterialTheme.typography.bodySmall
            )
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

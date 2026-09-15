package com.skb.music.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.skb.music.ui.components.LyricsSheet
import com.skb.music.ui.components.PaletteUtil
import com.skb.music.ui.components.SleepTimerSheet
import com.skb.music.ui.components.SpectrumVisualizer
import com.skb.music.ui.components.SpeedSheet
import com.skb.music.viewmodel.PlayerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NowPlayingScreen(
    playerVm: PlayerViewModel,
    onClose: () -> Unit
) {
    val ctx = LocalContext.current

    val song by playerVm.currentSong.collectAsStateWithLifecycle()
    val isPlaying by playerVm.isPlaying.collectAsStateWithLifecycle()
    val position by playerVm.positionMs.collectAsStateWithLifecycle()
    val duration by playerVm.durationMs.collectAsStateWithLifecycle()
    val shuffle by playerVm.shuffle.collectAsStateWithLifecycle()
    val repeat by playerVm.repeatMode.collectAsStateWithLifecycle()
    val sleepRemaining by playerVm.sleepRemainingMs.collectAsStateWithLifecycle()
    val speed by playerVm.playbackSpeed.collectAsStateWithLifecycle()
    val bassLevel by playerVm.bassLevel.collectAsStateWithLifecycle()
    val midLevel by playerVm.midLevel.collectAsStateWithLifecycle()
    val trebleLevel by playerVm.trebleLevel.collectAsStateWithLifecycle()

    var showEq by remember { mutableStateOf(false) }
    var showSleep by remember { mutableStateOf(false) }
    var showSpeed by remember { mutableStateOf(false) }
    var showLyrics by remember { mutableStateOf(false) }

    var gradient by remember { mutableStateOf<PaletteUtil.Gradient?>(null) }
    LaunchedEffect(song?.albumArtUri) {
        gradient = PaletteUtil.extract(ctx, song?.albumArtUri)
    }

    val topColor by animateColorAsState(
        targetValue = gradient?.top ?: Color(0xFF0A0A0A),
        animationSpec = tween(600),
        label = "topColor"
    )

    Box(
        Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(topColor, Color.Black, Color.Black)
                )
            )
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(24.dp)
                .systemBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.KeyboardArrowDown, "Close", tint = Color.White)
                }
                Spacer(Modifier.weight(1f))
                Text(
                    if (sleepRemaining != null) "SLEEP ${formatTime(sleepRemaining!!)}" else "NOW PLAYING",
                    color = if (sleepRemaining != null) Color(0xFF1DB954) else Color.Gray,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.weight(1f))
                IconButton(onClick = { showEq = true }) {
                    Icon(Icons.Default.Equalizer, "Equalizer", tint = Color.White)
                }
            }

            Spacer(Modifier.height(16.dp))

            Box(
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF141414)),
                contentAlignment = Alignment.Center
            ) {
                val art = song?.albumArtUri
                if (art != null) {
                    AsyncImage(model = art, contentDescription = null, modifier = Modifier.fillMaxSize())
                } else {
                    Icon(Icons.Default.MusicNote, null, tint = Color(0xFF1DB954), modifier = Modifier.size(96.dp))
                }
            }

            Spacer(Modifier.height(20.dp))

            SpectrumVisualizer(bass = bassLevel, mid = midLevel, treble = trebleLevel)

            Spacer(Modifier.height(16.dp))

            Text(
                song?.title ?: "Nothing playing",
                color = Color.White,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))
            Text(
                song?.artist ?: "",
                color = Color.Gray,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(16.dp))

            Slider(
                value = position.toFloat().coerceIn(0f, duration.toFloat().coerceAtLeast(1f)),
                onValueChange = { playerVm.seekTo(it.toLong()) },
                valueRange = 0f..duration.toFloat().coerceAtLeast(1f),
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFF1DB954),
                    activeTrackColor = Color(0xFF1DB954),
                    inactiveTrackColor = Color.DarkGray
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Row(Modifier.fillMaxWidth()) {
                Text(formatTime(position), color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.weight(1f))
                Text(formatTime(duration), color = Color.Gray, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(Modifier.height(8.dp))

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { playerVm.toggleShuffle() }) {
                    Icon(Icons.Default.Shuffle, "Shuffle", tint = if (shuffle) Color(0xFF1DB954) else Color.Gray)
                }
                IconButton(onClick = { playerVm.previous() }) {
                    Icon(Icons.Default.SkipPrevious, "Prev", tint = Color.White, modifier = Modifier.size(40.dp))
                }
                FilledIconButton(
                    onClick = { playerVm.togglePlayPause() },
                    modifier = Modifier.size(72.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color(0xFF1DB954))
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Play/Pause",
                        tint = Color.Black,
                        modifier = Modifier.size(36.dp)
                    )
                }
                IconButton(onClick = { playerVm.next() }) {
                    Icon(Icons.Default.SkipNext, "Next", tint = Color.White, modifier = Modifier.size(40.dp))
                }
                IconButton(onClick = { playerVm.cycleRepeat() }) {
                    val icon = when (repeat) {
                        1 -> Icons.Default.RepeatOne
                        else -> Icons.Default.Repeat
                    }
                    Icon(icon, "Repeat", tint = if (repeat != 0) Color(0xFF1DB954) else Color.Gray)
                }
            }

            Spacer(Modifier.height(8.dp))

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SmallToolButton(Icons.Default.Subtitles, "Lyrics", false) { showLyrics = true }
                SmallToolButton(
                    Icons.Default.Timer,
                    if (sleepRemaining != null) formatTime(sleepRemaining!!) else "Sleep",
                    sleepRemaining != null
                ) { showSleep = true }
                SmallToolButton(Icons.Default.Speed, "${trimSpeed(speed)}x", speed != 1f) { showSpeed = true }
            }
        }
    }

    if (showEq) EqualizerSheet(onDismiss = { showEq = false })
    if (showSleep) SleepTimerSheet(
        remainingMs = sleepRemaining,
        onPick = { playerVm.startSleepTimer(it) },
        onCancel = { playerVm.cancelSleepTimer() },
        onDismiss = { showSleep = false }
    )
    if (showSpeed) SpeedSheet(
        current = speed,
        onPick = { playerVm.setPlaybackSpeed(it) },
        onDismiss = { showSpeed = false }
    )
    if (showLyrics) LyricsSheet(playerVm = playerVm, onDismiss = { showLyrics = false })
}

@Composable
private fun SmallToolButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    active: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (active) Color(0xFF1DB954).copy(alpha = 0.15f) else Color.Transparent)
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        IconButton(onClick = onClick) {
            Icon(icon, contentDescription = label, tint = if (active) Color(0xFF1DB954) else Color.White)
        }
        Text(label, color = if (active) Color(0xFF1DB954) else Color.Gray, style = MaterialTheme.typography.labelSmall)
    }
}

private fun formatTime(ms: Long): String {
    val totalSec = (ms / 1000).coerceAtLeast(0)
    val m = totalSec / 60
    val s = totalSec % 60
    return "%d:%02d".format(m, s)
}

private fun trimSpeed(s: Float): String =
    if (s == s.toInt().toFloat()) s.toInt().toString() else s.toString()

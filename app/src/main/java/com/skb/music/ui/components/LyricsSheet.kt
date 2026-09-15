package com.skb.music.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.skb.music.lyrics.LyricLine
import com.skb.music.lyrics.LyricsRepository
import com.skb.music.viewmodel.PlayerViewModel
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LyricsSheet(
    playerVm: PlayerViewModel,
    onDismiss: () -> Unit
) {
    val ctx = LocalContext.current
    val song by playerVm.currentSong.collectAsStateWithLifecycle()
    val position by playerVm.positionMs.collectAsStateWithLifecycle()

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val listState = rememberLazyListState()

    var lyrics by remember { mutableStateOf<List<LyricLine>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(song?.id) {
        loading = true
        lyrics = if (song == null) emptyList()
        else withContext(Dispatchers.IO) {
            LyricsRepository(ctx).load(song!!)
        }
        loading = false
    }

    val currentIndex = remember(position, lyrics) {
        if (lyrics.isEmpty()) -1
        else lyrics.indexOfLast { it.timeMs <= position }
    }

    LaunchedEffect(currentIndex) {
        if (currentIndex in lyrics.indices) {
            listState.animateScrollToItem(currentIndex.coerceAtLeast(0))
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF0E0E0E)
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(horizontal = 20.dp)
        ) {
            Text(
                "Lyrics",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                song?.title ?: "",
                color = Color.Gray,
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(Modifier.height(12.dp))

            when {
                loading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF1DB954))
                }
                lyrics.isEmpty() -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                    Text(
                        "No lyrics found.\nPlace a .lrc file with the same name next to your audio file.",
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
                else -> LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize()
                ) {
                    item { Spacer(Modifier.height(40.dp)) }
                    items(lyrics.size) { i ->
                        val line = lyrics[i]
                        val active = i == currentIndex
                        Text(
                            text = line.text.ifBlank { "♪" },
                            color = if (active) Color(0xFF1DB954) else Color.White.copy(alpha = 0.6f),
                            fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp)
                        )
                    }
                    item { Spacer(Modifier.height(120.dp)) }
                }
            }
        }
    }
}

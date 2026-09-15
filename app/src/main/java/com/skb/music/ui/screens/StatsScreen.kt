package com.skb.music.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.skb.music.viewmodel.LibraryViewModel

@Composable
fun StatsScreen(libraryVm: LibraryViewModel) {
    val totalPlays by libraryVm.totalPlays.collectAsStateWithLifecycle()
    val totalListenMs by libraryVm.totalListenMs.collectAsStateWithLifecycle()
    val uniqueSongs by libraryVm.uniqueSongs.collectAsStateWithLifecycle()
    val topSongs by libraryVm.topStats.collectAsStateWithLifecycle()
    val allSongs by libraryVm.allSongs.collectAsStateWithLifecycle()

    val songById = remember(allSongs) { allSongs.associateBy { it.id } }

    Column(
        Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp)
    ) {
        Text(
            "Listening Stats",
            color = Color.White,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(16.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard("Plays", (totalPlays ?: 0).toString(), Modifier.weight(1f))
            StatCard("Unique", uniqueSongs.toString(), Modifier.weight(1f))
        }
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard("Listen Time", formatMs(totalListenMs ?: 0L), Modifier.weight(1f))
            StatCard("Top Count", topSongs.size.toString(), Modifier.weight(1f))
        }

        Spacer(Modifier.height(24.dp))
        Text("Top Songs", color = Color.White, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))

        if (topSongs.isEmpty()) {
            Box(Modifier.fillMaxSize(), Alignment.Center) {
                Text("No stats yet. Play some songs!", color = Color.Gray)
            }
        } else {
            LazyColumn(Modifier.fillMaxSize()) {
                items(topSongs, key = { it.songId }) { st ->
                    val s = songById[st.songId]
                    Row(
                        Modifier.fillMaxWidth().padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(s?.title ?: "Unknown (#${st.songId})", color = Color.White, maxLines = 1)
                            Text(
                                "${st.playCount} plays • ${formatMs(st.totalMs)}",
                                color = Color.Gray,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                    HorizontalDivider(color = Color(0xFF1A1A1A))
                }
            }
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        color = Color(0xFF141414),
        shape = MaterialTheme.shapes.medium,
        modifier = modifier
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(label, color = Color.Gray, style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.height(6.dp))
            Text(
                value,
                color = Color(0xFF1DB954),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private fun formatMs(ms: Long): String {
    val totalMin = ms / 60_000
    val h = totalMin / 60
    val m = totalMin % 60
    return if (h > 0) "${h}h ${m}m" else "${m}m"
}

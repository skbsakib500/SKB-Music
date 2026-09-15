package com.skb.music.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.skb.music.data.Song
import com.skb.music.viewmodel.LibraryViewModel
import com.skb.music.viewmodel.PlayerViewModel

private enum class BrowseMode { Albums, Artists }

@Composable
fun BrowseScreen(
    libraryVm: LibraryViewModel,
    playerVm: PlayerViewModel
) {
    val songs by libraryVm.allSongs.collectAsStateWithLifecycle()
    var mode by remember { mutableStateOf(BrowseMode.Albums) }

    val albums = remember(songs) {
        songs.groupBy { it.album }.toSortedMap()
    }
    val artists = remember(songs) {
        songs.groupBy { it.artist }.toSortedMap()
    }

    Column(Modifier.fillMaxSize().background(Color.Black)) {
        Row(
            Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterChip(
                selected = mode == BrowseMode.Albums,
                onClick = { mode = BrowseMode.Albums },
                label = { Text("Albums") },
                leadingIcon = { Icon(Icons.Default.Album, null, modifier = Modifier.size(18.dp)) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF1DB954),
                    selectedLabelColor = Color.Black,
                    selectedLeadingIconColor = Color.Black
                )
            )
            Spacer(Modifier.width(8.dp))
            FilterChip(
                selected = mode == BrowseMode.Artists,
                onClick = { mode = BrowseMode.Artists },
                label = { Text("Artists") },
                leadingIcon = { Icon(Icons.Default.Person, null, modifier = Modifier.size(18.dp)) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF1DB954),
                    selectedLabelColor = Color.Black,
                    selectedLeadingIconColor = Color.Black
                )
            )
        }

        val list = if (mode == BrowseMode.Albums) albums else artists
        if (list.isEmpty()) {
            Box(Modifier.fillMaxSize(), Alignment.Center) {
                Text("Nothing here", color = Color.Gray)
            }
        } else {
            LazyColumn(Modifier.fillMaxSize()) {
                items(list.keys.toList()) { key ->
                    val group = list[key] ?: emptyList()
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .clickable {
                                playerVm.playSongs(group, 0)
                            }
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        Text(key, color = Color.White, fontWeight = FontWeight.SemiBold)
                        Text(
                            "${group.size} song${if (group.size == 1) "" else "s"}",
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    HorizontalDivider(color = Color(0xFF1A1A1A))
                }
            }
        }
    }
}

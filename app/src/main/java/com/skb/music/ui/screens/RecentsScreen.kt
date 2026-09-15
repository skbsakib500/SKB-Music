package com.skb.music.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.skb.music.data.Song
import com.skb.music.ui.components.SongRow
import com.skb.music.viewmodel.LibraryViewModel
import com.skb.music.viewmodel.PlayerViewModel

@Composable
fun RecentsScreen(
    libraryVm: LibraryViewModel,
    playerVm: PlayerViewModel,
    onSongMenu: (Song) -> Unit
) {
    val recents by libraryVm.recents.collectAsStateWithLifecycle()
    val current by playerVm.currentSong.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize().background(Color.Black)) {
        if (recents.isEmpty()) {
            Box(Modifier.fillMaxSize(), Alignment.Center) {
                Text("Nothing played yet", color = Color.Gray)
            }
        } else {
            LazyColumn(Modifier.fillMaxSize()) {
                item {
                    Text(
                        "RECENTLY PLAYED - ${recents.size}",
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                items(recents, key = { it.id }) { song ->
                    SongRow(
                        song = song,
                        isPlaying = current?.id == song.id,
                        isFavorite = false,
                        onClick = { playerVm.playSongs(recents, recents.indexOf(song)) },
                        onLongClick = { onSongMenu(song) },
                        onFavoriteToggle = { },
                        onMenu = { onSongMenu(song) }
                    )
                    HorizontalDivider(color = Color(0xFF1A1A1A))
                }
            }
        }
    }
}

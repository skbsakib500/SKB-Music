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
import com.skb.music.ui.components.SongRow
import com.skb.music.viewmodel.LibraryViewModel
import com.skb.music.viewmodel.PlayerViewModel

@Composable
fun FavoritesScreen(
    libraryVm: LibraryViewModel,
    playerVm: PlayerViewModel
) {
    val favorites by libraryVm.favorites.collectAsStateWithLifecycle()
    val current by playerVm.currentSong.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize().background(Color.Black)) {
        if (favorites.isEmpty()) {
            Box(Modifier.fillMaxSize(), Alignment.Center) {
                Text("No favorites yet", color = Color.Gray)
            }
        } else {
            LazyColumn(Modifier.fillMaxSize()) {
                item {
                    Text(
                        "FAVORITES - ${favorites.size}",
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                items(favorites, key = { it.id }) { song ->
                    SongRow(
                        song = song,
                        isPlaying = current?.id == song.id,
                        isFavorite = true,
                        onClick = { playerVm.playSongs(favorites, favorites.indexOf(song)) },
                        onFavoriteToggle = { libraryVm.toggleFavorite(song.id, true) },
                        onMenu = { }
                    )
                    HorizontalDivider(color = Color(0xFF1A1A1A))
                }
            }
        }
    }
}

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
import com.skb.music.data.Song
import com.skb.music.ui.components.SongRow
import com.skb.music.viewmodel.LibraryViewModel
import com.skb.music.viewmodel.PlayerViewModel

@Composable
fun LibraryScreen(
    libraryVm: LibraryViewModel,
    playerVm: PlayerViewModel,
    onSongMenu: (Song) -> Unit
) {
    val songs by libraryVm.filteredSongs.collectAsStateWithLifecycle()
    val favorites by libraryVm.favorites.collectAsStateWithLifecycle()
    val query by libraryVm.query.collectAsStateWithLifecycle()
    val loading by libraryVm.loading.collectAsStateWithLifecycle()
    val current by playerVm.currentSong.collectAsStateWithLifecycle()

    val favoriteIds = remember(favorites) { favorites.map { it.id }.toHashSet() }

    Column(
        modifier = Modifier.fillMaxSize().background(Color.Black)
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = libraryVm::setQuery,
            placeholder = { Text("Search songs, artists, albums...") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF1DB954),
                unfocusedBorderColor = Color.DarkGray,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = Color(0xFF1DB954)
            )
        )

        when {
            loading && songs.isEmpty() -> {
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF1DB954))
                }
            }
            songs.isEmpty() -> {
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    Text("No songs found", color = Color.Gray)
                }
            }
            else -> {
                LazyColumn(Modifier.fillMaxSize()) {
                    item {
                        Text(
                            "SONGS - ${songs.size}",
                            color = Color.Gray,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                    items(songs, key = { it.id }) { song ->
                        SongRow(
                            song = song,
                            isPlaying = current?.id == song.id,
                            isFavorite = favoriteIds.contains(song.id),
                            onClick = {
                                playerVm.playSongs(songs, songs.indexOf(song))
                            },
                            onFavoriteToggle = {
                                libraryVm.toggleFavorite(song.id, favoriteIds.contains(song.id))
                            },
                            onMenu = { onSongMenu(song) }
                        )
                        HorizontalDivider(color = Color(0xFF1A1A1A))
                    }
                }
            }
        }
    }
}

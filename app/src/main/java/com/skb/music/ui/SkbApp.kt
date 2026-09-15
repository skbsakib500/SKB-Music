package com.skb.music.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.skb.music.data.Song
import com.skb.music.ui.screens.FavoritesScreen
import com.skb.music.ui.screens.LibraryScreen
import com.skb.music.ui.screens.NowPlayingScreen
import com.skb.music.ui.screens.PlaylistsScreen
import com.skb.music.viewmodel.LibraryViewModel
import com.skb.music.viewmodel.PlayerViewModel

private enum class Tab { Songs, Favorites, Playlists }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkbApp(
    libraryVm: LibraryViewModel,
    playerVm: PlayerViewModel = viewModel()
) {
    var tab by remember { mutableStateOf(Tab.Songs) }
    var showNowPlaying by remember { mutableStateOf(false) }
    var songMenuFor by remember { mutableStateOf<Song?>(null) }

    val current by playerVm.currentSong.collectAsStateWithLifecycle()
    val isPlaying by playerVm.isPlaying.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { libraryVm.refresh() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SKB Music", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black,
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = {
            Column {
                current?.let {
                    MiniPlayerBar(
                        song = it,
                        isPlaying = isPlaying,
                        onExpand = { showNowPlaying = true },
                        onToggle = { playerVm.togglePlayPause() },
                        onNext = { playerVm.next() }
                    )
                }
                NavigationBar(containerColor = Color(0xFF0A0A0A)) {
                    NavigationBarItem(
                        selected = tab == Tab.Songs,
                        onClick = { tab = Tab.Songs },
                        icon = { Icon(Icons.Default.LibraryMusic, null) },
                        label = { Text("Songs") }
                    )
                    NavigationBarItem(
                        selected = tab == Tab.Favorites,
                        onClick = { tab = Tab.Favorites },
                        icon = { Icon(Icons.Default.Favorite, null) },
                        label = { Text("Favorites") }
                    )
                    NavigationBarItem(
                        selected = tab == Tab.Playlists,
                        onClick = { tab = Tab.Playlists },
                        icon = { Icon(Icons.Default.PlaylistPlay, null) },
                        label = { Text("Playlists") }
                    )
                }
            }
        },
        containerColor = Color.Black
    ) { pad ->
        Box(
            Modifier
                .padding(pad)
                .fillMaxSize()
                .background(Color.Black)
        ) {
            when (tab) {
                Tab.Songs -> LibraryScreen(
                    libraryVm = libraryVm,
                    playerVm = playerVm,
                    onSongMenu = { songMenuFor = it }
                )
                Tab.Favorites -> FavoritesScreen(libraryVm, playerVm)
                Tab.Playlists -> PlaylistsScreen(libraryVm)
            }
        }
    }

    AnimatedVisibility(
        visible = showNowPlaying,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it })
    ) {
        NowPlayingScreen(playerVm = playerVm, onClose = { showNowPlaying = false })
    }

    songMenuFor?.let { song ->
        AddToPlaylistSheet(
            song = song,
            libraryVm = libraryVm,
            onDismiss = { songMenuFor = null }
        )
    }
}

@Composable
private fun MiniPlayerBar(
    song: Song,
    isPlaying: Boolean,
    onExpand: () -> Unit,
    onToggle: () -> Unit,
    onNext: () -> Unit
) {
    Surface(
        color = Color(0xFF0E0E0E),
        tonalElevation = 8.dp,
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .clickable(onClick = onExpand)
    ) {
        Row(
            Modifier.fillMaxSize().padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(44.dp)
                    .background(Color(0xFF141414)),
                contentAlignment = Alignment.Center
            ) {
                if (song.albumArtUri != null) {
                    AsyncImage(model = song.albumArtUri, contentDescription = null)
                } else {
                    Icon(Icons.Default.MusicNote, null, tint = Color(0xFF1DB954))
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(song.title, color = Color.White, maxLines = 1, fontWeight = FontWeight.SemiBold)
                Text(song.artist, color = Color.Gray, maxLines = 1)
            }
            IconButton(onClick = onToggle) {
                Icon(
                    if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = "Play/Pause",
                    tint = Color(0xFF1DB954)
                )
            }
            IconButton(onClick = onNext) {
                Icon(Icons.Default.SkipNext, contentDescription = "Next", tint = Color.White)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddToPlaylistSheet(
    song: Song,
    libraryVm: LibraryViewModel,
    onDismiss: () -> Unit
) {
    val playlists by libraryVm.playlists.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF141414)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                "Add to Playlist",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(Modifier.height(12.dp))
            if (playlists.isEmpty()) {
                Text("No playlists. Create one first.", color = Color.Gray)
            } else {
                playlists.forEach { pl ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable {
                                libraryVm.addSongToPlaylist(pl.id, song.id)
                                onDismiss()
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.PlaylistPlay, null, tint = Color(0xFF1DB954))
                        Spacer(Modifier.width(12.dp))
                        Text(pl.name, color = Color.White)
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

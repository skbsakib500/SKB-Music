package com.skb.music.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.skb.music.viewmodel.LibraryViewModel

@Composable
fun PlaylistsScreen(libraryVm: LibraryViewModel) {
    val playlists by libraryVm.playlists.collectAsStateWithLifecycle()
    var showDialog by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }

    Column(Modifier.fillMaxSize().background(Color.Black)) {
        Row(
            Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "PLAYLISTS - ${playlists.size}",
                color = Color.Gray,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "New", tint = Color(0xFF1DB954))
            }
        }

        if (playlists.isEmpty()) {
            Box(Modifier.fillMaxSize(), Alignment.Center) {
                Text("No playlists yet. Tap + to create.", color = Color.Gray)
            }
        } else {
            LazyColumn(Modifier.fillMaxSize()) {
                items(playlists, key = { it.id }) { pl ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.PlaylistPlay, null, tint = Color(0xFF1DB954))
                        Spacer(Modifier.width(12.dp))
                        Text(pl.name, color = Color.White, modifier = Modifier.weight(1f))
                        IconButton(onClick = { libraryVm.deletePlaylist(pl.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray)
                        }
                    }
                    HorizontalDivider(color = Color(0xFF1A1A1A))
                }
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("New Playlist") },
            text = {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    placeholder = { Text("Playlist name") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newName.isNotBlank()) {
                        libraryVm.createPlaylist(newName.trim())
                        newName = ""
                        showDialog = false
                    }
                }) { Text("Create", color = Color(0xFF1DB954)) }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            },
            containerColor = Color(0xFF141414),
            titleContentColor = Color.White,
            textContentColor = Color.White
        )
    }
}

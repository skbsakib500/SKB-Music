package com.skb.music.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.skb.music.SkbApplication
import com.skb.music.data.Song
import com.skb.music.data.db.PlaylistEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LibraryViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = (app as SkbApplication).repository

    private val _allSongs = MutableStateFlow<List<Song>>(emptyList())
    val allSongs: StateFlow<List<Song>> = _allSongs.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    val filteredSongs: StateFlow<List<Song>> =
        combine(_allSongs, _query) { songs, q ->
            if (q.isBlank()) songs
            else songs.filter {
                it.title.contains(q, true) ||
                    it.artist.contains(q, true) ||
                    it.album.contains(q, true)
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val favorites: StateFlow<List<Song>> =
        combine(_allSongs, repo.favoriteIds()) { songs, ids ->
            val set = ids.toHashSet()
            songs.filter { it.id in set }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val recents: StateFlow<List<Song>> =
        combine(_allSongs, repo.recentIds()) { songs, ids ->
            val map = songs.associateBy { it.id }
            ids.mapNotNull { map[it] }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val playlists: StateFlow<List<PlaylistEntity>> =
        repo.playlists().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun refresh() {
        viewModelScope.launch {
            _loading.value = true
            _allSongs.value = repo.scanLibrary()
            _loading.value = false
        }
    }

    fun setQuery(q: String) { _query.value = q }

    fun toggleFavorite(songId: Long, isFav: Boolean) {
        viewModelScope.launch {
            if (isFav) repo.removeFavorite(songId) else repo.addFavorite(songId)
        }
    }

    fun createPlaylist(name: String) {
        viewModelScope.launch { repo.createPlaylist(name) }
    }

    fun deletePlaylist(id: Long) {
        viewModelScope.launch { repo.deletePlaylist(id) }
    }

    fun addSongToPlaylist(playlistId: Long, songId: Long) {
        viewModelScope.launch { repo.addSongToPlaylist(playlistId, songId) }
    }

    fun songsOfPlaylist(playlistId: Long): StateFlow<List<Song>> =
        combine(_allSongs, repo.playlistSongIds(playlistId)) { songs, ids ->
            val map = songs.associateBy { it.id }
            ids.mapNotNull { map[it] }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}

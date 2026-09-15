package com.skb.music.data

import android.content.Context
import com.skb.music.data.db.FavoriteEntity
import com.skb.music.data.db.PlaylistEntity
import com.skb.music.data.db.PlaylistSongEntity
import com.skb.music.data.db.RecentEntity
import com.skb.music.data.db.SkbDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class MusicRepository(context: Context) {

    private val db = SkbDatabase.get(context)
    private val scanner = MediaStoreScanner(context)

    suspend fun scanLibrary(): List<Song> = withContext(Dispatchers.IO) {
        scanner.scan()
    }

    suspend fun addFavorite(songId: Long) = withContext(Dispatchers.IO) {
        db.favoriteDao().add(FavoriteEntity(songId))
    }

    suspend fun removeFavorite(songId: Long) = withContext(Dispatchers.IO) {
        db.favoriteDao().remove(songId)
    }

    fun favoriteIds(): Flow<List<Long>> = db.favoriteDao().allIds()

    fun isFavorite(songId: Long): Flow<Boolean> = db.favoriteDao().isFavorite(songId)

    suspend fun createPlaylist(name: String): Long = withContext(Dispatchers.IO) {
        db.playlistDao().create(PlaylistEntity(name = name))
    }

    suspend fun deletePlaylist(id: Long) = withContext(Dispatchers.IO) {
        db.playlistDao().delete(id)
    }

    fun playlists(): Flow<List<PlaylistEntity>> = db.playlistDao().all()

    suspend fun addSongToPlaylist(playlistId: Long, songId: Long) =
        withContext(Dispatchers.IO) {
            db.playlistDao().addSong(
                PlaylistSongEntity(
                    playlistId,
                    songId,
                    position = (System.currentTimeMillis() / 1000).toInt()
                )
            )
        }

    suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long) =
        withContext(Dispatchers.IO) {
            db.playlistDao().removeSong(playlistId, songId)
        }

    fun playlistSongIds(playlistId: Long): Flow<List<Long>> =
        db.playlistDao().songIds(playlistId)

    suspend fun markPlayed(songId: Long) = withContext(Dispatchers.IO) {
        db.recentDao().add(RecentEntity(songId, System.currentTimeMillis()))
    }

    fun recentIds(): Flow<List<Long>> = db.recentDao().recentIds()

    suspend fun clearRecents() = withContext(Dispatchers.IO) {
        db.recentDao().clear()
    }
}

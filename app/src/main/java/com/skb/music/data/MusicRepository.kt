package com.skb.music.data

import android.content.Context
import com.skb.music.data.db.FavoriteEntity
import com.skb.music.data.db.PlaylistEntity
import com.skb.music.data.db.PlaylistSongEntity
import com.skb.music.data.db.RecentEntity
import com.skb.music.data.db.SkbDatabase
import com.skb.music.data.db.StatEntity
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
        val existing = db.statDao().get(songId)
        val updated = if (existing == null) {
            StatEntity(songId, playCount = 1, totalMs = 0L, lastPlayedAt = System.currentTimeMillis())
        } else {
            existing.copy(
                playCount = existing.playCount + 1,
                lastPlayedAt = System.currentTimeMillis()
            )
        }
        db.statDao().upsert(updated)
    }

    suspend fun addListenTime(songId: Long, deltaMs: Long) = withContext(Dispatchers.IO) {
        if (deltaMs <= 0) return@withContext
        val existing = db.statDao().get(songId) ?: StatEntity(
            songId, playCount = 0, totalMs = 0L, lastPlayedAt = System.currentTimeMillis()
        )
        db.statDao().upsert(existing.copy(totalMs = existing.totalMs + deltaMs))
    }

    fun recentIds(): Flow<List<Long>> = db.recentDao().recentIds()

    suspend fun clearRecents() = withContext(Dispatchers.IO) {
        db.recentDao().clear()
    }

    fun topByCount(limit: Int = 25): Flow<List<StatEntity>> = db.statDao().topByCount(limit)
    fun recentlyPlayedStats(limit: Int = 25): Flow<List<StatEntity>> = db.statDao().recentlyPlayed(limit)
    fun totalPlays(): Flow<Int?> = db.statDao().totalPlays()
    fun totalListenMs(): Flow<Long?> = db.statDao().totalListenMs()
    fun uniqueSongs(): Flow<Int> = db.statDao().uniqueSongs()
    suspend fun clearStats() = withContext(Dispatchers.IO) { db.statDao().clear() }
}

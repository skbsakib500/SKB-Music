package com.skb.music.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun add(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE songId = :songId")
    suspend fun remove(songId: Long)

    @Query("SELECT songId FROM favorites ORDER BY addedAt DESC")
    fun allIds(): Flow<List<Long>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE songId = :songId)")
    fun isFavorite(songId: Long): Flow<Boolean>
}

@Dao
interface PlaylistDao {
    @Insert
    suspend fun create(playlist: PlaylistEntity): Long

    @Query("DELETE FROM playlists WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("SELECT * FROM playlists ORDER BY createdAt DESC")
    fun all(): Flow<List<PlaylistEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addSong(item: PlaylistSongEntity)

    @Query("DELETE FROM playlist_songs WHERE playlistId = :playlistId AND songId = :songId")
    suspend fun removeSong(playlistId: Long, songId: Long)

    @Query("SELECT songId FROM playlist_songs WHERE playlistId = :playlistId ORDER BY position ASC")
    fun songIds(playlistId: Long): Flow<List<Long>>
}

@Dao
interface RecentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun add(recent: RecentEntity)

    @Query("SELECT songId FROM recent ORDER BY playedAt DESC LIMIT :limit")
    fun recentIds(limit: Int = 50): Flow<List<Long>>

    @Query("DELETE FROM recent")
    suspend fun clear()
}

@Dao
interface StatDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(stat: StatEntity)

    @Query("SELECT * FROM stats WHERE songId = :songId LIMIT 1")
    suspend fun get(songId: Long): StatEntity?

    @Query("SELECT * FROM stats ORDER BY playCount DESC LIMIT :limit")
    fun topByCount(limit: Int): Flow<List<StatEntity>>

    @Query("SELECT * FROM stats ORDER BY lastPlayedAt DESC LIMIT :limit")
    fun recentlyPlayed(limit: Int): Flow<List<StatEntity>>

    @Query("SELECT SUM(playCount) FROM stats")
    fun totalPlays(): Flow<Int?>

    @Query("SELECT SUM(totalMs) FROM stats")
    fun totalListenMs(): Flow<Long?>

    @Query("SELECT COUNT(*) FROM stats")
    fun uniqueSongs(): Flow<Int>

    @Query("DELETE FROM stats")
    suspend fun clear()
}

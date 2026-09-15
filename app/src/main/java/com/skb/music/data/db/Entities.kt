package com.skb.music.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val songId: Long,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "playlist_songs", primaryKeys = ["playlistId", "songId"])
data class PlaylistSongEntity(
    val playlistId: Long,
    val songId: Long,
    val position: Int
)

@Entity(tableName = "recent")
data class RecentEntity(
    @PrimaryKey val songId: Long,
    val playedAt: Long
)

@Entity(tableName = "stats")
data class StatEntity(
    @PrimaryKey val songId: Long,
    val playCount: Int,
    val totalMs: Long,
    val lastPlayedAt: Long
)

package com.skb.music.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        FavoriteEntity::class,
        PlaylistEntity::class,
        PlaylistSongEntity::class,
        RecentEntity::class,
        StatEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class SkbDatabase : RoomDatabase() {
    abstract fun favoriteDao(): FavoriteDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun recentDao(): RecentDao
    abstract fun statDao(): StatDao

    companion object {
        @Volatile private var INSTANCE: SkbDatabase? = null

        fun get(context: Context): SkbDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    SkbDatabase::class.java,
                    "skb_music.db"
                ).fallbackToDestructiveMigration().build().also { INSTANCE = it }
            }
    }
}

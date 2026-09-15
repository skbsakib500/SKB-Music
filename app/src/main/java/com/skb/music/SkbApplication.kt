package com.skb.music

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.skb.music.core.Constants
import com.skb.music.data.MusicRepository
import com.skb.music.equalizer.AudioEffectsManager

class SkbApplication : Application() {

    lateinit var repository: MusicRepository
        private set

    override fun onCreate() {
        super.onCreate()
        repository = MusicRepository(this)
        AudioEffectsManager.init(this)
        createChannel()
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ch = NotificationChannel(
                Constants.CHANNEL_ID,
                "Playback",
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(ch)
        }
    }
}

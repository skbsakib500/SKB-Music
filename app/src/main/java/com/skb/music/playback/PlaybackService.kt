package com.skb.music.playback

import android.app.PendingIntent
import android.content.Intent
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.CommandButton
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.skb.music.MainActivity
import com.skb.music.R
import com.skb.music.core.Constants
import com.skb.music.equalizer.AudioEffectsManager

class PlaybackService : MediaSessionService() {

    private var mediaSession: MediaSession? = null

    override fun onCreate() {
        super.onCreate()

        val notificationProvider = DefaultMediaNotificationProvider.Builder(this)
            .setChannelId(Constants.CHANNEL_ID)
            .setChannelName(R.string.notif_channel_name)
            .setNotificationId(Constants.NOTIFICATION_ID)
            .build()
        notificationProvider.setSmallIcon(R.drawable.ic_notification)
        setMediaNotificationProvider(notificationProvider)

        val player = ExoPlayer.Builder(this)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .setUsage(C.USAGE_MEDIA)
                    .build(),
                true
            )
            .setHandleAudioBecomingNoisy(true)
            .build()

        player.addListener(object : Player.Listener {
            override fun onAudioSessionIdChanged(audioSessionId: Int) {
                if (audioSessionId != C.AUDIO_SESSION_ID_UNSET && audioSessionId > 0) {
                    AudioEffectsManager.attach(audioSessionId)
                }
            }
        })

        val sessionActivity = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val shuffleButton = CommandButton.Builder()
            .setDisplayName("Shuffle")
            .setIconResId(R.drawable.ic_notification)
            .setSessionCommand(SessionCommand("ACTION_TOGGLE_SHUFFLE", android.os.Bundle.EMPTY))
            .build()

        val queueButton = CommandButton.Builder()
            .setDisplayName("Queue")
            .setIconResId(R.drawable.ic_notification)
            .setSessionCommand(SessionCommand("ACTION_OPEN_QUEUE", android.os.Bundle.EMPTY))
            .build()

        val session = MediaSession.Builder(this, player)
            .setSessionActivity(sessionActivity)
            .setCustomLayout(ImmutableList.of(shuffleButton, queueButton))
            .setCallback(object : MediaSession.Callback {
                override fun onConnect(
                    session: MediaSession,
                    controller: MediaSession.ControllerInfo
                ): MediaSession.ConnectionResult {
                    val available = MediaSession.ConnectionResult.DEFAULT_SESSION_AND_LIBRARY_COMMANDS.buildUpon()
                        .add(SessionCommand("ACTION_TOGGLE_SHUFFLE", android.os.Bundle.EMPTY))
                        .add(SessionCommand("ACTION_OPEN_QUEUE", android.os.Bundle.EMPTY))
                        .build()
                    return MediaSession.ConnectionResult.AcceptedResultBuilder(session)
                        .setAvailableSessionCommands(available)
                        .build()
                }

                override fun onCustomCommand(
                    session: MediaSession,
                    controller: MediaSession.ControllerInfo,
                    customCommand: SessionCommand,
                    args: android.os.Bundle
                ): ListenableFuture<SessionResult> {
                    when (customCommand.customAction) {
                        "ACTION_TOGGLE_SHUFFLE" -> {
                            val p = session.player
                            p.shuffleModeEnabled = !p.shuffleModeEnabled
                        }
                        "ACTION_OPEN_QUEUE" -> {
                            // No-op for now; notification tap on this just brings app to front
                        }
                    }
                    return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
                }
            })
            .build()

        mediaSession = session
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo) = mediaSession

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        AudioEffectsManager.release(clearSession = true)
        super.onDestroy()
    }
}

package com.skb.music.data

import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata

fun Song.toMediaItem(): MediaItem = MediaItem.Builder()
    .setMediaId(id.toString())
    .setUri(uri)
    .setMediaMetadata(
        MediaMetadata.Builder()
            .setTitle(title)
            .setArtist(artist)
            .setAlbumTitle(album)
            .setArtworkUri(albumArtUri)
            .build()
    ).build()

fun MediaItem.toSong(fallbackDurationMs: Long = 0L): Song? {
    val u = localConfiguration?.uri ?: return null
    val meta = mediaMetadata
    return Song(
        id = mediaId.toLongOrNull() ?: 0L,
        title = meta.title?.toString().orEmpty().ifBlank { "Unknown" },
        artist = meta.artist?.toString().orEmpty().ifBlank { "Unknown" },
        album = meta.albumTitle?.toString().orEmpty().ifBlank { "Unknown" },
        duration = fallbackDurationMs,
        uri = u,
        albumArtUri = meta.artworkUri
    )
}

package ink.tenqui.flowtone.playback

import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import ink.tenqui.flowtone.model.Song

fun Song.toMediaItem(): MediaItem {
    val mediaMetadata = MediaMetadata.Builder()
        .setTitle(title)
        .setArtist(artist)
        .build()

    return MediaItem.Builder()
        .setMediaId(id.toString())
        .setUri(uri)
        .setMediaMetadata(mediaMetadata)
        .build()
}

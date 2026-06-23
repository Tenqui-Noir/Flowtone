package ink.tenqui.flowtone.playback

import androidx.media3.common.MediaItem
import ink.tenqui.flowtone.core.model.Song

fun Song.toMediaItem(): MediaItem {
    return MediaItemMapper.toMediaItem(this)
}

fun MediaItem.toSongOrNull(scannedSongs: List<Song>): Song? {
    return MediaItemMapper.toSongOrNull(this, scannedSongs)
}

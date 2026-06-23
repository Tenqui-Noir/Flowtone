package ink.tenqui.flowtone.data.local

import ink.tenqui.flowtone.core.model.Song

class LocalMusicRepository(
    private val audioScanner: AudioScanner
) {
    fun loadSongs(): List<Song> {
        return audioScanner.scanSongs()
    }
}

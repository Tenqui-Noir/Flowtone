package ink.tenqui.flowtone.data.repository

import ink.tenqui.flowtone.core.model.Song
import ink.tenqui.flowtone.data.local.LocalMusicRepository

class MusicRepository(
    private val localMusicRepository: LocalMusicRepository
) {
    fun loadLocalSongs(): List<Song> {
        return localMusicRepository.loadSongs()
    }
}

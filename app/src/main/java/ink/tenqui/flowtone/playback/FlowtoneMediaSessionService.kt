package ink.tenqui.flowtone.playback

import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

class FlowtoneMediaSessionService : MediaSessionService() {
    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        // 0.5.2 只提供 Service 骨架；后续迁移播放器所有权后，这里再返回 Service 内部持有的 MediaSession。
        return null
    }
}

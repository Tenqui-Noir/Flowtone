package ink.tenqui.flowtone.playback

import android.content.ComponentName
import android.content.Context
import androidx.core.content.ContextCompat
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture

class FlowtoneMediaControllerConnection(context: Context) {
    private val appContext = context.applicationContext
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var controller: MediaController? = null
    private var onConnected: ((MediaController) -> Unit)? = null
    private var onConnectionFailed: ((Throwable) -> Unit)? = null

    val currentController: MediaController?
        get() = controller

    fun connect(
        onConnected: (MediaController) -> Unit,
        onConnectionFailed: (Throwable) -> Unit
    ) {
        this.onConnected = onConnected
        this.onConnectionFailed = onConnectionFailed

        controller?.let {
            onConnected(it)
            return
        }

        if (controllerFuture != null || controller != null) {
            return
        }

        val sessionToken = SessionToken(
            appContext,
            ComponentName(appContext, FlowtoneMediaSessionService::class.java)
        )
        val future = MediaController.Builder(appContext, sessionToken).buildAsync()
        controllerFuture = future
        future.addListener(
            {
                runCatching {
                    controller = future.get()
                    controller?.let { onConnected(it) }
                }.onFailure { error ->
                    controller = null
                    onConnectionFailed(error)
                }
            },
            ContextCompat.getMainExecutor(appContext)
        )
    }

    fun release() {
        onConnected = null
        onConnectionFailed = null
        controller = null
        controllerFuture?.let { future ->
            runCatching {
                MediaController.releaseFuture(future)
            }
            future.cancel(false)
        }
        controllerFuture = null
    }
}

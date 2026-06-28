package ink.tenqui.flowtone.ui.components

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

private val SwipeBackTriggerDistance = 48.dp

fun Modifier.rightSwipeBackGesture(onBack: () -> Unit): Modifier = composed {
    val density = LocalDensity.current
    val triggerDistancePx = with(density) { SwipeBackTriggerDistance.toPx() }
    val currentOnBack = rememberUpdatedState(onBack)

    pointerInput(triggerDistancePx) {
        var horizontalDrag = 0f
        var backTriggered = false
        detectHorizontalDragGestures(
            onDragStart = {
                horizontalDrag = 0f
                backTriggered = false
            },
            onHorizontalDrag = { change, dragAmount ->
                if (backTriggered) {
                    return@detectHorizontalDragGestures
                }
                change.consume()
                horizontalDrag = (horizontalDrag + dragAmount).coerceAtLeast(0f)
                if (horizontalDrag >= triggerDistancePx) {
                    backTriggered = true
                    currentOnBack.value()
                }
            },
            onDragEnd = {
                horizontalDrag = 0f
                backTriggered = false
            },
            onDragCancel = {
                horizontalDrag = 0f
                backTriggered = false
            }
        )
    }
}

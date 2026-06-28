package ink.tenqui.flowtone.ui.components

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

fun Modifier.rightSwipeBackGesture(onBack: () -> Unit): Modifier = pointerInput(onBack) {
    var horizontalDrag = 0f
    detectHorizontalDragGestures(
        onDragStart = { horizontalDrag = 0f },
        onHorizontalDrag = { change, dragAmount ->
            change.consume()
            horizontalDrag = (horizontalDrag + dragAmount).coerceAtLeast(0f)
        },
        onDragEnd = {
            if (horizontalDrag >= 72.dp.toPx()) {
                onBack()
            }
            horizontalDrag = 0f
        },
        onDragCancel = { horizontalDrag = 0f }
    )
}

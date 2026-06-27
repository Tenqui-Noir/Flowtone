package ink.tenqui.flowtone.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

private val StaggeredElementEasing = CubicBezierEasing(0.2f, 0f, 0f, 1f)

@Composable
fun StaggeredPageElement(
    visible: Boolean,
    animationIndex: Int,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(
            tween(
                durationMillis = 220,
                delayMillis = 90 + animationIndex * 45,
                easing = StaggeredElementEasing
            )
        ) + slideInVertically(
            animationSpec = tween(
                durationMillis = 240,
                delayMillis = 90 + animationIndex * 45,
                easing = StaggeredElementEasing
            )
        ) { it / 6 },
        exit = fadeOut(
            tween(
                durationMillis = 180,
                delayMillis = animationIndex * 45,
                easing = StaggeredElementEasing
            )
        ) + slideOutVertically(
            animationSpec = tween(
                durationMillis = 240,
                delayMillis = animationIndex * 45,
                easing = StaggeredElementEasing
            )
        ) { -it / 6 },
        modifier = modifier
    ) {
        content()
    }
}

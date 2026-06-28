package ink.tenqui.flowtone.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

fun AnimatedVisibilityScope.staggeredPageElementModifier(
    animationIndex: Int
): Modifier {
    val delayMillis = FlowtoneMotion.staggerDelayMillis(animationIndex)
    val durationMillis = FlowtoneMotion.staggerDurationMillis(animationIndex)
    return Modifier.animateEnterExit(
        enter = fadeIn(
            tween(
                durationMillis = durationMillis,
                delayMillis = delayMillis,
                easing = FlowtoneMotion.Easing
            )
        ) + slideInVertically(
            animationSpec = tween(
                durationMillis = durationMillis,
                delayMillis = delayMillis,
                easing = FlowtoneMotion.Easing
            )
        ) { it / 6 },
        exit = fadeOut(
            tween(
                durationMillis = durationMillis,
                delayMillis = delayMillis,
                easing = FlowtoneMotion.Easing
            )
        ) + slideOutVertically(
            animationSpec = tween(
                durationMillis = durationMillis,
                delayMillis = delayMillis,
                easing = FlowtoneMotion.Easing
            )
        ) { -it / 6 }
    )
}

@Composable
fun StaggeredPageElement(
    visible: Boolean,
    animationIndex: Int,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val delayMillis = FlowtoneMotion.staggerDelayMillis(animationIndex)
    val durationMillis = FlowtoneMotion.staggerDurationMillis(animationIndex)
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(
            tween(
                durationMillis = durationMillis,
                delayMillis = delayMillis,
                easing = FlowtoneMotion.Easing
            )
        ) + slideInVertically(
            animationSpec = tween(
                durationMillis = durationMillis,
                delayMillis = delayMillis,
                easing = FlowtoneMotion.Easing
            )
        ) { it / 6 },
        exit = fadeOut(
            tween(
                durationMillis = durationMillis,
                delayMillis = delayMillis,
                easing = FlowtoneMotion.Easing
            )
        ) + slideOutVertically(
            animationSpec = tween(
                durationMillis = durationMillis,
                delayMillis = delayMillis,
                easing = FlowtoneMotion.Easing
            )
        ) { -it / 6 },
        modifier = modifier
    ) {
        content()
    }
}

package ink.tenqui.flowtone.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
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
    applyElementMotion: Boolean = true,
    content: @Composable AnimatedVisibilityScope.() -> Unit
) {
    val delayMillis = FlowtoneMotion.staggerDelayMillis(animationIndex)
    val durationMillis = FlowtoneMotion.staggerDurationMillis(animationIndex)
    val enterTransition = if (applyElementMotion) {
        fadeIn(
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
        ) { it / 6 }
    } else {
        EnterTransition.None
    }
    val exitTransition = if (applyElementMotion) {
        fadeOut(
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
    } else {
        ExitTransition.None
    }

    AnimatedVisibility(
        visible = visible,
        enter = enterTransition,
        exit = exitTransition,
        modifier = modifier
    ) {
        content()
    }
}

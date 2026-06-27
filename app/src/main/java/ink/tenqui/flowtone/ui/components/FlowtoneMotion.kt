package ink.tenqui.flowtone.ui.components

import androidx.compose.animation.core.CubicBezierEasing

internal object FlowtoneMotion {
    const val DurationMillis = 400
    val Easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)

    fun staggerDelayMillis(index: Int): Int {
        return (index.coerceAtLeast(0) * 18).coerceAtMost(180)
    }

    fun staggerDurationMillis(index: Int): Int {
        return DurationMillis - staggerDelayMillis(index)
    }
}

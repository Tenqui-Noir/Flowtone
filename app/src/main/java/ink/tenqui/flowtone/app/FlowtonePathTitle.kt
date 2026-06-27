package ink.tenqui.flowtone.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs

@Composable
internal fun FlowtonePathTitle(
    pagerState: PagerState,
    rootPage: TopLevelPage,
    segments: List<String>,
    levelProgress: List<Float>,
    navigationShiftPx: Float,
    modifier: Modifier = Modifier
) {
    val retainedSegments = remember { mutableStateListOf<String>() }
    LaunchedEffect(segments) {
        segments.forEachIndexed { index, title ->
            if (index < retainedSegments.size) {
                retainedSegments[index] = title
            } else {
                retainedSegments += title
            }
        }
    }

    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()
    val rootProgress = levelProgress.getOrElse(0) { 0f }
    val expandedRootStyle = MaterialTheme.typography.titleLarge
    val compactStyle = MaterialTheme.typography.labelLarge
    val expandedChildStyle = MaterialTheme.typography.headlineSmall
    val rootStyle = interpolateTextStyle(expandedRootStyle, compactStyle, rootProgress)
    val slideDistancePx = with(density) { 36.dp.toPx() }
    val titleBaseOffsetYPx = with(density) { -3.dp.toPx() }
    val ancestorOffsetYPx = with(density) { -17.dp.toPx() }
    val rootOpticalOffsetXPx = with(density) { 1.dp.toPx() }
    val childRestingOffsetYPx = with(density) { 3.dp.toPx() }
    val childHiddenOffsetYPx = with(density) { 48.dp.toPx() }
    val pathBaselineCorrectionPx = with(density) { 1.dp.toPx() }
    val pathGapPx = with(density) { 2.dp.toPx() }
    val ancestorYPx = titleBaseOffsetYPx + ancestorOffsetYPx + pathBaselineCorrectionPx
    val pagePosition = pagerState.currentPage + pagerState.currentPageOffsetFraction
    val rootCompactWidthPx = textMeasurer.measure(
        text = rootPage.title,
        style = compactStyle
    ).size.width.toFloat()
    val separatorWidthPx = textMeasurer.measure(
        text = "/",
        style = compactStyle
    ).size.width.toFloat()
    val compactSegmentWidths = retainedSegments.map { title ->
        textMeasurer.measure(text = title, style = compactStyle).size.width.toFloat()
    }
    val separatorTargetX = mutableListOf<Float>()
    val segmentAncestorTargetX = mutableListOf<Float>()
    var pathCursorX = navigationShiftPx + rootOpticalOffsetXPx * rootProgress +
        rootCompactWidthPx
    compactSegmentWidths.forEach { width ->
        val separatorX = pathCursorX + pathGapPx
        val segmentX = separatorX + separatorWidthPx + pathGapPx
        separatorTargetX += separatorX
        segmentAncestorTargetX += segmentX
        pathCursorX = segmentX + width
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.CenterStart
    ) {
        TopLevelPage.entries.forEach { page ->
            val distance = page.index - pagePosition
            val isRoot = page == rootPage
            val pageAlpha = (1f - abs(distance)).coerceIn(0f, 1f)
            Text(
                text = page.title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = if (isRoot) rootStyle else expandedRootStyle,
                fontWeight = FontWeight.Medium,
                color = if (isRoot) {
                    lerp(
                        MaterialTheme.colorScheme.onSurface,
                        MaterialTheme.colorScheme.onSurfaceVariant,
                        rootProgress
                    )
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                modifier = Modifier.graphicsLayer {
                    translationX = distance * slideDistancePx +
                        if (isRoot) {
                            navigationShiftPx + rootOpticalOffsetXPx * rootProgress
                        } else {
                            0f
                        }
                    translationY = titleBaseOffsetYPx +
                        if (isRoot) ancestorOffsetYPx * rootProgress else 0f
                    alpha = pageAlpha * if (isRoot) 1f else 1f - rootProgress
                }
            )
        }

        retainedSegments.forEachIndexed { index, title ->
            val ownProgress = levelProgress.getOrElse(index) { 0f }
            val promotionProgress = levelProgress.getOrElse(index + 1) { 0f }
            val childOffsetY = childHiddenOffsetYPx +
                (childRestingOffsetYPx - childHiddenOffsetYPx) * ownProgress
            val segmentStyle = interpolateTextStyle(
                expandedChildStyle,
                compactStyle,
                promotionProgress
            )
            Text(
                text = "/",
                maxLines = 1,
                style = compactStyle,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.graphicsLayer {
                    translationX = separatorTargetX[index]
                    translationY = childHiddenOffsetYPx +
                        (ancestorYPx - childHiddenOffsetYPx) * promotionProgress
                    alpha = promotionProgress
                }
            )
            Text(
                text = title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = segmentStyle,
                fontWeight = FontWeight.Medium,
                color = lerp(
                    MaterialTheme.colorScheme.onSurface,
                    MaterialTheme.colorScheme.onSurfaceVariant,
                    promotionProgress
                ),
                modifier = Modifier.graphicsLayer {
                    translationX = navigationShiftPx +
                        (segmentAncestorTargetX[index] - navigationShiftPx) *
                        promotionProgress
                    translationY = childOffsetY +
                        (ancestorYPx - childRestingOffsetYPx) * promotionProgress
                    alpha = ownProgress
                }
            )
        }
    }
}

private fun interpolateTextStyle(
    from: TextStyle,
    to: TextStyle,
    progress: Float
): TextStyle {
    return from.copy(
        fontSize = (from.fontSize.value + (to.fontSize.value - from.fontSize.value) * progress).sp,
        lineHeight = (
            from.lineHeight.value + (to.lineHeight.value - from.lineHeight.value) * progress
            ).sp
    )
}

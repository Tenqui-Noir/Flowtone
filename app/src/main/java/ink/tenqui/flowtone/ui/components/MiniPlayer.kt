package ink.tenqui.flowtone.ui.components

import android.graphics.BlurMaskFilter
import android.graphics.Paint as NativePaint
import android.graphics.RectF
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.palette.graphics.Palette
import coil3.compose.AsyncImage
import coil3.imageLoader
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.request.allowHardware
import coil3.request.crossfade
import coil3.toBitmap
import ink.tenqui.flowtone.playback.PlaybackState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

private const val MINI_PLAYER_ANIMATION_DURATION_MS = 300
private val MiniPlayerEasing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)
private val SoftElementEasing = CubicBezierEasing(0.16f, 1.0f, 0.3f, 1.0f)
private val HeavyElementEasing = CubicBezierEasing(0.3f, 0.0f, 0.0f, 1.0f)

@Composable
fun MiniPlayer(
    playbackState: PlaybackState,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onTogglePlayPause: () -> Unit,
    onPlayPrevious: () -> Unit,
    onPlayNext: () -> Unit,
    onSeekTo: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentSong = playbackState.currentSong
    val hasCurrentSong = currentSong != null
    val title = currentSong?.title.orEmpty()
    val artist = currentSong?.artist.orEmpty()
    val artworkUri = currentSong?.artworkUri
    val durationMs = when {
        playbackState.durationMs > 0L -> playbackState.durationMs
        currentSong?.durationMs != null && currentSong.durationMs > 0L -> currentSong.durationMs
        else -> 0L
    }
    val playbackProgress = if (durationMs > 0L) {
        playbackState.positionMs.toFloat() / durationMs.toFloat()
    } else {
        0f
    }.coerceIn(0f, 1f)
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val context = LocalContext.current
    val collapsedHeight = 92.dp
    val dragHotZoneHeight = 40.dp
    val swipeThresholdPx = with(density) { 40.dp.toPx() }
    val targetExpandedHeight = configuration.screenHeightDp.dp * 0.618f
    val widthBasedArtworkSize = if (configuration.screenWidthDp.dp * 0.76f < 340.dp) {
        configuration.screenWidthDp.dp * 0.76f
    } else {
        340.dp
    }
    val expandedHeight = if (targetExpandedHeight > collapsedHeight) {
        targetExpandedHeight
    } else {
        collapsedHeight
    }
    val heightLimitedArtworkSize = expandedHeight * 0.52f
    val expandedArtworkSize = if (widthBasedArtworkSize < heightLimitedArtworkSize) {
        widthBasedArtworkSize
    } else {
        heightLimitedArtworkSize
    }
    val expandedArtworkTop = 24.dp
    val expandedMetadataTop = expandedArtworkTop + expandedArtworkSize + 14.dp
    val expandedProgressTop = expandedMetadataTop + 76.dp
    val expandedControlsTop = expandedProgressTop + 58.dp
    val animationProgress by animateFloatAsState(
        targetValue = if (expanded) 1f else 0f,
        animationSpec = tween(
            durationMillis = MINI_PLAYER_ANIMATION_DURATION_MS,
            easing = FastOutSlowInEasing
        ),
        label = "MiniPlayerProgress"
    )
    val currentHeight = collapsedHeight + (expandedHeight - collapsedHeight) * animationProgress
    val visibleProgress by animateFloatAsState(
        targetValue = if (hasCurrentSong) 1f else 0f,
        animationSpec = tween(
            durationMillis = MINI_PLAYER_ANIMATION_DURATION_MS,
            easing = FastOutSlowInEasing
        ),
        label = "MiniPlayerVisibleProgress"
    )
    val hiddenOffsetDp = currentHeight + dragHotZoneHeight + 32.dp

    val slideInEasing = CubicBezierEasing(0.05f, 0.85f, 0.18f, 1.0f)

    val miniPlayerSlideOffsetY by animateDpAsState(
        targetValue = if (hasCurrentSong) 0.dp else hiddenOffsetDp,
        animationSpec = tween(
            durationMillis = 360,
            easing = if (hasCurrentSong) {
                slideInEasing
            } else {
                FastOutSlowInEasing
            }
        ),
        label = "MiniPlayerSlideOffsetY"
    )
    val hasArtworkBackground = artworkUri != null
    val backgroundImageRequest: ImageRequest? = remember(artworkUri, context) {
        artworkUri?.let { uri ->
            ImageRequest.Builder(context)
                .data(uri)
                .size(256, 256)
                .crossfade(false)
                .build()
        }
    }
    val coverImageRequest: ImageRequest? = remember(artworkUri, context) {
        artworkUri?.let { uri ->
            ImageRequest.Builder(context)
                .data(uri)
                .size(768, 768)
                .crossfade(false)
                .build()
        }
    }
    val paletteImageRequest: ImageRequest? = remember(artworkUri, context) {
        artworkUri?.let { uri ->
            ImageRequest.Builder(context)
                .data(uri)
                .size(128, 128)
                .allowHardware(false)
                .crossfade(false)
                .build()
        }
    }
    LaunchedEffect(coverImageRequest) {
        coverImageRequest?.let { request ->
            context.imageLoader.enqueue(request)
        }
    }
    val fallbackCloudColors = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondaryContainer,
        MaterialTheme.colorScheme.tertiaryContainer
    )
    var extractedCloudColors by remember {
        mutableStateOf<List<Color>?>(null)
    }
    LaunchedEffect(paletteImageRequest) {
        extractedCloudColors = paletteImageRequest?.let { request ->
            runCatching {
                withContext(Dispatchers.Default) {
                    val result = context.imageLoader.execute(request)
                    val bitmap = (result as? SuccessResult)?.image?.toBitmap(128, 128)
                    bitmap?.let { sourceBitmap ->
                        val palette = Palette.from(sourceBitmap).generate()
                        listOf(
                            Color(
                                palette.vibrantSwatch?.rgb
                                    ?: palette.dominantSwatch?.rgb
                                    ?: palette.getDominantColor(android.graphics.Color.DKGRAY)
                            ),
                            Color(
                                palette.mutedSwatch?.rgb
                                    ?: palette.darkVibrantSwatch?.rgb
                                    ?: palette.getMutedColor(android.graphics.Color.GRAY)
                            ),
                            Color(
                                palette.lightVibrantSwatch?.rgb
                                    ?: palette.lightMutedSwatch?.rgb
                                    ?: palette.getLightMutedColor(android.graphics.Color.LTGRAY)
                            )
                        )
                    }
                }
            }.getOrNull()
        }
    }
    val cloudColors = extractedCloudColors ?: fallbackCloudColors
    val noRippleInteractionSource = remember { MutableInteractionSource() }
    val titleColor = if (hasArtworkBackground) {
        Color.White
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    val artistColor = if (hasArtworkBackground) {
        Color.White.copy(alpha = 0.78f)
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    val controlIconColor = if (hasArtworkBackground) {
        Color.White
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    val progressTrackColor = if (hasArtworkBackground) {
        Color(0xFF9E9E9E)
    } else {
        Color(0xFF8A8A8A)
    }
    val progressColor = if (hasArtworkBackground) {
        Color.White
    } else {
        MaterialTheme.colorScheme.primary
    }
    var isProgressScrubbing by remember { mutableStateOf(false) }
    var lockedIsPlayingDuringScrub by remember { mutableStateOf(playbackState.isPlaying) }
    var keepPlayPauseVisualLockedAfterSeek by remember { mutableStateOf(false) }
    LaunchedEffect(currentSong?.id) {
        isProgressScrubbing = false
        keepPlayPauseVisualLockedAfterSeek = false
    }
    LaunchedEffect(isProgressScrubbing, keepPlayPauseVisualLockedAfterSeek) {
        if (keepPlayPauseVisualLockedAfterSeek && !isProgressScrubbing) {
            delay(500)
            keepPlayPauseVisualLockedAfterSeek = false
        }
    }
    val visualIsPlaying = if (isProgressScrubbing || keepPlayPauseVisualLockedAfterSeek) {
        lockedIsPlayingDuringScrub
    } else {
        playbackState.isPlaying
    }
    var accumulatedDragY by remember { mutableStateOf(0f) }
    val gestureModifier = Modifier.pointerInput(hasCurrentSong, expanded, swipeThresholdPx) {
        detectVerticalDragGestures(
            onDragStart = {
                accumulatedDragY = 0f
            },
            onVerticalDrag = { _, dragAmount ->
                if (hasCurrentSong) {
                    accumulatedDragY += dragAmount
                }
            },
            onDragEnd = {
                if (!hasCurrentSong) {
                    return@detectVerticalDragGestures
                }
                if (!expanded && accumulatedDragY <= -swipeThresholdPx) {
                    onExpandedChange(true)
                } else if (expanded && accumulatedDragY >= swipeThresholdPx) {
                    onExpandedChange(false)
                }
            },
            onDragCancel = {
                accumulatedDragY = 0f
            }
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(currentHeight + dragHotZoneHeight)
            .graphicsLayer {
                translationY = miniPlayerSlideOffsetY.toPx()
                alpha = visibleProgress
            }
            .then(gestureModifier)
    ) {
        val playerShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        val playerShadowElevation = lerpDp(0.dp, 18.dp, animationProgress)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(dragHotZoneHeight)
                .align(Alignment.TopCenter)
                .clickable(
                    enabled = hasCurrentSong && !expanded,
                    interactionSource = noRippleInteractionSource,
                    indication = null
                ) {
                    onExpandedChange(true)
                }
        ) {
            val handleAlpha = lerpFloat(1f, 0.65f, animationProgress)
            val isLightTheme = MaterialTheme.colorScheme.background.luminance() > 0.5f
            val handleShape = RoundedCornerShape(percent = 50)
            val handleBaseColor = if (isLightTheme) {
                Color.Black.copy(alpha = 0.22f)
            } else {
                Color.White.copy(alpha = 0.26f)
            }
            val handleBlurColor = if (isLightTheme) {
                Color.Black.copy(alpha = 0.18f)
            } else {
                Color.White.copy(alpha = 0.20f)
            }
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 8.dp)
                    .offset(y = 0.dp)
                    .width(72.dp)
                    .height(6.dp)
                    .graphicsLayer {
                        alpha = handleAlpha
                    }
                    .clip(handleShape)
                    .background(handleBaseColor, handleShape)
            ) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            color = handleBlurColor,
                            shape = handleShape
                        )
                        .blur(8.dp)
                )
            }
        }
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = dragHotZoneHeight)
                .fillMaxWidth()
                .height(currentHeight)
                .shadow(
                    elevation = playerShadowElevation,
                    shape = playerShape,
                    clip = false
                )
                .clickable(
                    enabled = hasCurrentSong && !expanded,
                    interactionSource = noRippleInteractionSource,
                    indication = null
                ) {
                    onExpandedChange(true)
                }
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .height(currentHeight)
                    .clip(playerShape)
                    .then(
                        if (hasArtworkBackground) {
                            Modifier
                        } else {
                            Modifier.background(MaterialTheme.colorScheme.surfaceContainerHigh)
                        }
                    )
            ) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                )
                BlurredArtworkBackground(
                    imageRequest = backgroundImageRequest,
                    alpha = lerpFloat(0.78f, 0f, animationProgress),
                    modifier = Modifier.matchParentSize()
                )
                FlowCloudBackground(
                    colors = cloudColors,
                    progress = animationProgress,
                    modifier = Modifier.matchParentSize()
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(Color.Black.copy(alpha = lerpFloat(0.28f, 0.42f, animationProgress)))
                )
            }
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(currentHeight)
                    .align(Alignment.TopCenter),
                contentAlignment = Alignment.BottomCenter
            ) {
                val playerWidth = maxWidth
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(currentHeight)
                        .align(Alignment.TopCenter)
                ) {
                    MorphArtworkLayer(
                        imageRequest = coverImageRequest,
                        progress = animationProgress,
                        playerWidth = playerWidth,
                        collapsedHeight = collapsedHeight,
                        expandedArtworkSize = expandedArtworkSize,
                        expandedArtworkTop = expandedArtworkTop,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                    )
                    SharedSongInfo(
                        title = title,
                        artist = artist,
                        progress = animationProgress,
                        titleColor = titleColor,
                        artistColor = artistColor,
                        playerWidth = playerWidth,
                        collapsedHeight = collapsedHeight,
                        expandedTop = expandedMetadataTop,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                    )
                    ExpandedOnlyContent(
                        progress = animationProgress,
                        playbackProgress = playbackProgress,
                        durationMs = durationMs,
                        hasCurrentSong = hasCurrentSong,
                        progressTrackColor = progressTrackColor,
                        progressColor = progressColor,
                        onSeekTo = onSeekTo,
                        onScrubbingChange = { scrubbing ->
                            if (scrubbing) {
                                lockedIsPlayingDuringScrub = playbackState.isPlaying
                                keepPlayPauseVisualLockedAfterSeek = true
                            }
                            isProgressScrubbing = scrubbing
                        },
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = expandedProgressTop)
                    )
                    SharedPlaybackControls(
                        progress = animationProgress,
                        isPlaying = visualIsPlaying,
                        iconColor = controlIconColor,
                        screenWidth = playerWidth,
                        collapsedHeight = collapsedHeight,
                        expandedTop = expandedControlsTop,
                        onPlayPrevious = {
                            if (hasCurrentSong) {
                                onPlayPrevious()
                            }
                        },
                        onTogglePlayPause = {
                            if (hasCurrentSong) {
                                isProgressScrubbing = false
                                keepPlayPauseVisualLockedAfterSeek = false
                                onTogglePlayPause()
                            }
                        },
                        onPlayNext = {
                            if (hasCurrentSong) {
                                onPlayNext()
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.TopStart)
                    )
                }
            }
        }
    }
}

@Composable
private fun FlowCloudBackground(
    colors: List<Color>,
    progress: Float,
    modifier: Modifier = Modifier
) {
    val cloudColors = if (colors.size >= 3) {
        colors
    } else {
        listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.tertiaryContainer
        )
    }
    val infiniteTransition = rememberInfiniteTransition(label = "FlowCloudDrift")
    val blob1Drift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4_800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "FlowCloudBlob1Drift"
    )
    val blob2Drift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6_400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "FlowCloudBlob2Drift"
    )
    val blob3Drift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 7_200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "FlowCloudBlob3Drift"
    )
    val blob4Drift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 9_500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "FlowCloudBlob4Drift"
    )

    Canvas(modifier = modifier) {
        val alphaMultiplier = 1f + progress * 0.08f
        val minSide = size.minDimension

        val blob1Radius = minSide * 0.62f
        val blob2Radius = minSide * 0.54f
        val blob3Radius = minSide * 0.48f
        val blob4Radius = minSide * 0.42f
        drawCircle(
            brush = Brush.radialGradient(
                colorStops = arrayOf(
                    0f to cloudColors[0].copy(alpha = 0.82f * alphaMultiplier),
                    0.58f to cloudColors[0].copy(alpha = 0.30f * alphaMultiplier),
                    1f to Color.Transparent
                ),
                center = Offset(
                    x = size.width * (-0.28f + blob1Drift * 0.50f),
                    y = size.height * (-0.06f + blob1Drift * 0.36f)
                ),
                radius = blob1Radius
            ),
            radius = blob1Radius,
            center = Offset(
                x = size.width * (-0.28f + blob1Drift * 0.50f),
                y = size.height * (-0.06f + blob1Drift * 0.36f)
            )
        )
        drawCircle(
            brush = Brush.radialGradient(
                colorStops = arrayOf(
                    0f to cloudColors[1].copy(alpha = 0.68f * alphaMultiplier),
                    0.6f to cloudColors[1].copy(alpha = 0.25f * alphaMultiplier),
                    1f to Color.Transparent
                ),
                center = Offset(
                    x = size.width * (1.24f - blob2Drift * 0.46f),
                    y = size.height * (-0.02f + blob2Drift * 0.28f)
                ),
                radius = blob2Radius
            ),
            radius = blob2Radius,
            center = Offset(
                x = size.width * (1.24f - blob2Drift * 0.46f),
                y = size.height * (-0.02f + blob2Drift * 0.28f)
            )
        )
        drawCircle(
            brush = Brush.radialGradient(
                colorStops = arrayOf(
                    0f to cloudColors[2].copy(alpha = 0.58f * alphaMultiplier),
                    0.62f to cloudColors[2].copy(alpha = 0.22f * alphaMultiplier),
                    1f to Color.Transparent
                ),
                center = Offset(
                    x = size.width * (0.46f - blob3Drift * 0.38f),
                    y = size.height * (1.20f - blob3Drift * 0.40f)
                ),
                radius = blob3Radius
            ),
            radius = blob3Radius,
            center = Offset(
                x = size.width * (0.46f - blob3Drift * 0.38f),
                y = size.height * (1.20f - blob3Drift * 0.40f)
            )
        )
        drawCircle(
            brush = Brush.radialGradient(
                colorStops = arrayOf(
                    0f to cloudColors[1].copy(alpha = 0.50f * alphaMultiplier),
                    0.64f to cloudColors[1].copy(alpha = 0.18f * alphaMultiplier),
                    1f to Color.Transparent
                ),
                center = Offset(
                    x = size.width * (1.08f - blob4Drift * 0.28f),
                    y = size.height * (1.08f - blob4Drift * 0.18f)
                ),
                radius = blob4Radius
            ),
            radius = blob4Radius,
            center = Offset(
                x = size.width * (1.08f - blob4Drift * 0.28f),
                y = size.height * (1.08f - blob4Drift * 0.18f)
            )
        )
    }
}

@Composable
private fun BlurredArtworkBackground(
    imageRequest: ImageRequest?,
    alpha: Float,
    modifier: Modifier = Modifier
) {
    if (imageRequest == null || alpha <= 0.01f) {
        return
    }

    Box(
        modifier = modifier
            .graphicsLayer {
                this.alpha = alpha
                scaleX = 1.22f
                scaleY = 1.22f
            }
            .blur(100.dp)
    ) {
        AsyncImage(
            model = imageRequest,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
private fun MorphArtworkLayer(
    imageRequest: ImageRequest?,
    progress: Float,
    playerWidth: Dp,
    collapsedHeight: Dp,
    expandedArtworkSize: Dp,
    expandedArtworkTop: Dp,
    modifier: Modifier = Modifier
) {
    val collapsedX = 0.dp
    val collapsedY = 0.dp
    val collapsedWidth = playerWidth
    val expandedX = (playerWidth - expandedArtworkSize) / 2f
    val expandedY = expandedArtworkTop
    val artworkX = lerpDp(collapsedX, expandedX, progress)
    val artworkY = lerpDp(collapsedY, expandedY, progress)
    val artworkWidth = lerpDp(collapsedWidth, expandedArtworkSize, progress)
    val artworkHeight = lerpDp(collapsedHeight, expandedArtworkSize, progress)
    val blurRadius = lerpDp(16.dp, 0.dp, progress)
    val cornerRadius = lerpDp(24.dp, 28.dp, progress)
    val shadowPadding = 32.dp
    val shadowProgress = progress.coerceIn(0f, 1f)
    val imageScale = lerpFloat(1.22f, 1f, progress)
    val coverShape = RoundedCornerShape(cornerRadius)
    val blurModifier = if (blurRadius > 0.5.dp) {
        Modifier.blur(blurRadius)
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .offset(x = artworkX - shadowPadding, y = artworkY - shadowPadding)
            .width(artworkWidth + shadowPadding * 2)
            .height(artworkHeight + shadowPadding * 2)
            .graphicsLayer {
                alpha = 1f
            }
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            if (shadowProgress > 0f) {
                val shadowPaddingPx = shadowPadding.toPx()
                val shadowAlpha = 0.20f * shadowProgress
                val shadowBlur = 22.dp.toPx() * shadowProgress
                val shadowOffsetY = 10.dp.toPx() * shadowProgress
                drawIntoCanvas { canvas ->
                    val paint = NativePaint().apply {
                        isAntiAlias = true
                        color = Color.Black.copy(alpha = shadowAlpha).toArgb()
                        maskFilter = BlurMaskFilter(
                            shadowBlur.coerceAtLeast(0.1f),
                            BlurMaskFilter.Blur.NORMAL
                        )
                    }
                    val rect = RectF(
                        shadowPaddingPx,
                        shadowPaddingPx + shadowOffsetY,
                        shadowPaddingPx + artworkWidth.toPx(),
                        shadowPaddingPx + artworkHeight.toPx() + shadowOffsetY
                    )
                    canvas.nativeCanvas.drawRoundRect(
                        rect,
                        cornerRadius.toPx(),
                        cornerRadius.toPx(),
                        paint
                    )
                }
            }
        }
        Box(
            modifier = Modifier
                .offset(x = shadowPadding, y = shadowPadding)
                .width(artworkWidth)
                .height(artworkHeight)
                .graphicsLayer {
                    shape = coverShape
                    clip = true
                }
                .background(
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    shape = coverShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (imageRequest != null) {
                AsyncImage(
                    model = imageRequest,
                    contentDescription = "\u4e13\u8f91\u5c01\u9762",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .matchParentSize()
                        .graphicsLayer {
                            scaleX = imageScale
                            scaleY = imageScale
                            transformOrigin = TransformOrigin.Center
                        }
                        .then(blurModifier)
                )
            } else {
                Icon(
                    imageVector = Icons.Filled.MusicNote,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(42.dp)
                )
            }
        }
    }
}

@Composable
private fun ExpandedOnlyContent(
    progress: Float,
    playbackProgress: Float,
    durationMs: Long,
    hasCurrentSong: Boolean,
    progressTrackColor: Color,
    progressColor: Color,
    onSeekTo: (Long) -> Unit,
    onScrubbingChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val progressEnterProgress = ((progress - 0.18f) / 0.82f).coerceIn(0f, 1f)

    Column(
        modifier = modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        PlaybackProgressBar(
            playbackProgress = playbackProgress,
            durationMs = durationMs,
            enabled = hasCurrentSong && durationMs > 0L,
            trackColor = progressTrackColor,
            progressColor = progressColor,
            onSeekTo = onSeekTo,
            onScrubbingChange = onScrubbingChange,
            enterProgress = progressEnterProgress,
            modifier = Modifier
                .fillMaxWidth(0.76f)
        )
    }
}

@Composable
private fun SharedSongInfo(
    title: String,
    artist: String,
    progress: Float,
    titleColor: Color,
    artistColor: Color,
    playerWidth: Dp,
    collapsedHeight: Dp,
    expandedTop: Dp,
    modifier: Modifier = Modifier
) {
    val metadataGroupHeight = 60.dp
    val collapsedCenterY = collapsedHeight / 2f
    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()
    val titleStyle = MaterialTheme.typography.titleMedium
    val artistStyle = MaterialTheme.typography.bodyMedium
    val collapsedViewportX = 30.dp
    val collapsedControlsReservedWidth = 48.dp * 3f + 8.dp * 2f + 30.dp
    val collapsedViewportWidth = playerWidth - collapsedViewportX - collapsedControlsReservedWidth
    val collapsedViewportY = collapsedCenterY - metadataGroupHeight / 2f
    val expandedViewportWidth = playerWidth * 0.82f
    val expandedViewportX = (playerWidth - expandedViewportWidth) / 2f
    val expandedViewportY = expandedTop
    val viewportX = lerpDp(collapsedViewportX, expandedViewportX, progress)
    val viewportY = lerpDp(collapsedViewportY, expandedViewportY, progress)
    val viewportWidth = lerpDp(collapsedViewportWidth, expandedViewportWidth, progress)
    val titleWidth = with(density) {
        textMeasurer.measure(
            text = AnnotatedString(title),
            style = titleStyle,
            maxLines = 1
        ).size.width.toDp()
    }
    val artistWidth = with(density) {
        textMeasurer.measure(
            text = AnnotatedString(artist),
            style = artistStyle,
            maxLines = 1
        ).size.width.toDp()
    }
    val rawTitleLineWidth = titleWidth + 12.dp
    val titleLineWidth = when {
        rawTitleLineWidth < 48.dp -> 48.dp
        rawTitleLineWidth > expandedViewportWidth -> expandedViewportWidth
        else -> rawTitleLineWidth
    }
    val rawArtistLineWidth = artistWidth + 12.dp
    val artistLineWidth = when {
        rawArtistLineWidth < 48.dp -> 48.dp
        rawArtistLineWidth > expandedViewportWidth -> expandedViewportWidth
        else -> rawArtistLineWidth
    }
    val collapsedTitleX = 0.dp
    val centeredTitleX = (viewportWidth - titleLineWidth) / 2f
    val expandedTitleX = if (centeredTitleX < 0.dp) {
        0.dp
    } else {
        centeredTitleX
    }
    val collapsedArtistX = 0.dp
    val centeredArtistX = (viewportWidth - artistLineWidth) / 2f
    val expandedArtistX = if (centeredArtistX < 0.dp) {
        0.dp
    } else {
        centeredArtistX
    }
    val titleX = lerpDp(collapsedTitleX, expandedTitleX, progress)
    val artistX = lerpDp(collapsedArtistX, expandedArtistX, progress)

    Box(
        modifier = modifier
            .width(viewportWidth)
            .height(metadataGroupHeight)
            .graphicsLayer {
                translationX = viewportX.toPx()
                translationY = viewportY.toPx()
            }
            .clipToBounds()
    ) {
        Column(
            modifier = Modifier
                .width(viewportWidth)
                .height(metadataGroupHeight),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = title,
                style = titleStyle,
                color = titleColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Start,
                modifier = Modifier
                    .width(titleLineWidth)
                    .offset(x = titleX)
            )
            Text(
                text = artist,
                style = artistStyle,
                color = artistColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Start,
                modifier = Modifier
                    .width(artistLineWidth)
                    .offset(x = artistX)
                    .padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun SharedPlaybackControls(
    progress: Float,
    isPlaying: Boolean,
    iconColor: Color,
    screenWidth: Dp,
    collapsedHeight: Dp,
    expandedTop: Dp,
    onPlayPrevious: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onPlayNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    val collapsedTouchSize = 48.dp
    val expandedPreviousNextTouchSize = 64.dp
    val expandedPlayPauseTouchSize = 80.dp
    val collapsedSpacing = 8.dp
    val expandedSpacing = 36.dp
    val previousNextTouchSize = lerpDp(collapsedTouchSize, expandedPreviousNextTouchSize, progress)
    val playPauseTouchSize = lerpDp(collapsedTouchSize, expandedPlayPauseTouchSize, progress)
    val previousNextIconSize = lerpDp(24.dp, 32.dp, progress)
    val playPauseIconSize = lerpDp(28.dp, 42.dp, progress)
    val spacing = lerpDp(collapsedSpacing, expandedSpacing, progress)
    val collapsedControlsWidth = collapsedTouchSize * 3f + collapsedSpacing * 2f
    val expandedControlsWidth = expandedPreviousNextTouchSize * 2f +
        expandedPlayPauseTouchSize +
        expandedSpacing * 2f
    val controlsWidth = previousNextTouchSize * 2f + playPauseTouchSize + spacing * 2f
    val collapsedLeft = screenWidth - collapsedControlsWidth - 30.dp
    val expandedLeft = (screenWidth - expandedControlsWidth) / 2f
    val collapsedControlsY = (collapsedHeight - collapsedTouchSize) / 2f
    val currentLeft = lerpDp(collapsedLeft, expandedLeft, progress)
    val currentTop = lerpDp(collapsedControlsY, expandedTop, progress)

    Row(
        modifier = modifier
            .width(controlsWidth)
            .graphicsLayer {
                translationX = currentLeft.toPx()
                translationY = currentTop.toPx()
            },
        horizontalArrangement = Arrangement.spacedBy(spacing),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TransparentControlButton(
            onClick = onPlayPrevious,
            modifier = Modifier.size(previousNextTouchSize)
        ) {
            Icon(
                imageVector = Icons.Filled.SkipPrevious,
                contentDescription = "\u4e0a\u4e00\u66f2",
                tint = iconColor,
                modifier = Modifier.size(previousNextIconSize)
            )
        }
        TransparentControlButton(
            onClick = onTogglePlayPause,
            modifier = Modifier.size(playPauseTouchSize)
        ) {
            Icon(
                imageVector = if (isPlaying) {
                    Icons.Filled.Pause
                } else {
                    Icons.Filled.PlayArrow
                },
                contentDescription = if (isPlaying) {
                    "\u6682\u505c"
                } else {
                    "\u64ad\u653e"
                },
                tint = iconColor,
                modifier = Modifier.size(playPauseIconSize)
            )
        }
        TransparentControlButton(
            onClick = onPlayNext,
            modifier = Modifier.size(previousNextTouchSize)
        ) {
            Icon(
                imageVector = Icons.Filled.SkipNext,
                contentDescription = "\u4e0b\u4e00\u66f2",
                tint = iconColor,
                modifier = Modifier.size(previousNextIconSize)
            )
        }
    }
}

@Composable
private fun PlaybackProgressBar(
    playbackProgress: Float,
    durationMs: Long,
    enabled: Boolean,
    trackColor: Color,
    progressColor: Color,
    onSeekTo: (Long) -> Unit,
    onScrubbingChange: (Boolean) -> Unit,
    enterProgress: Float,
    modifier: Modifier = Modifier
) {
    var isScrubbing by remember { mutableStateOf(false) }
    var scrubProgress by remember { mutableStateOf(0f) }
    var containerSize by remember { mutableStateOf(IntSize.Zero) }
    val animatedTrackHeight by animateDpAsState(
        targetValue = if (isScrubbing) 16.dp else 8.dp,
        animationSpec = tween(
            durationMillis = 160,
            easing = FastOutSlowInEasing
        ),
        label = "ProgressTrackHeight"
    )
    val visibleProgress = if (isScrubbing) {
        scrubProgress
    } else {
        playbackProgress
    }.coerceIn(0f, 1f)
    val activeProgressColor = progressColor

    fun updateScrubProgress(x: Float) {
        val width = containerSize.width.toFloat().coerceAtLeast(1f)
        scrubProgress = (x / width).coerceIn(0f, 1f)
    }

    val density = LocalDensity.current
    val progressBarTranslationY = with(density) {
        (300.dp * (1f - enterProgress)).toPx()
    }
    val progressBarScale = lerpFloat(2.6f, 1f, enterProgress)
    val progressBarAlpha = lerpFloat(0.18f, 1f, enterProgress)

    Box(
        modifier = modifier
            .height(40.dp)
            .onSizeChanged { size ->
                containerSize = size
            }
            .pointerInput(enabled, durationMs, containerSize) {
                if (!enabled || durationMs <= 0L) {
                    return@pointerInput
                }

                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    try {
                        isScrubbing = true
                        onScrubbingChange(true)
                        updateScrubProgress(down.position.x)
                        down.consume()

                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull { it.id == down.id }
                                ?: event.changes.firstOrNull()
                                ?: continue
                            if (!change.pressed) {
                                break
                            }

                            updateScrubProgress(change.position.x)
                            change.consume()
                        }

                        val targetPositionMs = (durationMs * scrubProgress).toLong()
                        onSeekTo(targetPositionMs)
                    } finally {
                        isScrubbing = false
                        onScrubbingChange(false)
                    }
                }
            }
    ) {
        Canvas(
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer {
                    alpha = progressBarAlpha
                    translationY = progressBarTranslationY
                    scaleX = progressBarScale
                    scaleY = progressBarScale
                    transformOrigin = TransformOrigin.Center
                }
        ) {
            val centerY = size.height / 2f
            val trackHeight = animatedTrackHeight.toPx()
            val trackTop = centerY - trackHeight / 2f
            val trackLeft = 0f
            val trackWidth = size.width
            val cornerRadius = trackHeight / 2f
            val progressWidth = trackWidth * visibleProgress.coerceIn(0f, 1f)
            val shadowOffsetY = 3.dp.toPx()
            val shadowBlurRadius = 8.dp.toPx()
            val shadowAlpha = enterProgress * 0.14f
            drawIntoCanvas { canvas ->
                val paint = NativePaint().apply {
                    isAntiAlias = true
                    color = Color.Black.copy(alpha = shadowAlpha).toArgb()
                    maskFilter = BlurMaskFilter(
                        shadowBlurRadius,
                        BlurMaskFilter.Blur.NORMAL
                    )
                }
                canvas.nativeCanvas.drawRoundRect(
                    RectF(
                        trackLeft,
                        trackTop + shadowOffsetY,
                        trackLeft + trackWidth,
                        trackTop + trackHeight + shadowOffsetY
                    ),
                    cornerRadius,
                    cornerRadius,
                    paint
                )
            }
            drawRoundRect(
                color = trackColor,
                topLeft = Offset(trackLeft, trackTop),
                size = Size(trackWidth, trackHeight),
                cornerRadius = CornerRadius(cornerRadius, cornerRadius)
            )
            if (visibleProgress > 0f) {
                val trackPath = Path().apply {
                    addRoundRect(
                        RoundRect(
                            left = trackLeft,
                            top = trackTop,
                            right = trackLeft + trackWidth,
                            bottom = trackTop + trackHeight,
                            cornerRadius = CornerRadius(cornerRadius, cornerRadius)
                        )
                    )
                }
                clipPath(trackPath) {
                    drawRect(
                        color = activeProgressColor,
                        topLeft = Offset(trackLeft, trackTop),
                        size = Size(progressWidth, trackHeight)
                    )
                }
            }
        }
    }
}

@Composable
private fun TransparentControlButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier.size(52.dp),
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
private fun ExpandedArtwork(
    imageRequest: ImageRequest?,
    progress: Float,
    artworkSize: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(24.dp)
    val translationY = with(LocalDensity.current) {
        (80.dp * (1f - progress)).toPx()
    }
    val scale = 0.82f + (1f - 0.82f) * progress
    val helperAlpha = 0.9f + 0.1f * progress

    Box(
        modifier = modifier
            .size(artworkSize)
            .graphicsLayer {
                alpha = helperAlpha
                scaleX = scale
                scaleY = scale
                this.translationY = translationY
            }
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh),
        contentAlignment = Alignment.Center
    ) {
        if (imageRequest != null) {
            AsyncImage(
                model = imageRequest,
                contentDescription = "\u4e13\u8f91\u5c01\u9762",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Icon(
                imageVector = Icons.Filled.MusicNote,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun lerpFloat(start: Float, end: Float, progress: Float): Float {
    return start + (end - start) * progress.coerceIn(0f, 1f)
}

private fun lerpDp(start: Dp, end: Dp, progress: Float): Dp {
    return start + (end - start) * progress.coerceIn(0f, 1f)
}

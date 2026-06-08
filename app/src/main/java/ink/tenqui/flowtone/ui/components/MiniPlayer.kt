package ink.tenqui.flowtone.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
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
    modifier: Modifier = Modifier
) {
    val currentSong = playbackState.currentSong ?: return
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
    val expandedLyricTop = expandedMetadataTop + 72.dp
    val expandedControlsTop = expandedLyricTop + 62.dp
    val animationProgress by animateFloatAsState(
        targetValue = if (expanded) 1f else 0f,
        animationSpec = tween(
            durationMillis = MINI_PLAYER_ANIMATION_DURATION_MS,
            easing = FastOutSlowInEasing
        ),
        label = "MiniPlayerProgress"
    )
    val currentHeight = collapsedHeight + (expandedHeight - collapsedHeight) * animationProgress
    val hasArtworkBackground = currentSong.artworkUri != null
    val backgroundImageRequest: ImageRequest? = remember(currentSong.artworkUri, context) {
        currentSong.artworkUri?.let { artworkUri ->
            ImageRequest.Builder(context)
                .data(artworkUri)
                .size(256, 256)
                .crossfade(false)
                .build()
        }
    }
    val coverImageRequest: ImageRequest? = remember(currentSong.artworkUri, context) {
        currentSong.artworkUri?.let { artworkUri ->
            ImageRequest.Builder(context)
                .data(artworkUri)
                .size(768, 768)
                .crossfade(false)
                .build()
        }
    }
    val paletteImageRequest: ImageRequest? = remember(currentSong.artworkUri, context) {
        currentSong.artworkUri?.let { artworkUri ->
            ImageRequest.Builder(context)
                .data(artworkUri)
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
    val expandedSecondaryColor = if (hasArtworkBackground) {
        Color.White.copy(alpha = 0.82f)
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    val progressTrackColor = if (hasArtworkBackground) {
        Color.White.copy(alpha = 0.24f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val progressColor = if (hasArtworkBackground) {
        Color.White.copy(alpha = 0.86f)
    } else {
        MaterialTheme.colorScheme.primary
    }
    var accumulatedDragY by remember { mutableStateOf(0f) }
    val gestureModifier = Modifier.pointerInput(expanded, swipeThresholdPx) {
        detectVerticalDragGestures(
            onDragStart = {
                accumulatedDragY = 0f
            },
            onVerticalDrag = { _, dragAmount ->
                accumulatedDragY += dragAmount
            },
            onDragEnd = {
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
            .then(gestureModifier)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(dragHotZoneHeight)
                .align(Alignment.TopCenter)
                .clickable(
                    enabled = !expanded,
                    interactionSource = noRippleInteractionSource,
                    indication = null
                ) {
                    onExpandedChange(true)
                }
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(currentHeight)
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .then(
                    if (hasArtworkBackground) {
                        Modifier
                    } else {
                        Modifier.background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    }
                )
                .clickable(
                    enabled = !expanded,
                    interactionSource = noRippleInteractionSource,
                    indication = null
                ) {
                    onExpandedChange(true)
                }
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(currentHeight),
                contentAlignment = Alignment.BottomCenter
            ) {
                val playerWidth = maxWidth
                FlowCloudBackground(
                    colors = cloudColors,
                    progress = animationProgress,
                    modifier = Modifier.matchParentSize()
                )
                HiddenBlurArtworkBridge(
                    imageRequest = backgroundImageRequest,
                    progress = animationProgress,
                    modifier = Modifier.matchParentSize()
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(Color.Black.copy(alpha = 0.42f))
                )
                MorphArtworkLayer(
                    imageRequest = coverImageRequest,
                    progress = animationProgress,
                    playerWidth = playerWidth,
                    currentHeight = currentHeight,
                    collapsedHeight = collapsedHeight,
                    expandedArtworkSize = expandedArtworkSize,
                    expandedArtworkTop = expandedArtworkTop,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                )
                SharedSongInfo(
                    title = currentSong.title,
                    artist = currentSong.artist,
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
                    lyricColor = expandedSecondaryColor,
                    progressTrackColor = progressTrackColor,
                    progressColor = progressColor,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = expandedLyricTop)
                )
                SharedPlaybackControls(
                    progress = animationProgress,
                    isPlaying = playbackState.isPlaying,
                    iconColor = controlIconColor,
                    screenWidth = playerWidth,
                    collapsedHeight = collapsedHeight,
                    expandedTop = expandedControlsTop,
                    onPlayPrevious = onPlayPrevious,
                    onTogglePlayPause = onTogglePlayPause,
                    onPlayNext = onPlayNext,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                )
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
    val slowDrift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 12_000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "FlowCloudSlowDrift"
    )
    val softDrift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 9_000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "FlowCloudSoftDrift"
    )

    Canvas(modifier = modifier.background(cloudColors[0].copy(alpha = 0.62f))) {
        val largestSide = size.width.coerceAtLeast(size.height)
        val expandedBreath = progress * largestSide * 0.04f
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(cloudColors[0].copy(alpha = 0.72f), Color.Transparent),
                center = Offset(
                    x = size.width * (0.18f + slowDrift * 0.08f),
                    y = size.height * (0.25f + progress * 0.08f)
                ),
                radius = largestSide * 0.62f + expandedBreath
            ),
            radius = largestSide * 0.62f + expandedBreath,
            center = Offset(
                x = size.width * (0.18f + slowDrift * 0.08f),
                y = size.height * (0.25f + progress * 0.08f)
            )
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(cloudColors[1].copy(alpha = 0.58f), Color.Transparent),
                center = Offset(
                    x = size.width * (0.82f - softDrift * 0.1f),
                    y = size.height * (0.18f + slowDrift * 0.1f)
                ),
                radius = largestSide * 0.58f
            ),
            radius = largestSide * 0.58f,
            center = Offset(
                x = size.width * (0.82f - softDrift * 0.1f),
                y = size.height * (0.18f + slowDrift * 0.1f)
            )
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(cloudColors[2].copy(alpha = 0.66f), Color.Transparent),
                center = Offset(
                    x = size.width * (0.54f + softDrift * 0.06f),
                    y = size.height * (0.86f - progress * 0.12f)
                ),
                radius = largestSide * 0.68f
            ),
            radius = largestSide * 0.68f,
            center = Offset(
                x = size.width * (0.54f + softDrift * 0.06f),
                y = size.height * (0.86f - progress * 0.12f)
            )
        )
    }
}

@Composable
private fun HiddenBlurArtworkBridge(
    imageRequest: ImageRequest?,
    progress: Float,
    modifier: Modifier = Modifier
) {
    if (imageRequest == null) {
        return
    }

    val bridgeAlpha = lerpFloat(0f, 0.32f, progress)
    val bridgeBlur = lerpDp(40.dp, 0.dp, progress)
    val blurModifier = if (bridgeBlur > 0.5.dp) {
        Modifier.blur(bridgeBlur)
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .graphicsLayer {
                alpha = bridgeAlpha
            }
            .then(blurModifier)
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
    currentHeight: Dp,
    collapsedHeight: Dp,
    expandedArtworkSize: Dp,
    expandedArtworkTop: Dp,
    modifier: Modifier = Modifier
) {
    val collapsedX = 0.dp
    val collapsedY = currentHeight - collapsedHeight
    val collapsedWidth = playerWidth
    val collapsedHeightForArtwork = collapsedHeight
    val expandedX = (playerWidth - expandedArtworkSize) / 2f
    val expandedY = expandedArtworkTop
    val artworkX = lerpDp(collapsedX, expandedX, progress)
    val artworkY = lerpDp(collapsedY, expandedY, progress)
    val artworkWidth = lerpDp(collapsedWidth, expandedArtworkSize, progress)
    val artworkHeight = lerpDp(collapsedHeightForArtwork, expandedArtworkSize, progress)
    val blurRadius = lerpDp(16.dp, 0.dp, progress)
    val cornerRadius = lerpDp(24.dp, 28.dp, progress)
    val layerAlpha = lerpFloat(0.85f, 1f, progress)
    val shape = RoundedCornerShape(cornerRadius)
    val blurModifier = if (blurRadius > 0.5.dp) {
        Modifier.blur(blurRadius)
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .offset(x = artworkX, y = artworkY)
            .width(artworkWidth)
            .height(artworkHeight)
            .graphicsLayer {
                alpha = layerAlpha
            }
            .clip(shape)
            .then(blurModifier)
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
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(42.dp)
            )
        }
    }
}

@Composable
private fun ExpandedOnlyContent(
    progress: Float,
    lyricColor: Color,
    progressTrackColor: Color,
    progressColor: Color,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    fun offsetPx(dp: androidx.compose.ui.unit.Dp): Float = with(density) {
        (dp * (1f - progress)).toPx()
    }
    val helperAlpha = 0.85f + 0.15f * progress

    Column(
        modifier = modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "\u266a \u6682\u65e0\u6b4c\u8bcd",
            style = MaterialTheme.typography.bodyMedium,
            color = lyricColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .padding(top = 14.dp)
                .graphicsLayer {
                    alpha = helperAlpha
                    translationY = offsetPx(40.dp)
                }
        )
        FakeProgressBar(
            progress = 0.35f,
            trackColor = progressTrackColor,
            progressColor = progressColor,
            modifier = Modifier
                .padding(top = 18.dp)
                .fillMaxWidth(0.76f)
                .graphicsLayer {
                    alpha = helperAlpha
                scaleX = 0.92f + (1f - 0.92f) * progress
                translationY = offsetPx(36.dp)
            }
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
private fun FakeProgressBar(
    progress: Float,
    trackColor: Color,
    progressColor: Color,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(50)

    Box(
        modifier = modifier
            .height(4.dp)
            .clip(shape)
            .background(trackColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .height(4.dp)
                .clip(shape)
                .background(progressColor)
        )
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

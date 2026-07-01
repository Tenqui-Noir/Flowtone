package ink.tenqui.flowtone.ui.components

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private const val FlowtoneAboutCardSharedKey = "flowtone-about-card"
private val FlowtoneAboutCardShape = RoundedCornerShape(24.dp)

@Composable
internal fun rememberFlowtoneVersionName(): String {
    val context = LocalContext.current
    return remember(context) {
        context.packageManager
            .getPackageInfo(context.packageName, 0)
            .versionName
            .orEmpty()
            .ifBlank { "x.x.x" }
    }
}

@Composable
internal fun FlowtoneAboutCard(
    versionName: String,
    description: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
    bottomContent: (@Composable () -> Unit)? = null
) {
    val clickableModifier = if (onClick != null) {
        Modifier.clickable(onClick = onClick)
    } else {
        Modifier
    }
    val descriptionMotionModifier = animatedVisibilityScope?.run {
        staggeredPageElementModifier(animationIndex = 1)
    } ?: Modifier
    val bottomMotionModifier = animatedVisibilityScope?.run {
        staggeredPageElementModifier(animationIndex = 2)
    } ?: Modifier

    Column(
        modifier = modifier
            .clip(FlowtoneAboutCardShape)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .then(clickableModifier)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            )
            Column(
                modifier = Modifier.padding(start = 16.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = "声流 / Flowtone",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "v$versionName",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = descriptionMotionModifier
                )
            }
        }

        if (bottomContent != null) {
            Spacer(modifier = Modifier.weight(1f))
            Box(modifier = bottomMotionModifier) {
                bottomContent()
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun Modifier.flowtoneAboutCardSharedBounds(
    sharedTransitionScope: SharedTransitionScope?,
    animatedVisibilityScope: AnimatedVisibilityScope?
): Modifier {
    if (sharedTransitionScope == null || animatedVisibilityScope == null) {
        return this
    }

    return with(sharedTransitionScope) {
        val sharedState = rememberSharedContentState(FlowtoneAboutCardSharedKey)
        this@flowtoneAboutCardSharedBounds.sharedBounds(
            sharedContentState = sharedState,
            animatedVisibilityScope = animatedVisibilityScope,
            enter = EnterTransition.None,
            exit = ExitTransition.None,
            clipInOverlayDuringTransition = OverlayClip(FlowtoneAboutCardShape)
        )
    }
}

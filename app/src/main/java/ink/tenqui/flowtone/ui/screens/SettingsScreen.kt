package ink.tenqui.flowtone.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ink.tenqui.flowtone.app.AppPreferences
import ink.tenqui.flowtone.app.FlowtonePageEasing
import ink.tenqui.flowtone.app.TopLevelPage
import ink.tenqui.flowtone.ui.components.OptionGroup
import ink.tenqui.flowtone.ui.components.ThemeModeSelector
import ink.tenqui.flowtone.ui.components.rightSwipeBackGesture
import ink.tenqui.flowtone.ui.components.staggeredPageElementModifier
import ink.tenqui.flowtone.ui.theme.AppThemeMode
import kotlin.math.roundToInt

private enum class SettingsSection(val title: String) {
    Appearance("外观"),
    Playback("播放"),
    Advanced("高级"),
    General("通用")
}

@Composable
internal fun SettingsScreen(
    appPreferences: AppPreferences,
    themeMode: AppThemeMode,
    onThemeModeChange: (AppThemeMode) -> Unit,
    onBack: () -> Unit,
    onBackActionChange: ((() -> Unit)?) -> Unit,
    onPathSegmentsChange: (List<String>) -> Unit,
    hideSecondaryBackButton: Boolean,
    onHideSecondaryBackButtonChange: (Boolean) -> Unit,
    resumePlaybackAfterCall: Boolean,
    onResumePlaybackAfterCallChange: (Boolean) -> Unit,
    allowFullscreenFromCollapsed: Boolean,
    onAllowFullscreenFromCollapsedChange: (Boolean) -> Unit,
    preloadSongMetadataCount: Int,
    onPreloadSongMetadataCountChange: (Int) -> Unit,
    elementModifier: (Int) -> Modifier,
    modifier: Modifier = Modifier
) {
    var selectedSection by rememberSaveable {
        mutableStateOf<SettingsSection?>(null)
    }
    var selectedStartPage by rememberSaveable {
        mutableStateOf(appPreferences.getDefaultStartPage())
    }
    val currentOnBack by rememberUpdatedState(onBack)
    val currentOnBackActionChange by rememberUpdatedState(onBackActionChange)
    val currentOnPathSegmentsChange by rememberUpdatedState(onPathSegmentsChange)
    val handleBack = remember(selectedSection) {
        {
            if (selectedSection == null) {
                currentOnBack()
            } else {
                selectedSection = null
            }
        }
    }

    DisposableEffect(handleBack) {
        currentOnBackActionChange(handleBack)
        onDispose { currentOnBackActionChange(null) }
    }
    SideEffect {
        currentOnPathSegmentsChange(selectedSection?.let { listOf(it.title) } ?: emptyList())
    }
    DisposableEffect(Unit) {
        onDispose { currentOnPathSegmentsChange(emptyList()) }
    }
    BackHandler(onBack = handleBack)

    AnimatedContent(
        targetState = selectedSection,
        transitionSpec = { EnterTransition.None togetherWith ExitTransition.None },
        label = "SettingsContent",
        modifier = modifier
            .fillMaxSize()
            .rightSwipeBackGesture(handleBack)
    ) { section ->
        fun viewElementModifier(index: Int): Modifier {
            return elementModifier(index).then(staggeredPageElementModifier(index))
        }

        when (section) {
            null -> SettingsSectionList(
                onSectionClick = { selectedSection = it },
                elementModifier = ::viewElementModifier
            )

            SettingsSection.Appearance -> AppearanceSettingsPage(
                themeMode = themeMode,
                onThemeModeChange = onThemeModeChange,
                elementModifier = ::viewElementModifier
            )

            SettingsSection.Playback -> PlaybackSettingsPage(
                resumePlaybackAfterCall = resumePlaybackAfterCall,
                onResumePlaybackAfterCallChange = onResumePlaybackAfterCallChange,
                elementModifier = ::viewElementModifier
            )

            SettingsSection.Advanced -> AdvancedSettingsPage(
                preloadSongMetadataCount = preloadSongMetadataCount,
                onPreloadSongMetadataCountChange = onPreloadSongMetadataCountChange,
                elementModifier = ::viewElementModifier
            )

            SettingsSection.General -> GeneralSettingsPage(
                selectedStartPage = selectedStartPage,
                onStartPageSelected = { page ->
                    selectedStartPage = page
                    appPreferences.setDefaultStartPage(page)
                },
                hideSecondaryBackButton = hideSecondaryBackButton,
                onHideSecondaryBackButtonChange = onHideSecondaryBackButtonChange,
                allowFullscreenFromCollapsed = allowFullscreenFromCollapsed,
                onAllowFullscreenFromCollapsedChange = onAllowFullscreenFromCollapsedChange,
                elementModifier = ::viewElementModifier
            )
        }
    }
}

@Composable
private fun SettingsSectionList(
    onSectionClick: (SettingsSection) -> Unit,
    elementModifier: (Int) -> Modifier,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        SettingsSectionRow(
            title = "外观",
            subtitle = "主题模式",
            icon = Icons.Rounded.Palette,
            onClick = { onSectionClick(SettingsSection.Appearance) },
            modifier = elementModifier(0)
        )
        SettingsSectionRow(
            title = "播放",
            subtitle = "播放恢复行为",
            icon = Icons.Rounded.PlayCircle,
            onClick = { onSectionClick(SettingsSection.Playback) },
            modifier = elementModifier(1).padding(top = 12.dp)
        )
        SettingsSectionRow(
            title = "高级",
            subtitle = "预载与性能",
            icon = Icons.Rounded.Tune,
            onClick = { onSectionClick(SettingsSection.Advanced) },
            modifier = elementModifier(2).padding(top = 12.dp)
        )
        SettingsSectionRow(
            title = "通用",
            subtitle = "启动与界面行为",
            icon = Icons.Rounded.Settings,
            onClick = { onSectionClick(SettingsSection.General) },
            modifier = elementModifier(3).padding(top = 12.dp)
        )
    }
}

@Composable
private fun AppearanceSettingsPage(
    themeMode: AppThemeMode,
    onThemeModeChange: (AppThemeMode) -> Unit,
    elementModifier: (Int) -> Modifier,
    modifier: Modifier = Modifier
) {
    SettingsPageColumn(modifier = modifier) {
        OptionGroup(
            title = "外观",
            modifier = elementModifier(0)
        ) {
            ThemeModeSelector(
                selectedMode = themeMode,
                onModeSelected = onThemeModeChange
            )
        }
    }
}

@Composable
private fun PlaybackSettingsPage(
    resumePlaybackAfterCall: Boolean,
    onResumePlaybackAfterCallChange: (Boolean) -> Unit,
    elementModifier: (Int) -> Modifier,
    modifier: Modifier = Modifier
) {
    SettingsPageColumn(modifier = modifier) {
        OptionGroup(
            title = "播放",
            modifier = elementModifier(0)
        ) {
            SettingSwitchRow(
                title = "来电后恢复播放",
                subtitle = "仅在来电前正在播放且音频焦点短暂丢失时恢复",
                checked = resumePlaybackAfterCall,
                onCheckedChange = onResumePlaybackAfterCallChange
            )
        }
    }
}

@Composable
private fun AdvancedSettingsPage(
    preloadSongMetadataCount: Int,
    onPreloadSongMetadataCountChange: (Int) -> Unit,
    elementModifier: (Int) -> Modifier,
    modifier: Modifier = Modifier
) {
    SettingsPageColumn(modifier = modifier) {
        OptionGroup(
            title = "高级",
            modifier = elementModifier(0)
        ) {
            PreloadStrengthRow(
                selectedCount = preloadSongMetadataCount,
                onSelectedCountChange = onPreloadSongMetadataCountChange
            )
        }
    }
}

@Composable
private fun GeneralSettingsPage(
    selectedStartPage: TopLevelPage,
    onStartPageSelected: (TopLevelPage) -> Unit,
    hideSecondaryBackButton: Boolean,
    onHideSecondaryBackButtonChange: (Boolean) -> Unit,
    allowFullscreenFromCollapsed: Boolean,
    onAllowFullscreenFromCollapsedChange: (Boolean) -> Unit,
    elementModifier: (Int) -> Modifier,
    modifier: Modifier = Modifier
) {
    SettingsPageColumn(modifier = modifier) {
        OptionGroup(
            title = "通用",
            modifier = elementModifier(0)
        ) {
            DefaultStartPageRow(
                selectedPage = selectedStartPage,
                onPageSelected = onStartPageSelected
            )
            SettingSwitchRow(
                title = "关闭子菜单返回按钮",
                subtitle = "右滑屏幕即可返回上一级",
                checked = hideSecondaryBackButton,
                onCheckedChange = onHideSecondaryBackButtonChange,
                modifier = Modifier.padding(top = 12.dp)
            )
            SettingSwitchRow(
                title = "正常态上滑直达全屏",
                subtitle = "关闭后需要先展开 MiniPlayer，再上滑进入全屏",
                checked = allowFullscreenFromCollapsed,
                onCheckedChange = onAllowFullscreenFromCollapsedChange,
                modifier = Modifier.padding(top = 12.dp)
            )
        }
    }
}

@Composable
private fun SettingsPageColumn(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        content()
    }
}

@Composable
private fun SettingsSectionRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
        Icon(
            imageVector = Icons.Rounded.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun DefaultStartPageRow(
    selectedPage: TopLevelPage,
    onPageSelected: (TopLevelPage) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by rememberSaveable {
        mutableStateOf(false)
    }
    val expandIconRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(280, easing = FlowtonePageEasing),
        label = "StartPageExpandIconRotation"
    )
    val pages = listOf(
        TopLevelPage.Home,
        TopLevelPage.Library,
        TopLevelPage.Mine
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "启动时默认进入",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "下次打开应用时生效",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            Icon(
                imageVector = Icons.Rounded.ExpandMore,
                contentDescription = if (expanded) "收起" else "展开",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.graphicsLayer { rotationZ = expandIconRotation }
            )
        }
        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(
                animationSpec = tween(300, easing = FlowtonePageEasing)
            ) + fadeIn(tween(220, easing = FlowtonePageEasing)),
            exit = shrinkVertically(
                animationSpec = tween(300, easing = FlowtonePageEasing)
            ) + fadeOut(tween(180, easing = FlowtonePageEasing))
        ) {
            Column(modifier = Modifier.padding(start = 8.dp, end = 8.dp, bottom = 8.dp)) {
                pages.forEach { page ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onPageSelected(page) }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedPage == page,
                            onClick = { onPageSelected(page) }
                        )
                        Text(
                            text = page.label,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PreloadStrengthRow(
    selectedCount: Int,
    onSelectedCountChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val options = listOf(1, 3, 5, 7, 10)
    val selectedIndex = options.indexOf(selectedCount).takeIf { it != -1 } ?: 2
    var expanded by rememberSaveable {
        mutableStateOf(false)
    }
    val expandIconRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(280, easing = FlowtonePageEasing),
        label = "PreloadStrengthExpandIconRotation"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "预载歌曲元信息强度",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "当前：$selectedCount 首",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            Icon(
                imageVector = Icons.Rounded.ExpandMore,
                contentDescription = if (expanded) "收起" else "展开",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.graphicsLayer { rotationZ = expandIconRotation }
            )
        }

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(
                animationSpec = tween(300, easing = FlowtonePageEasing)
            ) + fadeIn(tween(220, easing = FlowtonePageEasing)),
            exit = shrinkVertically(
                animationSpec = tween(300, easing = FlowtonePageEasing)
            ) + fadeOut(tween(180, easing = FlowtonePageEasing))
        ) {
            Column(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 14.dp)
            ) {
                Text(
                    text = "提前加载接下来歌曲的封面与元信息，减少切歌时的封面闪烁。强度越高，占用的内存与后台加载越多。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Slider(
                    value = selectedIndex.toFloat(),
                    onValueChange = { value ->
                        val index = value
                            .roundToInt()
                            .coerceIn(options.indices)
                        onSelectedCountChange(options[index])
                    },
                    valueRange = 0f..(options.size - 1).toFloat(),
                    steps = options.size - 2,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    options.forEach { count ->
                        Text(
                            text = count.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (count == selectedCount) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "低",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "中",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "高",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .clickable {
                onCheckedChange(!checked)
            }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

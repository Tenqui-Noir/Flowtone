package ink.tenqui.flowtone.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ink.tenqui.flowtone.ui.components.OptionGroup
import ink.tenqui.flowtone.ui.components.rightSwipeBackGesture
import ink.tenqui.flowtone.ui.components.staggeredPageElementModifier

private const val APACHE_LICENSE_URL = "https://www.apache.org/licenses/LICENSE-2.0"
private const val GPL_LICENSE_URL = "https://opensource.org/license/gpl-3-0"

private enum class LicenseType(
    val displayName: String,
    val summary: String,
    val fullTextUrl: String
) {
    Apache20(
        displayName = "Apache License 2.0",
        summary = "允许使用、修改和分发软件，并保留版权与许可证声明。该许可证同时提供明确的专利授权。",
        fullTextUrl = APACHE_LICENSE_URL
    ),
    Gpl30(
        displayName = "GNU General Public License v3.0",
        summary = "允许使用、研究、修改和分发软件；分发修改版本时，需要继续以 GPLv3 提供对应源代码。",
        fullTextUrl = GPL_LICENSE_URL
    )
}

private data class OpenSourceComponent(
    val id: String,
    val name: String,
    val description: String,
    val projectUrl: String,
    val category: ComponentCategory,
    val licenseType: LicenseType
)

private enum class ComponentCategory(val title: String) {
    Kotlin("Kotlin 组件"),
    Android("Android 原生组件"),
    Media("媒体播放组件"),
    Flowtone("Flowtone 项目")
}

private val openSourceComponents = listOf(
    OpenSourceComponent(
        id = "kotlin",
        name = "Kotlin",
        description = "Flowtone 使用的主要编程语言。",
        projectUrl = "https://kotlinlang.org/",
        category = ComponentCategory.Kotlin,
        licenseType = LicenseType.Apache20
    ),
    OpenSourceComponent(
        id = "compose",
        name = "Jetpack Compose",
        description = "用于构建 Flowtone 的声明式用户界面。",
        projectUrl = "https://developer.android.com/compose",
        category = ComponentCategory.Android,
        licenseType = LicenseType.Apache20
    ),
    OpenSourceComponent(
        id = "material3",
        name = "Material Design 3",
        description = "提供界面组件、主题和 Material 设计规范。",
        projectUrl = "https://m3.material.io/",
        category = ComponentCategory.Android,
        licenseType = LicenseType.Apache20
    ),
    OpenSourceComponent(
        id = "androidx-core",
        name = "AndroidX Core",
        description = "提供兼容性 API 与 Android 平台基础扩展。",
        projectUrl = "https://developer.android.com/jetpack/androidx/releases/core",
        category = ComponentCategory.Android,
        licenseType = LicenseType.Apache20
    ),
    OpenSourceComponent(
        id = "androidx-lifecycle",
        name = "AndroidX Lifecycle",
        description = "管理界面生命周期、ViewModel 与可观察状态。",
        projectUrl = "https://developer.android.com/jetpack/androidx/releases/lifecycle",
        category = ComponentCategory.Android,
        licenseType = LicenseType.Apache20
    ),
    OpenSourceComponent(
        id = "media3-exoplayer",
        name = "AndroidX Media3 ExoPlayer",
        description = "负责本地音频的解码与播放。",
        projectUrl = "https://developer.android.com/media/media3/exoplayer",
        category = ComponentCategory.Media,
        licenseType = LicenseType.Apache20
    ),
    OpenSourceComponent(
        id = "media3-session",
        name = "AndroidX Media3 Session",
        description = "连接播放器、系统媒体控件与后台播放服务。",
        projectUrl = "https://developer.android.com/media/media3/session",
        category = ComponentCategory.Media,
        licenseType = LicenseType.Apache20
    ),
    OpenSourceComponent(
        id = "coil",
        name = "Coil",
        description = "加载并显示歌曲与专辑封面。",
        projectUrl = "https://github.com/coil-kt/coil",
        category = ComponentCategory.Kotlin,
        licenseType = LicenseType.Apache20
    ),
    OpenSourceComponent(
        id = "androidx-palette",
        name = "AndroidX Palette",
        description = "从封面图像中提取适合界面使用的颜色。",
        projectUrl = "https://developer.android.com/jetpack/androidx/releases/palette",
        category = ComponentCategory.Android,
        licenseType = LicenseType.Apache20
    ),
    OpenSourceComponent(
        id = "material-components",
        name = "Material Components for Android",
        description = "提供 Android Material 组件及相关资源。",
        projectUrl = "https://github.com/material-components/material-components-android",
        category = ComponentCategory.Android,
        licenseType = LicenseType.Apache20
    ),
    OpenSourceComponent(
        id = "flowtone",
        name = "Flowtone",
        description = "本应用自身的源代码与发行项目。",
        projectUrl = "https://github.com/FlowtoneApp/Flowtone",
        category = ComponentCategory.Flowtone,
        licenseType = LicenseType.Gpl30
    )
)

@Composable
fun OpenSourceScreen(
    onBack: () -> Unit,
    onBackActionChange: ((() -> Unit)?) -> Unit,
    onPathSegmentsChange: (List<String>) -> Unit,
    elementModifier: (Int) -> Modifier,
    modifier: Modifier = Modifier
) {
    var selectedComponentId by rememberSaveable { mutableStateOf<String?>(null) }
    var showingLicense by rememberSaveable { mutableStateOf(false) }
    val selectedComponent = openSourceComponents.firstOrNull {
        it.id == selectedComponentId
    }
    var retainedComponent by remember { mutableStateOf<OpenSourceComponent?>(null) }
    SideEffect {
        if (selectedComponent != null) {
            retainedComponent = selectedComponent
        }
    }
    val displayedComponent = selectedComponent ?: retainedComponent
    val pathSegments = when {
        showingLicense && selectedComponent != null -> listOf(
            selectedComponent.name,
            selectedComponent.licenseType.displayName
        )
        selectedComponent != null -> listOf(selectedComponent.name)
        else -> emptyList()
    }
    val currentOnBack by rememberUpdatedState(onBack)
    val currentOnBackActionChange by rememberUpdatedState(onBackActionChange)
    val currentOnPathSegmentsChange by rememberUpdatedState(onPathSegmentsChange)
    val handleBack = remember(selectedComponentId, showingLicense) {
        {
            when {
                showingLicense -> showingLicense = false
                selectedComponentId != null -> selectedComponentId = null
                else -> currentOnBack()
            }
        }
    }

    DisposableEffect(handleBack) {
        currentOnBackActionChange(handleBack)
        onDispose { currentOnBackActionChange(null) }
    }
    SideEffect {
        currentOnPathSegmentsChange(pathSegments)
    }
    DisposableEffect(Unit) {
        onDispose { currentOnPathSegmentsChange(emptyList()) }
    }
    BackHandler(onBack = handleBack)

    AnimatedContent(
        targetState = when {
            showingLicense && selectedComponent != null -> OpenSourceView.License
            selectedComponent != null -> OpenSourceView.Detail
            else -> OpenSourceView.List
        },
        transitionSpec = { EnterTransition.None togetherWith ExitTransition.None },
        label = "OpenSourceContent",
        modifier = modifier
            .fillMaxSize()
            .rightSwipeBackGesture(handleBack)
    ) { view ->
        fun viewElementModifier(index: Int): Modifier {
            return elementModifier(index).then(staggeredPageElementModifier(index))
        }

        when (view) {
            OpenSourceView.List -> ComponentList(
                onComponentClick = { selectedComponentId = it.id },
                elementModifier = ::viewElementModifier
            )

            OpenSourceView.Detail -> displayedComponent?.let { component ->
                ComponentDetail(
                    component = component,
                    onShowLicense = { showingLicense = true },
                    elementModifier = ::viewElementModifier
                )
            }

            OpenSourceView.License -> displayedComponent?.let { component ->
                LicenseDescription(
                    licenseType = component.licenseType,
                    elementModifier = ::viewElementModifier
                )
            }
        }
    }
}

private enum class OpenSourceView {
    List,
    Detail,
    License
}

@Composable
private fun ComponentList(
    onComponentClick: (OpenSourceComponent) -> Unit,
    elementModifier: (Int) -> Modifier
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Text(
            text = "Flowtone 使用了以下开源项目。感谢这些项目的开发者。",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = elementModifier(0)
        )
        ComponentCategory.entries.forEachIndexed { categoryIndex, category ->
            val components = openSourceComponents.filter { it.category == category }
            OptionGroup(
                title = category.title,
                modifier = elementModifier(categoryIndex + 1).padding(top = 24.dp)
            ) {
                components.forEachIndexed { componentIndex, component ->
                    ComponentCard(
                        component = component,
                        onClick = { onComponentClick(component) },
                        modifier = Modifier.padding(
                            top = if (componentIndex == 0) 0.dp else 10.dp
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun ComponentCard(
    component: OpenSourceComponent,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = component.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = component.licenseType.displayName,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = component.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            imageVector = Icons.Rounded.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 12.dp)
        )
    }
}

@Composable
private fun ComponentDetail(
    component: OpenSourceComponent,
    onShowLicense: () -> Unit,
    elementModifier: (Int) -> Modifier
) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        OptionGroup(title = "组件信息", modifier = elementModifier(0)) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                DetailField(label = "组件名", value = component.name)
                DetailField(label = "许可证", value = component.licenseType.displayName)
                DetailField(label = "用途", value = component.description)
                DetailField(label = "项目地址", value = component.projectUrl)
            }
        }
        OptionGroup(
            title = "相关链接",
            modifier = elementModifier(1).padding(top = 24.dp)
        ) {
            ActionCard(
                text = "打开项目页面",
                icon = {
                    Icon(Icons.AutoMirrored.Rounded.OpenInNew, contentDescription = null)
                },
                onClick = { context.openExternalUrl(component.projectUrl) }
            )
            ActionCard(
                text = "查看许可证全文",
                icon = { Icon(Icons.Rounded.Description, contentDescription = null) },
                onClick = onShowLicense,
                modifier = Modifier.padding(top = 10.dp)
            )
        }
    }
}

@Composable
private fun DetailField(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun ActionCard(
    text: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        icon()
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(start = 12.dp)
        )
        Spacer(modifier = Modifier.weight(1f))
        Icon(
            imageVector = Icons.Rounded.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun LicenseDescription(
    licenseType: LicenseType,
    elementModifier: (Int) -> Modifier
) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        OptionGroup(title = "许可证说明", modifier = elementModifier(0)) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = licenseType.displayName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = licenseType.summary,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "此处提供许可证摘要，完整英文文本以许可证发布方页面为准。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        ActionCard(
            text = "打开完整许可证页面",
            icon = {
                Icon(Icons.AutoMirrored.Rounded.OpenInNew, contentDescription = null)
            },
            onClick = { context.openExternalUrl(licenseType.fullTextUrl) },
            modifier = elementModifier(1).padding(top = 24.dp)
        )
    }
}

private fun Context.openExternalUrl(url: String) {
    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
}

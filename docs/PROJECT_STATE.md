# Flowtone 项目状态

## 项目目标

Flowtone 是一个 Android 本地音乐播放器。当前阶段是 Flowtone 0.3 internal：在本地播放和基础播放队列之上，完成第一轮 Material 3 风格 UI 整理。

## 当前技术栈

- Kotlin
- Jetpack Compose
- Material Design 3
- MVVM
- AndroidX Lifecycle ViewModel
- AndroidX Media3 ExoPlayer
- Gradle Version Catalog

## 已完成基础能力

- `MainActivity` 只负责启动 Compose，并调用 `FlowtoneApp`。
- 已声明并请求音频读取权限。
- 已使用 `ContentResolver + MediaStore.Audio.Media` 扫描本地音乐。
- 已显示本地歌曲列表。
- 已使用 `PlaybackController` 封装 Media3 ExoPlayer。
- `ExoPlayer` 只在 `PlaybackController` 中创建，不在 Composable 中创建。
- 已将播放控制和播放状态接入 `MusicViewModel`。
- 点击歌曲可以播放本地音频。
- 底部 MiniPlayer 显示当前歌曲标题、艺术家，并支持播放 / 暂停。

## Flowtone 0.2 Internal 已完成内容

- 已建立基础播放队列：`playbackQueue`。
- 点击歌曲后会记录当前队列索引：`currentQueueIndex`。
- 已实现下一曲：播放队列中的下一首。
- 已实现上一曲：播放队列中的上一首。
- 当前歌曲自然结束后会自动播放下一首。
- 最后一首播放结束后停止，不循环。
- 队列为空或索引非法时不会崩溃。
- 队列逻辑保留在 `MusicViewModel`，不写进 Composable。

## Flowtone 0.3 Internal 已完成内容

- 已整理 UI 状态传递：列表页只接收当前播放歌曲用于高亮，MiniPlayer 使用完整播放状态。
- 歌曲列表项已调整为 Material 3 风格。
- 当前播放歌曲在列表中有更清晰的高亮状态。
- MiniPlayer 已完成视觉优化，保留贴底播放器结构。
- 已修正 MiniPlayer 外层黑色直角底色和重复容器问题。
- 空状态 / 无权限状态文案已优化。
- 主页面背景、顶栏、列表区域和 MiniPlayer 的视觉层次已统一。

## 当前文件结构概览

```text
app/src/main/java/ink/tenqui/flowtone/
|-- MainActivity.kt
|-- data/
|   `-- AudioScanner.kt
|-- model/
|   `-- Song.kt
|-- permissions/
|   `-- AudioPermission.kt
|-- playback/
|   |-- PlaybackController.kt
|   `-- PlaybackState.kt
|-- ui/
|   |-- FlowtoneApp.kt
|   |-- components/
|   |   |-- MiniPlayer.kt
|   |   `-- SongListItem.kt
|   |-- screens/
|   |   `-- LibraryScreen.kt
|   `-- theme/
`-- viewmodel/
    `-- MusicViewModel.kt
```

## 当前仍未实现

- 后台播放。
- Android 通知栏媒体控件。
- MediaSessionService。
- 耳机按钮。
- 搜索。
- 歌单 / 收藏。
- 专辑封面。
- 歌词。
- 随机播放。
- 单曲循环 / 列表循环。
- 播放进度条。
- 动画和更现代的交互过渡。

## 下一阶段建议

1. 先手动测试 0.3 internal：权限、扫描、列表、MiniPlayer、上一曲、下一曲、自动下一首、最后一首停止。
2. 修正手动测试中发现的 UI 边界、权限兼容或播放队列问题。
3. 若继续做体验增强，优先考虑搜索、进度条或封面信息，但保持小步可编译。
4. 若进入后台播放阶段，再考虑通知栏媒体控件、MediaSessionService 和耳机按钮。

## 禁止事项

- 不要在 Composable 中创建或直接操作 ExoPlayer。
- 不要把播放队列逻辑写进 Composable。
- 不要把 UI、权限、扫描、播放控制混在同一个文件里。
- UI 优化应保持简单，不要为了视觉效果引入复杂框架。
- 不要引入 Hilt、Room、Navigation 等额外框架，除非后续有明确需求。
- 不要实现后台播放、通知栏媒体控件、耳机按钮、随机播放或循环播放，除非进入对应阶段。
- 不要在基础播放和队列控制未稳定前扩展在线音乐、账号、插件系统或复杂歌单功能。

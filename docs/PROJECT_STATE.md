# Flowtone 项目状态

## 项目目标

Flowtone 是一个 Android 本地音乐播放器。当前阶段是 Flowtone 0.4：在 0.3 UI internal 的基础上，整理播放架构边界，为后续接入 MediaSession 做准备。

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
- MiniPlayer 控制按钮已改为 Material 图标按钮。

## Flowtone 0.4 当前目标

- 检查并整理播放架构边界。
- 为后续接入 MediaSession 做准备。
- 暂不接入 MediaSession、MediaSessionService、通知栏媒体控件或后台播放。

## Flowtone 0.4 Step 1 检查结果

- `ExoPlayer` 只由 `PlaybackController` 私有持有和操作。
- Composable 不直接创建、持有或操作 `ExoPlayer`。
- `FlowtoneApp` 和 `MiniPlayer` 只调用 `MusicViewModel` 暴露的方法。
- `MusicViewModel` 负责播放队列、当前索引、上一曲、下一曲和自动下一首。
- `PlaybackController` 负责底层播放、暂停、切换播放状态、播放结束监听和释放播放器。
- 播放、暂停、上一曲、下一曲、自动下一首目前都经过 ViewModel / PlaybackController 边界，没有把播放逻辑写进 UI。
- 当前结构适合下一步接入 MediaSession：后续可以优先在播放控制层或其上方增加 MediaSession 适配，不需要让 Composable 接触播放器。

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

- MediaSession。
- MediaSessionService。
- 后台播放。
- Android 通知栏媒体控件。
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

1. 接入 MediaSession 前，先决定 MediaSession 放在 `playback` 层还是单独新增轻量适配层。
2. 保持 Composable 只消费状态和触发 ViewModel 方法。
3. MediaSession 接入后仍应复用现有播放队列逻辑，避免把队列复制到多个地方。
4. MediaSession 稳定后，再考虑 MediaSessionService、通知栏媒体控件和后台播放。

## 禁止事项

- 不要在 Composable 中创建或直接操作 ExoPlayer。
- 不要把播放队列逻辑写进 Composable。
- 不要把 UI、权限、扫描、播放控制混在同一个文件里。
- UI 优化应保持简单，不要为了视觉效果引入复杂框架。
- 不要引入 Hilt、Room、Navigation 等额外框架，除非后续有明确需求。
- 不要实现后台播放、通知栏媒体控件、耳机按钮、随机播放或循环播放，除非进入对应阶段。
- 不要在基础播放和队列控制未稳定前扩展在线音乐、账号、插件系统或复杂歌单功能。

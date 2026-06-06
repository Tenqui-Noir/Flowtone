# Flowtone 项目状态

## 项目目标

Flowtone 是一个 Android 本地音乐播放器，目标是先完成最小可用的本地音乐播放闭环。

## 当前已完成内容

- 已建立基础 Compose 入口：`MainActivity` -> `FlowtoneApp`。
- 已声明音频读取权限：Android 13+ 使用 `READ_MEDIA_AUDIO`，Android 12 及以下使用 `READ_EXTERNAL_STORAGE`。
- 已实现运行时音频权限请求 UI。
- 已实现本地音频扫描：`ContentResolver + MediaStore.Audio.Media`。
- 已显示本地歌曲列表。
- 已添加 Media3 ExoPlayer 依赖。
- 已新增播放控制层：`PlaybackController` 内部持有 `ExoPlayer`，但尚未接入 UI。

## 当前技术栈

- Kotlin
- Jetpack Compose
- Material Design 3
- MVVM
- AndroidX Lifecycle ViewModel
- AndroidX Media3 ExoPlayer
- Gradle Version Catalog

## 当前文件结构概览

```text
app/src/main/java/ink/tenqui/flowtone/
├── MainActivity.kt
├── data/
│   └── AudioScanner.kt
├── model/
│   └── Song.kt
├── permissions/
│   └── AudioPermission.kt
├── playback/
│   ├── PlaybackController.kt
│   └── PlaybackState.kt
├── ui/
│   ├── FlowtoneApp.kt
│   ├── components/
│   │   └── SongListItem.kt
│   ├── screens/
│   │   └── LibraryScreen.kt
│   └── theme/
└── viewmodel/
    └── MusicViewModel.kt
```

## MVP 0.1 完成标准

- 可以请求音频权限。
- 可以扫描本地音乐文件。
- 可以显示本地歌曲列表。
- 点击歌曲后可以使用 Media3 ExoPlayer 播放。
- 底部显示 MiniPlayer。
- MiniPlayer 显示当前歌曲信息。
- MiniPlayer 支持播放和暂停。
- 每一步修改后项目保持可编译。

## 下一步计划

1. 将 `PlaybackController` 接入 `MusicViewModel`。
2. 点击歌曲时调用播放控制层开始播放。
3. 将播放状态暴露给 UI。
4. 新增 `MiniPlayer` 组件。
5. 在 `Scaffold.bottomBar` 中显示 MiniPlayer。
6. MiniPlayer 支持播放和暂停。
7. 运行 Gradle build 验证。

## 明确禁止事项

- 不要在 Composable 中创建或直接操作 ExoPlayer。
- 不要在本地播放闭环完成前实现在线音乐服务。
- 不要添加账号系统。
- 不要添加歌词功能。
- 不要添加插件系统。
- 不要添加复杂播放列表功能。
- 不要引入 Hilt、Room、Navigation 等额外框架，除非有明确需求。
- 不要把权限、扫描、播放逻辑混在 UI 组件里。

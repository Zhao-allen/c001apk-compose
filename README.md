# c001apk-compose (UI 优化版)

基于 Kotlin + Jetpack Compose + Material 3 的酷安第三方客户端。

本项目 Fork 自 [frisk1127/c001apk-compose](https://github.com/frisk1127/c001apk-compose)，并在其基础上进行了 UI/UX 重构。

## 本仓库的修改内容

版本号自 `1.0.1` 起（`versionCode` 沿用 git 提交数自动递增）。

相较上游原版，本仓库主要修改如下：

### 视觉主题
- 默认主题种子色调整为品牌紫（`#7C3AED`）
- 浅色模式下实现「灰底 + 白色圆角卡片」的层次配色（`toCardStyle()` 翻转 surface / surfaceContainer）
- 补全 Material 3 Typography 全套字阶（Type.kt），移除组件内散落的 `fontSize` 覆写

### 首页
- 保留原版布局结构（板块 Tab 行 + 编辑 / 搜索图标），顶栏新增设置入口图标
- 发布动态 FAB 固定为品牌紫色

### 导航结构
- 底部导航改为悬浮胶囊样式（距底 12dp、高 62dp、全圆角、白色磨砂底 + 柔和阴影 + 淡紫描边）
- 选中态为品牌紫 16% 药丸背景，整块包裹「图标 + 文字」
- 底部导航精简为 3 Tab：首页 / 圈子 / 我的（原版的消息 / 设置 Tab 移除，设置入口移至首页顶栏）
- 内容 edge-to-edge：页面全屏延伸至底部，胶囊悬浮于内容之上，各列表通过 `FloatingNavBottomClearance` 预留胶囊间隙
- 「我的」页为消息界面（含登录入口、消息菜单与通知列表）；圈子页补齐状态栏 / 底部 inset，内容不再顶到系统栏
- 设置页支持返回导航（从首页顶栏进入时显示返回按钮）

### 信息流
- FeedCard 字阶规范化（meta 12sp / 正文 16sp / 徽标 labelSmall）
- 修复详情页回复引用块背景与卡片同色不可见的问题
- 引用块统一 12dp 圆角与内边距；元信息文字改用 `onSurfaceVariant` 提升对比度
- 计数文字启用 tabular 数字（`tnum`），点赞数变化时不再抖动
- 信息流卡片水平边距 10 → 12dp

## 构建

要求：JDK 17+、Android SDK（compileSdk 36、Build-Tools 36.1.0）。

> 注意：AGP 9.0 需附带 `-Pandroid.newDsl=false -Pandroid.builtInKotlin=false` 构建参数（与上游 CI 一致）。

```bash
# 在项目根目录创建 local.properties，指向本机 SDK
echo "sdk.dir=/path/to/Android/Sdk" > local.properties

# Debug 构建
./gradlew assembleDebug -Pandroid.newDsl=false -Pandroid.builtInKotlin=false

# Release 构建（签名信息从 local.properties 读取，缺省时使用 debug 签名）
./gradlew assembleRelease -Pandroid.newDsl=false -Pandroid.builtInKotlin=false
```

## 开源协议

本项目基于 [frisk1127/c001apk-compose](https://github.com/frisk1127/c001apk-compose) 修改，原作者版权声明与许可证一并保留。

采用 [GNU AGPL-3.0](LICENSE) 协议发布：

- 保留原作者的版权与许可声明（见 `LICENSE` 及各源文件头部）
- 任何对本项目的修改，以 AGPL-3.0 分发时**必须同样开源**
- 若通过网络提供服务（含分发 APK），须向服务使用者提供对应源代码

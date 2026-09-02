# c001apk-compose (UI 优化版)

基于 Kotlin + Jetpack Compose + Material 3 的酷安第三方客户端。

本项目 Fork 自 [frisk1127/c001apk-compose](https://github.com/frisk1127/c001apk-compose)，并在其基础上进行了 UI/UX 重构。

## 本仓库的修改内容

相较上游原版，本仓库主要修改如下：

### 视觉主题
- 默认主题种子色调整为品牌紫（`#7C3AED`）
- 浅色模式下实现「灰底 + 白色圆角卡片」的层次配色（`toCardStyle()` 翻转 surface / surfaceContainer）
- 补全 Material 3 Typography 全套字阶（Type.kt），移除组件内散落的 `fontSize` 覆写

### 首页
- 顶部新增胶囊形搜索栏（白底、24dp 圆角、紫色搜索图标）
- 新增六宫格快捷入口卡片（应用 / 数码 / 话题 / 热榜 / 酷图 / 关注，紫色图标 + 淡紫圆底，随自定义板块自动过滤）
- 发布动态 FAB 固定为品牌紫色
- 头部搜索图标移入胶囊搜索栏

### 导航结构
- 底部导航由 3 Tab 扩展为 5 Tab：首页 / 圈子 / 应用 / 消息 / 我的
- 新增「我的」页：未登录显示登录入口，登录后展示个人主页
- 设置页支持返回导航（从底部导航进入时显示返回按钮）

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

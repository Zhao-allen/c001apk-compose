/*
 * 修改声明（UI 优化版，基于 frisk1127/c001apk-compose，AGPL-3.0）：
 * 本文件按新版首页视觉规范重构头部：问候语 + 搜索胶囊 + 下划线 Tab 行
 * （选中紫色加粗 + 底部指示条），Tab 行尾部「更多」按钮打开板块编辑。
 * 原作者版权与许可见 LICENSE。
 */
package com.example.c001apk.compose.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.c001apk.compose.R
import com.example.c001apk.compose.logic.model.HomeMenu
import com.example.c001apk.compose.logic.model.UpdateCheckItem
import com.example.c001apk.compose.ui.component.CapsuleSearchBar
import com.example.c001apk.compose.ui.component.CompactTopBar
import com.example.c001apk.compose.ui.component.ScrollFab
import com.example.c001apk.compose.ui.component.rememberHapticClick
import com.example.c001apk.compose.ui.feed.reply.startCreateFeedActivity
import com.example.c001apk.compose.ui.home.app.AppListScreen
import com.example.c001apk.compose.ui.home.feed.HomeFeedScreen
import com.example.c001apk.compose.ui.home.topic.HomeTopicScreen
import com.example.c001apk.compose.ui.main.FloatingNavBottomClearance
import com.example.c001apk.compose.util.CookieUtil.isLogin
import com.example.c001apk.compose.util.ReportType
import com.example.c001apk.compose.util.makeToast
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * Created by bggRGjQaUbCoE on 2024/6/5
 */

enum class TabType {
    FOLLOW, APP, FEED, HOT, TOPIC, PRODUCT, COOLPIC
}

private fun tabTitle(type: TabType): String {
    return when (type) {
        TabType.FOLLOW -> "关注"
        TabType.APP -> "应用"
        TabType.FEED -> "动态"
        TabType.HOT -> "热榜"
        TabType.TOPIC -> "话题"
        TabType.PRODUCT -> "数码"
        TabType.COOLPIC -> "酷图"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeTabEditorDialog(
    enabledTabs: Set<TabType>,
    onDismiss: () -> Unit,
    onConfirm: (Set<TabType>) -> Unit,
) {
    var selectedTabs by remember(enabledTabs) { mutableStateOf(enabledTabs) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                topBar = {
                    CompactTopBar(
                        title = { Text("编辑首页板块") },
                        navigationIcon = {
                            IconButton(onClick = onDismiss) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "返回",
                                )
                            }
                        },
                        actions = {
                            TextButton(onClick = { onConfirm(selectedTabs) }) {
                                Text("完成")
                            }
                        },
                    )
                },
            ) { paddingValues ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                ) {
                    items(TabType.entries, key = TabType::name) { type ->
                        val checked = type in selectedTabs
                        val canToggle = !checked || selectedTabs.size > 1
                        ListItem(
                            modifier = Modifier.clickable(enabled = canToggle) {
                                selectedTabs = if (checked) {
                                    selectedTabs - type
                                } else {
                                    selectedTabs + type
                                }
                            },
                            headlineContent = { Text(tabTitle(type)) },
                            trailingContent = {
                                Checkbox(
                                    checked = checked,
                                    enabled = canToggle,
                                    onCheckedChange = null,
                                )
                            },
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    refreshState: Boolean,
    resetRefreshState: () -> Unit,
    onRefresh: () -> Unit,
    onSearch: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenNotice: () -> Unit,
    onViewUser: (String) -> Unit,
    onViewFeed: (String, Boolean) -> Unit,
    onOpenLink: (String, String?) -> Unit,
    onCopyText: (String?) -> Unit,
    onViewApp: (String) -> Unit,
    onCheckUpdate: (List<UpdateCheckItem>) -> Unit,
    onReport: (String, ReportType) -> Unit,
) {

    val scope = rememberCoroutineScope()

    val storedMenus by viewModel.homeMenus.collectAsStateWithLifecycle(initialValue = emptyList())
    val tabList = remember(storedMenus) {
        if (storedMenus.isEmpty()) {
            listOf(
                TabType.FEED,
                TabType.FOLLOW,
                TabType.TOPIC,
                TabType.PRODUCT,
                TabType.APP,
                TabType.HOT,
                TabType.COOLPIC,
            )
        } else {
            storedMenus
                .asSequence()
                .filter(HomeMenu::isEnable)
                .sortedBy(HomeMenu::position)
                .mapNotNull { menu ->
                    runCatching { TabType.valueOf(menu.title) }.getOrNull()
                }
                .toList()
                .ifEmpty { listOf(TabType.FEED) }
        }
    }
    var selectedTabType by rememberSaveable { mutableStateOf(TabType.FEED) }
    val initialPage = tabList.indexOf(selectedTabType).coerceAtLeast(0)
    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = {
            tabList.size
        }
    )
    val context = LocalContext.current
    var isScrollingUp by remember { mutableStateOf(false) }
    var showTabEditor by rememberSaveable { mutableStateOf(false) }
    val selectedTabIndex = pagerState.currentPage.coerceIn(tabList.indices)

    LaunchedEffect(tabList) {
        val targetType = selectedTabType.takeIf(tabList::contains)
            ?: TabType.FEED.takeIf(tabList::contains)
            ?: tabList.first()
        selectedTabType = targetType
        val targetIndex = tabList.indexOf(targetType)
        if (pagerState.currentPage != targetIndex) {
            pagerState.scrollToPage(targetIndex)
        }
    }

    LaunchedEffect(pagerState, tabList) {
        snapshotFlow { pagerState.settledPage }.collect { page ->
            tabList.getOrNull(page)?.let { selectedTabType = it }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        floatingActionButton = {
            if (isLogin && tabList.getOrNull(selectedTabIndex) == TabType.FEED) {
                ScrollFab(
                    visible = isScrollingUp,
                    onClick = {
                        context.startCreateFeedActivity()
                    },
                    modifier = Modifier.padding(bottom = FloatingNavBottomClearance),
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null
                    )
                }
            }
        },
        contentWindowInsets = ScaffoldDefaults
            .contentWindowInsets
            .exclude(WindowInsets.navigationBars)
    ) { paddingValues ->

        Column(
            modifier = Modifier.padding(top = paddingValues.calculateTopPadding()),
        ) {
            // 1) 问候区：左侧问候语 + 副标题，右侧扫一扫 / 消息 / 设置
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 14.dp, end = 4.dp, top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val greeting = remember { greetingText() }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = greeting,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = 21.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                        maxLines = 1,
                    )
                    Text(
                        text = "发现科技新生活",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                }
                IconButton(
                    onClick = rememberHapticClick {
                        context.makeToast("扫一扫即将上线")
                    },
                    modifier = Modifier.size(40.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = "扫一扫",
                        modifier = Modifier.size(21.dp),
                    )
                }
                Box {
                    IconButton(
                        onClick = rememberHapticClick(onClick = onOpenNotice),
                        modifier = Modifier.size(40.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Notifications,
                            contentDescription = "消息",
                            modifier = Modifier.size(21.dp),
                        )
                    }
                    // 未读小红点
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 9.dp, end = 9.dp)
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEF4444)),
                    )
                }
                IconButton(
                    onClick = rememberHapticClick(onClick = onOpenSettings),
                    modifier = Modifier.size(40.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Settings,
                        contentDescription = "设置",
                        modifier = Modifier.size(21.dp),
                    )
                }
            }

            // 2) 搜索胶囊
            CapsuleSearchBar(
                hint = "搜索酷安内容",
                onClick = onSearch,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            )

            // 3) Tab 行：紫色下划线样式 + 尾部「更多」入口
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                HomeTopTabs(
                    modifier = Modifier.weight(1f),
                    tabList = tabList,
                    selectedIndex = pagerState.currentPage,
                    onSelectTab = { index ->
                        if (pagerState.currentPage == index) {
                            onRefresh()
                        }
                        tabList.getOrNull(index)?.let { selectedTabType = it }
                        scope.launch { pagerState.animateScrollToPage(index) }
                    },
                )
                IconButton(
                    onClick = { showTabEditor = true },
                    modifier = Modifier.size(40.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "编辑首页板块",
                        modifier = Modifier.size(20.dp),
                    )
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f),
            ) { index ->

                when (val type = tabList[index]) {
                    TabType.FOLLOW, TabType.FEED, TabType.HOT, TabType.COOLPIC ->
                        HomeFeedScreen(
                            refreshState = refreshState,
                            resetRefreshState = resetRefreshState,
                            type = type,
                            onViewUser = onViewUser,
                            onViewFeed = onViewFeed,
                            onOpenLink = onOpenLink,
                            onCopyText = onCopyText,
                            onReport = onReport,
                            bottomPadding = FloatingNavBottomClearance,
                            isScrollingUp = {
                                isScrollingUp = it
                            }
                        )

                    TabType.APP -> AppListScreen(
                        refreshState = refreshState,
                        resetRefreshState = resetRefreshState,
                        onViewApp = onViewApp,
                        onCheckUpdate = onCheckUpdate,
                    )

                    TabType.TOPIC, TabType.PRODUCT -> HomeTopicScreen(
                        type = type,
                        onViewUser = onViewUser,
                        onViewFeed = onViewFeed,
                        onOpenLink = onOpenLink,
                        onCopyText = onCopyText,
                        bottomPadding = FloatingNavBottomClearance
                    )
                }

            }

        }

    }

    if (showTabEditor) {
        HomeTabEditorDialog(
            enabledTabs = tabList.toSet(),
            onDismiss = { showTabEditor = false },
            onConfirm = { enabledTabs ->
                viewModel.setEnabledTabs(enabledTabs)
                showTabEditor = false
            },
        )
    }

}

@Composable
private fun HomeTopTabs(
    tabList: List<TabType>,
    selectedIndex: Int,
    onSelectTab: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    LaunchedEffect(selectedIndex) {
        if (selectedIndex in tabList.indices) {
            listState.animateScrollToItem(selectedIndex)
        }
    }
    LazyRow(
        modifier = modifier,
        state = listState,
        contentPadding = PaddingValues(horizontal = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        itemsIndexed(tabList, key = { _, item -> item.name }) { index, tab ->
            val selected = index == selectedIndex
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = rememberHapticClick { onSelectTab(index) })
                    .padding(horizontal = 2.dp, vertical = 10.dp),
            ) {
                Text(
                    text = tabTitle(tab),
                    fontSize = 15.sp,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                    color = if (selected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                )
                // 选中态底部指示条
                Spacer(
                    modifier = Modifier
                        .padding(top = 5.dp)
                        .size(width = 18.dp, height = 3.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(
                            if (selected) MaterialTheme.colorScheme.primary
                            else Color.Transparent
                        ),
                )
            }
        }
    }
}

/**
 * 依据当前时间返回问候语，用于首页头部。
 */
private fun greetingText(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when {
        hour in 5..10 -> "早上好"
        hour in 11..13 -> "中午好"
        hour in 14..18 -> "下午好"
        else -> "晚上好"
    }
}

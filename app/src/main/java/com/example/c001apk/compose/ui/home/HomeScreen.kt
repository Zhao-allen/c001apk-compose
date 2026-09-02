/*
 * 修改声明（UI 优化版，基于 frisk1127/c001apk-compose，AGPL-3.0）：
 * 本文件在原版基础上新增胶囊搜索栏（含设置入口）、六宫格快捷入口、
 * 品牌紫 FAB 与主题种子色调整。原作者版权与许可见 LICENSE。
 */
package com.example.c001apk.compose.ui.home

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Devices
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.SecondaryScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.c001apk.compose.R
import com.example.c001apk.compose.logic.model.HomeMenu
import com.example.c001apk.compose.logic.model.UpdateCheckItem
import com.example.c001apk.compose.ui.component.rememberHapticClick
import com.example.c001apk.compose.ui.feed.reply.ReplyActivity
import com.example.c001apk.compose.ui.home.app.AppListScreen
import com.example.c001apk.compose.ui.home.feed.HomeFeedScreen
import com.example.c001apk.compose.ui.home.topic.HomeTopicScreen
import com.example.c001apk.compose.ui.theme.cardBg
import com.example.c001apk.compose.util.CookieUtil.isLogin
import com.example.c001apk.compose.util.ReportType
import com.example.c001apk.compose.util.makeToast
import kotlinx.coroutines.launch

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
                    TopAppBar(
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
            TabType.entries
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
                AnimatedVisibility(
                    visible = isScrollingUp,
                    enter = slideInVertically { it * 2 },
                    exit = slideOutVertically { it * 2 }
                ) {
                    FloatingActionButton(
                        onClick = rememberHapticClick {
                            val intent = Intent(context, ReplyActivity::class.java)
                            intent.putExtra("type", "createFeed")
                            val animationBundle = ActivityOptionsCompat.makeCustomAnimation(
                                context,
                                R.anim.anim_bottom_sheet_slide_up,
                                R.anim.anim_bottom_sheet_slide_down
                            ).toBundle()
                            ContextCompat.startActivity(context, intent, animationBundle)
                        },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null
                        )
                    }
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HomeSearchBar(
                    modifier = Modifier.weight(1f),
                    onClick = onSearch
                )
                IconButton(onClick = rememberHapticClick { onOpenSettings() }) {
                    Icon(
                        imageVector = Icons.Outlined.Settings,
                        contentDescription = "设置",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            QuickEntryGrid(
                tabList = tabList,
                onEntryClick = { tab ->
                    val index = tabList.indexOf(tab)
                    if (index >= 0) {
                        selectedTabType = tab
                        scope.launch { pagerState.animateScrollToPage(index) }
                    }
                }
            )

            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                SecondaryScrollableTabRow(
                    modifier = Modifier.weight(1f),
                    selectedTabIndex = selectedTabIndex,
                    indicator = {
                        TabRowDefaults.SecondaryIndicator(
                            Modifier
                                .tabIndicatorOffset(selectedTabIndex, matchContentSize = true)
                                .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                        )
                    },
                    divider = {}
                ) {
                    tabList.forEachIndexed { index, tab ->
                        Tab(
                            selected = pagerState.currentPage == index,
                            onClick = rememberHapticClick {
                                if (pagerState.currentPage == index) {
                                    onRefresh()
                                }
                                selectedTabType = tab
                                scope.launch { pagerState.animateScrollToPage(index) }
                            },
                            text = { Text(text = tabTitle(tab)) }
                        )
                    }
                }
                IconButton(onClick = { showTabEditor = true }) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = "编辑首页板块",
                    )
                }
            }

            HorizontalDivider()

            HorizontalPager(
                state = pagerState,
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
                        onCopyText = onCopyText
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
private fun HomeSearchBar(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .padding(start = 16.dp, top = 8.dp, bottom = 8.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(cardBg())
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.Search,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = "搜索应用 / 数码 / 帖子",
            modifier = Modifier.padding(start = 10.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun QuickEntryGrid(
    tabList: List<TabType>,
    onEntryClick: (TabType) -> Unit,
) {
    val entries = remember(tabList) {
        listOf(
            TabType.APP to Icons.Outlined.Apps,
            TabType.PRODUCT to Icons.Outlined.Devices,
            TabType.TOPIC to Icons.Outlined.Forum,
            TabType.HOT to Icons.Outlined.LocalFireDepartment,
            TabType.COOLPIC to Icons.Outlined.PhotoLibrary,
            TabType.FOLLOW to Icons.Outlined.FavoriteBorder,
        ).filter { (type, _) -> type in tabList }
    }
    if (entries.isEmpty()) return
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        color = cardBg()
    ) {
        Column(modifier = Modifier.padding(vertical = 12.dp)) {
            entries.chunked(3).forEach { rowEntries ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    rowEntries.forEach { (type, icon) ->
                        QuickEntryItem(
                            modifier = Modifier.weight(1f),
                            icon = icon,
                            label = tabTitle(type),
                            onClick = rememberHapticClick { onEntryClick(type) }
                        )
                    }
                    repeat(3 - rowEntries.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickEntryItem(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
        }
        Text(
            text = label,
            modifier = Modifier.padding(top = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

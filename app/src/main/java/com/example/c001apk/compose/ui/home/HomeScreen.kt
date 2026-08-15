package com.example.c001apk.compose.ui.home

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
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
import com.example.c001apk.compose.util.CookieUtil.isLogin
import com.example.c001apk.compose.util.ReportType
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
                        }
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
                IconButton(onClick = { onSearch() }) {
                    Icon(imageVector = Icons.Default.Search, contentDescription = null)
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

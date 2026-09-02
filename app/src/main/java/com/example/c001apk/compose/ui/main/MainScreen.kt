/*
 * 修改声明（UI 优化版，基于 frisk1127/c001apk-compose，AGPL-3.0）：
 * 本文件将底部导航改为悬浮胶囊样式（首页/圈子/我的 三 Tab），
 * 内容全屏延伸（edge-to-edge），各列表自行预留胶囊底部间隙；
 * 「我的」页改为消息界面（MessageScreen，含登录入口）。
 * 原作者版权与许可见 LICENSE。
 */
package com.example.c001apk.compose.ui.main

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.c001apk.compose.logic.model.UpdateCheckItem
import com.example.c001apk.compose.ui.component.SlideTransition
import com.example.c001apk.compose.ui.component.rememberHapticClick
import com.example.c001apk.compose.ui.home.HomeScreen
import com.example.c001apk.compose.ui.home.TabType
import com.example.c001apk.compose.ui.home.topic.HomeTopicScreen
import com.example.c001apk.compose.ui.message.MessageScreen
import com.example.c001apk.compose.ui.theme.cardBg
import com.example.c001apk.compose.util.ReportType

/**
 * 悬浮胶囊导航总占位（距底 12dp + 胶囊高 62dp），
 * 各页面列表底部需预留此间隙，避免内容被胶囊遮挡。
 */
internal val FloatingNavBottomClearance = 74.dp

/**
 * Created by bggRGjQaUbCoE on 2024/5/30
 */
@Composable
fun MainScreen(
    selectIndex: Int,
    setSelectIndex: (Int) -> Unit,
    onViewUser: (String) -> Unit,
    onViewFeed: (String, Boolean) -> Unit,
    onSearch: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenLink: (String, String?) -> Unit,
    onCopyText: (String?) -> Unit,
    onViewApp: (String) -> Unit,
    onLogin: () -> Unit,
    onCheckUpdate: (List<UpdateCheckItem>) -> Unit,
    onViewFFFList: (String?, String) -> Unit,
    onReport: (String, ReportType) -> Unit,
    onViewNotice: (String) -> Unit,
    onViewHistory: (String) -> Unit,
) {

    val screens = mainScreens

    val savableStateHolder = rememberSaveableStateHolder()
    val performHapticClick = rememberHapticClick {}
    var refreshState by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {

        AnimatedContent(
            modifier = Modifier.fillMaxSize(),
            label = "home-content",
            targetState = selectIndex,
            transitionSpec = {
                SlideTransition.slideLeft.enterTransition()
                    .togetherWith(SlideTransition.slideLeft.exitTransition())
            },
        ) { page ->
            savableStateHolder.SaveableStateProvider(
                key = page,
                content = {
                    when (page) {
                        0 -> HomeScreen(
                            refreshState = refreshState,
                            onRefresh = {
                                refreshState = true
                            },
                            resetRefreshState = {
                                refreshState = false
                            },
                            onSearch = onSearch,
                            onOpenSettings = onOpenSettings,
                            onViewUser = onViewUser,
                            onViewFeed = onViewFeed,
                            onOpenLink = onOpenLink,
                            onCopyText = onCopyText,
                            onViewApp = onViewApp,
                            onCheckUpdate = onCheckUpdate,
                            onReport = onReport,
                        )

                        1 -> Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(cardBg())
                                .statusBarsPadding()
                        ) {
                            HomeTopicScreen(
                                type = TabType.TOPIC,
                                onViewUser = onViewUser,
                                onViewFeed = onViewFeed,
                                onOpenLink = onOpenLink,
                                onCopyText = onCopyText,
                                bottomPadding = FloatingNavBottomClearance,
                            )
                        }

                        2 -> MessageScreen(
                            onLogin = onLogin,
                            onViewUser = onViewUser,
                            onViewFeed = onViewFeed,
                            onOpenLink = onOpenLink,
                            onCopyText = onCopyText,
                            onViewFFFList = onViewFFFList,
                            onReport = onReport,
                            onViewNotice = onViewNotice,
                            onViewHistory = onViewHistory,
                        )

                        else -> {}
                    }
                }
            )
        }

        FloatingCapsuleNav(
            screens = screens,
            selectIndex = selectIndex,
            onSelect = { index ->
                performHapticClick()
                if (selectIndex == 0 && index == 0) {
                    refreshState = true
                }
                setSelectIndex(index)
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(start = 14.dp, end = 14.dp, bottom = 12.dp),
        )

    }

    BackHandler(enabled = selectIndex != 0) {
        setSelectIndex(0)
    }

}

@Composable
private fun FloatingCapsuleNav(
    screens: List<Router>,
    selectIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(31.dp)
    Row(
        modifier = modifier
            .shadow(elevation = 10.dp, shape = shape)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                shape = shape,
            )
            .padding(horizontal = 8.dp, vertical = 5.dp)
            .height(62.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        screens.forEachIndexed { index, screen ->
            val selected = selectIndex == index
            val contentColor =
                if (selected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(26.dp))
                    .background(
                        if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
                        else Color.Transparent
                    )
                    .clickable { onSelect(index) },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Icon(
                    imageVector = if (selected) screen.selectedIcon!! else screen.unselectedIcon!!,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(20.dp),
                )
                Text(
                    text = stringResource(id = screen.stringId!!),
                    style = MaterialTheme.typography.labelSmall,
                    color = contentColor,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }
    }
}

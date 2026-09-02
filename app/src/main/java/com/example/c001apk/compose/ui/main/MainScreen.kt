/*
 * 修改声明（UI 优化版，基于 frisk1127/c001apk-compose，AGPL-3.0）：
 * 本文件在原版基础上将底部导航扩展为五 Tab（首页/圈子/应用/消息/我的），
 * 新增「我的」页并调整 HomeScreen 参数。原作者版权与许可见 LICENSE。
 */
package com.example.c001apk.compose.ui.main

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.c001apk.compose.logic.model.UpdateCheckItem
import com.example.c001apk.compose.ui.component.SlideTransition
import com.example.c001apk.compose.ui.component.rememberHapticClick
import com.example.c001apk.compose.ui.home.HomeScreen
import com.example.c001apk.compose.ui.home.TabType
import com.example.c001apk.compose.ui.home.app.AppListScreen
import com.example.c001apk.compose.ui.home.topic.HomeTopicScreen
import com.example.c001apk.compose.ui.message.MessageScreen
import com.example.c001apk.compose.ui.user.UserScreen
import com.example.c001apk.compose.util.CookieUtil
import com.example.c001apk.compose.util.CookieUtil.isLogin
import com.example.c001apk.compose.util.ReportType

/**
 * Created by bggRGjQaUbCoE on 2024/5/30
 */
@Composable
fun MainScreen(
    selectIndex: Int,
    setSelectIndex: (Int) -> Unit,
    badge: Int,
    resetBadge: () -> Unit,
    onViewUser: (String) -> Unit,
    onViewFeed: (String, Boolean) -> Unit,
    onSearch: () -> Unit,
    onOpenSearch: (String?, String?, String?) -> Unit,
    onOpenLink: (String, String?) -> Unit,
    onCopyText: (String?) -> Unit,
    onViewApp: (String) -> Unit,
    onLogin: () -> Unit,
    onCheckUpdate: (List<UpdateCheckItem>) -> Unit,
    onViewFFFList: (String?, String) -> Unit,
    onReport: (String, ReportType) -> Unit,
    onViewNotice: (String) -> Unit,
    onViewHistory: (String) -> Unit,
    onOpenSettings: () -> Unit,
    onPMUser: (String, String) -> Unit,
    widthSizeClass: WindowWidthSizeClass,
) {

    val screens = mainScreens
    val messageIndex = screens.indexOf(Router.MESSAGE)

    val savableStateHolder = rememberSaveableStateHolder()
    val performHapticClick = rememberHapticClick {}
    var refreshState by remember { mutableStateOf(false) }

    val customNavSuiteType = when (widthSizeClass) {
        WindowWidthSizeClass.Compact -> NavigationSuiteType.NavigationBar
        else -> NavigationSuiteType.NavigationRail
    }

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            screens.forEachIndexed { index, screen ->
                item(
                    icon = {
                        BadgedBox(
                            badge = {
                                AnimatedVisibility(
                                    visible = if (index == messageIndex) badge > 0
                                    else false,
                                    enter = scaleIn(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)),
                                    exit = scaleOut(animationSpec = spring(stiffness = Spring.StiffnessMediumLow))
                                ) {
                                    Badge(
                                        modifier = Modifier
                                            .padding(start = 15.dp, bottom = 10.dp)
                                    ) {
                                        Text(text = badge.toString())
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector =
                                if (selectIndex == screens.indexOf(screen)) {
                                    screen.selectedIcon!!
                                } else {
                                    screen.unselectedIcon!!
                                },
                                contentDescription = null
                            )
                        }
                    },
                    label = { Text(text = stringResource(id = screen.stringId!!)) },
                    selected = selectIndex == screens.indexOf(screen),
                    onClick = {
                        performHapticClick()
                        with(screens.indexOf(screen)) {
                            if (selectIndex == 0 && this == 0) {
                                refreshState = true
                            } else if (this == messageIndex && badge != 0) {
                                resetBadge()
                            }
                            setSelectIndex(this)
                        }
                    },
                    alwaysShowLabel = true
                )
            }
        },
        layoutType = customNavSuiteType
    ) {
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
                            onViewUser = onViewUser,
                            onViewFeed = onViewFeed,
                            onSearch = onSearch,
                            onOpenLink = onOpenLink,
                            onCopyText = onCopyText,
                            onViewApp = onViewApp,
                            onCheckUpdate = onCheckUpdate,
                            onReport = onReport,
                            onOpenSettings = onOpenSettings,
                        )

                        1 -> HomeTopicScreen(
                            type = TabType.TOPIC,
                            onViewUser = onViewUser,
                            onViewFeed = onViewFeed,
                            onOpenLink = onOpenLink,
                            onCopyText = onCopyText,
                        )

                        2 -> AppListScreen(
                            refreshState = refreshState,
                            resetRefreshState = {
                                refreshState = false
                            },
                            onViewApp = onViewApp,
                            onCheckUpdate = onCheckUpdate,
                        )

                        3 -> MessageScreen(
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

                        4 -> MineScreen(
                            onLogin = onLogin,
                            onViewUser = onViewUser,
                            onViewFeed = onViewFeed,
                            onOpenLink = onOpenLink,
                            onCopyText = onCopyText,
                            onOpenSearch = onOpenSearch,
                            onViewFFFList = onViewFFFList,
                            onReport = onReport,
                            onPMUser = onPMUser,
                        )

                        else -> {}
                    }
                }
            )
        }
    }

    BackHandler(enabled = selectIndex != 0) {
        setSelectIndex(0)
    }

}

@Composable
private fun MineScreen(
    onLogin: () -> Unit,
    onViewUser: (String) -> Unit,
    onViewFeed: (String, Boolean) -> Unit,
    onOpenLink: (String, String?) -> Unit,
    onCopyText: (String?) -> Unit,
    onOpenSearch: (String?, String?, String?) -> Unit,
    onViewFFFList: (String?, String) -> Unit,
    onReport: (String, ReportType) -> Unit,
    onPMUser: (String, String) -> Unit,
) {
    if (isLogin) {
        UserScreen(
            uid = CookieUtil.uid,
            onBackClick = {},
            onViewUser = onViewUser,
            onViewFeed = onViewFeed,
            onOpenLink = onOpenLink,
            onCopyText = onCopyText,
            onSearch = { title, pageType, pageParam ->
                onOpenSearch(title, pageType, pageParam)
            },
            onViewFFFList = { uid, type ->
                onViewFFFList(uid, type)
            },
            onReport = onReport,
            onPMUser = onPMUser,
        )
    } else {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(64.dp)
                )
                Text(
                    text = "登录后查看个人主页",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(top = 12.dp)
                )
                FilledTonalButton(
                    onClick = rememberHapticClick { onLogin() },
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Text(text = "登录")
                }
            }
        }
    }
}

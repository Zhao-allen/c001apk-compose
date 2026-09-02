/*
 * 修改声明（UI 优化版，基于 frisk1127/c001apk-compose，AGPL-3.0）：
 * 本文件在原版基础上新增 CIRCLE / APPS / MINE 路由以支持五 Tab 导航。
 * 原作者版权与许可见 LICENSE。
 */
package com.example.c001apk.compose.ui.main

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.automirrored.outlined.Message
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.c001apk.compose.R

/**
 * Created by bggRGjQaUbCoE on 2024/5/30
 */
sealed class Router(
    val name: String,
    @param:StringRes val stringId: Int? = null,
    val unselectedIcon: ImageVector? = null,
    val selectedIcon: ImageVector? = null,
) {

    data object MAIN : Router(
        name = "MAIN"
    )

    data object HOME : Router(
        name = "HOME",
        stringId = R.string.home,
        unselectedIcon = Icons.Outlined.Home,
        selectedIcon = Icons.Default.Home
    )

    data object CIRCLE : Router(
        name = "CIRCLE",
        stringId = R.string.circle,
        unselectedIcon = Icons.Outlined.Forum,
        selectedIcon = Icons.Default.Forum
    )

    data object APPS : Router(
        name = "APPS",
        stringId = R.string.apps,
        unselectedIcon = Icons.Outlined.Apps,
        selectedIcon = Icons.Default.Apps
    )

    data object MESSAGE : Router(
        name = "MESSAGE",
        stringId = R.string.message,
        unselectedIcon = Icons.AutoMirrored.Outlined.Message,
        selectedIcon = Icons.AutoMirrored.Filled.Message
    )

    data object MINE : Router(
        name = "MINE",
        stringId = R.string.mine,
        unselectedIcon = Icons.Outlined.Person,
        selectedIcon = Icons.Default.Person
    )

    data object SETTINGS : Router(name = "SETTINGS")

    data object PARAMS : Router(name = "PARAMS")

    data object ABOUT : Router(name = "ABOUT")

    data object LICENSE : Router(name = "LICENSE")

    data object BLACKLIST : Router(name = "BLACKLIST")

    data object SEARCH : Router(name = "SEARCH")

    data object SEARCHRESULT : Router(name = "SEARCHRESULT")

    data object TAB : Router(name = "TAB")

    data object FEED : Router(name = "FEED")

    data object USER : Router(name = "USER")

    data object TOPIC : Router(name = "TOPIC")

    data object COPY : Router(name = "COPY")

    data object WEBVIEW : Router(name = "WEBVIEW")

    data object APP : Router(name = "APP")

    data object LOGIN : Router(name = "LOGIN")

    data object CAROUSEL : Router(name = "CAROUSEL")

    data object EVENT : Router(name = "EVENT")

    data object UPDATE : Router(name = "UPDATE")

    data object FFFLIST : Router(name = "FFFLIST")

    data object DYH : Router(name = "DYH")

    data object COOLPIC : Router(name = "COOLPIC")

    data object NOTICE : Router(name = "NOTICE")

    data object HISTORY : Router(name = "HISTORY")

    data object CHAT : Router(name = "CHAT")

    data object COLLECTION : Router(name = "COLLECTION")

}

val mainScreens = listOf(
    Router.HOME,
    Router.CIRCLE,
    Router.APPS,
    Router.MESSAGE,
    Router.MINE
)

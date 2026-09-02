/*
 * 修改声明（UI 优化版，基于 frisk1127/c001apk-compose，AGPL-3.0）：
 * 本文件为新增组件：抽取首页/圈子话题/应用页三处重复的
 * 「上滑显示 FAB」结构（AnimatedVisibility + 竖直滑入滑出 + FloatingActionButton），
 * 并统一内置触感反馈。原作者版权与许可见 LICENSE。
 */
package com.example.c001apk.compose.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.material3.FloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun ScrollFab(
    visible: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically { it * 2 },
        exit = slideOutVertically { it * 2 },
        modifier = modifier,
    ) {
        FloatingActionButton(onClick = rememberHapticClick(onClick = onClick)) {
            content()
        }
    }
}

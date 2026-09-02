/*
 * 修改声明（UI 优化版，基于 frisk1127/c001apk-compose，AGPL-3.0）：
 * 本文件按新版首页渲染图重构为「五宫格」快捷入口：图标在上、文字在下，
 * 每行 5 个等宽排布，图标直接展示彩色 logo（无底板）。原作者版权与许可见 LICENSE。
 */
package com.example.c001apk.compose.ui.component.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.c001apk.compose.logic.model.HomeFeedResponse
import com.example.c001apk.compose.ui.component.CoilLoader
import com.example.c001apk.compose.ui.component.rememberHapticClick
import com.example.c001apk.compose.ui.theme.cardBg

/**
 * Created by bggRGjQaUbCoE on 2024/6/7
 */

private const val GridColumns = 5

@Composable
fun IconMiniGridCard(
    modifier: Modifier = Modifier,
    data: HomeFeedResponse.Data,
    onOpenLink: (String, String?) -> Unit,
) {

    val entities = data.entities.orEmpty()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(cardBg())
            .padding(horizontal = 6.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {

        if (!data.title.isNullOrEmpty()) {
            TitleCard(
                url = data.url.orEmpty(),
                title = data.title,
                onOpenLink = onOpenLink,
            )
        }

        entities.chunked(GridColumns).forEach { rowItems ->
            Row(modifier = Modifier.fillMaxWidth()) {
                rowItems.forEach { entity ->
                    QuickGridItem(
                        modifier = Modifier.weight(1f),
                        logoUrl = entity.logo.orEmpty(),
                        linkUrl = entity.url.orEmpty(),
                        titleText = entity.title.orEmpty(),
                        onOpenLink = onOpenLink,
                    )
                }
                // 末行数量不足时补位，保证每格等宽
                repeat(GridColumns - rowItems.size) {
                    Box(modifier = Modifier.weight(1f))
                }
            }
        }

    }

}

/**
 * 五宫格单项：图标在上（无底板，直接展示彩色 logo），下方为标题。
 */
@Composable
private fun QuickGridItem(
    modifier: Modifier = Modifier,
    logoUrl: String,
    linkUrl: String,
    titleText: String,
    onOpenLink: (String, String?) -> Unit,
) {

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(
                onClick = rememberHapticClick { onOpenLink(linkUrl, titleText) },
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier.size(40.dp),
            contentAlignment = Alignment.Center,
        ) {
            CoilLoader(
                url = logoUrl,
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(9.dp)),
            )
        }
        Text(
            text = titleText,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 4.dp),
        )
    }

}

/*
 * 修改声明（UI 优化版，基于 frisk1127/c001apk-compose，AGPL-3.0）：
 * 本文件按新版首页渲染图把「广场话题」横滑行改为胶囊标签样式（小图标 + 文字），
 * 行首保留板块标题。IconMiniScrollCardItem 保留原实现供其他页面复用。
 * 原作者版权与许可见 LICENSE。
 */
package com.example.c001apk.compose.ui.component.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.example.c001apk.compose.logic.model.HomeFeedResponse
import com.example.c001apk.compose.ui.component.CoilLoader
import com.example.c001apk.compose.ui.component.rememberHapticClick
import com.example.c001apk.compose.ui.theme.cardBg

/**
 * Created by bggRGjQaUbCoE on 2024/6/6
 */
@Composable
fun IconMiniScrollCard(
    modifier: Modifier = Modifier,
    data: HomeFeedResponse.Data,
    onOpenLink: (String, String?) -> Unit,
) {

    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {

        if (!data.title.isNullOrEmpty()) {
            item(key = "title") {
                Text(
                    text = data.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(end = 2.dp),
                )
            }
        }

        data.entities?.forEach {
            item(key = it.id) {
                TopicChip(
                    logoUrl = it.logo.orEmpty(),
                    linkUrl = it.url.orEmpty(),
                    titleText = it.title.orEmpty(),
                    onOpenLink = onOpenLink,
                )
            }
        }

    }

}

/**
 * 话题胶囊标签：浅色描边圆角，内部为可选小图标 + 标题。
 */
@Composable
private fun TopicChip(
    modifier: Modifier = Modifier,
    logoUrl: String,
    linkUrl: String,
    titleText: String,
    onOpenLink: (String, String?) -> Unit,
) {

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(percent = 50))
            .background(MaterialTheme.colorScheme.surface)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(percent = 50),
            )
            .clickable(
                onClick = rememberHapticClick { onOpenLink(linkUrl, titleText) },
            )
            .padding(start = 8.dp, end = 11.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        if (logoUrl.isNotEmpty()) {
            CoilLoader(
                url = logoUrl,
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape),
            )
        }
        Text(
            text = titleText,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }

}

@Composable
fun IconMiniScrollCardItem(
    modifier: Modifier = Modifier,
    isFeedContent: Boolean,
    logoUrl: String,
    linkUrl: String,
    titleText: String,
    onOpenLink: (String, String?) -> Unit,
    isGridCard: Boolean = false,
) {

    ConstraintLayout(
        modifier = modifier
            .clip(if (isGridCard) RectangleShape else RoundedCornerShape(8.dp))
            .background(
                if (isFeedContent) cardBg()
                else MaterialTheme.colorScheme.surface
            )
            .clickable {
                onOpenLink(linkUrl, titleText)
            }
            .padding(start = if (isGridCard) 10.dp else 5.dp, end = 5.dp)
            .padding(vertical = 5.dp)
    ) {
        val (logo, title) = createRefs()

        CoilLoader(
            url = logoUrl,
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .aspectRatio(1f)
                .constrainAs(logo) {
                    start.linkTo(parent.start)
                    top.linkTo(title.top)
                    bottom.linkTo(title.bottom)
                    height = Dimension.fillToConstraints
                }
        )

        Text(
            text = titleText,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 5.dp)
                .constrainAs(title) {
                    start.linkTo(logo.end)
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    if (isGridCard) {
                        end.linkTo(parent.end)
                        width = Dimension.fillToConstraints
                    }
                },
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }

}

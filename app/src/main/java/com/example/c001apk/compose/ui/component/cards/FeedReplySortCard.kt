/*
 * 修改声明（UI 优化版，基于 frisk1127/c001apk-compose，AGPL-3.0）：
 * 本文件将回复排序由下划线文字 Tab 改为全圆角分段控件
 * （浅紫底容器 + 白底投影选中格），与全局胶囊设计语言统一。
 * 原作者版权与许可见 LICENSE。
 */
package com.example.c001apk.compose.ui.component.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.c001apk.compose.ui.component.rememberHapticClick

/**
 * Created by bggRGjQaUbCoE on 2024/6/12
 */
@Composable
fun FeedReplySortCard(
    modifier: Modifier = Modifier,
    replyCount: String,
    selected: Int = 0,
    updateSortReply: (Int) -> Unit,
) {

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface),
    ) {
        HorizontalDivider()

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {

            Text(
                text = "共 $replyCount 回复",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleSmall.copy(fontSize = 13.sp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                    .padding(3.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                listOf("默认", "最新", "热门", "楼主").forEachIndexed { index, title ->
                    FeedReplySortSegmentItem(
                        title = title,
                        isSelected = selected == index,
                        updateSortReply = {
                            updateSortReply(index)
                        }
                    )
                }
            }

        }

        HorizontalDivider()
    }

}

@Composable
private fun FeedReplySortSegmentItem(
    title: String,
    isSelected: Boolean,
    updateSortReply: () -> Unit,
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall.copy(
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
        ),
        color = if (isSelected) MaterialTheme.colorScheme.onSurface
        else MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .shadow(
                elevation = if (isSelected) 2.dp else 0.dp,
                shape = RoundedCornerShape(50),
            )
            .background(
                if (isSelected) MaterialTheme.colorScheme.surface
                else Color.Transparent
            )
            .clickable(onClick = rememberHapticClick { updateSortReply() })
            .padding(horizontal = 12.dp, vertical = 4.dp),
    )
}

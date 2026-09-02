/*
 * 修改声明（UI 优化版，基于 frisk1127/c001apk-compose，AGPL-3.0）：
 * 本文件在原版基础上统一字阶样式（MaterialTheme.typography）。
 * 原作者版权与许可见 LICENSE。
 */
package com.example.c001apk.compose.ui.component

import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import com.example.c001apk.compose.util.noRippleClickable

/**
 * Created by bggRGjQaUbCoE on 2024/6/9
 */
@Composable
fun IconText(
    modifier: Modifier = Modifier,
    imageVector: ImageVector,
    title: String,
    textSize: Float = 14f,
    onClick: (() -> Unit)? = null,
    isLike: Boolean = false,
) {

    val hapticClick = rememberHapticClick {
        onClick?.invoke()
    }
    val color = if (isLike) MaterialTheme.colorScheme.primary
    else MaterialTheme.colorScheme.onSurfaceVariant

    val id = "0"
    val text1 = buildAnnotatedString {
        if (title.isNotEmpty()) appendInlineContent(id, "[icon]")
        append(title)
    }

    val inlineContent = if (title.isNotEmpty()) mapOf(
        Pair(
            id,
            InlineTextContent(
                Placeholder(
                    width = textSize.sp,
                    height = textSize.sp,
                    placeholderVerticalAlign = PlaceholderVerticalAlign.TextCenter
                )
            ) {
                Icon(imageVector, null, tint = color)
            }
        )
    ) else mapOf()

    Text(
        inlineContent = inlineContent,
        text = text1,
        lineHeight = textSize.sp,
        fontSize = textSize.sp,
        color = color,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        style = LocalTextStyle.current.copy(fontFeatureSettings = "tnum"),
        modifier = run {
            val tmp = if (onClick == null) modifier
            else modifier
                .noRippleClickable {
                    hapticClick()
                }
            tmp
        }
    )

}

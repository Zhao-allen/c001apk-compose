/*
 * 修改声明（UI 优化版，基于 frisk1127/c001apk-compose，AGPL-3.0）：
 * 本文件将页面 M3 TopAppBar（64dp）替换为统一紧凑顶栏
 * CompactTopBar（48dp），实现全应用顶栏高度统一。原作者版权与许可见 LICENSE。
 */
package com.example.c001apk.compose.ui.event

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.outlined.CardGiftcard
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.SupervisorAccount
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import com.example.c001apk.compose.ui.component.CompactTopBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.c001apk.compose.logic.model.EventDetailResponse
import com.example.c001apk.compose.logic.state.LoadingState
import com.example.c001apk.compose.ui.component.BackButton
import com.example.c001apk.compose.ui.component.CoilLoader
import com.example.c001apk.compose.ui.component.cards.LoadingCard
import com.example.c001apk.compose.ui.component.rememberHapticClick
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val EVENT_RULE_URL = "/page?url=V8_GOODS_ZHONGCE_README"
private const val EVENT_RULE_TITLE = "酷品-众测规则"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventScreen(
    id: String,
    onBackClick: () -> Unit,
    onViewUser: (String) -> Unit,
    onOpenTab: (String, String) -> Unit,
    onRegister: (String) -> Unit,
) {
    val viewModel =
        hiltViewModel<EventViewModel, EventViewModel.ViewModelFactory>(key = id) { factory ->
            factory.create(id)
        }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CompactTopBar(
                windowInsets = WindowInsets.systemBars
                    .only(WindowInsetsSides.Start + WindowInsetsSides.Top),
                navigationIcon = { BackButton(onBackClick) },
                title = { Text("酷安众测") },
            )
        },
    ) { paddingValues ->
        when (val state = viewModel.eventState) {
            LoadingState.Loading, LoadingState.Empty, is LoadingState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = paddingValues.calculateTopPadding()),
                ) {
                    LoadingCard(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(horizontal = 16.dp),
                        state = state,
                        onClick = if (state is LoadingState.Loading) null else viewModel::fetchEvent,
                    )
                }
            }

            is LoadingState.Success -> EventContent(
                event = state.response,
                paddingValues = paddingValues,
                onViewUser = onViewUser,
                onOpenTab = onOpenTab,
                onRegister = onRegister,
            )
        }
    }
}

@Composable
private fun EventContent(
    event: EventDetailResponse.Data,
    paddingValues: PaddingValues,
    onViewUser: (String) -> Unit,
    onOpenTab: (String, String) -> Unit,
    onRegister: (String) -> Unit,
) {
    val now = System.currentTimeMillis() / 1000
    val registrationOpen = event.actionUrl?.isNotBlank() == true &&
            event.stageStatus != 2 && event.stageStatus != 3 &&
            (event.registrationStart == null || now >= event.registrationStart) &&
            (event.registrationEnd == null || now <= event.registrationEnd)
    val statusText = when {
        event.stageStatus == 3 || (event.eventEnd != null && now > event.eventEnd) -> "活动已结束"
        event.registrationStart != null && now < event.registrationStart -> "报名未开始"
        event.stageStatus == 2 -> "活动进行中"
        event.registrationEnd != null && now > event.registrationEnd -> "报名已结束"
        registrationOpen -> "立即报名"
        else -> "暂不可报名"
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            top = paddingValues.calculateTopPadding(),
            bottom = paddingValues.calculateBottomPadding() + 16.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(key = "header") {
            Column {
                CoilLoader(
                    url = event.logo,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1080f / 608f),
                )
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = event.title.orEmpty(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Schedule,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(20.dp),
                        )
                        Text(
                            text = registrationPeriod(event),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline,
                        )
                    }
                    Button(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        enabled = registrationOpen,
                        onClick = rememberHapticClick {
                            event.actionUrl?.let(onRegister)
                        },
                        colors = if (registrationOpen) ButtonDefaults.buttonColors()
                        else ButtonDefaults.buttonColors(
                            disabledContainerColor = MaterialTheme.colorScheme.errorContainer,
                            disabledContentColor = MaterialTheme.colorScheme.onErrorContainer,
                        ),
                    ) {
                        Text(statusText)
                    }
                    HorizontalDivider()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                onClick = rememberHapticClick {
                                    onOpenTab(EVENT_RULE_URL, EVENT_RULE_TITLE)
                                },
                            )
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.HelpOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(20.dp),
                        )
                        Text(
                            text = "众测须知",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.outline,
                        )
                    }
                }
            }
        }

        event.noticeRule?.takeIf { it.isNotBlank() }?.let { notice ->
            item(key = "notice") {
                EventSection(title = "报名须知", icon = Icons.Outlined.Info) {
                    Text(
                        text = notice.trim(),
                        style = MaterialTheme.typography.bodyLarge,
                        lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.25f,
                    )
                }
            }
        }

        event.sponsorUser?.takeIf { it.isNotEmpty() }?.let { sponsors ->
            item(key = "sponsors") {
                EventSection(title = "主办方", icon = Icons.Outlined.SupervisorAccount) {
                    sponsors.forEachIndexed { index, sponsor ->
                        SponsorRow(sponsor = sponsor, onViewUser = onViewUser)
                        if (index != sponsors.lastIndex) HorizontalDivider()
                    }
                }
            }
        }

        event.sponsorPrize?.takeIf { it.isNotEmpty() }?.let { prizes ->
            item(key = "prizes") {
                EventSection(title = "活动奖品", icon = Icons.Outlined.CardGiftcard) {
                    prizes.forEachIndexed { index, prize ->
                        PrizeRow(prize)
                        if (index != prizes.lastIndex) HorizontalDivider()
                    }
                }
            }
        }

        event.tabList?.takeIf { it.isNotEmpty() }?.let { tabs ->
            item(key = "tabs") {
                EventSection(title = "相关内容") {
                    tabs.forEachIndexed { index, tab ->
                        val title = tab.title.orEmpty()
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(
                                    enabled = !tab.url.isNullOrBlank(),
                                    onClick = rememberHapticClick {
                                        tab.url?.let { onOpenTab(it, title) }
                                    },
                                )
                                .padding(vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.weight(1f),
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.outline,
                            )
                        }
                        if (index != tabs.lastIndex) HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Composable
private fun EventSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                icon?.let {
                    Icon(
                        imageVector = it,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(21.dp),
                    )
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Spacer(Modifier.height(10.dp))
            content()
        }
    }
}

@Composable
private fun SponsorRow(
    sponsor: EventDetailResponse.SponsorUser,
    onViewUser: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                enabled = !sponsor.uid.isNullOrBlank(),
                onClick = rememberHapticClick { sponsor.uid?.let(onViewUser) },
            )
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        CoilLoader(
            url = sponsor.userAvatar,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape),
        )
        Text(
            text = sponsor.displayUsername.orEmpty(),
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outline,
        )
    }
}

@Composable
private fun PrizeRow(prize: EventDetailResponse.SponsorPrize) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        CoilLoader(
            url = prize.logo,
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(6.dp)),
        )
        Text(
            text = prize.title.orEmpty(),
            style = MaterialTheme.typography.titleMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
    }
}

private fun registrationPeriod(event: EventDetailResponse.Data): String {
    val start = event.registrationStart?.formatEventDate()
    val end = event.registrationEnd?.formatEventDate()
    return if (start != null && end != null) "报名时间：$start 至 $end" else "报名时间待定"
}

private fun Long.formatEventDate(): String =
    SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(Date(this * 1000))

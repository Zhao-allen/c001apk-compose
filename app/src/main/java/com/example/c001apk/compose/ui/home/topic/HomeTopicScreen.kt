/*
 * 修改声明（UI 优化版，基于 frisk1127/c001apk-compose，AGPL-3.0）：
 * 新增 bottomPadding 参数，列表与轮播底部为悬浮胶囊导航预留间隙；
 * 左侧分类栏改为渲染图样式（圆角胶囊高亮、选中紫色加粗，去掉左侧竖条），栏宽调整为 0.25。
 * 原作者版权与许可见 LICENSE。
 */
package com.example.c001apk.compose.ui.home.topic

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.c001apk.compose.logic.model.HomeFeedResponse
import com.example.c001apk.compose.logic.model.TopicBean
import com.example.c001apk.compose.logic.state.LoadingState
import com.example.c001apk.compose.ui.carousel.CarouselContentScreen
import com.example.c001apk.compose.ui.component.cards.LoadingCard
import com.example.c001apk.compose.ui.home.TabType
import com.example.c001apk.compose.ui.theme.cardBg
import kotlinx.coroutines.launch

/**
 * Created by bggRGjQaUbCoE on 2024/6/11
 */
@Composable
fun HomeTopicScreen(
    type: TabType,
    onViewUser: (String) -> Unit,
    onViewFeed: (String, Boolean) -> Unit,
    onOpenLink: (String, String?) -> Unit,
    onCopyText: (String?) -> Unit,
    bottomPadding: Dp = 0.dp,
) {

    val viewModel =
        hiltViewModel<HomeTopicViewModel, HomeTopicViewModel.ViewModelFactory>(key = type.name) { factory ->
            factory.create(
                url = when (type) {
                    TabType.TOPIC -> "/v6/page/dataList?url=V11_VERTICAL_TOPIC&title=话题&page=1"
                    TabType.PRODUCT -> "/v6/product/categoryList"
                    else -> throw IllegalArgumentException("invalid type: $type")
                }
            )
        }
    val scope = rememberCoroutineScope()
    val currentIndex = when (type) {
        TabType.TOPIC -> 1
        TabType.PRODUCT -> 0
        else -> throw IllegalArgumentException("invalid type: $type")
    }
    val listState = rememberLazyListState()
    var pageState: PagerState
    var tabList: List<TopicBean>?

    Box(modifier = Modifier.fillMaxSize()) {
        when (viewModel.loadingState) {
            LoadingState.Loading, LoadingState.Empty, is LoadingState.Error -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    LoadingCard(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(horizontal = 10.dp),
                        state = viewModel.loadingState,
                        onClick = if (viewModel.loadingState is LoadingState.Loading) null
                        else viewModel::loadMore
                    )
                }
            }

            is LoadingState.Success -> {

                tabList = when (type) {
                    TabType.TOPIC -> (viewModel.loadingState as LoadingState.Success<List<HomeFeedResponse.Data>>)
                        .response.getOrNull(0)?.entities?.map {
                            TopicBean(it.url.orEmpty(), it.title.orEmpty())
                        }

                    TabType.PRODUCT -> (viewModel.loadingState as LoadingState.Success<List<HomeFeedResponse.Data>>)
                        .response.map {
                            TopicBean(it.url.orEmpty(), it.title.orEmpty())
                        }

                    else -> throw IllegalArgumentException("invalid type: $type")
                }

                tabList?.let {
                    pageState = rememberPagerState(
                        initialPage = currentIndex,
                        pageCount = {
                            it.size
                        }
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(cardBg())
                    ) {

                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxHeight()
                                .weight(0.25f),
                            contentPadding = PaddingValues(
                                start = 6.dp,
                                end = 6.dp,
                                top = 4.dp,
                                bottom = bottomPadding,
                            ),
                        ) {
                            itemsIndexed(it, key = { _, item -> item.title }) { index, item ->
                                val selected = index == pageState.currentPage
                                Text(
                                    text = item.title,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 3.dp)
                                        .clip(RoundedCornerShape(percent = 50))
                                        .background(
                                            if (selected)
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                            else Color.Transparent,
                                        )
                                        .clickable {
                                            scope.launch {
                                                pageState.scrollToPage(index)
                                            }
                                        }
                                        .padding(vertical = 9.dp),
                                    textAlign = TextAlign.Center,
                                    fontSize = 13.sp,
                                    fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
                                    maxLines = 1,
                                    color = if (selected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurface,
                                )
                            }
                        }

                        VerticalPager(
                            state = pageState,
                            modifier = Modifier
                                .fillMaxHeight()
                                .weight(0.75f),
                            userScrollEnabled = false,
                        ) { index ->
                            CarouselContentScreen(
                                url = it[index].url,
                                title = it[index].title,
                                paddingValues = PaddingValues(bottom = bottomPadding),
                                refreshState = null,
                                resetRefreshState = {},
                                onViewUser = onViewUser,
                                onViewFeed = onViewFeed,
                                onOpenLink = onOpenLink,
                                onCopyText = onCopyText,
                                isHomeFeed = true,
                            )
                        }

                    }
                }
            }
        }
    }

}
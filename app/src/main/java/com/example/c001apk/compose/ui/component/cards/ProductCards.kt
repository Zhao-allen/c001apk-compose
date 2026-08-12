package com.example.c001apk.compose.ui.component.cards

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.c001apk.compose.logic.model.HomeFeedResponse
import com.example.c001apk.compose.ui.component.CoilLoader
import com.example.c001apk.compose.ui.theme.cardBg

@Composable
fun ProductConfigListCard(
    data: HomeFeedResponse.Data,
    configRows: List<HomeFeedResponse.ProductConfig>,
    selectedCompareIds: Set<String>,
    onViewConfig: (HomeFeedResponse.ProductConfig) -> Unit,
    onToggleCompare: (HomeFeedResponse.ProductConfig) -> Unit,
    onShowComparison: () -> Unit,
    onShowGlobalComparison: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = cardBg(),
    ) {
        Column(modifier = Modifier.padding(vertical = 12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 8.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = data.title.orEmpty(),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                TextButton(onClick = onShowComparison) {
                    Text("配置对比")
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }

            configRows.forEachIndexed { index, config ->
                if (index > 0) {
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                }
                ProductConfigRow(
                    config = config,
                    selectedForCompare = config.id in selectedCompareIds,
                    onViewConfig = onViewConfig,
                    onToggleCompare = onToggleCompare,
                )
            }
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            TextButton(
                onClick = onShowGlobalComparison,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            ) {
                Text("跨机型对比（${selectedCompareIds.size}/10）")
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

@Composable
private fun ProductConfigRow(
    config: HomeFeedResponse.ProductConfig,
    selectedForCompare: Boolean,
    onViewConfig: (HomeFeedResponse.ProductConfig) -> Unit,
    onToggleCompare: (HomeFeedResponse.ProductConfig) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = config.title.orEmpty(),
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = config.price.asPrice(),
                style = MaterialTheme.typography.bodyLarge,
                color = if (config.price.isPositivePrice()) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            if (!config.id.isNullOrBlank()) {
                OutlinedButton(
                    modifier = Modifier.height(36.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp),
                    onClick = { onViewConfig(config) },
                ) {
                    Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("参数", style = MaterialTheme.typography.labelMedium)
                }
            }
            if (!config.id.isNullOrBlank()) {
                OutlinedButton(
                    modifier = Modifier.height(36.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp),
                    onClick = { onToggleCompare(config) },
                ) {
                    Icon(
                        if (selectedForCompare) Icons.Default.Check else Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        if (selectedForCompare) "已添加" else "对比",
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }
        }
    }
}

@Composable
fun ProductListCard(
    data: HomeFeedResponse.Data,
    onOpenLink: (String, String?) -> Unit,
    selectedCompareIds: Set<String>,
    onToggleCompare: (HomeFeedResponse.Entities) -> Unit,
    modifier: Modifier = Modifier,
) {
    val products = data.entities.orEmpty()
    if (products.isEmpty()) return

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = cardBg(),
    ) {
        Column(modifier = Modifier.padding(vertical = 12.dp)) {
            TitleCard(
                modifier = Modifier.padding(bottom = 8.dp),
                url = data.url.orEmpty(),
                title = data.title.orEmpty(),
                onOpenLink = onOpenLink,
            )
            products.forEachIndexed { index, product ->
                if (index > 0) {
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                }
                ProductListRow(
                    product = product,
                    selectedForCompare = product.configId in selectedCompareIds,
                    onOpenLink = onOpenLink,
                    onToggleCompare = onToggleCompare,
                )
            }
        }
    }
}

@Composable
private fun ProductListRow(
    product: HomeFeedResponse.Entities,
    selectedForCompare: Boolean,
    onOpenLink: (String, String?) -> Unit,
    onToggleCompare: (HomeFeedResponse.Entities) -> Unit,
) {
    val description = product.configName
        ?.takeIf { it.isNotBlank() }
        ?: product.productSpecs.orEmpty().filter { it.isNotBlank() }.take(2).joinToString(" · ")
    val price = productPrice(product)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .clickable(enabled = !product.url.isNullOrBlank()) {
                onOpenLink(product.url.orEmpty(), product.title)
            }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CoilLoader(
            url = product.logo,
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(8.dp)),
        )
        Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
            Text(
                text = product.title.orEmpty(),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (description.isNotBlank()) {
                Spacer(Modifier.height(3.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.height(3.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = price,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (price == "暂无价格") {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.error
                    },
                )
                if (!product.configId.isNullOrBlank()) {
                    OutlinedButton(
                        onClick = { onToggleCompare(product) },
                        modifier = Modifier.height(32.dp),
                        contentPadding = PaddingValues(horizontal = 9.dp),
                    ) {
                        Icon(
                            if (selectedForCompare) Icons.Default.Check else Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp),
                        )
                        Spacer(Modifier.width(3.dp))
                        Text(if (selectedForCompare) "已添加" else "对比")
                    }
                }
            }
        }
    }
}

private fun productPrice(product: HomeFeedResponse.Entities): String {
    val min = product.priceMin.takeIf { it.isPositivePrice() }
    val max = product.priceMax.takeIf { it.isPositivePrice() }
    if (min == null && max == null) return "暂无价格"

    val currency = product.priceCurrency?.takeIf { it.isNotBlank() } ?: "¥"
    return when {
        min == null -> "$currency$max"
        max == null || min == max -> "$currency$min"
        else -> "$currency$min - $currency$max"
    }
}

private fun String?.asPrice(): String =
    takeIf { it.isPositivePrice() }?.let { "¥$it" } ?: "暂无价格"

private fun String?.isPositivePrice(): Boolean =
    !isNullOrBlank() && (toBigDecimalOrNull()?.signum() ?: 0) > 0

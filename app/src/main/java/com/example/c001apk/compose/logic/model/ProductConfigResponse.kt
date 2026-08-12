package com.example.c001apk.compose.logic.model

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

data class ProductConfigResponse(
    val status: Int?,
    val message: String?,
    val title: String?,
    @SerializedName("dataRows")
    val dataRows: Map<String, List<ProductConfigRow>>?,
)

data class ProductConfigData(
    val title: String?,
    val dataRows: Map<String, List<ProductConfigRow>>,
)

data class ProductConfigRow(
    val type: String?,
    @SerializedName("keyName")
    val keyName: String?,
    val depth: Int?,
    val data: ProductConfigItemData?,
)

data class ProductConfigItemData(
    val title: String?,
    val unit: String?,
    @SerializedName("show_value")
    val showValue: JsonElement?,
    @SerializedName("addition_value")
    val additionValue: JsonElement?,
)

data class ProductSpecSection(
    val title: String,
    val items: List<ProductSpecItem>,
)

data class ProductSpecItem(
    val name: String,
    val value: String,
    val group: String? = null,
)

fun ProductConfigData.specSections(): List<ProductSpecSection> = dataRows.mapNotNull { (title, rows) ->
    val groupPath = mutableListOf<String>()
    val items = buildList {
        rows.forEach { row ->
            when (row.type) {
                "groupData" -> {
                    val groupName = row.keyName?.trim().orEmpty()
                    if (groupName.isNotEmpty()) {
                        val depth = (row.depth ?: 1).coerceAtLeast(1)
                        while (groupPath.size >= depth) groupPath.removeAt(groupPath.lastIndex)
                        while (groupPath.size < depth - 1) groupPath += ""
                        groupPath += groupName
                    }
                }

                "itemData" -> row.data?.toSpecItem(
                    group = groupPath.filter { it.isNotEmpty() }.joinToString(" / ").ifBlank { null }
                )?.let(::add)
            }
        }
    }
    items.takeIf { it.isNotEmpty() }?.let { ProductSpecSection(title, it) }
}

private fun ProductConfigItemData.toSpecItem(group: String?): ProductSpecItem? {
    val name = title?.trim().orEmpty()
    val shown = showValue.displayValue() ?: return null
    if (name.isEmpty()) return null

    val mainValue = if (shown == "-" || unit.isNullOrBlank()) shown else shown + unit
    val addition = additionValue.displayValue()?.takeIf { it != "-" && it != mainValue }
    return ProductSpecItem(
        name = name,
        value = listOfNotNull(mainValue, addition).joinToString("\n"),
        group = group,
    )
}

private fun JsonElement?.displayValue(): String? {
    if (this == null || isJsonNull) return null
    val text = when {
        isJsonPrimitive -> asJsonPrimitive.asString
        isJsonArray -> asJsonArray.mapNotNull { it.displayValue() }.joinToString(" / ")
        isJsonObject -> asJsonObject.entrySet().mapNotNull { (name, value) ->
            value.displayValue()?.let { "$name: $it" }
        }.joinToString(" / ")
        else -> toString()
    }
    return text.trim().takeIf { it.isNotEmpty() }
}

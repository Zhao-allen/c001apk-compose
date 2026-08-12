package com.example.c001apk.compose.logic.model

data class ProductComparisonSelection(
    val productTitle: String,
    val productLogo: String?,
    val config: HomeFeedResponse.ProductConfig,
)

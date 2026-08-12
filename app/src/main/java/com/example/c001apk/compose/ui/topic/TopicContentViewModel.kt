package com.example.c001apk.compose.ui.topic

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.example.c001apk.compose.constant.Constants.EMPTY_STRING
import com.example.c001apk.compose.logic.model.HomeFeedResponse
import com.example.c001apk.compose.logic.model.ProductConfigData
import com.example.c001apk.compose.logic.model.ProductComparisonSelection
import com.example.c001apk.compose.logic.model.ProductSpecSection
import com.example.c001apk.compose.logic.model.specSections
import com.example.c001apk.compose.logic.repository.BlackListRepo
import com.example.c001apk.compose.logic.repository.NetworkRepo
import com.example.c001apk.compose.logic.repository.ProductComparisonRepo
import com.example.c001apk.compose.logic.repository.ToggleResult
import com.example.c001apk.compose.ui.base.BaseViewModel
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Created by bggRGjQaUbCoE on 2024/6/9
 */
@HiltViewModel(assistedFactory = TopicContentViewModel.ViewModelFactory::class)
class TopicContentViewModel @AssistedInject constructor(
    @Assisted("url") var url: String,
    @Assisted("title") var title: String,
    networkRepo: NetworkRepo,
    blackListRepo: BlackListRepo,
    private val productComparisonRepo: ProductComparisonRepo,
) : BaseViewModel(networkRepo, blackListRepo) {

    @AssistedFactory
    interface ViewModelFactory {
        fun create(
            @Assisted("url") url: String,
            @Assisted("title") title: String,
        ): TopicContentViewModel
    }

    var sortType = ProductSortType.REPLY

    var productConfigSheetState by mutableStateOf<ProductConfigSheetState>(ProductConfigSheetState.Hidden)
        private set

    private val productConfigCache = mutableMapOf<String, ProductConfigData>()

    val productComparisonSelections = productComparisonRepo.selections

    init {
        fetchData()
    }

    override suspend fun customFetchData() =
        networkRepo.getDataList(url, title, EMPTY_STRING, lastItem, page)

    override fun isAdditionalItemSupported(item: HomeFeedResponse.Data): Boolean =
        title == "参数" && item.entityTemplate in PRODUCT_CARD_TEMPLATES

    override fun handleLoadMore(response: List<HomeFeedResponse.Data>): List<HomeFeedResponse.Data> {
        return response.distinctBy { it.entityId }
    }

    fun showProductConfig(config: HomeFeedResponse.ProductConfig) {
        val id = config.id ?: return
        productConfigSheetState = ProductConfigSheetState.Loading(config.title.orEmpty())
        viewModelScope.launch {
            loadProductConfig(id).fold(
                onSuccess = { data ->
                    productConfigSheetState = ProductConfigSheetState.Details(
                        title = config.title.orEmpty().ifBlank { data.title.orEmpty() },
                        sections = data.specSections(),
                    )
                },
                onFailure = { error ->
                    productConfigSheetState = ProductConfigSheetState.Error(
                        error.message ?: "参数加载失败"
                    )
                },
            )
        }
    }

    fun toggleGlobalProductComparison(
        config: HomeFeedResponse.ProductConfig,
        productTitle: String,
        productLogo: String?,
    ) {
        when (
            productComparisonRepo.toggle(
                ProductComparisonSelection(productTitle, productLogo, config)
            )
        ) {
            ToggleResult.LIMIT_REACHED -> toastText = "最多选择 ${ProductComparisonRepo.MAX_SELECTIONS} 款配置"
            ToggleResult.INVALID -> toastText = "该配置暂不支持对比"
            ToggleResult.ADDED, ToggleResult.REMOVED -> Unit
        }
    }

    fun toggleGlobalProductComparison(product: HomeFeedResponse.Entities) {
        val configId = product.configId
        if (configId.isNullOrBlank()) {
            toastText = "请进入机型参数页选择具体版本"
            return
        }
        toggleGlobalProductComparison(
            config = HomeFeedResponse.ProductConfig(
                id = configId,
                productId = product.productId ?: product.id,
                title = product.configName.orEmpty().ifBlank { "默认配置" },
                price = product.priceMin?.takeIf { it.toBigDecimalOrNull()?.signum() == 1 }
                    ?: product.priceMax,
                url = product.url,
                isAddCompare = product.isAddCompare,
            ),
            productTitle = product.title.orEmpty(),
            productLogo = product.logo,
        )
    }

    fun showCurrentProductConfigComparison(
        configRows: List<HomeFeedResponse.ProductConfig>,
        productTitle: String,
        productLogo: String?,
    ) {
        val selections = configRows
            .filter { !it.id.isNullOrBlank() }
            .map { ProductComparisonSelection(productTitle, productLogo, it) }
        loadComparison(
            title = "${productTitle}对比",
            mode = ProductComparisonMode.CURRENT_PRODUCT,
            selections = selections,
        )
    }

    fun showGlobalProductComparison() {
        loadComparison(
            title = "配置对比",
            mode = ProductComparisonMode.CROSS_PRODUCT,
            selections = productComparisonSelections.value,
        )
    }

    fun removeGlobalProductComparison(configId: String) {
        productComparisonRepo.remove(configId)
        val comparison = productConfigSheetState as? ProductConfigSheetState.Comparison ?: return
        if (comparison.mode == ProductComparisonMode.CROSS_PRODUCT) {
            productConfigSheetState = comparison.copy(
                columns = comparison.columns.filterNot { it.selection.config.id == configId }
            )
        }
    }

    private fun loadComparison(
        title: String,
        mode: ProductComparisonMode,
        selections: List<ProductComparisonSelection>,
    ) {
        if (selections.isEmpty()) {
            productConfigSheetState = ProductConfigSheetState.Comparison(title, mode, emptyList())
            return
        }
        productConfigSheetState = ProductConfigSheetState.Loading("配置对比")
        viewModelScope.launch {
            val result = runCatching {
                coroutineScope {
                    selections.map { selection ->
                        async {
                            val configId = selection.config.id ?: error("配置 ID 为空")
                            val data = loadProductConfig(configId).getOrThrow()
                            ProductComparisonColumn(
                                selection = selection,
                                sections = data.specSections(),
                            )
                        }
                    }.map { it.await() }
                }
            }
            result.fold(
                onSuccess = { columns ->
                    productConfigSheetState = ProductConfigSheetState.Comparison(
                        title = title,
                        mode = mode,
                        columns = columns,
                    )
                },
                onFailure = { error ->
                    productConfigSheetState = ProductConfigSheetState.Error(
                        error.message ?: "对比加载失败"
                    )
                },
            )
        }
    }

    fun dismissProductConfigSheet() {
        productConfigSheetState = ProductConfigSheetState.Hidden
    }

    private suspend fun loadProductConfig(id: String): Result<ProductConfigData> {
        productConfigCache[id]?.let { return Result.success(it) }
        return networkRepo.getProductConfig(id).first().onSuccess {
            productConfigCache[id] = it
        }
    }

    private companion object {
        val PRODUCT_CARD_TEMPLATES = setOf("productConfigList", "listCard")
    }

}

sealed interface ProductConfigSheetState {
    data object Hidden : ProductConfigSheetState
    data class Loading(val title: String) : ProductConfigSheetState
    data class Details(
        val title: String,
        val sections: List<ProductSpecSection>,
    ) : ProductConfigSheetState
    data class Comparison(
        val title: String,
        val mode: ProductComparisonMode,
        val columns: List<ProductComparisonColumn>,
    ) : ProductConfigSheetState
    data class Error(val message: String) : ProductConfigSheetState
}

enum class ProductComparisonMode {
    CURRENT_PRODUCT,
    CROSS_PRODUCT,
}

data class ProductComparisonColumn(
    val selection: ProductComparisonSelection,
    val sections: List<ProductSpecSection>,
)

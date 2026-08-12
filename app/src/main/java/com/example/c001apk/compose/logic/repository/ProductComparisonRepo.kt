package com.example.c001apk.compose.logic.repository

import com.example.c001apk.compose.logic.model.ProductComparisonSelection
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductComparisonRepo @Inject constructor() {
    private val _selections = MutableStateFlow<List<ProductComparisonSelection>>(emptyList())
    val selections = _selections.asStateFlow()

    fun toggle(selection: ProductComparisonSelection): ToggleResult {
        val configId = selection.config.id ?: return ToggleResult.INVALID
        val current = _selections.value
        if (current.any { it.config.id == configId }) {
            _selections.value = current.filterNot { it.config.id == configId }
            return ToggleResult.REMOVED
        }
        if (current.size >= MAX_SELECTIONS) return ToggleResult.LIMIT_REACHED
        _selections.value = current + selection
        return ToggleResult.ADDED
    }

    fun remove(configId: String) {
        _selections.value = _selections.value.filterNot { it.config.id == configId }
    }

    companion object {
        const val MAX_SELECTIONS = 10
    }
}

enum class ToggleResult {
    ADDED,
    REMOVED,
    LIMIT_REACHED,
    INVALID,
}

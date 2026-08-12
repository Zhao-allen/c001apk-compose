package com.example.c001apk.compose.logic.repository

import com.example.c001apk.compose.logic.model.HomeFeedResponse
import com.example.c001apk.compose.logic.model.ProductComparisonSelection
import org.junit.Assert.assertEquals
import org.junit.Test

class ProductComparisonRepoTest {
    @Test
    fun acceptsTenSelectionsAndRejectsTheEleventh() {
        val repo = ProductComparisonRepo()

        repeat(ProductComparisonRepo.MAX_SELECTIONS) { index ->
            assertEquals(ToggleResult.ADDED, repo.toggle(selection(index)))
        }

        assertEquals(ToggleResult.LIMIT_REACHED, repo.toggle(selection(10)))
        assertEquals(ProductComparisonRepo.MAX_SELECTIONS, repo.selections.value.size)
    }

    @Test
    fun togglingAnExistingSelectionRemovesIt() {
        val repo = ProductComparisonRepo()
        val selection = selection(1)

        assertEquals(ToggleResult.ADDED, repo.toggle(selection))
        assertEquals(ToggleResult.REMOVED, repo.toggle(selection))
        assertEquals(emptyList<ProductComparisonSelection>(), repo.selections.value)
    }

    private fun selection(index: Int) = ProductComparisonSelection(
        productTitle = "Product $index",
        productLogo = null,
        config = HomeFeedResponse.ProductConfig(
            id = index.toString(),
            productId = "product-$index",
            title = "12GB+256GB",
            price = "9999",
            url = null,
            isAddCompare = 0,
        ),
    )
}

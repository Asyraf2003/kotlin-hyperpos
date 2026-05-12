package id.hyperpos.mobile.application.product

import id.hyperpos.mobile.domain.product.MobileProductSearchRow

sealed class ProductSearchResult {
    data class Success(
        val rows: List<MobileProductSearchRow>,
        val query: String,
        val limit: Int,
    ) : ProductSearchResult()

    data class Failure(val message: String) : ProductSearchResult()
}

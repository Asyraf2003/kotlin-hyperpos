package id.hyperpos.mobile.application.ports

import id.hyperpos.mobile.application.product.ProductSearchResult

interface ProductSearchApiPort {
    fun searchProducts(token: String, query: String): ProductSearchResult
}

package id.hyperpos.mobile.application.product

import id.hyperpos.mobile.application.ports.ProductSearchApiPort
import id.hyperpos.mobile.application.ports.SessionTokenStore

class SearchProductsUseCase(
    private val productSearchApi: ProductSearchApiPort,
    private val tokenStore: SessionTokenStore,
) {
    fun execute(query: String): ProductSearchResult {
        val token = tokenStore.read()
        if (token.isNullOrBlank()) {
            return ProductSearchResult.Failure("Sesi login tidak ditemukan.")
        }

        return productSearchApi.searchProducts(token, query)
    }
}

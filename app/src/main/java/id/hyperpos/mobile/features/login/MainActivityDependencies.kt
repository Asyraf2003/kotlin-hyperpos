package id.hyperpos.mobile.features.login

import androidx.appcompat.app.AppCompatActivity
import id.hyperpos.mobile.adapters.http.MobileApiConfig
import id.hyperpos.mobile.adapters.http.OkHttpAuthApiClient
import id.hyperpos.mobile.adapters.http.OkHttpProductSearchApiClient
import id.hyperpos.mobile.adapters.http.OkHttpSupplierInvoiceApiClient
import id.hyperpos.mobile.adapters.storage.AndroidKeystoreSessionTokenStore
import id.hyperpos.mobile.application.auth.LoginUseCase
import id.hyperpos.mobile.application.auth.LogoutUseCase
import id.hyperpos.mobile.application.procurement.GetSupplierInvoiceDetailUseCase
import id.hyperpos.mobile.application.procurement.ListSupplierInvoicesUseCase
import id.hyperpos.mobile.application.procurement.UploadSupplierInvoicePaymentProofUseCase
import id.hyperpos.mobile.application.product.SearchProductsUseCase
import okhttp3.OkHttpClient

class MainActivityDependencies(activity: AppCompatActivity) {
    private val apiConfig = MobileApiConfig(
        baseUrl = "http://127.0.0.1:8000/api/v1",
    )
    private val httpClient = OkHttpClient()

    val tokenStore = AndroidKeystoreSessionTokenStore(activity)

    private val authApi = OkHttpAuthApiClient(
        config = apiConfig,
        httpClient = httpClient,
    )
    private val supplierInvoiceApi = OkHttpSupplierInvoiceApiClient(
        config = apiConfig,
        httpClient = httpClient,
    )
    private val productSearchApi = OkHttpProductSearchApiClient(
        config = apiConfig,
        httpClient = httpClient,
    )

    val loginUseCase = LoginUseCase(
        authApi = authApi,
        tokenStore = tokenStore,
    )
    val logoutUseCase = LogoutUseCase(
        authApi = authApi,
        tokenStore = tokenStore,
    )
    val searchProductsUseCase = SearchProductsUseCase(
        productSearchApi = productSearchApi,
        tokenStore = tokenStore,
    )
    val listSupplierInvoicesUseCase = ListSupplierInvoicesUseCase(
        supplierInvoiceApi = supplierInvoiceApi,
        tokenStore = tokenStore,
    )
    val getSupplierInvoiceDetailUseCase = GetSupplierInvoiceDetailUseCase(
        supplierInvoiceApi = supplierInvoiceApi,
        tokenStore = tokenStore,
    )
    val uploadSupplierInvoicePaymentProofUseCase = UploadSupplierInvoicePaymentProofUseCase(
        supplierInvoiceApi = supplierInvoiceApi,
        tokenStore = tokenStore,
    )
}

package id.hyperpos.mobile.adapters.http

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import id.hyperpos.mobile.adapters.storage.AndroidKeystoreSessionTokenStore
import id.hyperpos.mobile.application.auth.LoginRequest
import id.hyperpos.mobile.application.auth.LoginResult
import id.hyperpos.mobile.application.auth.LoginUseCase
import id.hyperpos.mobile.application.product.ProductSearchResult
import id.hyperpos.mobile.application.product.SearchProductsUseCase
import okhttp3.OkHttpClient
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OkHttpProductSearchApiClientInstrumentedTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val tokenStore = AndroidKeystoreSessionTokenStore(context)
    private val httpClient = OkHttpClient()
    private val config = MobileApiConfig(baseUrl = "http://127.0.0.1:8000/api/v1")
    private val authApi = OkHttpAuthApiClient(
        config = config,
        httpClient = httpClient,
    )
    private val productSearchApi = OkHttpProductSearchApiClient(
        config = config,
        httpClient = httpClient,
    )

    @After
    fun tearDown() {
        tokenStore.clear()
    }

    @Test
    fun cashierCanSearchProductsUsingStoredToken() {
        tokenStore.clear()

        val login = LoginUseCase(
            authApi = authApi,
            tokenStore = tokenStore,
        ).execute(
            LoginRequest(
                email = "mobile-android-smoke@example.test",
                password = "MobileSmoke123!",
                deviceName = "android-product-search-proof",
            ),
        )

        when (login) {
            is LoginResult.Success -> Unit
            is LoginResult.Failure -> fail("Expected login success for Android product search smoke: ${login.message}")
        }

        val result = SearchProductsUseCase(
            productSearchApi = productSearchApi,
            tokenStore = tokenStore,
        ).execute("ban")

        when (result) {
            is ProductSearchResult.Success -> {
                assertEquals("ban", result.query)
                assertEquals(20, result.limit)
                assertTrue("Expected at least one product row for query ban.", result.rows.isNotEmpty())
                assertTrue(
                    "Expected at least one Ban product label.",
                    result.rows.any { row -> row.label.contains("Ban", ignoreCase = true) },
                )
                assertTrue(
                    "Expected non-negative stock values.",
                    result.rows.all { row -> row.availableStock >= 0 },
                )
                assertTrue(
                    "Expected positive default prices.",
                    result.rows.all { row -> row.defaultUnitPriceRupiah > 0 },
                )
                assertTrue(
                    "Expected minimum price to match current backend floor price.",
                    result.rows.all { row -> row.minimumUnitPriceRupiah == row.defaultUnitPriceRupiah },
                )
            }
            is ProductSearchResult.Failure -> {
                fail("Expected product search success using stored token: ${result.message}")
            }
            is ProductSearchResult.Unauthenticated -> {
                fail("Expected authenticated product search using stored token: ${result.message}")
            }
        }
    }
}

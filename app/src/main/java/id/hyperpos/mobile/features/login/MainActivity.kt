package id.hyperpos.mobile.features.login

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import id.hyperpos.mobile.adapters.http.MobileApiConfig
import id.hyperpos.mobile.adapters.http.OkHttpAuthApiClient
import id.hyperpos.mobile.adapters.http.OkHttpProductSearchApiClient
import id.hyperpos.mobile.adapters.storage.AndroidKeystoreSessionTokenStore
import id.hyperpos.mobile.application.auth.LoginRequest
import id.hyperpos.mobile.application.auth.LoginResult
import id.hyperpos.mobile.application.auth.LoginUseCase
import id.hyperpos.mobile.application.product.ProductSearchResult
import id.hyperpos.mobile.application.product.SearchProductsUseCase
import id.hyperpos.mobile.databinding.ActivityMainBinding
import id.hyperpos.mobile.domain.product.MobileProductSearchRow
import okhttp3.OkHttpClient
import java.text.NumberFormat
import java.util.Locale
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private val apiConfig by lazy {
        MobileApiConfig(baseUrl = "http://127.0.0.1:8000/api/v1")
    }

    private val httpClient by lazy {
        OkHttpClient()
    }

    private val tokenStore by lazy {
        AndroidKeystoreSessionTokenStore(this)
    }

    private val loginUseCase by lazy {
        LoginUseCase(
            authApi = OkHttpAuthApiClient(
                config = apiConfig,
                httpClient = httpClient,
            ),
            tokenStore = tokenStore,
        )
    }

    private val searchProductsUseCase by lazy {
        SearchProductsUseCase(
            productSearchApi = OkHttpProductSearchApiClient(
                config = apiConfig,
                httpClient = httpClient,
            ),
            tokenStore = tokenStore,
        )
    }

    private val rupiahFormat by lazy {
        NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID"))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.productSearchContainer.visibility = View.GONE

        binding.loginButton.setOnClickListener {
            login()
        }

        binding.productSearchButton.setOnClickListener {
            searchProducts()
        }
    }

    private fun login() {
        binding.loginButton.isEnabled = false
        binding.statusText.text = "Login berjalan..."

        val request = LoginRequest(
            email = binding.emailInput.text.toString().trim(),
            password = binding.passwordInput.text.toString(),
            deviceName = binding.deviceNameInput.text.toString().trim(),
        )

        thread {
            val result = loginUseCase.execute(request)

            runOnUiThread {
                binding.loginButton.isEnabled = true
                binding.statusText.text = when (result) {
                    is LoginResult.Success -> {
                        binding.productSearchContainer.visibility = View.VISIBLE
                        "Login berhasil: ${result.session.actor.name} (${result.session.actor.role})"
                    }
                    is LoginResult.Failure -> result.message
                }
            }
        }
    }

    private fun searchProducts() {
        binding.productSearchButton.isEnabled = false
        binding.productSearchStatusText.text = "Mencari produk..."
        binding.productSearchResultsText.text = ""

        val query = binding.productSearchInput.text.toString().trim()

        thread {
            val result = searchProductsUseCase.execute(query)

            runOnUiThread {
                binding.productSearchButton.isEnabled = true

                when (result) {
                    is ProductSearchResult.Success -> {
                        binding.productSearchStatusText.text = "Hasil untuk \"${result.query}\" (${result.rows.size}/${result.limit})"
                        binding.productSearchResultsText.text = renderProductRows(result.rows)
                    }
                    is ProductSearchResult.Failure -> {
                        binding.productSearchStatusText.text = result.message
                        binding.productSearchResultsText.text = ""
                    }
                }
            }
        }
    }

    private fun renderProductRows(rows: List<MobileProductSearchRow>): String {
        if (rows.isEmpty()) {
            return "Tidak ada produk ditemukan."
        }

        return rows.joinToString(separator = "\n\n") { row ->
            listOf(
                row.label,
                "Stok: ${row.availableStock}",
                "Harga jual: Rp ${rupiahFormat.format(row.defaultUnitPriceRupiah)}",
            ).joinToString(separator = "\n")
        }
    }
}

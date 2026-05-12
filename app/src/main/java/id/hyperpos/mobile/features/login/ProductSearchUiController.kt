package id.hyperpos.mobile.features.login

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import id.hyperpos.mobile.R
import id.hyperpos.mobile.application.product.ProductSearchResult
import id.hyperpos.mobile.application.product.SearchProductsUseCase
import id.hyperpos.mobile.databinding.ActivityMainBinding
import kotlin.concurrent.thread

class ProductSearchUiController(
    private val activity: AppCompatActivity,
    private val binding: ActivityMainBinding,
    private val searchUseCase: SearchProductsUseCase,
    private val renderer: MobileUiTextRenderer,
    private val onUnauthenticated: (String) -> Unit,
) {
    fun bind() {
        binding.productSearchButton.setOnClickListener { searchProducts() }
    }

    fun show() {
        binding.productSearchContainer.visibility = View.VISIBLE
    }

    fun hide() {
        binding.productSearchContainer.visibility = View.GONE
    }

    fun reset() {
        hide()
        binding.productSearchButton.isEnabled = true
        binding.productSearchInput.setText("")
        binding.productSearchStatusText.text = activity.getString(R.string.product_search_ready)
        binding.productSearchResultsText.text = ""
    }

    private fun searchProducts() {
        binding.productSearchButton.isEnabled = false
        binding.productSearchStatusText.text = "Mencari produk..."
        binding.productSearchResultsText.text = ""
        val query = binding.productSearchInput.text.toString().trim()

        thread {
            val result = searchUseCase.execute(query)
            activity.runOnUiThread {
                binding.productSearchButton.isEnabled = true
                when (result) {
                    is ProductSearchResult.Success -> {
                        binding.productSearchStatusText.text =
                            "Hasil untuk \"${result.query}\" (${result.rows.size}/${result.limit})"
                        binding.productSearchResultsText.text = renderer.productRows(result.rows)
                    }
                    is ProductSearchResult.Failure -> {
                        binding.productSearchStatusText.text = result.message
                        binding.productSearchResultsText.text = ""
                    }
                    is ProductSearchResult.Unauthenticated -> onUnauthenticated(result.message)
                }
            }
        }
    }
}

package id.hyperpos.mobile.features.supplierinvoice

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import id.hyperpos.mobile.adapters.http.MobileApiConfig
import id.hyperpos.mobile.adapters.http.OkHttpSupplierInvoiceApiClient
import id.hyperpos.mobile.adapters.storage.AndroidKeystoreSessionTokenStore
import id.hyperpos.mobile.application.procurement.GetSupplierInvoiceDetailUseCase
import id.hyperpos.mobile.application.procurement.SupplierInvoiceDetailResult
import id.hyperpos.mobile.databinding.ActivitySupplierInvoiceDetailBinding
import id.hyperpos.mobile.features.login.MobileUiTextRenderer
import okhttp3.OkHttpClient
import kotlin.concurrent.thread

class SupplierInvoiceDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySupplierInvoiceDetailBinding
    private val renderer = MobileUiTextRenderer()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySupplierInvoiceDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.supplierInvoiceDetailBackButton.setOnClickListener { finish() }
        loadDetail()
    }

    private fun loadDetail() {
        val id = intent.getStringExtra(EXTRA_SUPPLIER_INVOICE_ID)
        if (id.isNullOrBlank()) {
            binding.supplierInvoiceDetailPageStatusText.text = "Faktur supplier tidak valid."
            return
        }
        thread {
            val result = detailUseCase().execute(id)
            runOnUiThread { render(result) }
        }
    }

    private fun render(result: SupplierInvoiceDetailResult) {
        when (result) {
            is SupplierInvoiceDetailResult.Success -> {
                binding.supplierInvoiceDetailPageStatusText.text = "Detail faktur supplier dimuat"
                binding.supplierInvoiceDetailPageResultsText.text =
                    renderer.supplierInvoiceDetail(result.summary, result.lines)
            }
            is SupplierInvoiceDetailResult.Failure -> {
                binding.supplierInvoiceDetailPageStatusText.text = result.message
                binding.supplierInvoiceDetailPageResultsText.text = ""
            }
            is SupplierInvoiceDetailResult.Unauthenticated -> {
                binding.supplierInvoiceDetailPageStatusText.text = result.message
                binding.supplierInvoiceDetailPageResultsText.text = ""
            }
        }
    }

    private fun detailUseCase(): GetSupplierInvoiceDetailUseCase {
        return GetSupplierInvoiceDetailUseCase(
            api = OkHttpSupplierInvoiceApiClient(MobileApiConfig(), OkHttpClient()),
            tokenStore = AndroidKeystoreSessionTokenStore(this),
        )
    }

    companion object {
        const val EXTRA_SUPPLIER_INVOICE_ID = "supplier_invoice_id"
    }
}

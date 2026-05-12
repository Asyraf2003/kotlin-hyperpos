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
        if (id.isNullOrBlank()) return invalidInvoice()
        thread {
            val result = detailUseCase().execute(id)
            runOnUiThread { render(result) }
        }
    }

    private fun render(result: SupplierInvoiceDetailResult) {
        when (result) {
            is SupplierInvoiceDetailResult.Success -> renderSuccess(result)
            is SupplierInvoiceDetailResult.Failure -> renderFailure(result.message)
            is SupplierInvoiceDetailResult.Unauthenticated -> renderFailure(result.message)
        }
    }

    private fun renderSuccess(result: SupplierInvoiceDetailResult.Success) {
        binding.supplierInvoiceDetailPageStatusText.text = "Detail faktur supplier dimuat"
        binding.supplierInvoiceDetailPageResultsText.text =
            renderer.supplierInvoiceDetail(result.summary, result.lines)
    }

    private fun renderFailure(message: String) {
        binding.supplierInvoiceDetailPageStatusText.text = message
        binding.supplierInvoiceDetailPageResultsText.text = ""
    }

    private fun invalidInvoice() = renderFailure("Faktur supplier tidak valid.")

    private fun detailUseCase(): GetSupplierInvoiceDetailUseCase {
        val config = MobileApiConfig(baseUrl = "http://127.0.0.1:8000/api/v1")
        val api = OkHttpSupplierInvoiceApiClient(config, OkHttpClient())
        return GetSupplierInvoiceDetailUseCase(api, AndroidKeystoreSessionTokenStore(this))
    }

    companion object {
        const val EXTRA_SUPPLIER_INVOICE_ID = "supplier_invoice_id"
    }
}

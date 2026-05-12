package id.hyperpos.mobile.features.login

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import id.hyperpos.mobile.R
import id.hyperpos.mobile.application.procurement.GetSupplierInvoiceDetailUseCase
import id.hyperpos.mobile.application.procurement.ListSupplierInvoicesUseCase
import id.hyperpos.mobile.databinding.ActivityMainBinding
import kotlin.concurrent.thread

class SupplierInvoiceUiController(
    private val activity: AppCompatActivity,
    private val binding: ActivityMainBinding,
    private val listUseCase: ListSupplierInvoicesUseCase,
    private val detailUseCase: GetSupplierInvoiceDetailUseCase,
    private val listView: SupplierInvoiceListResultView,
    private val detailView: SupplierInvoiceDetailResultView,
) {
    private var selectedId: String? = null

    fun bind() {
        binding.supplierInvoiceListButton.setOnClickListener { listSupplierInvoices() }
        binding.supplierInvoiceDetailButton.setOnClickListener { loadDetail() }
    }

    fun setVisible(visible: Boolean) {
        binding.supplierInvoiceContainer.visibility = if (visible) View.VISIBLE else View.GONE
    }

    fun reset() {
        setVisible(false)
        binding.supplierInvoiceListButton.isEnabled = true
        binding.supplierInvoiceSearchInput.setText("")
        binding.supplierInvoiceListStatusText.text = activity.getString(R.string.supplier_invoice_ready)
        binding.supplierInvoiceListResultsText.text = ""
        resetDetailSelection()
    }

    fun listSupplierInvoices() {
        binding.supplierInvoiceListButton.isEnabled = false
        binding.supplierInvoiceListStatusText.text = "Memuat nota supplier..."
        binding.supplierInvoiceListResultsText.text = ""
        resetDetailSelection()

        thread {
            val result = listUseCase.execute(query = query(), paymentStatus = "all", page = 1)
            activity.runOnUiThread {
                binding.supplierInvoiceListButton.isEnabled = true
                selectedId = listView.apply(result, keepId = null)
            }
        }
    }

    fun loadDetail() {
        val id = selectedId
        if (id.isNullOrBlank()) {
            binding.supplierInvoiceDetailStatusText.text = "Pilih nota supplier dari daftar terlebih dahulu."
            binding.supplierInvoiceDetailResultsText.text = ""
            return
        }

        binding.supplierInvoiceDetailButton.isEnabled = false
        binding.supplierInvoiceDetailStatusText.text = "Memuat detail nota supplier..."
        binding.supplierInvoiceDetailResultsText.text = ""

        thread {
            val result = detailUseCase.execute(id)
            activity.runOnUiThread {
                binding.supplierInvoiceDetailButton.isEnabled = true
                detailView.apply(result)
            }
        }
    }

    fun refreshListKeepingSelection() {
        val id = selectedId ?: return
        thread {
            val result = listUseCase.execute(query = query(), paymentStatus = "all", page = 1)
            activity.runOnUiThread {
                selectedId = listView.apply(result, keepId = id)
            }
        }
    }

    private fun resetDetailSelection() {
        selectedId = null
        binding.supplierInvoiceDetailButton.visibility = View.GONE
        binding.supplierInvoiceDetailButton.isEnabled = true
        binding.supplierInvoiceDetailStatusText.text =
            activity.getString(R.string.supplier_invoice_detail_ready)
        binding.supplierInvoiceDetailResultsText.text = ""
        listView.resetProof()
    }

    private fun query(): String? {
        return binding.supplierInvoiceSearchInput.text.toString().trim().takeIf { it.isNotEmpty() }
    }
}

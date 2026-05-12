package id.hyperpos.mobile.features.login

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import id.hyperpos.mobile.R
import id.hyperpos.mobile.application.procurement.GetSupplierInvoiceDetailUseCase
import id.hyperpos.mobile.application.procurement.ListSupplierInvoicesUseCase
import id.hyperpos.mobile.application.procurement.SupplierInvoiceDetailResult
import id.hyperpos.mobile.application.procurement.SupplierInvoiceListResult
import id.hyperpos.mobile.databinding.ActivityMainBinding
import id.hyperpos.mobile.domain.procurement.MobileSupplierInvoiceListRow
import kotlin.concurrent.thread

class SupplierInvoiceUiController(
    private val activity: AppCompatActivity,
    private val binding: ActivityMainBinding,
    private val listUseCase: ListSupplierInvoicesUseCase,
    private val detailUseCase: GetSupplierInvoiceDetailUseCase,
    private val renderer: MobileUiTextRenderer,
    private val proofUi: SupplierInvoicePaymentProofUiController,
    private val onUnauthenticated: (String) -> Unit,
) {
    private var selectedId: String? = null

    fun bind() {
        binding.supplierInvoiceListButton.setOnClickListener { listSupplierInvoices() }
        binding.supplierInvoiceDetailButton.setOnClickListener { loadDetail() }
    }

    fun show() {
        binding.supplierInvoiceContainer.visibility = View.VISIBLE
    }

    fun hide() {
        binding.supplierInvoiceContainer.visibility = View.GONE
    }

    fun reset() {
        hide()
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
                handleListResult(result, null)
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
                when (result) {
                    is SupplierInvoiceDetailResult.Success -> {
                        binding.supplierInvoiceDetailStatusText.text = "Detail nota supplier dimuat"
                        binding.supplierInvoiceDetailResultsText.text =
                            renderer.supplierInvoiceDetail(result.summary, result.lines)
                        proofUi.onDetailLoaded()
                    }
                    is SupplierInvoiceDetailResult.Failure -> {
                        binding.supplierInvoiceDetailStatusText.text = result.message
                        binding.supplierInvoiceDetailResultsText.text = ""
                        proofUi.onFailure()
                    }
                    is SupplierInvoiceDetailResult.Unauthenticated -> onUnauthenticated(result.message)
                }
            }
        }
    }

    fun refreshListKeepingSelection() {
        val id = selectedId ?: return
        thread {
            val result = listUseCase.execute(query = query(), paymentStatus = "all", page = 1)
            activity.runOnUiThread { handleListResult(result, id) }
        }
    }

    private fun handleListResult(result: SupplierInvoiceListResult, keepId: String?) {
        when (result) {
            is SupplierInvoiceListResult.Success -> applyListSuccess(result, keepId)
            is SupplierInvoiceListResult.Failure -> binding.supplierInvoiceListStatusText.text = result.message
            is SupplierInvoiceListResult.Unauthenticated -> onUnauthenticated(result.message)
        }
    }

    private fun applyListSuccess(result: SupplierInvoiceListResult.Success, keepId: String?) {
        val row = selectedRow(result.rows, keepId)
        selectedId = row?.supplierInvoiceId
        proofUi.updateSelection(selectedId, row)
        binding.supplierInvoiceDetailButton.visibility = if (selectedId.isNullOrBlank()) View.GONE else View.VISIBLE
        binding.supplierInvoiceListStatusText.text = "Nota supplier dimuat (${result.rows.size}/${result.perPage})"
        binding.supplierInvoiceListResultsText.text = renderer.supplierInvoiceRows(result.rows)
    }

    private fun selectedRow(rows: List<MobileSupplierInvoiceListRow>, keepId: String?): MobileSupplierInvoiceListRow? {
        return if (keepId.isNullOrBlank()) rows.firstOrNull() else rows.firstOrNull { it.supplierInvoiceId == keepId }
    }

    private fun resetDetailSelection() {
        selectedId = null
        binding.supplierInvoiceDetailButton.visibility = View.GONE
        binding.supplierInvoiceDetailButton.isEnabled = true
        binding.supplierInvoiceDetailStatusText.text = activity.getString(R.string.supplier_invoice_detail_ready)
        binding.supplierInvoiceDetailResultsText.text = ""
        proofUi.reset()
    }

    private fun query(): String? {
        return binding.supplierInvoiceSearchInput.text.toString().trim().takeIf { it.isNotEmpty() }
    }
}

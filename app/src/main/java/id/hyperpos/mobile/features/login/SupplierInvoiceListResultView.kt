package id.hyperpos.mobile.features.login

import android.view.View
import id.hyperpos.mobile.application.procurement.SupplierInvoiceListResult
import id.hyperpos.mobile.databinding.ActivityMainBinding
import id.hyperpos.mobile.domain.procurement.MobileSupplierInvoiceListRow

class SupplierInvoiceListResultView(
    private val binding: ActivityMainBinding,
    private val renderer: MobileUiTextRenderer,
    private val proofUi: SupplierInvoicePaymentProofUiController,
    private val rowButtonFactory: SupplierInvoiceRowButtonFactory,
    private val onRowSelected: (MobileSupplierInvoiceListRow) -> Unit,
    private val onUnauthenticated: (String) -> Unit,
) {
    private var rows: List<MobileSupplierInvoiceListRow> = emptyList()

    fun apply(result: SupplierInvoiceListResult, keepId: String?): String? {
        return when (result) {
            is SupplierInvoiceListResult.Success -> applySuccess(result, keepId)
            is SupplierInvoiceListResult.Failure -> fail(result.message)
            is SupplierInvoiceListResult.Unauthenticated -> unauthenticated(result.message)
        }
    }

    fun select(row: MobileSupplierInvoiceListRow): String {
        val selected = rows.firstOrNull { it.supplierInvoiceId == row.supplierInvoiceId } ?: row
        renderRows(selected.supplierInvoiceId)
        proofUi.updateSelection(selected.supplierInvoiceId, selected)
        binding.supplierInvoiceListStatusText.text = "Faktur dipilih: ${selected.nomorFaktur}"
        return selected.supplierInvoiceId
    }

    fun resetProof() {
        rows = emptyList()
        binding.supplierInvoiceRowsContainer.removeAllViews()
        binding.supplierInvoiceRowsContainer.visibility = View.GONE
        proofUi.reset()
    }

    private fun applySuccess(result: SupplierInvoiceListResult.Success, keepId: String?): String? {
        rows = result.rows
        val row = selectedRow(result.rows, keepId)
        val selectedId = row?.supplierInvoiceId

        proofUi.updateSelection(selectedId, row)
        binding.supplierInvoiceDetailButton.visibility = View.GONE
        binding.supplierInvoiceListStatusText.text =
            "Nota supplier dimuat (${result.rows.size}/${result.perPage})"
        renderRows(selectedId)

        return selectedId
    }

    private fun renderRows(selectedId: String?) {
        binding.supplierInvoiceRowsContainer.removeAllViews()
        binding.supplierInvoiceRowsContainer.visibility = if (rows.isEmpty()) View.GONE else View.VISIBLE
        binding.supplierInvoiceListResultsText.text = if (rows.isEmpty()) {
            "Tidak ada nota supplier ditemukan."
        } else {
            "Klik faktur untuk membuka detail dan aksi bayar."
        }

        rows.forEach { row ->
            binding.supplierInvoiceRowsContainer.addView(
                rowButtonFactory.create(row, row.supplierInvoiceId == selectedId, onRowSelected),
            )
        }
    }

    private fun fail(message: String): String? {
        binding.supplierInvoiceListStatusText.text = message
        return null
    }

    private fun unauthenticated(message: String): String? {
        onUnauthenticated(message)
        return null
    }

    private fun selectedRow(
        rows: List<MobileSupplierInvoiceListRow>,
        keepId: String?,
    ): MobileSupplierInvoiceListRow? {
        return if (keepId.isNullOrBlank()) rows.firstOrNull()
        else rows.firstOrNull { it.supplierInvoiceId == keepId }
    }
}

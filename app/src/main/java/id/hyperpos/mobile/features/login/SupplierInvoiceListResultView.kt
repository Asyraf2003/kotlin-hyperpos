package id.hyperpos.mobile.features.login

import android.view.View
import id.hyperpos.mobile.application.procurement.SupplierInvoiceListResult
import id.hyperpos.mobile.databinding.ActivityMainBinding
import id.hyperpos.mobile.domain.procurement.MobileSupplierInvoiceListRow

class SupplierInvoiceListResultView(
    private val binding: ActivityMainBinding,
    private val renderer: MobileUiTextRenderer,
    private val proofUi: SupplierInvoicePaymentProofUiController,
    private val onUnauthenticated: (String) -> Unit,
) {
    fun apply(result: SupplierInvoiceListResult, keepId: String?): String? {
        return when (result) {
            is SupplierInvoiceListResult.Success -> applySuccess(result, keepId)
            is SupplierInvoiceListResult.Failure -> {
                binding.supplierInvoiceListStatusText.text = result.message
                null
            }
            is SupplierInvoiceListResult.Unauthenticated -> {
                onUnauthenticated(result.message)
                null
            }
        }
    }

    fun resetProof() {
        proofUi.reset()
    }

    private fun applySuccess(result: SupplierInvoiceListResult.Success, keepId: String?): String? {
        val row = selectedRow(result.rows, keepId)
        val selectedId = row?.supplierInvoiceId

        proofUi.updateSelection(selectedId, row)
        binding.supplierInvoiceDetailButton.visibility =
            if (selectedId.isNullOrBlank()) View.GONE else View.VISIBLE
        binding.supplierInvoiceListStatusText.text =
            "Nota supplier dimuat (${result.rows.size}/${result.perPage})"
        binding.supplierInvoiceListResultsText.text = renderer.supplierInvoiceRows(result.rows)

        return selectedId
    }

    private fun selectedRow(
        rows: List<MobileSupplierInvoiceListRow>,
        keepId: String?,
    ): MobileSupplierInvoiceListRow? {
        return if (keepId.isNullOrBlank()) {
            rows.firstOrNull()
        } else {
            rows.firstOrNull { row -> row.supplierInvoiceId == keepId }
        }
    }
}

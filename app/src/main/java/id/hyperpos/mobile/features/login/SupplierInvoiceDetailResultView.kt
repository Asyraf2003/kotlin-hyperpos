package id.hyperpos.mobile.features.login

import id.hyperpos.mobile.application.procurement.SupplierInvoiceDetailResult
import id.hyperpos.mobile.databinding.ActivityMainBinding

class SupplierInvoiceDetailResultView(
    private val binding: ActivityMainBinding,
    private val renderer: MobileUiTextRenderer,
    private val proofUi: SupplierInvoicePaymentProofUiController,
    private val onUnauthenticated: (String) -> Unit,
) {
    fun apply(result: SupplierInvoiceDetailResult) {
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
            is SupplierInvoiceDetailResult.Unauthenticated -> {
                onUnauthenticated(result.message)
            }
        }
    }
}

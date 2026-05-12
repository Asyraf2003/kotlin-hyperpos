package id.hyperpos.mobile.application.procurement

import id.hyperpos.mobile.domain.procurement.MobileSupplierInvoiceLine
import id.hyperpos.mobile.domain.procurement.MobileSupplierInvoiceSummary

sealed class SupplierInvoiceDetailResult {
    data class Success(
        val summary: MobileSupplierInvoiceSummary,
        val lines: List<MobileSupplierInvoiceLine>,
    ) : SupplierInvoiceDetailResult()

    data class Failure(val message: String) : SupplierInvoiceDetailResult()
    data class Unauthenticated(val message: String) : SupplierInvoiceDetailResult()
}

package id.hyperpos.mobile.application.procurement

import id.hyperpos.mobile.domain.procurement.MobileSupplierInvoiceListRow

sealed class SupplierInvoiceListResult {
    data class Success(
        val rows: List<MobileSupplierInvoiceListRow>,
        val page: Int,
        val perPage: Int,
        val paymentStatus: String,
        val query: String?,
    ) : SupplierInvoiceListResult()

    data class Failure(val message: String) : SupplierInvoiceListResult()
    data class Unauthenticated(val message: String) : SupplierInvoiceListResult()
}

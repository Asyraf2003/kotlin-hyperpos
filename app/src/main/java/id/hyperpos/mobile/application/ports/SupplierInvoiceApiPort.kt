package id.hyperpos.mobile.application.ports

import id.hyperpos.mobile.application.procurement.SupplierInvoiceDetailResult
import id.hyperpos.mobile.application.procurement.SupplierInvoiceListResult

interface SupplierInvoiceApiPort {
    fun listSupplierInvoices(
        token: String,
        query: String?,
        paymentStatus: String,
        page: Int,
    ): SupplierInvoiceListResult

    fun getSupplierInvoiceDetail(
        token: String,
        supplierInvoiceId: String,
    ): SupplierInvoiceDetailResult
}

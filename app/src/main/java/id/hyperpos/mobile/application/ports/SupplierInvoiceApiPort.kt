package id.hyperpos.mobile.application.ports

import id.hyperpos.mobile.application.procurement.SupplierInvoiceDetailResult
import id.hyperpos.mobile.application.procurement.SupplierInvoiceListResult
import id.hyperpos.mobile.application.procurement.SupplierInvoicePaymentProofFile
import id.hyperpos.mobile.application.procurement.UploadSupplierInvoicePaymentProofResult

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

    fun uploadPaymentProofBySupplierInvoiceId(
        token: String,
        supplierInvoiceId: String,
        proofFiles: List<SupplierInvoicePaymentProofFile>,
    ): UploadSupplierInvoicePaymentProofResult
}

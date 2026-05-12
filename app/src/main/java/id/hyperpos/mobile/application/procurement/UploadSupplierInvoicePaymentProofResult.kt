package id.hyperpos.mobile.application.procurement

import id.hyperpos.mobile.domain.procurement.MobileSupplierInvoicePaymentProofUpload

sealed class UploadSupplierInvoicePaymentProofResult {
    data class Success(
        val upload: MobileSupplierInvoicePaymentProofUpload,
        val message: String,
    ) : UploadSupplierInvoicePaymentProofResult()

    data class Failure(val message: String) : UploadSupplierInvoicePaymentProofResult()
    data class Unauthenticated(val message: String) : UploadSupplierInvoicePaymentProofResult()
}

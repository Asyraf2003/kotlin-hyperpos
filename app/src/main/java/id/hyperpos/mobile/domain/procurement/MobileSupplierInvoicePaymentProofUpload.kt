package id.hyperpos.mobile.domain.procurement

data class MobileSupplierInvoicePaymentProofUpload(
    val supplierInvoiceId: String,
    val supplierPaymentId: String,
    val amountRupiah: Long,
    val outstandingRupiah: Long,
    val proofStatus: String,
    val attachmentCount: Int,
)

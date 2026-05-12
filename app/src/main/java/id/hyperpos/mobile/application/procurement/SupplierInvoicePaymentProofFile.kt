package id.hyperpos.mobile.application.procurement

data class SupplierInvoicePaymentProofFile(
    val fileName: String,
    val mediaType: String,
    val bytes: ByteArray,
)

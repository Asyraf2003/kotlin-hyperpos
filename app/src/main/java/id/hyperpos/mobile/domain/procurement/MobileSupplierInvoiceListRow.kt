package id.hyperpos.mobile.domain.procurement

data class MobileSupplierInvoiceListRow(
    val supplierInvoiceId: String,
    val nomorFaktur: String,
    val supplierNamaPtPengirimCurrent: String?,
    val supplierNamaPtPengirimSnapshot: String?,
    val shipmentDate: String?,
    val dueDate: String?,
    val grandTotalRupiah: Long,
    val totalPaidRupiah: Long,
    val outstandingRupiah: Long,
    val paymentCount: Int,
    val receiptCount: Int,
    val totalReceivedQty: Int,
    val proofAttachmentCount: Int,
    val canRecordPayment: Boolean,
    val hasUploadedProof: Boolean,
    val policyState: String,
)

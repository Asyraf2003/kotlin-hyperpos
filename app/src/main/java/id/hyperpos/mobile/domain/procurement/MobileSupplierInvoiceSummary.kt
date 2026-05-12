package id.hyperpos.mobile.domain.procurement

data class MobileSupplierInvoiceSummary(
    val supplierInvoiceId: String,
    val nomorFaktur: String,
    val supplierId: String?,
    val supplierNamaPtPengirimCurrent: String?,
    val supplierNamaPtPengirimSnapshot: String?,
    val shipmentDate: String?,
    val dueDate: String?,
    val grandTotalRupiah: Long,
    val totalPaidRupiah: Long,
    val outstandingRupiah: Long,
    val receiptCount: Int,
    val totalReceivedQty: Int,
    val voidedAt: String?,
    val voidReason: String?,
    val policyState: String,
)

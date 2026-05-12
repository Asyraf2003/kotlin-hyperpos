package id.hyperpos.mobile.domain.procurement

data class MobileSupplierInvoiceLine(
    val id: String,
    val supplierInvoiceId: String,
    val productId: String,
    val kodeBarang: String?,
    val namaBarang: String,
    val merek: String?,
    val ukuran: Int?,
    val qtyPcs: Int,
    val lineTotalRupiah: Long,
    val unitCostRupiah: Long,
)

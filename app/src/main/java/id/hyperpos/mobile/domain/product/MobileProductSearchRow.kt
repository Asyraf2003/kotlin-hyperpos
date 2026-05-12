package id.hyperpos.mobile.domain.product

data class MobileProductSearchRow(
    val id: String,
    val label: String,
    val kodeBarang: String?,
    val namaBarang: String,
    val merek: String?,
    val ukuran: Int?,
    val availableStock: Int,
    val defaultUnitPriceRupiah: Int,
    val minimumUnitPriceRupiah: Int,
)

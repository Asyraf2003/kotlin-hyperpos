package id.hyperpos.mobile.adapters.http

import id.hyperpos.mobile.domain.product.MobileProductSearchRow
import org.json.JSONArray
import org.json.JSONObject

object ProductSearchJsonMapper {
    fun rows(rowsJson: JSONArray): List<MobileProductSearchRow> {
        return buildList {
            for (index in 0 until rowsJson.length()) {
                add(row(rowsJson.getJSONObject(index)))
            }
        }
    }

    private fun row(row: JSONObject): MobileProductSearchRow {
        return MobileProductSearchRow(
            id = row.getString("id"),
            label = row.getString("label"),
            kodeBarang = row.optNullableString("kode_barang"),
            namaBarang = row.getString("nama_barang"),
            merek = row.optNullableString("merek"),
            ukuran = row.optNullableInt("ukuran"),
            availableStock = row.getInt("available_stock"),
            defaultUnitPriceRupiah = row.getInt("default_unit_price_rupiah"),
            minimumUnitPriceRupiah = row.getInt("minimum_unit_price_rupiah"),
        )
    }
}

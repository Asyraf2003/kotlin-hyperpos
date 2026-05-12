package id.hyperpos.mobile.adapters.http

import id.hyperpos.mobile.application.ports.ProductSearchApiPort
import id.hyperpos.mobile.application.product.ProductSearchResult
import id.hyperpos.mobile.domain.product.MobileProductSearchRow
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException

class OkHttpProductSearchApiClient(
    private val config: MobileApiConfig,
    private val httpClient: OkHttpClient,
) : ProductSearchApiPort {
    override fun searchProducts(token: String, query: String): ProductSearchResult {
        val url = config.normalizedBaseUrl.toHttpUrl()
            .newBuilder()
            .addPathSegment("products")
            .addPathSegment("search")
            .addQueryParameter("q", query)
            .build()

        val httpRequest = Request.Builder()
            .url(url)
            .get()
            .header("Accept", "application/json")
            .header("Authorization", "Bearer $token")
            .build()

        return try {
            httpClient.newCall(httpRequest).execute().use { response ->
                val body = response.body?.string().orEmpty()
                val json = JSONObject(body)

                if (response.code == HTTP_UNAUTHORIZED) {
                    return ProductSearchResult.Unauthenticated(
                        json.optString("message", "Sesi login tidak valid. Silakan login ulang."),
                    )
                }

                if (!response.isSuccessful || !json.optBoolean("success", false)) {
                    return ProductSearchResult.Failure(
                        json.optString("message", "Pencarian produk gagal."),
                    )
                }

                val data = json.getJSONObject("data")
                val rowsJson = data.getJSONArray("rows")
                val rows = buildList {
                    for (index in 0 until rowsJson.length()) {
                        add(parseRow(rowsJson.getJSONObject(index)))
                    }
                }

                val meta = json.getJSONObject("meta")

                ProductSearchResult.Success(
                    rows = rows,
                    query = meta.getString("query"),
                    limit = meta.getInt("limit"),
                )
            }
        } catch (_: IOException) {
            ProductSearchResult.Failure("Tidak bisa terhubung ke server HyperPOS.")
        } catch (_: Exception) {
            ProductSearchResult.Failure("Respons server tidak valid.")
        }
    }

    private fun parseRow(row: JSONObject): MobileProductSearchRow {
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

    private fun JSONObject.optNullableString(name: String): String? {
        if (isNull(name)) {
            return null
        }

        return getString(name)
    }

    private fun JSONObject.optNullableInt(name: String): Int? {
        if (isNull(name)) {
            return null
        }

        return getInt(name)
    }

    private companion object {
        private const val HTTP_UNAUTHORIZED = 401
    }
}

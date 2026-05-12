package id.hyperpos.mobile.adapters.http

import id.hyperpos.mobile.application.procurement.SupplierInvoiceListResult
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException

class SupplierInvoiceListHttpCall(
    private val config: MobileApiConfig,
    private val httpClient: OkHttpClient,
) {
    fun execute(token: String, query: String?, paymentStatus: String, page: Int): SupplierInvoiceListResult {
        val httpRequest = Request.Builder()
            .url(url(query, paymentStatus, page))
            .get()
            .header("Accept", "application/json")
            .header("Authorization", "Bearer $token")
            .build()

        return try {
            httpClient.newCall(httpRequest).execute().use { response ->
                val json = JSONObject(response.body?.string().orEmpty())

                if (response.code == HTTP_UNAUTHORIZED) {
                    return SupplierInvoiceListResult.Unauthenticated(
                        json.optString("message", "Sesi login tidak valid. Silakan login ulang."),
                    )
                }

                if (!response.isSuccessful || !json.optBoolean("success", false)) {
                    return SupplierInvoiceListResult.Failure(
                        json.optString("message", "Daftar nota supplier gagal dimuat."),
                    )
                }

                success(json, paymentStatus, page)
            }
        } catch (_: IOException) {
            SupplierInvoiceListResult.Failure("Tidak bisa terhubung ke server HyperPOS.")
        } catch (_: Exception) {
            SupplierInvoiceListResult.Failure("Respons server tidak valid.")
        }
    }

    private fun url(query: String?, paymentStatus: String, page: Int): HttpUrl {
        val builder = config.normalizedBaseUrl.toHttpUrl()
            .newBuilder()
            .addPathSegment("supplier-invoices")
            .addQueryParameter("payment_status", paymentStatus)
            .addQueryParameter("page", page.coerceAtLeast(1).toString())

        val normalizedQuery = query?.trim()
        if (!normalizedQuery.isNullOrEmpty()) {
            builder.addQueryParameter("q", normalizedQuery)
        }

        return builder.build()
    }

    private fun success(json: JSONObject, paymentStatus: String, page: Int): SupplierInvoiceListResult.Success {
        val data = json.getJSONObject("data")
        val meta = json.getJSONObject("meta")
        val filters = meta.optJSONObject("filters")

        return SupplierInvoiceListResult.Success(
            rows = SupplierInvoiceListJsonMapper.rows(data.getJSONArray("rows")),
            page = meta.optInt("page", page.coerceAtLeast(1)),
            perPage = meta.optInt("per_page", 10),
            paymentStatus = filters?.optString("payment_status", paymentStatus) ?: paymentStatus,
            query = filters?.optNullableString("q"),
        )
    }
}

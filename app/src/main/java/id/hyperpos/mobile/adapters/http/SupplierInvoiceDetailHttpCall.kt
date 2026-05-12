package id.hyperpos.mobile.adapters.http

import id.hyperpos.mobile.application.procurement.SupplierInvoiceDetailResult
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException

class SupplierInvoiceDetailHttpCall(
    private val config: MobileApiConfig,
    private val httpClient: OkHttpClient,
) {
    fun execute(token: String, supplierInvoiceId: String): SupplierInvoiceDetailResult {
        val httpRequest = Request.Builder()
            .url(url(supplierInvoiceId))
            .get()
            .header("Accept", "application/json")
            .header("Authorization", "Bearer $token")
            .build()

        return try {
            httpClient.newCall(httpRequest).execute().use { response ->
                val json = JSONObject(response.body?.string().orEmpty())

                if (response.code == HTTP_UNAUTHORIZED) {
                    return SupplierInvoiceDetailResult.Unauthenticated(
                        json.optString("message", "Sesi login tidak valid. Silakan login ulang."),
                    )
                }

                if (!response.isSuccessful || !json.optBoolean("success", false)) {
                    return SupplierInvoiceDetailResult.Failure(
                        json.optString("message", "Detail nota supplier gagal dimuat."),
                    )
                }

                success(json)
            }
        } catch (_: IOException) {
            SupplierInvoiceDetailResult.Failure("Tidak bisa terhubung ke server HyperPOS.")
        } catch (_: Exception) {
            SupplierInvoiceDetailResult.Failure("Respons server tidak valid.")
        }
    }

    private fun url(supplierInvoiceId: String): okhttp3.HttpUrl {
        return config.normalizedBaseUrl.toHttpUrl()
            .newBuilder()
            .addPathSegment("supplier-invoices")
            .addPathSegment(supplierInvoiceId)
            .build()
    }

    private fun success(json: JSONObject): SupplierInvoiceDetailResult.Success {
        val data = json.getJSONObject("data")

        return SupplierInvoiceDetailResult.Success(
            summary = SupplierInvoiceDetailJsonMapper.summary(data.getJSONObject("summary")),
            lines = SupplierInvoiceDetailJsonMapper.lines(data.getJSONArray("lines")),
        )
    }
}

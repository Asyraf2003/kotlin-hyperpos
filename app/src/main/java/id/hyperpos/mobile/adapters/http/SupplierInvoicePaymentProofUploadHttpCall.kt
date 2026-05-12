package id.hyperpos.mobile.adapters.http

import id.hyperpos.mobile.application.procurement.SupplierInvoicePaymentProofFile
import id.hyperpos.mobile.application.procurement.UploadSupplierInvoicePaymentProofResult
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class SupplierInvoicePaymentProofUploadHttpCall(
    private val config: MobileApiConfig,
    private val httpClient: OkHttpClient,
) {
    fun execute(
        token: String,
        supplierInvoiceId: String,
        proofFiles: List<SupplierInvoicePaymentProofFile>,
    ): UploadSupplierInvoicePaymentProofResult {
        val httpRequest = Request.Builder()
            .url(url(supplierInvoiceId))
            .post(multipart(proofFiles))
            .header("Accept", "application/json")
            .header("Authorization", "Bearer $token")
            .build()

        return try {
            httpClient.newCall(httpRequest).execute().use { response ->
                val json = JSONObject(response.body?.string().orEmpty())

                if (response.code == HTTP_UNAUTHORIZED) {
                    return UploadSupplierInvoicePaymentProofResult.Unauthenticated(
                        json.optString("message", "Sesi login tidak valid. Silakan login ulang."),
                    )
                }

                if (!response.isSuccessful || !json.optBoolean("success", false)) {
                    return UploadSupplierInvoicePaymentProofResult.Failure(
                        json.optString("message", "Bukti pembayaran supplier gagal diunggah."),
                    )
                }

                UploadSupplierInvoicePaymentProofResult.Success(
                    upload = SupplierInvoicePaymentProofJsonMapper.upload(json.getJSONObject("data")),
                    message = json.optString("message", "Bukti pembayaran supplier berhasil diunggah."),
                )
            }
        } catch (_: IOException) {
            UploadSupplierInvoicePaymentProofResult.Failure("Tidak bisa terhubung ke server HyperPOS.")
        } catch (_: Exception) {
            UploadSupplierInvoicePaymentProofResult.Failure("Respons server tidak valid.")
        }
    }

    private fun url(supplierInvoiceId: String): okhttp3.HttpUrl {
        return config.normalizedBaseUrl.toHttpUrl()
            .newBuilder()
            .addPathSegment("supplier-invoices")
            .addPathSegment(supplierInvoiceId)
            .addPathSegment("payment-proof")
            .build()
    }

    private fun multipart(files: List<SupplierInvoicePaymentProofFile>): MultipartBody {
        val builder = MultipartBody.Builder().setType(MultipartBody.FORM)

        files.forEach { file ->
            builder.addFormDataPart(
                "proof_files[]",
                file.fileName,
                file.bytes.toRequestBody(file.mediaType.toMediaTypeOrNull()),
            )
        }

        return builder.build()
    }
}

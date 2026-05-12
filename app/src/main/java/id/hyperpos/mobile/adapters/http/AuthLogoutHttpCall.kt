package id.hyperpos.mobile.adapters.http

import id.hyperpos.mobile.application.auth.LogoutResult
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class AuthLogoutHttpCall(
    private val config: MobileApiConfig,
    private val httpClient: OkHttpClient,
) {
    fun execute(token: String): LogoutResult {
        val httpRequest = Request.Builder()
            .url("${config.normalizedBaseUrl}/auth/logout")
            .post("{}".toRequestBody(JSON_MEDIA_TYPE))
            .header("Accept", "application/json")
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer $token")
            .build()

        return try {
            httpClient.newCall(httpRequest).execute().use { response ->
                val json = JSONObject(response.body?.string().orEmpty())

                if (!response.isSuccessful || !json.optBoolean("success", false)) {
                    return LogoutResult.Failure(json.optString("message", "Logout server gagal."))
                }

                LogoutResult.Success(json.optString("message", "Logout berhasil."))
            }
        } catch (_: IOException) {
            LogoutResult.Failure("Tidak bisa terhubung ke server HyperPOS.")
        } catch (_: Exception) {
            LogoutResult.Failure("Respons server tidak valid.")
        }
    }
}

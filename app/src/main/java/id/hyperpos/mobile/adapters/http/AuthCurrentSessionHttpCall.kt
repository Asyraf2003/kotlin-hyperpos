package id.hyperpos.mobile.adapters.http

import id.hyperpos.mobile.application.auth.CurrentSessionResult
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException

class AuthCurrentSessionHttpCall(
    private val config: MobileApiConfig,
    private val httpClient: OkHttpClient,
) {
    fun execute(token: String): CurrentSessionResult {
        val httpRequest = Request.Builder()
            .url("${config.normalizedBaseUrl}/me")
            .get()
            .header("Accept", "application/json")
            .header("Authorization", "Bearer $token")
            .build()

        return try {
            httpClient.newCall(httpRequest).execute().use { response ->
                val json = JSONObject(response.body?.string().orEmpty())

                if (!response.isSuccessful || !json.optBoolean("success", false)) {
                    return CurrentSessionResult.Failure(
                        json.optString("message", "Sesi login tidak valid."),
                    )
                }

                CurrentSessionResult.Success(
                    actor = MobileActorJsonMapper.from(
                        json.getJSONObject("data").getJSONObject("actor"),
                    ),
                )
            }
        } catch (_: IOException) {
            CurrentSessionResult.Failure("Tidak bisa terhubung ke server HyperPOS.")
        } catch (_: Exception) {
            CurrentSessionResult.Failure("Respons server tidak valid.")
        }
    }
}

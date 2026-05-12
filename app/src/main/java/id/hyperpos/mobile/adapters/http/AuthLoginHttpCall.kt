package id.hyperpos.mobile.adapters.http

import id.hyperpos.mobile.application.auth.LoginRequest
import id.hyperpos.mobile.application.auth.LoginResult
import id.hyperpos.mobile.domain.auth.MobileSession
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class AuthLoginHttpCall(
    private val config: MobileApiConfig,
    private val httpClient: OkHttpClient,
) {
    fun execute(request: LoginRequest): LoginResult {
        val httpRequest = Request.Builder()
            .url("${config.normalizedBaseUrl}/auth/login")
            .post(payload(request).toRequestBody(JSON_MEDIA_TYPE))
            .header("Accept", "application/json")
            .header("Content-Type", "application/json")
            .build()

        return try {
            httpClient.newCall(httpRequest).execute().use { response ->
                val json = JSONObject(response.body?.string().orEmpty())

                if (!response.isSuccessful || !json.optBoolean("success", false)) {
                    return LoginResult.Failure(json.optString("message", "Login gagal."))
                }

                LoginResult.Success(session(json.getJSONObject("data")))
            }
        } catch (_: IOException) {
            LoginResult.Failure("Tidak bisa terhubung ke server HyperPOS.")
        } catch (_: Exception) {
            LoginResult.Failure("Respons server tidak valid.")
        }
    }

    private fun payload(request: LoginRequest): String {
        return JSONObject()
            .put("email", request.email)
            .put("password", request.password)
            .put("device_name", request.deviceName)
            .toString()
    }

    private fun session(data: JSONObject): MobileSession {
        return MobileSession(
            token = data.getString("token"),
            tokenType = data.getString("token_type"),
            expiresAt = data.getString("expires_at"),
            actor = MobileActorJsonMapper.from(data.getJSONObject("actor")),
        )
    }
}

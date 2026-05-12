package id.hyperpos.mobile.adapters.http

import id.hyperpos.mobile.application.auth.CurrentSessionResult
import id.hyperpos.mobile.application.auth.LoginRequest
import id.hyperpos.mobile.application.auth.LoginResult
import id.hyperpos.mobile.application.auth.LogoutResult
import id.hyperpos.mobile.application.ports.AuthApiPort
import id.hyperpos.mobile.domain.auth.MobileActor
import id.hyperpos.mobile.domain.auth.MobileSession
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class OkHttpAuthApiClient(
    private val config: MobileApiConfig,
    private val httpClient: OkHttpClient,
) : AuthApiPort {
    override fun login(request: LoginRequest): LoginResult {
        val payload = JSONObject()
            .put("email", request.email)
            .put("password", request.password)
            .put("device_name", request.deviceName)
            .toString()

        val httpRequest = Request.Builder()
            .url("${config.normalizedBaseUrl}/auth/login")
            .post(payload.toRequestBody(JSON_MEDIA_TYPE))
            .header("Accept", "application/json")
            .header("Content-Type", "application/json")
            .build()

        return try {
            httpClient.newCall(httpRequest).execute().use { response ->
                val body = response.body?.string().orEmpty()
                val json = JSONObject(body)

                if (!response.isSuccessful || !json.optBoolean("success", false)) {
                    return LoginResult.Failure(
                        json.optString("message", "Login gagal."),
                    )
                }

                val data = json.getJSONObject("data")

                LoginResult.Success(
                    MobileSession(
                        token = data.getString("token"),
                        tokenType = data.getString("token_type"),
                        expiresAt = data.getString("expires_at"),
                        actor = parseActor(data.getJSONObject("actor")),
                    ),
                )
            }
        } catch (_: IOException) {
            LoginResult.Failure("Tidak bisa terhubung ke server HyperPOS.")
        } catch (_: Exception) {
            LoginResult.Failure("Respons server tidak valid.")
        }
    }

    override fun currentSession(token: String): CurrentSessionResult {
        val httpRequest = Request.Builder()
            .url("${config.normalizedBaseUrl}/me")
            .get()
            .header("Accept", "application/json")
            .header("Authorization", "Bearer $token")
            .build()

        return try {
            httpClient.newCall(httpRequest).execute().use { response ->
                val body = response.body?.string().orEmpty()
                val json = JSONObject(body)

                if (!response.isSuccessful || !json.optBoolean("success", false)) {
                    return CurrentSessionResult.Failure(
                        json.optString("message", "Sesi login tidak valid."),
                    )
                }

                CurrentSessionResult.Success(
                    actor = parseActor(json.getJSONObject("data").getJSONObject("actor")),
                )
            }
        } catch (_: IOException) {
            CurrentSessionResult.Failure("Tidak bisa terhubung ke server HyperPOS.")
        } catch (_: Exception) {
            CurrentSessionResult.Failure("Respons server tidak valid.")
        }
    }

    override fun logout(token: String): LogoutResult {
        val httpRequest = Request.Builder()
            .url("${config.normalizedBaseUrl}/auth/logout")
            .post("{}".toRequestBody(JSON_MEDIA_TYPE))
            .header("Accept", "application/json")
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer $token")
            .build()

        return try {
            httpClient.newCall(httpRequest).execute().use { response ->
                val body = response.body?.string().orEmpty()
                val json = JSONObject(body)

                if (!response.isSuccessful || !json.optBoolean("success", false)) {
                    return LogoutResult.Failure(
                        json.optString("message", "Logout server gagal."),
                    )
                }

                LogoutResult.Success(
                    json.optString("message", "Logout berhasil."),
                )
            }
        } catch (_: IOException) {
            LogoutResult.Failure("Tidak bisa terhubung ke server HyperPOS.")
        } catch (_: Exception) {
            LogoutResult.Failure("Respons server tidak valid.")
        }
    }

    private fun parseActor(actor: JSONObject): MobileActor {
        return MobileActor(
            id = actor.getString("id"),
            name = actor.getString("name"),
            email = actor.getString("email"),
            role = actor.getString("role"),
        )
    }

    private companion object {
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
    }
}

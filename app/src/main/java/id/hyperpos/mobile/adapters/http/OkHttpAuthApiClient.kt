package id.hyperpos.mobile.adapters.http

import id.hyperpos.mobile.application.auth.CurrentSessionResult
import id.hyperpos.mobile.application.auth.LoginRequest
import id.hyperpos.mobile.application.auth.LoginResult
import id.hyperpos.mobile.application.auth.LogoutResult
import id.hyperpos.mobile.application.ports.AuthApiPort
import okhttp3.OkHttpClient

class OkHttpAuthApiClient(
    config: MobileApiConfig,
    httpClient: OkHttpClient,
) : AuthApiPort {
    private val loginCall = AuthLoginHttpCall(
        config = config,
        httpClient = httpClient,
    )
    private val currentSessionCall = AuthCurrentSessionHttpCall(
        config = config,
        httpClient = httpClient,
    )
    private val logoutCall = AuthLogoutHttpCall(
        config = config,
        httpClient = httpClient,
    )

    override fun login(request: LoginRequest): LoginResult {
        return loginCall.execute(request)
    }

    override fun currentSession(token: String): CurrentSessionResult {
        return currentSessionCall.execute(token)
    }

    override fun logout(token: String): LogoutResult {
        return logoutCall.execute(token)
    }
}

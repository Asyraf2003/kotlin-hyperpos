package id.hyperpos.mobile.application.auth

import id.hyperpos.mobile.application.ports.AuthApiPort
import id.hyperpos.mobile.application.ports.SessionTokenStore

class LogoutUseCase(
    private val authApi: AuthApiPort,
    private val tokenStore: SessionTokenStore,
) {
    fun execute(): LogoutResult {
        val token = tokenStore.read()
        if (token.isNullOrBlank()) {
            tokenStore.clear()
            return LogoutResult.NoSession("Sesi login tidak ditemukan.")
        }

        val result = authApi.logout(token)
        tokenStore.clear()

        return result
    }
}

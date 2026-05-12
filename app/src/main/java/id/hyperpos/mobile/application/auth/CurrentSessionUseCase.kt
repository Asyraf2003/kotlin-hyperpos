package id.hyperpos.mobile.application.auth

import id.hyperpos.mobile.application.ports.AuthApiPort
import id.hyperpos.mobile.application.ports.SessionTokenStore

class CurrentSessionUseCase(
    private val authApi: AuthApiPort,
    private val tokenStore: SessionTokenStore,
) {
    fun execute(): CurrentSessionResult {
        val token = tokenStore.read()
        if (token.isNullOrBlank()) {
            return CurrentSessionResult.Failure("Sesi login tidak ditemukan.")
        }

        return authApi.currentSession(token)
    }
}

package id.hyperpos.mobile.application.auth

import id.hyperpos.mobile.application.ports.AuthApiPort
import id.hyperpos.mobile.application.ports.SessionTokenStore

class LoginUseCase(
    private val authApi: AuthApiPort,
    private val tokenStore: SessionTokenStore,
) {
    fun execute(request: LoginRequest): LoginResult {
        if (request.email.isBlank()) {
            return LoginResult.Failure("Email wajib diisi.")
        }

        if (request.password.isBlank()) {
            return LoginResult.Failure("Password wajib diisi.")
        }

        if (request.deviceName.isBlank()) {
            return LoginResult.Failure("Nama perangkat wajib diisi.")
        }

        val result = authApi.login(request)
        if (result is LoginResult.Success) {
            tokenStore.save(result.session.token)
        }

        return result
    }
}

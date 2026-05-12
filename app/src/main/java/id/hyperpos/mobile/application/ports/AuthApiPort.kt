package id.hyperpos.mobile.application.ports

import id.hyperpos.mobile.application.auth.CurrentSessionResult
import id.hyperpos.mobile.application.auth.LoginRequest
import id.hyperpos.mobile.application.auth.LoginResult
import id.hyperpos.mobile.application.auth.LogoutResult

interface AuthApiPort {
    fun login(request: LoginRequest): LoginResult
    fun currentSession(token: String): CurrentSessionResult
    fun logout(token: String): LogoutResult
}

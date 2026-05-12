package id.hyperpos.mobile.application.auth

import id.hyperpos.mobile.domain.auth.MobileSession

sealed class LoginResult {
    data class Success(val session: MobileSession) : LoginResult()
    data class Failure(val message: String) : LoginResult()
}

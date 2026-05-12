package id.hyperpos.mobile.application.auth

sealed class LogoutResult {
    data class Success(val message: String) : LogoutResult()
    data class NoSession(val message: String) : LogoutResult()
    data class Failure(val message: String) : LogoutResult()
}

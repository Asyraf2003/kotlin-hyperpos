package id.hyperpos.mobile.application.auth

data class LoginRequest(
    val email: String,
    val password: String,
    val deviceName: String,
)

package id.hyperpos.mobile.domain.auth

data class MobileSession(
    val token: String,
    val tokenType: String,
    val expiresAt: String,
    val actor: MobileActor,
)

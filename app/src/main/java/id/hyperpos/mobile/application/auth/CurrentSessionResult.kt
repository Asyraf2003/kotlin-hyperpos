package id.hyperpos.mobile.application.auth

import id.hyperpos.mobile.domain.auth.MobileActor

sealed class CurrentSessionResult {
    data class Success(val actor: MobileActor) : CurrentSessionResult()
    data class Failure(val message: String) : CurrentSessionResult()
}

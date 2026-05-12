package id.hyperpos.mobile.adapters.http

import id.hyperpos.mobile.domain.auth.MobileActor
import org.json.JSONObject

object MobileActorJsonMapper {
    fun from(actor: JSONObject): MobileActor {
        return MobileActor(
            id = actor.getString("id"),
            name = actor.getString("name"),
            email = actor.getString("email"),
            role = actor.getString("role"),
        )
    }
}

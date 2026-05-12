package id.hyperpos.mobile.adapters.http

import org.json.JSONObject

fun JSONObject.optNullableString(name: String): String? {
    if (!has(name) || isNull(name)) {
        return null
    }

    return getString(name)
}

fun JSONObject.optNullableInt(name: String): Int? {
    if (!has(name) || isNull(name)) {
        return null
    }

    return getInt(name)
}

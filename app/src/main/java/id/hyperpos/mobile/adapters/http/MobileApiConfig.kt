package id.hyperpos.mobile.adapters.http

data class MobileApiConfig(
    val baseUrl: String,
) {
    val normalizedBaseUrl: String = baseUrl.trimEnd('/')
}

package id.hyperpos.mobile.application.ports

interface SessionTokenStore {
    fun save(token: String)
    fun read(): String?
    fun clear()
}

package id.hyperpos.mobile.application.procurement

import id.hyperpos.mobile.application.ports.SessionTokenStore
import id.hyperpos.mobile.application.ports.SupplierInvoiceApiPort

class ListSupplierInvoicesUseCase(
    private val supplierInvoiceApi: SupplierInvoiceApiPort,
    private val tokenStore: SessionTokenStore,
) {
    fun execute(
        query: String? = null,
        paymentStatus: String = "all",
        page: Int = 1,
    ): SupplierInvoiceListResult {
        val token = tokenStore.read()
        if (token.isNullOrBlank()) {
            return SupplierInvoiceListResult.Unauthenticated("Sesi login tidak ditemukan. Silakan login ulang.")
        }

        return supplierInvoiceApi.listSupplierInvoices(
            token = token,
            query = query,
            paymentStatus = paymentStatus,
            page = page,
        )
    }
}

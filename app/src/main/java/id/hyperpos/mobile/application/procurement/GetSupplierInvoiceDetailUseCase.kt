package id.hyperpos.mobile.application.procurement

import id.hyperpos.mobile.application.ports.SessionTokenStore
import id.hyperpos.mobile.application.ports.SupplierInvoiceApiPort

class GetSupplierInvoiceDetailUseCase(
    private val supplierInvoiceApi: SupplierInvoiceApiPort,
    private val tokenStore: SessionTokenStore,
) {
    fun execute(supplierInvoiceId: String): SupplierInvoiceDetailResult {
        val token = tokenStore.read()
        if (token.isNullOrBlank()) {
            return SupplierInvoiceDetailResult.Unauthenticated("Sesi login tidak ditemukan. Silakan login ulang.")
        }

        return supplierInvoiceApi.getSupplierInvoiceDetail(
            token = token,
            supplierInvoiceId = supplierInvoiceId,
        )
    }
}

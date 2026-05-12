package id.hyperpos.mobile.application.procurement

import id.hyperpos.mobile.application.ports.SessionTokenStore
import id.hyperpos.mobile.application.ports.SupplierInvoiceApiPort

class UploadSupplierInvoicePaymentProofUseCase(
    private val supplierInvoiceApi: SupplierInvoiceApiPort,
    private val tokenStore: SessionTokenStore,
) {
    fun execute(
        supplierInvoiceId: String,
        proofFiles: List<SupplierInvoicePaymentProofFile>,
    ): UploadSupplierInvoicePaymentProofResult {
        val token = tokenStore.read()
        if (token.isNullOrBlank()) {
            return UploadSupplierInvoicePaymentProofResult.Unauthenticated(
                "Sesi login tidak ditemukan. Silakan login ulang.",
            )
        }

        val normalizedSupplierInvoiceId = supplierInvoiceId.trim()
        if (normalizedSupplierInvoiceId.isEmpty()) {
            return UploadSupplierInvoicePaymentProofResult.Failure("Nota supplier belum dipilih.")
        }

        if (proofFiles.isEmpty()) {
            return UploadSupplierInvoicePaymentProofResult.Failure("Pilih bukti pembayaran terlebih dahulu.")
        }

        if (proofFiles.size > MAX_PROOF_FILES) {
            return UploadSupplierInvoicePaymentProofResult.Failure("Maksimal 3 file bukti pembayaran.")
        }

        return supplierInvoiceApi.uploadPaymentProofBySupplierInvoiceId(
            token = token,
            supplierInvoiceId = normalizedSupplierInvoiceId,
            proofFiles = proofFiles,
        )
    }

    private companion object {
        private const val MAX_PROOF_FILES = 3
    }
}

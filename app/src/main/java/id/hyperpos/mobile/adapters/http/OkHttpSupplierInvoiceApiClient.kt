package id.hyperpos.mobile.adapters.http

import id.hyperpos.mobile.application.ports.SupplierInvoiceApiPort
import id.hyperpos.mobile.application.procurement.SupplierInvoiceDetailResult
import id.hyperpos.mobile.application.procurement.SupplierInvoiceListResult
import id.hyperpos.mobile.application.procurement.SupplierInvoicePaymentProofFile
import id.hyperpos.mobile.application.procurement.UploadSupplierInvoicePaymentProofResult
import okhttp3.OkHttpClient

class OkHttpSupplierInvoiceApiClient(
    config: MobileApiConfig,
    httpClient: OkHttpClient,
) : SupplierInvoiceApiPort {
    private val listCall = SupplierInvoiceListHttpCall(
        config = config,
        httpClient = httpClient,
    )
    private val detailCall = SupplierInvoiceDetailHttpCall(
        config = config,
        httpClient = httpClient,
    )
    private val uploadCall = SupplierInvoicePaymentProofUploadHttpCall(
        config = config,
        httpClient = httpClient,
    )

    override fun listSupplierInvoices(
        token: String,
        query: String?,
        paymentStatus: String,
        page: Int,
    ): SupplierInvoiceListResult {
        return listCall.execute(
            token = token,
            query = query,
            paymentStatus = paymentStatus,
            page = page,
        )
    }

    override fun getSupplierInvoiceDetail(
        token: String,
        supplierInvoiceId: String,
    ): SupplierInvoiceDetailResult {
        return detailCall.execute(
            token = token,
            supplierInvoiceId = supplierInvoiceId,
        )
    }

    override fun uploadPaymentProofBySupplierInvoiceId(
        token: String,
        supplierInvoiceId: String,
        proofFiles: List<SupplierInvoicePaymentProofFile>,
    ): UploadSupplierInvoicePaymentProofResult {
        return uploadCall.execute(
            token = token,
            supplierInvoiceId = supplierInvoiceId,
            proofFiles = proofFiles,
        )
    }
}

package id.hyperpos.mobile.adapters.http

import id.hyperpos.mobile.domain.procurement.MobileSupplierInvoicePaymentProofUpload
import org.json.JSONObject

object SupplierInvoicePaymentProofJsonMapper {
    fun upload(data: JSONObject): MobileSupplierInvoicePaymentProofUpload {
        return MobileSupplierInvoicePaymentProofUpload(
            supplierInvoiceId = data.getString("supplier_invoice_id"),
            supplierPaymentId = data.getString("supplier_payment_id"),
            amountRupiah = data.getLong("amount_rupiah"),
            outstandingRupiah = data.getLong("outstanding_rupiah"),
            proofStatus = data.getString("proof_status"),
            attachmentCount = data.getInt("attachment_count"),
        )
    }
}

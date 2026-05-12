package id.hyperpos.mobile.features.login

import id.hyperpos.mobile.domain.procurement.MobileSupplierInvoiceListRow

object SupplierInvoicePaymentProofPolicy {
    fun canUpload(row: MobileSupplierInvoiceListRow): Boolean {
        return row.outstandingRupiah > 0L &&
            row.canRecordPayment &&
            !row.hasUploadedProof
    }
}

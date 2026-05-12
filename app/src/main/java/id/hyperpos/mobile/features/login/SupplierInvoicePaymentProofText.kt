package id.hyperpos.mobile.features.login

import id.hyperpos.mobile.domain.procurement.MobileSupplierInvoiceListRow

object SupplierInvoicePaymentProofText {
    fun forSelection(row: MobileSupplierInvoiceListRow?): String {
        if (row == null) {
            return "Pilih faktur supplier dari daftar."
        }

        return when {
            SupplierInvoicePaymentProofPolicy.canUpload(row) ->
                "Klik Bayar / Unggah Bukti untuk pilih Kamera, Galeri, atau File."
            row.outstandingRupiah <= 0L ->
                "Faktur supplier sudah Lunas. Bukti baru tidak diperlukan."
            row.hasUploadedProof ->
                "Bukti pembayaran faktur ini sudah diunggah."
            !row.canRecordPayment ->
                "Faktur supplier ini belum bisa dicatat pembayarannya."
            else ->
                "Faktur supplier ini tidak bisa diunggah bukti pembayarannya."
        }
    }
}

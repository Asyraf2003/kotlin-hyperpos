package id.hyperpos.mobile.features.login

import id.hyperpos.mobile.application.procurement.UploadSupplierInvoicePaymentProofResult
import id.hyperpos.mobile.domain.procurement.MobileSupplierInvoiceLine
import id.hyperpos.mobile.domain.procurement.MobileSupplierInvoiceListRow
import id.hyperpos.mobile.domain.procurement.MobileSupplierInvoiceSummary
import id.hyperpos.mobile.domain.product.MobileProductSearchRow
import java.text.NumberFormat
import java.util.Locale

class MobileUiTextRenderer {
    private val rupiahFormat = NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID"))

    fun productRows(rows: List<MobileProductSearchRow>): String {
        if (rows.isEmpty()) {
            return "Tidak ada produk ditemukan."
        }

        return rows.joinToString(separator = "\n\n") { row ->
            listOf(
                row.label,
                "Stok: ${row.availableStock}",
                "Harga jual: Rp ${rupiahFormat.format(row.defaultUnitPriceRupiah)}",
            ).joinToString(separator = "\n")
        }
    }

    fun supplierInvoiceRows(rows: List<MobileSupplierInvoiceListRow>): String {
        if (rows.isEmpty()) {
            return "Tidak ada nota supplier ditemukan."
        }

        return rows.joinToString(separator = "\n\n") { row ->
            val supplierName = row.supplierNamaPtPengirimCurrent
                ?: row.supplierNamaPtPengirimSnapshot
                ?: "Supplier tidak diketahui"

            listOf(
                "Nomor faktur: ${row.nomorFaktur}",
                "Supplier: $supplierName",
                "Status pembayaran: ${paymentStatusLabel(row.outstandingRupiah)}",
            ).joinToString(separator = "\n")
        }
    }

    fun supplierInvoiceDetail(
        summary: MobileSupplierInvoiceSummary,
        lines: List<MobileSupplierInvoiceLine>,
    ): String {
        val supplierName = summary.supplierNamaPtPengirimCurrent
            ?: summary.supplierNamaPtPengirimSnapshot
            ?: "Supplier tidak diketahui"

        return listOf(
            listOf(
                "Nomor faktur: ${summary.nomorFaktur}",
                "Supplier: $supplierName",
                "Total: Rp ${rupiahFormat.format(summary.grandTotalRupiah)}",
                "Status pembayaran: ${paymentStatusLabel(summary.outstandingRupiah)}",
            ).joinToString(separator = "\n"),
            "Rincian barang:",
            supplierInvoiceLines(lines),
        ).joinToString(separator = "\n\n")
    }

    fun paymentProofUploadSuccess(
        result: UploadSupplierInvoicePaymentProofResult.Success,
    ): String {
        return listOf(
            result.message,
            "Status pembayaran: ${paymentStatusLabel(result.upload.outstandingRupiah)}",
            "Lampiran bukti: ${result.upload.attachmentCount}",
        ).joinToString(separator = "\n")
    }

    fun paymentStatusLabel(outstandingRupiah: Long): String {
        return if (outstandingRupiah <= 0L) "Lunas" else "Belum lunas"
    }

    private fun supplierInvoiceLines(lines: List<MobileSupplierInvoiceLine>): String {
        if (lines.isEmpty()) {
            return "Tidak ada rincian barang."
        }

        return lines.take(5).joinToString(separator = "\n\n") { line ->
            listOf(
                line.namaBarang,
                "Qty: ${line.qtyPcs}",
                "Harga satuan: Rp ${rupiahFormat.format(line.unitCostRupiah)}",
                "Subtotal: Rp ${rupiahFormat.format(line.lineTotalRupiah)}",
            ).joinToString(separator = "\n")
        }
    }
}

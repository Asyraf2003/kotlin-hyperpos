package id.hyperpos.mobile.features.login

import android.content.Context
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import id.hyperpos.mobile.domain.procurement.MobileSupplierInvoiceListRow
import java.text.NumberFormat
import java.util.Locale

class SupplierInvoiceRowButtonFactory(
    private val context: Context,
) {
    private val rupiah = NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID"))

    fun create(
        row: MobileSupplierInvoiceListRow,
        selected: Boolean,
        onClick: (MobileSupplierInvoiceListRow) -> Unit,
    ): Button {
        return Button(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            ).also { params -> params.topMargin = 8 }
            isAllCaps = false
            text = label(row, selected)
            contentDescription = "Faktur supplier ${row.nomorFaktur}"
            setOnClickListener { onClick(row) }
        }
    }

    private fun label(row: MobileSupplierInvoiceListRow, selected: Boolean): String {
        val marker = if (selected) "Dipilih" else "Klik untuk detail"
        return listOf(
            marker,
            "Nomor faktur: ${row.nomorFaktur}",
            "Supplier: ${supplierName(row)}",
            "Status pembayaran: ${status(row)}",
            "Outstanding: Rp ${rupiah.format(row.outstandingRupiah)}",
        ).joinToString(separator = "\n")
    }

    private fun status(row: MobileSupplierInvoiceListRow): String {
        return if (row.outstandingRupiah <= 0L) "Lunas" else "Belum lunas"
    }

    private fun supplierName(row: MobileSupplierInvoiceListRow): String {
        return row.supplierNamaPtPengirimCurrent
            ?: row.supplierNamaPtPengirimSnapshot
            ?: "Supplier tidak diketahui"
    }
}

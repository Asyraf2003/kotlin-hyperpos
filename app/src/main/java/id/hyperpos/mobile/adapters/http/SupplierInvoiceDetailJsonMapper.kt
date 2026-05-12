package id.hyperpos.mobile.adapters.http

import id.hyperpos.mobile.domain.procurement.MobileSupplierInvoiceLine
import id.hyperpos.mobile.domain.procurement.MobileSupplierInvoiceSummary
import org.json.JSONArray
import org.json.JSONObject

object SupplierInvoiceDetailJsonMapper {
    fun summary(summary: JSONObject): MobileSupplierInvoiceSummary {
        return MobileSupplierInvoiceSummary(
            supplierInvoiceId = summary.getString("supplier_invoice_id"),
            nomorFaktur = summary.getString("nomor_faktur"),
            supplierId = summary.optNullableString("supplier_id"),
            supplierNamaPtPengirimCurrent = summary.optNullableString("supplier_nama_pt_pengirim_current"),
            supplierNamaPtPengirimSnapshot = summary.optNullableString("supplier_nama_pt_pengirim_snapshot"),
            shipmentDate = summary.optNullableString("shipment_date"),
            dueDate = summary.optNullableString("due_date"),
            grandTotalRupiah = summary.getLong("grand_total_rupiah"),
            totalPaidRupiah = summary.getLong("total_paid_rupiah"),
            outstandingRupiah = summary.getLong("outstanding_rupiah"),
            receiptCount = summary.optInt("receipt_count", 0),
            totalReceivedQty = summary.optInt("total_received_qty", 0),
            voidedAt = summary.optNullableString("voided_at"),
            voidReason = summary.optNullableString("void_reason"),
            policyState = summary.optString("policy_state", ""),
        )
    }

    fun lines(linesJson: JSONArray): List<MobileSupplierInvoiceLine> {
        return buildList {
            for (index in 0 until linesJson.length()) {
                add(line(linesJson.getJSONObject(index)))
            }
        }
    }

    private fun line(line: JSONObject): MobileSupplierInvoiceLine {
        return MobileSupplierInvoiceLine(
            id = line.getString("id"),
            supplierInvoiceId = line.getString("supplier_invoice_id"),
            productId = line.getString("product_id"),
            kodeBarang = line.optNullableString("kode_barang"),
            namaBarang = line.getString("nama_barang"),
            merek = line.optNullableString("merek"),
            ukuran = line.optNullableInt("ukuran"),
            qtyPcs = line.getInt("qty_pcs"),
            lineTotalRupiah = line.getLong("line_total_rupiah"),
            unitCostRupiah = line.getLong("unit_cost_rupiah"),
        )
    }
}

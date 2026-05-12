package id.hyperpos.mobile.adapters.http

import id.hyperpos.mobile.domain.procurement.MobileSupplierInvoiceListRow
import org.json.JSONArray
import org.json.JSONObject

object SupplierInvoiceListJsonMapper {
    fun rows(rowsJson: JSONArray): List<MobileSupplierInvoiceListRow> {
        return buildList {
            for (index in 0 until rowsJson.length()) {
                add(row(rowsJson.getJSONObject(index)))
            }
        }
    }

    private fun row(row: JSONObject): MobileSupplierInvoiceListRow {
        return MobileSupplierInvoiceListRow(
            supplierInvoiceId = row.getString("supplier_invoice_id"),
            nomorFaktur = row.getString("nomor_faktur"),
            supplierNamaPtPengirimCurrent = row.optNullableString("supplier_nama_pt_pengirim_current"),
            supplierNamaPtPengirimSnapshot = row.optNullableString("supplier_nama_pt_pengirim_snapshot"),
            shipmentDate = row.optNullableString("shipment_date"),
            dueDate = row.optNullableString("due_date"),
            grandTotalRupiah = row.getLong("grand_total_rupiah"),
            totalPaidRupiah = row.getLong("total_paid_rupiah"),
            outstandingRupiah = row.getLong("outstanding_rupiah"),
            paymentCount = row.optInt("payment_count", 0),
            receiptCount = row.optInt("receipt_count", 0),
            totalReceivedQty = row.optInt("total_received_qty", 0),
            proofAttachmentCount = row.optInt("proof_attachment_count", 0),
            canRecordPayment = row.optBoolean("can_record_payment", false),
            hasUploadedProof = row.optBoolean("has_uploaded_proof", false),
            policyState = row.optString("policy_state", ""),
        )
    }
}

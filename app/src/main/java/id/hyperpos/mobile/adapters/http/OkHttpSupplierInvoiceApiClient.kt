package id.hyperpos.mobile.adapters.http

import id.hyperpos.mobile.application.ports.SupplierInvoiceApiPort
import id.hyperpos.mobile.application.procurement.SupplierInvoiceDetailResult
import id.hyperpos.mobile.application.procurement.SupplierInvoiceListResult
import id.hyperpos.mobile.domain.procurement.MobileSupplierInvoiceLine
import id.hyperpos.mobile.domain.procurement.MobileSupplierInvoiceListRow
import id.hyperpos.mobile.domain.procurement.MobileSupplierInvoiceSummary
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException

class OkHttpSupplierInvoiceApiClient(
    private val config: MobileApiConfig,
    private val httpClient: OkHttpClient,
) : SupplierInvoiceApiPort {
    override fun listSupplierInvoices(
        token: String,
        query: String?,
        paymentStatus: String,
        page: Int,
    ): SupplierInvoiceListResult {
        val urlBuilder = config.normalizedBaseUrl.toHttpUrl()
            .newBuilder()
            .addPathSegment("supplier-invoices")
            .addQueryParameter("payment_status", paymentStatus)
            .addQueryParameter("page", page.coerceAtLeast(1).toString())

        val normalizedQuery = query?.trim()
        if (!normalizedQuery.isNullOrEmpty()) {
            urlBuilder.addQueryParameter("q", normalizedQuery)
        }

        val httpRequest = Request.Builder()
            .url(urlBuilder.build())
            .get()
            .header("Accept", "application/json")
            .header("Authorization", "Bearer $token")
            .build()

        return try {
            httpClient.newCall(httpRequest).execute().use { response ->
                val body = response.body?.string().orEmpty()
                val json = JSONObject(body)

                if (response.code == HTTP_UNAUTHORIZED) {
                    return SupplierInvoiceListResult.Unauthenticated(
                        json.optString("message", "Sesi login tidak valid. Silakan login ulang."),
                    )
                }

                if (!response.isSuccessful || !json.optBoolean("success", false)) {
                    return SupplierInvoiceListResult.Failure(
                        json.optString("message", "Daftar nota supplier gagal dimuat."),
                    )
                }

                val rowsJson = json.getJSONObject("data").getJSONArray("rows")
                val rows = buildList {
                    for (index in 0 until rowsJson.length()) {
                        add(parseListRow(rowsJson.getJSONObject(index)))
                    }
                }

                val meta = json.getJSONObject("meta")
                val filters = meta.optJSONObject("filters")

                SupplierInvoiceListResult.Success(
                    rows = rows,
                    page = meta.optInt("page", page.coerceAtLeast(1)),
                    perPage = meta.optInt("per_page", 10),
                    paymentStatus = filters?.optString("payment_status", paymentStatus) ?: paymentStatus,
                    query = filters?.optNullableString("q"),
                )
            }
        } catch (_: IOException) {
            SupplierInvoiceListResult.Failure("Tidak bisa terhubung ke server HyperPOS.")
        } catch (_: Exception) {
            SupplierInvoiceListResult.Failure("Respons server tidak valid.")
        }
    }

    override fun getSupplierInvoiceDetail(
        token: String,
        supplierInvoiceId: String,
    ): SupplierInvoiceDetailResult {
        val url = config.normalizedBaseUrl.toHttpUrl()
            .newBuilder()
            .addPathSegment("supplier-invoices")
            .addPathSegment(supplierInvoiceId)
            .build()

        val httpRequest = Request.Builder()
            .url(url)
            .get()
            .header("Accept", "application/json")
            .header("Authorization", "Bearer $token")
            .build()

        return try {
            httpClient.newCall(httpRequest).execute().use { response ->
                val body = response.body?.string().orEmpty()
                val json = JSONObject(body)

                if (response.code == HTTP_UNAUTHORIZED) {
                    return SupplierInvoiceDetailResult.Unauthenticated(
                        json.optString("message", "Sesi login tidak valid. Silakan login ulang."),
                    )
                }

                if (!response.isSuccessful || !json.optBoolean("success", false)) {
                    return SupplierInvoiceDetailResult.Failure(
                        json.optString("message", "Detail nota supplier gagal dimuat."),
                    )
                }

                val data = json.getJSONObject("data")
                val linesJson = data.getJSONArray("lines")
                val lines = buildList {
                    for (index in 0 until linesJson.length()) {
                        add(parseLine(linesJson.getJSONObject(index)))
                    }
                }

                SupplierInvoiceDetailResult.Success(
                    summary = parseSummary(data.getJSONObject("summary")),
                    lines = lines,
                )
            }
        } catch (_: IOException) {
            SupplierInvoiceDetailResult.Failure("Tidak bisa terhubung ke server HyperPOS.")
        } catch (_: Exception) {
            SupplierInvoiceDetailResult.Failure("Respons server tidak valid.")
        }
    }

    private fun parseListRow(row: JSONObject): MobileSupplierInvoiceListRow {
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

    private fun parseSummary(summary: JSONObject): MobileSupplierInvoiceSummary {
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

    private fun parseLine(line: JSONObject): MobileSupplierInvoiceLine {
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

    private fun JSONObject.optNullableString(name: String): String? {
        if (!has(name) || isNull(name)) {
            return null
        }

        return getString(name)
    }

    private fun JSONObject.optNullableInt(name: String): Int? {
        if (!has(name) || isNull(name)) {
            return null
        }

        return getInt(name)
    }

    private companion object {
        private const val HTTP_UNAUTHORIZED = 401
    }
}

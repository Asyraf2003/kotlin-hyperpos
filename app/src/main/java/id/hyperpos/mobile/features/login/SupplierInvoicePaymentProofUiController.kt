package id.hyperpos.mobile.features.login

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import id.hyperpos.mobile.R
import id.hyperpos.mobile.application.procurement.UploadSupplierInvoicePaymentProofResult
import id.hyperpos.mobile.application.procurement.UploadSupplierInvoicePaymentProofUseCase
import id.hyperpos.mobile.domain.procurement.MobileSupplierInvoiceListRow
import kotlin.concurrent.thread

class SupplierInvoicePaymentProofUiController(
    private val activity: AppCompatActivity,
    private val uploadUseCase: UploadSupplierInvoicePaymentProofUseCase,
    private val fileReader: SupplierInvoicePaymentProofFileReader,
    private val renderer: MobileUiTextRenderer,
    private val actionView: SupplierInvoicePaymentProofActionView,
    private val openPicker: () -> Unit,
    private val onUnauthenticated: (String) -> Unit,
    private val refreshList: () -> Unit,
    private val loadDetail: () -> Unit,
) {
    private var selectedId: String? = null
    private var canUpload = false

    fun bind() = actionView.onClick { open() }

    fun updateSelection(id: String?, row: MobileSupplierInvoiceListRow?) {
        selectedId = id
        canUpload = row?.let(SupplierInvoicePaymentProofPolicy::canUpload) ?: false
        actionView.sync(canUpload)
    }

    fun reset() {
        selectedId = null
        canUpload = false
        actionView.clear()
    }

    fun onDetailLoaded() {
        if (canUpload) {
            actionView.message(activity.getString(R.string.supplier_invoice_upload_proof_ready), canUpload)
        } else {
            actionView.sync(canUpload)
        }
    }

    fun onFailure() {
        canUpload = false
        actionView.sync(canUpload)
    }

    fun onPicked(uri: Uri?) {
        if (uri == null) {
            actionView.message(activity.getString(R.string.supplier_invoice_upload_proof_ready), canUpload)
            return
        }
        upload(uri)
    }

    private fun open() {
        val id = selectedId
        when {
            id.isNullOrBlank() -> actionView.message("Pilih nota supplier terlebih dahulu.", canUpload)
            !canUpload -> actionView.message(
                "Nota supplier ini tidak bisa diunggah bukti pembayarannya.",
                canUpload,
            )
            else -> openPicker()
        }
    }

    private fun upload(uri: Uri) {
        val id = selectedId ?: return
        val file = fileReader.read(uri)
        if (file == null) {
            actionView.message("File bukti pembayaran harus JPG, PNG, atau PDF maksimal 2 MB.", canUpload)
            return
        }

        actionView.uploading()
        thread {
            val result = uploadUseCase.execute(id, listOf(file))
            activity.runOnUiThread { handleUploadResult(result) }
        }
    }

    private fun handleUploadResult(result: UploadSupplierInvoicePaymentProofResult) {
        when (result) {
            is UploadSupplierInvoicePaymentProofResult.Success -> {
                canUpload = false
                actionView.message(successMessage(result), canUpload)
                refreshList()
                loadDetail()
            }
            is UploadSupplierInvoicePaymentProofResult.Failure -> actionView.message(result.message, canUpload)
            is UploadSupplierInvoicePaymentProofResult.Unauthenticated -> onUnauthenticated(result.message)
        }
    }

    private fun successMessage(result: UploadSupplierInvoicePaymentProofResult.Success): String {
        return listOf(
            result.message,
            "Status pembayaran: ${renderer.paymentStatusLabel(result.upload.outstandingRupiah)}",
            "Lampiran bukti: ${result.upload.attachmentCount}",
        ).joinToString(separator = "\n")
    }
}

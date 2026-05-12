package id.hyperpos.mobile.features.login

import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import id.hyperpos.mobile.R
import id.hyperpos.mobile.application.procurement.SupplierInvoicePaymentProofFile
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
    private val sourceDialog: SupplierInvoiceProofSourceDialog,
    private val cameraFileFactory: SupplierInvoiceCameraProofFileFactory,
    private val openFilePicker: () -> Unit,
    private val openGalleryPicker: () -> Unit,
    private val openCamera: () -> Unit,
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
        actionView.message(SupplierInvoicePaymentProofText.forSelection(row), canUpload)
    }

    fun reset() {
        selectedId = null
        canUpload = false
        actionView.clear()
    }

    fun onDetailLoaded() = readyMessage()
    fun onFailure() {
        canUpload = false
        actionView.sync(canUpload)
    }

    fun onPicked(uri: Uri?) {
        if (uri == null) readyMessage() else uploadUri(uri)
    }

    fun onCaptured(bitmap: Bitmap?) {
        if (bitmap == null) return readyMessage()
        cameraFileFactory.from(bitmap)?.let(::uploadFile)
            ?: actionView.message("Foto bukti pembayaran maksimal 2 MB.", canUpload)
    }

    private fun open() {
        when {
            selectedId.isNullOrBlank() ->
                actionView.message("Pilih faktur supplier terlebih dahulu.", canUpload)
            !canUpload -> actionView.message(
                "Faktur supplier ini tidak bisa diunggah bukti pembayarannya.",
                canUpload,
            )
            else -> sourceDialog.show(openCamera, openGalleryPicker, openFilePicker)
        }
    }

    private fun uploadUri(uri: Uri) {
        val file = fileReader.read(uri)
        if (file == null) {
            actionView.message("File bukti pembayaran harus JPG, PNG, atau PDF maksimal 2 MB.", canUpload)
            return
        }
        uploadFile(file)
    }

    private fun uploadFile(file: SupplierInvoicePaymentProofFile) {
        val id = selectedId ?: return
        actionView.uploading()
        thread {
            val result = uploadUseCase.execute(id, listOf(file))
            activity.runOnUiThread { handleUploadResult(result) }
        }
    }

    private fun handleUploadResult(result: UploadSupplierInvoicePaymentProofResult) {
        when (result) {
            is UploadSupplierInvoicePaymentProofResult.Success -> handleSuccess(result)
            is UploadSupplierInvoicePaymentProofResult.Failure ->
                actionView.message(result.message, canUpload)
            is UploadSupplierInvoicePaymentProofResult.Unauthenticated ->
                onUnauthenticated(result.message)
        }
    }

    private fun handleSuccess(result: UploadSupplierInvoicePaymentProofResult.Success) {
        canUpload = false
        actionView.message(renderer.paymentProofUploadSuccess(result), canUpload)
        refreshList()
        loadDetail()
    }

    private fun readyMessage() {
        actionView.message(activity.getString(R.string.supplier_invoice_upload_proof_ready), canUpload)
    }
}

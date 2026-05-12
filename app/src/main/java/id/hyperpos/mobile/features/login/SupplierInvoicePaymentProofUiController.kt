package id.hyperpos.mobile.features.login

import android.net.Uri
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import id.hyperpos.mobile.R
import id.hyperpos.mobile.application.procurement.UploadSupplierInvoicePaymentProofResult
import id.hyperpos.mobile.application.procurement.UploadSupplierInvoicePaymentProofUseCase
import id.hyperpos.mobile.databinding.ActivityMainBinding
import id.hyperpos.mobile.domain.procurement.MobileSupplierInvoiceListRow
import kotlin.concurrent.thread

class SupplierInvoicePaymentProofUiController(
    private val activity: AppCompatActivity,
    private val binding: ActivityMainBinding,
    private val uploadUseCase: UploadSupplierInvoicePaymentProofUseCase,
    private val fileReader: SupplierInvoicePaymentProofFileReader,
    private val renderer: MobileUiTextRenderer,
    private val openPicker: () -> Unit,
    private val onUnauthenticated: (String) -> Unit,
    private val refreshList: () -> Unit,
    private val loadDetail: () -> Unit,
) {
    private var selectedId: String? = null
    private var canUpload = false

    fun bind() {
        binding.supplierInvoicePaymentProofButton.setOnClickListener { open() }
    }

    fun updateSelection(id: String?, row: MobileSupplierInvoiceListRow?) {
        selectedId = id
        canUpload = row?.let(::canUploadProof) ?: false
        sync()
    }

    fun reset() {
        selectedId = null
        canUpload = false
        binding.supplierInvoicePaymentProofStatusText.text = ""
        sync()
    }

    fun onDetailLoaded() {
        if (canUpload) {
            binding.supplierInvoicePaymentProofStatusText.text =
                activity.getString(R.string.supplier_invoice_upload_proof_ready)
        }
        sync()
    }

    fun onFailure() {
        canUpload = false
        sync()
    }

    fun onPicked(uri: Uri?) {
        if (uri == null) {
            binding.supplierInvoicePaymentProofStatusText.text =
                activity.getString(R.string.supplier_invoice_upload_proof_ready)
            sync()
            return
        }
        upload(uri)
    }

    private fun open() {
        val id = selectedId
        if (id.isNullOrBlank()) {
            binding.supplierInvoicePaymentProofStatusText.text = "Pilih nota supplier terlebih dahulu."
        } else if (!canUpload) {
            binding.supplierInvoicePaymentProofStatusText.text =
                "Nota supplier ini tidak bisa diunggah bukti pembayarannya."
        } else {
            openPicker()
            return
        }
        sync()
    }

    private fun upload(uri: Uri) {
        val id = selectedId ?: return
        val file = fileReader.read(uri)
        if (file == null) {
            binding.supplierInvoicePaymentProofStatusText.text =
                "File bukti pembayaran harus JPG, PNG, atau PDF maksimal 2 MB."
            sync()
            return
        }

        binding.supplierInvoicePaymentProofButton.isEnabled = false
        binding.supplierInvoicePaymentProofStatusText.text = "Mengunggah bukti pembayaran supplier..."
        sync()

        thread {
            val result = uploadUseCase.execute(id, listOf(file))
            activity.runOnUiThread {
                binding.supplierInvoicePaymentProofButton.isEnabled = true
                handleUploadResult(result)
            }
        }
    }

    private fun handleUploadResult(result: UploadSupplierInvoicePaymentProofResult) {
        when (result) {
            is UploadSupplierInvoicePaymentProofResult.Success -> {
                canUpload = false
                binding.supplierInvoicePaymentProofStatusText.text = listOf(
                    result.message,
                    "Status pembayaran: ${renderer.paymentStatusLabel(result.upload.outstandingRupiah)}",
                    "Lampiran bukti: ${result.upload.attachmentCount}",
                ).joinToString(separator = "\n")
                sync()
                refreshList()
                loadDetail()
            }
            is UploadSupplierInvoicePaymentProofResult.Failure -> {
                binding.supplierInvoicePaymentProofStatusText.text = result.message
                sync()
            }
            is UploadSupplierInvoicePaymentProofResult.Unauthenticated -> onUnauthenticated(result.message)
        }
    }

    private fun sync() {
        binding.supplierInvoicePaymentProofButton.visibility = if (canUpload) View.VISIBLE else View.GONE
        val hasStatus = binding.supplierInvoicePaymentProofStatusText.text.toString().isNotBlank()
        binding.supplierInvoicePaymentProofStatusText.visibility =
            if (canUpload || hasStatus) View.VISIBLE else View.GONE
    }

    private fun canUploadProof(row: MobileSupplierInvoiceListRow): Boolean {
        return row.outstandingRupiah > 0L && row.canRecordPayment && !row.hasUploadedProof
    }
}

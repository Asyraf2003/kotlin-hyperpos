package id.hyperpos.mobile.features.login

import android.view.View
import id.hyperpos.mobile.databinding.ActivityMainBinding

class SupplierInvoicePaymentProofActionView(
    private val binding: ActivityMainBinding,
) {
    fun onClick(action: () -> Unit) {
        binding.supplierInvoicePaymentProofButton.setOnClickListener { action() }
    }

    fun clear() {
        binding.supplierInvoicePaymentProofStatusText.text = ""
        sync(canUpload = false)
    }

    fun uploading() {
        binding.supplierInvoicePaymentProofButton.isEnabled = false
        binding.supplierInvoicePaymentProofStatusText.text =
            "Mengunggah bukti pembayaran supplier..."
        sync(canUpload = true)
    }

    fun message(message: String, canUpload: Boolean) {
        binding.supplierInvoicePaymentProofButton.isEnabled = true
        binding.supplierInvoicePaymentProofStatusText.text = message
        sync(canUpload)
    }

    fun sync(canUpload: Boolean) {
        binding.supplierInvoicePaymentProofButton.visibility =
            if (canUpload) View.VISIBLE else View.GONE

        val hasStatus = binding.supplierInvoicePaymentProofStatusText.text
            .toString()
            .isNotBlank()

        binding.supplierInvoicePaymentProofStatusText.visibility =
            if (canUpload || hasStatus) View.VISIBLE else View.GONE
    }
}

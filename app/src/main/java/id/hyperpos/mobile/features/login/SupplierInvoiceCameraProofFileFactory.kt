package id.hyperpos.mobile.features.login

import android.graphics.Bitmap
import id.hyperpos.mobile.application.procurement.SupplierInvoicePaymentProofFile
import java.io.ByteArrayOutputStream

class SupplierInvoiceCameraProofFileFactory {
    fun from(bitmap: Bitmap): SupplierInvoicePaymentProofFile? {
        val output = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, output)

        val bytes = output.toByteArray()
        if (bytes.isEmpty() || bytes.size > MAX_BYTES) {
            return null
        }

        return SupplierInvoicePaymentProofFile(
            fileName = "supplier-payment-proof-camera.jpg",
            mediaType = "image/jpeg",
            bytes = bytes,
        )
    }

    private companion object {
        const val JPEG_QUALITY = 85
        const val MAX_BYTES = 2 * 1024 * 1024
    }
}

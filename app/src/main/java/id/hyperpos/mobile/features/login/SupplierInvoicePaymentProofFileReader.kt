package id.hyperpos.mobile.features.login

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import id.hyperpos.mobile.application.procurement.SupplierInvoicePaymentProofFile

class SupplierInvoicePaymentProofFileReader(
    private val contentResolver: ContentResolver,
) {
    fun read(uri: Uri): SupplierInvoicePaymentProofFile? {
        val mediaType = contentResolver.getType(uri) ?: return null
        if (!MIME_TYPES.contains(mediaType)) {
            return null
        }

        val bytes = contentResolver.openInputStream(uri)?.use { input ->
            input.readBytes()
        } ?: return null

        if (bytes.isEmpty() || bytes.size > MAX_BYTES) {
            return null
        }

        return SupplierInvoicePaymentProofFile(
            fileName = resolveFileName(uri, mediaType),
            mediaType = mediaType,
            bytes = bytes,
        )
    }

    private fun resolveFileName(uri: Uri, mediaType: String): String {
        val displayName = contentResolver.query(
            uri,
            arrayOf(OpenableColumns.DISPLAY_NAME),
            null,
            null,
            null,
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
            } else {
                null
            }
        }

        if (!displayName.isNullOrBlank()) {
            return displayName
        }

        return when (mediaType) {
            "image/jpeg" -> "supplier-payment-proof.jpg"
            "image/png" -> "supplier-payment-proof.png"
            "application/pdf" -> "supplier-payment-proof.pdf"
            else -> "supplier-payment-proof.bin"
        }
    }

    companion object {
        val MIME_TYPES = arrayOf(
            "image/jpeg",
            "image/png",
            "application/pdf",
        )
        private const val MAX_BYTES = 2 * 1024 * 1024
    }
}

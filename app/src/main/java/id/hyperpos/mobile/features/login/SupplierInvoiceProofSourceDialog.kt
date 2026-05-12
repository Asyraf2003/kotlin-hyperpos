package id.hyperpos.mobile.features.login

import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class SupplierInvoiceProofSourceDialog(
    private val activity: AppCompatActivity,
) {
    fun show(
        openCamera: () -> Unit,
        openGallery: () -> Unit,
        openFile: () -> Unit,
    ) {
        AlertDialog.Builder(activity)
            .setTitle("Pilih bukti pembayaran")
            .setItems(arrayOf("Kamera", "Galeri", "File")) { _, which ->
                when (which) {
                    CAMERA -> openCamera()
                    GALLERY -> openGallery()
                    FILE -> openFile()
                }
            }
            .show()
    }

    private companion object {
        const val CAMERA = 0
        const val GALLERY = 1
        const val FILE = 2
    }
}

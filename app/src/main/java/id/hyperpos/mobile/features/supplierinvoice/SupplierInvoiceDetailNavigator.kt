package id.hyperpos.mobile.features.supplierinvoice

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity

class SupplierInvoiceDetailNavigator(
    private val activity: AppCompatActivity,
) {
    fun open(supplierInvoiceId: String?) {
        val intent = Intent(activity, SupplierInvoiceDetailActivity::class.java).apply {
            putExtra(SupplierInvoiceDetailActivity.EXTRA_SUPPLIER_INVOICE_ID, supplierInvoiceId)
        }
        activity.startActivity(intent)
    }
}

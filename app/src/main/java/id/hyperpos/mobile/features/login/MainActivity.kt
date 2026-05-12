package id.hyperpos.mobile.features.login

import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import id.hyperpos.mobile.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var authUi: LoginUiController
    private lateinit var productUi: ProductSearchUiController
    private lateinit var supplierUi: SupplierInvoiceUiController
    private lateinit var proofUi: SupplierInvoicePaymentProofUiController

    private val deps by lazy { MainActivityDependencies(this) }
    private val proofFilePicker = registerForActivityResult(ActivityResultContracts.OpenDocument()) {
        uri -> proofUi.onPicked(uri)
    }
    private val proofGalleryPicker = registerForActivityResult(ActivityResultContracts.OpenDocument()) {
        uri -> proofUi.onPicked(uri)
    }
    private val proofCamera = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) {
        bitmap -> proofUi.onCaptured(bitmap)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        wireControllers()
        resetAuthenticatedUi()
    }

    private fun wireControllers() {
        val renderer = MobileUiTextRenderer()
        proofUi = proofController(renderer)
        supplierUi = supplierController(renderer)
        authUi = LoginUiController(
            this, binding, deps.loginUseCase, deps.logoutUseCase,
            ::showAuthenticatedUi, ::resetAuthenticatedUi,
        )
        productUi = ProductSearchUiController(
            this, binding, deps.searchProductsUseCase, renderer, ::handleUnauthenticated,
        )
        authUi.bind()
        productUi.bind()
        supplierUi.bind()
        proofUi.bind()
    }

    private fun proofController(renderer: MobileUiTextRenderer) =
        SupplierInvoicePaymentProofUiController(
            this, deps.uploadSupplierInvoicePaymentProofUseCase,
            SupplierInvoicePaymentProofFileReader(contentResolver), renderer,
            SupplierInvoicePaymentProofActionView(binding),
            SupplierInvoiceProofSourceDialog(this), SupplierInvoiceCameraProofFileFactory(),
            { proofFilePicker.launch(SupplierInvoicePaymentProofFileReader.MIME_TYPES) },
            { proofGalleryPicker.launch(arrayOf("image/jpeg", "image/png")) },
            { proofCamera.launch(null) }, ::handleUnauthenticated,
            { supplierUi.refreshListKeepingSelection() }, { supplierUi.loadDetail() },
        )

    private fun supplierController(renderer: MobileUiTextRenderer) =
        SupplierInvoiceUiController(
            this, binding, deps.listSupplierInvoicesUseCase, deps.getSupplierInvoiceDetailUseCase,
            SupplierInvoiceListResultView(
                binding, renderer, proofUi, SupplierInvoiceRowButtonFactory(this),
                { row -> supplierUi.selectAndLoadDetail(row) }, ::handleUnauthenticated,
            ),
            SupplierInvoiceDetailResultView(binding, renderer, proofUi, ::handleUnauthenticated),
        )

    private fun showAuthenticatedUi(role: String) {
        val isAdmin = role == "admin"
        authUi.showAuthenticatedControls()
        productUi.setVisible(role == "kasir")
        supplierUi.setVisible(isAdmin)
        if (isAdmin) supplierUi.listSupplierInvoices()
    }

    private fun handleUnauthenticated(message: String) {
        deps.tokenStore.clear()
        resetAuthenticatedUi()
        binding.statusText.text = message
    }

    private fun resetAuthenticatedUi() {
        authUi.reset()
        productUi.reset()
        supplierUi.reset()
    }
}

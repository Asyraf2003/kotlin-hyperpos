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
    private val proofFilePicker = registerForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri -> proofUi.onPicked(uri) }
    private val proofGalleryPicker = registerForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri -> proofUi.onPicked(uri) }
    private val proofCamera = registerForActivityResult(
        ActivityResultContracts.TakePicturePreview(),
    ) { bitmap -> proofUi.onCaptured(bitmap) }

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
        listOf(authUi::bind, productUi::bind, supplierUi::bind, proofUi::bind).forEach { it() }
    }

    private fun proofController(renderer: MobileUiTextRenderer) =
        SupplierInvoicePaymentProofUiController(
            activity = this,
            uploadUseCase = deps.uploadSupplierInvoicePaymentProofUseCase,
            fileReader = SupplierInvoicePaymentProofFileReader(contentResolver),
            renderer = renderer,
            actionView = SupplierInvoicePaymentProofActionView(binding),
            sourceDialog = SupplierInvoiceProofSourceDialog(this),
            cameraFileFactory = SupplierInvoiceCameraProofFileFactory(),
            openFilePicker = { proofFilePicker.launch(SupplierInvoicePaymentProofFileReader.MIME_TYPES) },
            openGalleryPicker = { proofGalleryPicker.launch(arrayOf("image/jpeg", "image/png")) },
            openCamera = { proofCamera.launch(null) },
            onUnauthenticated = ::handleUnauthenticated,
            refreshList = { supplierUi.refreshListKeepingSelection() },
            loadDetail = { supplierUi.loadDetail() },
        )

    private fun supplierController(renderer: MobileUiTextRenderer) =
        SupplierInvoiceUiController(
            activity = this,
            binding = binding,
            listUseCase = deps.listSupplierInvoicesUseCase,
            detailUseCase = deps.getSupplierInvoiceDetailUseCase,
            listView = SupplierInvoiceListResultView(
                binding = binding,
                renderer = renderer,
                proofUi = proofUi,
                rowButtonFactory = SupplierInvoiceRowButtonFactory(this),
                onRowSelected = { row -> supplierUi.selectAndLoadDetail(row) },
                onUnauthenticated = ::handleUnauthenticated,
            ),
            detailView = SupplierInvoiceDetailResultView(
                binding, renderer, proofUi, ::handleUnauthenticated,
            ),
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

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
    private val proofPicker = registerForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri -> proofUi.onPicked(uri) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        wireControllers()
        resetAuthenticatedUi()
    }

    private fun wireControllers() {
        val renderer = MobileUiTextRenderer()
        val proofView = SupplierInvoicePaymentProofActionView(binding)

        proofUi = SupplierInvoicePaymentProofUiController(
            activity = this,
            uploadUseCase = deps.uploadSupplierInvoicePaymentProofUseCase,
            fileReader = SupplierInvoicePaymentProofFileReader(contentResolver),
            renderer = renderer,
            actionView = proofView,
            openPicker = { proofPicker.launch(SupplierInvoicePaymentProofFileReader.MIME_TYPES) },
            onUnauthenticated = ::handleUnauthenticated,
            refreshList = { supplierUi.refreshListKeepingSelection() },
            loadDetail = { supplierUi.loadDetail() },
        )
        supplierUi = SupplierInvoiceUiController(
            activity = this,
            binding = binding,
            listUseCase = deps.listSupplierInvoicesUseCase,
            detailUseCase = deps.getSupplierInvoiceDetailUseCase,
            listView = SupplierInvoiceListResultView(binding, renderer, proofUi, ::handleUnauthenticated),
            detailView = SupplierInvoiceDetailResultView(binding, renderer, proofUi, ::handleUnauthenticated),
        )
        authUi = LoginUiController(
            activity = this,
            binding = binding,
            loginUseCase = deps.loginUseCase,
            logoutUseCase = deps.logoutUseCase,
            onAuthenticated = ::showAuthenticatedUi,
            resetAuthenticatedUi = ::resetAuthenticatedUi,
        )
        productUi = ProductSearchUiController(
            activity = this,
            binding = binding,
            searchUseCase = deps.searchProductsUseCase,
            renderer = renderer,
            onUnauthenticated = ::handleUnauthenticated,
        )

        authUi.bind()
        productUi.bind()
        supplierUi.bind()
        proofUi.bind()
    }

    private fun showAuthenticatedUi(role: String) {
        authUi.showAuthenticatedControls()
        productUi.setVisible(role == "kasir")
        supplierUi.setVisible(role == "admin")
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

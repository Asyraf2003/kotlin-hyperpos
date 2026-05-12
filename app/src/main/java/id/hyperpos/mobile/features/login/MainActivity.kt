package id.hyperpos.mobile.features.login

import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import id.hyperpos.mobile.adapters.http.MobileApiConfig
import id.hyperpos.mobile.adapters.http.OkHttpAuthApiClient
import id.hyperpos.mobile.adapters.http.OkHttpProductSearchApiClient
import id.hyperpos.mobile.adapters.http.OkHttpSupplierInvoiceApiClient
import id.hyperpos.mobile.adapters.storage.AndroidKeystoreSessionTokenStore
import id.hyperpos.mobile.application.auth.LoginRequest
import id.hyperpos.mobile.application.auth.LoginResult
import id.hyperpos.mobile.application.auth.LoginUseCase
import id.hyperpos.mobile.application.auth.LogoutResult
import id.hyperpos.mobile.application.auth.LogoutUseCase
import id.hyperpos.mobile.application.procurement.GetSupplierInvoiceDetailUseCase
import id.hyperpos.mobile.application.procurement.ListSupplierInvoicesUseCase
import id.hyperpos.mobile.application.procurement.SupplierInvoiceDetailResult
import id.hyperpos.mobile.application.procurement.SupplierInvoiceListResult
import id.hyperpos.mobile.application.procurement.SupplierInvoicePaymentProofFile
import id.hyperpos.mobile.application.procurement.UploadSupplierInvoicePaymentProofResult
import id.hyperpos.mobile.application.procurement.UploadSupplierInvoicePaymentProofUseCase
import id.hyperpos.mobile.application.product.ProductSearchResult
import id.hyperpos.mobile.application.product.SearchProductsUseCase
import id.hyperpos.mobile.databinding.ActivityMainBinding
import id.hyperpos.mobile.domain.procurement.MobileSupplierInvoiceLine
import id.hyperpos.mobile.domain.procurement.MobileSupplierInvoiceListRow
import id.hyperpos.mobile.domain.procurement.MobileSupplierInvoiceSummary
import id.hyperpos.mobile.domain.product.MobileProductSearchRow
import okhttp3.OkHttpClient
import java.text.NumberFormat
import java.util.Locale
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var selectedSupplierInvoiceId: String? = null
    private var selectedSupplierInvoiceCanUploadProof: Boolean = false

    private val supplierInvoiceProofPicker = registerForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri == null) {
            binding.supplierInvoicePaymentProofStatusText.text = getString(
                id.hyperpos.mobile.R.string.supplier_invoice_upload_proof_ready,
            )
            syncSupplierInvoicePaymentProofAction()
            return@registerForActivityResult
        }

        uploadSupplierInvoicePaymentProof(uri)
    }

    private val apiConfig by lazy {
        MobileApiConfig(baseUrl = "http://127.0.0.1:8000/api/v1")
    }

    private val httpClient by lazy {
        OkHttpClient()
    }

    private val tokenStore by lazy {
        AndroidKeystoreSessionTokenStore(this)
    }

    private val authApi by lazy {
        OkHttpAuthApiClient(
            config = apiConfig,
            httpClient = httpClient,
        )
    }

    private val loginUseCase by lazy {
        LoginUseCase(
            authApi = authApi,
            tokenStore = tokenStore,
        )
    }

    private val logoutUseCase by lazy {
        LogoutUseCase(
            authApi = authApi,
            tokenStore = tokenStore,
        )
    }

    private val supplierInvoiceApi by lazy {
        OkHttpSupplierInvoiceApiClient(
            config = apiConfig,
            httpClient = httpClient,
        )
    }

    private val searchProductsUseCase by lazy {
        SearchProductsUseCase(
            productSearchApi = OkHttpProductSearchApiClient(
                config = apiConfig,
                httpClient = httpClient,
            ),
            tokenStore = tokenStore,
        )
    }

    private val listSupplierInvoicesUseCase by lazy {
        ListSupplierInvoicesUseCase(
            supplierInvoiceApi = supplierInvoiceApi,
            tokenStore = tokenStore,
        )
    }

    private val getSupplierInvoiceDetailUseCase by lazy {
        GetSupplierInvoiceDetailUseCase(
            supplierInvoiceApi = supplierInvoiceApi,
            tokenStore = tokenStore,
        )
    }

    private val uploadSupplierInvoicePaymentProofUseCase by lazy {
        UploadSupplierInvoicePaymentProofUseCase(
            supplierInvoiceApi = supplierInvoiceApi,
            tokenStore = tokenStore,
        )
    }

    private val rupiahFormat by lazy {
        NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID"))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        resetAuthenticatedUi()

        binding.loginButton.setOnClickListener {
            login()
        }

        binding.logoutButton.setOnClickListener {
            logout()
        }

        binding.productSearchButton.setOnClickListener {
            searchProducts()
        }

        binding.supplierInvoiceListButton.setOnClickListener {
            listSupplierInvoices()
        }

        binding.supplierInvoiceDetailButton.setOnClickListener {
            loadSupplierInvoiceDetail()
        }

        binding.supplierInvoicePaymentProofButton.setOnClickListener {
            openSupplierInvoiceProofPicker()
        }
    }

    private fun login() {
        binding.loginButton.isEnabled = false
        binding.statusText.text = "Login berjalan..."

        val request = LoginRequest(
            email = binding.emailInput.text.toString().trim(),
            password = binding.passwordInput.text.toString(),
            deviceName = binding.deviceNameInput.text.toString().trim(),
        )

        thread {
            val result = loginUseCase.execute(request)

            runOnUiThread {
                binding.loginButton.isEnabled = true
                binding.statusText.text = when (result) {
                    is LoginResult.Success -> {
                        showAuthenticatedUi(result.session.actor.role)
                        "Login berhasil: ${result.session.actor.name} (${result.session.actor.role})"
                    }
                    is LoginResult.Failure -> result.message
                }
            }
        }
    }

    private fun logout() {
        binding.logoutButton.isEnabled = false
        binding.statusText.text = "Logout berjalan..."

        thread {
            val result = logoutUseCase.execute()

            runOnUiThread {
                binding.logoutButton.isEnabled = true
                resetAuthenticatedUi()

                binding.statusText.text = when (result) {
                    is LogoutResult.Success -> result.message
                    is LogoutResult.NoSession -> result.message
                    is LogoutResult.Failure -> "${result.message} Sesi lokal dibersihkan."
                }
            }
        }
    }

    private fun searchProducts() {
        binding.productSearchButton.isEnabled = false
        binding.productSearchStatusText.text = "Mencari produk..."
        binding.productSearchResultsText.text = ""

        val query = binding.productSearchInput.text.toString().trim()

        thread {
            val result = searchProductsUseCase.execute(query)

            runOnUiThread {
                binding.productSearchButton.isEnabled = true

                when (result) {
                    is ProductSearchResult.Success -> {
                        binding.productSearchStatusText.text = "Hasil untuk \"${result.query}\" (${result.rows.size}/${result.limit})"
                        binding.productSearchResultsText.text = renderProductRows(result.rows)
                    }
                    is ProductSearchResult.Failure -> {
                        binding.productSearchStatusText.text = result.message
                        binding.productSearchResultsText.text = ""
                    }
                    is ProductSearchResult.Unauthenticated -> handleUnauthenticated(result.message)
                }
            }
        }
    }

    private fun listSupplierInvoices() {
        binding.supplierInvoiceListButton.isEnabled = false
        binding.supplierInvoiceListStatusText.text = "Memuat nota supplier..."
        binding.supplierInvoiceListResultsText.text = ""
        resetSupplierInvoiceDetailSelection()

        val query = binding.supplierInvoiceSearchInput.text.toString()
            .trim()
            .takeIf { it.isNotEmpty() }

        thread {
            val result = listSupplierInvoicesUseCase.execute(
                query = query,
                paymentStatus = "all",
                page = 1,
            )

            runOnUiThread {
                binding.supplierInvoiceListButton.isEnabled = true

                when (result) {
                    is SupplierInvoiceListResult.Success -> {
                        val selectedRow = result.rows.firstOrNull()
                        selectedSupplierInvoiceId = selectedRow?.supplierInvoiceId
                        selectedSupplierInvoiceCanUploadProof = selectedRow?.let(::canUploadPaymentProof) ?: false
                        syncSupplierInvoicePaymentProofAction()
                        binding.supplierInvoiceDetailButton.visibility = if (selectedSupplierInvoiceId.isNullOrBlank()) {
                            View.GONE
                        } else {
                            View.VISIBLE
                        }
                        binding.supplierInvoiceListStatusText.text = "Nota supplier dimuat (${result.rows.size}/${result.perPage})"
                        binding.supplierInvoiceListResultsText.text = renderSupplierInvoiceRows(result.rows)
                    }
                    is SupplierInvoiceListResult.Failure -> {
                        binding.supplierInvoiceListStatusText.text = result.message
                        binding.supplierInvoiceListResultsText.text = ""
                    }
                    is SupplierInvoiceListResult.Unauthenticated -> handleUnauthenticated(result.message)
                }
            }
        }
    }

    private fun loadSupplierInvoiceDetail() {
        val supplierInvoiceId = selectedSupplierInvoiceId
        if (supplierInvoiceId.isNullOrBlank()) {
            binding.supplierInvoiceDetailStatusText.text = "Pilih nota supplier dari daftar terlebih dahulu."
            binding.supplierInvoiceDetailResultsText.text = ""
            return
        }

        binding.supplierInvoiceDetailButton.isEnabled = false
        binding.supplierInvoiceDetailStatusText.text = "Memuat detail nota supplier..."
        binding.supplierInvoiceDetailResultsText.text = ""

        thread {
            val result = getSupplierInvoiceDetailUseCase.execute(supplierInvoiceId)

            runOnUiThread {
                binding.supplierInvoiceDetailButton.isEnabled = true

                when (result) {
                    is SupplierInvoiceDetailResult.Success -> {
                        binding.supplierInvoiceDetailStatusText.text = "Detail nota supplier dimuat"
                        binding.supplierInvoiceDetailResultsText.text = renderSupplierInvoiceDetail(
                            summary = result.summary,
                            lines = result.lines,
                        )
                        if (selectedSupplierInvoiceCanUploadProof) {
                            binding.supplierInvoicePaymentProofStatusText.text = getString(
                                id.hyperpos.mobile.R.string.supplier_invoice_upload_proof_ready,
                            )
                        }
                        syncSupplierInvoicePaymentProofAction()
                    }
                    is SupplierInvoiceDetailResult.Failure -> {
                        binding.supplierInvoiceDetailStatusText.text = result.message
                        binding.supplierInvoiceDetailResultsText.text = ""
                        selectedSupplierInvoiceCanUploadProof = false
                        syncSupplierInvoicePaymentProofAction()
                    }
                    is SupplierInvoiceDetailResult.Unauthenticated -> handleUnauthenticated(result.message)
                }
            }
        }
    }

    private fun showAuthenticatedUi(role: String) {
        setLoginFormVisible(false)
        binding.logoutButton.visibility = View.VISIBLE

        when (role) {
            "admin" -> {
                binding.productSearchContainer.visibility = View.GONE
                binding.supplierInvoiceContainer.visibility = View.VISIBLE
            }
            "kasir" -> {
                binding.productSearchContainer.visibility = View.VISIBLE
                binding.supplierInvoiceContainer.visibility = View.GONE
            }
            else -> {
                binding.productSearchContainer.visibility = View.GONE
                binding.supplierInvoiceContainer.visibility = View.GONE
            }
        }
    }

    private fun handleUnauthenticated(message: String) {
        tokenStore.clear()
        resetAuthenticatedUi()
        binding.statusText.text = message
    }

    private fun resetAuthenticatedUi() {
        setLoginFormVisible(true)

        binding.logoutButton.visibility = View.GONE
        binding.logoutButton.isEnabled = true

        binding.productSearchContainer.visibility = View.GONE
        binding.productSearchButton.isEnabled = true
        binding.productSearchInput.setText("")
        binding.productSearchStatusText.text = getString(id.hyperpos.mobile.R.string.product_search_ready)
        binding.productSearchResultsText.text = ""

        binding.supplierInvoiceContainer.visibility = View.GONE
        binding.supplierInvoiceListButton.isEnabled = true
        binding.supplierInvoiceSearchInput.setText("")
        binding.supplierInvoiceListStatusText.text = getString(id.hyperpos.mobile.R.string.supplier_invoice_ready)
        binding.supplierInvoiceListResultsText.text = ""
        resetSupplierInvoiceDetailSelection()
    }

    private fun setLoginFormVisible(visible: Boolean) {
        val visibility = if (visible) View.VISIBLE else View.GONE

        binding.emailInput.visibility = visibility
        binding.passwordInput.visibility = visibility
        binding.deviceNameInput.visibility = visibility
        binding.loginButton.visibility = visibility
        binding.loginButton.isEnabled = true
    }

    private fun resetSupplierInvoiceDetailSelection() {
        selectedSupplierInvoiceId = null
        binding.supplierInvoiceDetailButton.visibility = View.GONE
        binding.supplierInvoiceDetailButton.isEnabled = true
        binding.supplierInvoiceDetailStatusText.text = getString(id.hyperpos.mobile.R.string.supplier_invoice_detail_ready)
        binding.supplierInvoiceDetailResultsText.text = ""
        selectedSupplierInvoiceCanUploadProof = false
        binding.supplierInvoicePaymentProofStatusText.text = ""
        syncSupplierInvoicePaymentProofAction()
    }

    private fun openSupplierInvoiceProofPicker() {
        val supplierInvoiceId = selectedSupplierInvoiceId
        if (supplierInvoiceId.isNullOrBlank()) {
            binding.supplierInvoicePaymentProofStatusText.text = "Pilih nota supplier terlebih dahulu."
            syncSupplierInvoicePaymentProofAction()
            return
        }

        if (!selectedSupplierInvoiceCanUploadProof) {
            binding.supplierInvoicePaymentProofStatusText.text = "Nota supplier ini tidak bisa diunggah bukti pembayarannya."
            syncSupplierInvoicePaymentProofAction()
            return
        }

        supplierInvoiceProofPicker.launch(SUPPLIER_INVOICE_PROOF_MIME_TYPES)
    }

    private fun uploadSupplierInvoicePaymentProof(uri: Uri) {
        val supplierInvoiceId = selectedSupplierInvoiceId
        if (supplierInvoiceId.isNullOrBlank()) {
            binding.supplierInvoicePaymentProofStatusText.text = "Pilih nota supplier terlebih dahulu."
            syncSupplierInvoicePaymentProofAction()
            return
        }

        val proofFile = readSupplierInvoicePaymentProofFile(uri)
        if (proofFile == null) {
            binding.supplierInvoicePaymentProofStatusText.text = "File bukti pembayaran harus JPG, PNG, atau PDF maksimal 2 MB."
            syncSupplierInvoicePaymentProofAction()
            return
        }

        binding.supplierInvoicePaymentProofButton.isEnabled = false
        binding.supplierInvoicePaymentProofStatusText.text = "Mengunggah bukti pembayaran supplier..."
        syncSupplierInvoicePaymentProofAction()

        thread {
            val result = uploadSupplierInvoicePaymentProofUseCase.execute(
                supplierInvoiceId = supplierInvoiceId,
                proofFiles = listOf(proofFile),
            )

            runOnUiThread {
                binding.supplierInvoicePaymentProofButton.isEnabled = true

                when (result) {
                    is UploadSupplierInvoicePaymentProofResult.Success -> {
                        selectedSupplierInvoiceCanUploadProof = false
                        binding.supplierInvoicePaymentProofStatusText.text = listOf(
                            result.message,
                            "Status pembayaran: ${paymentStatusLabel(result.upload.outstandingRupiah)}",
                            "Lampiran bukti: ${result.upload.attachmentCount}",
                        ).joinToString(separator = "\n")
                        syncSupplierInvoicePaymentProofAction()
                        refreshSupplierInvoiceListKeepingSelection()
                        loadSupplierInvoiceDetail()
                    }
                    is UploadSupplierInvoicePaymentProofResult.Failure -> {
                        binding.supplierInvoicePaymentProofStatusText.text = result.message
                        syncSupplierInvoicePaymentProofAction()
                    }
                    is UploadSupplierInvoicePaymentProofResult.Unauthenticated -> handleUnauthenticated(result.message)
                }
            }
        }
    }

    private fun refreshSupplierInvoiceListKeepingSelection() {
        val supplierInvoiceId = selectedSupplierInvoiceId ?: return
        val query = binding.supplierInvoiceSearchInput.text.toString()
            .trim()
            .takeIf { it.isNotEmpty() }

        thread {
            val result = listSupplierInvoicesUseCase.execute(
                query = query,
                paymentStatus = "all",
                page = 1,
            )

            runOnUiThread {
                when (result) {
                    is SupplierInvoiceListResult.Success -> {
                        val selectedRow = result.rows.firstOrNull { row ->
                            row.supplierInvoiceId == supplierInvoiceId
                        }
                        selectedSupplierInvoiceCanUploadProof = selectedRow?.let(::canUploadPaymentProof) ?: false
                        binding.supplierInvoiceListStatusText.text = "Nota supplier dimuat (${result.rows.size}/${result.perPage})"
                        binding.supplierInvoiceListResultsText.text = renderSupplierInvoiceRows(result.rows)
                        syncSupplierInvoicePaymentProofAction()
                    }
                    is SupplierInvoiceListResult.Failure -> {
                        binding.supplierInvoiceListStatusText.text = result.message
                    }
                    is SupplierInvoiceListResult.Unauthenticated -> handleUnauthenticated(result.message)
                }
            }
        }
    }

    private fun readSupplierInvoicePaymentProofFile(uri: Uri): SupplierInvoicePaymentProofFile? {
        val mediaType = contentResolver.getType(uri) ?: return null
        if (!SUPPLIER_INVOICE_PROOF_MIME_TYPES.contains(mediaType)) {
            return null
        }

        val bytes = contentResolver.openInputStream(uri)?.use { input ->
            input.readBytes()
        } ?: return null

        if (bytes.isEmpty() || bytes.size > MAX_SUPPLIER_INVOICE_PROOF_BYTES) {
            return null
        }

        return SupplierInvoicePaymentProofFile(
            fileName = resolveSupplierInvoicePaymentProofFileName(uri, mediaType),
            mediaType = mediaType,
            bytes = bytes,
        )
    }

    private fun resolveSupplierInvoicePaymentProofFileName(uri: Uri, mediaType: String): String {
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

    private fun canUploadPaymentProof(row: MobileSupplierInvoiceListRow): Boolean {
        return row.outstandingRupiah > 0L &&
            row.canRecordPayment &&
            !row.hasUploadedProof
    }

    private fun syncSupplierInvoicePaymentProofAction() {
        binding.supplierInvoicePaymentProofButton.visibility = if (selectedSupplierInvoiceCanUploadProof) {
            View.VISIBLE
        } else {
            View.GONE
        }

        val statusMessage = binding.supplierInvoicePaymentProofStatusText.text.toString()
        binding.supplierInvoicePaymentProofStatusText.visibility = if (
            selectedSupplierInvoiceCanUploadProof || statusMessage.isNotBlank()
        ) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    private fun renderProductRows(rows: List<MobileProductSearchRow>): String {
        if (rows.isEmpty()) {
            return "Tidak ada produk ditemukan."
        }

        return rows.joinToString(separator = "\n\n") { row ->
            listOf(
                row.label,
                "Stok: ${row.availableStock}",
                "Harga jual: Rp ${rupiahFormat.format(row.defaultUnitPriceRupiah)}",
            ).joinToString(separator = "\n")
        }
    }

    private fun renderSupplierInvoiceRows(rows: List<MobileSupplierInvoiceListRow>): String {
        if (rows.isEmpty()) {
            return "Tidak ada nota supplier ditemukan."
        }

        return rows.joinToString(separator = "\n\n") { row ->
            val supplierName = row.supplierNamaPtPengirimCurrent
                ?: row.supplierNamaPtPengirimSnapshot
                ?: "Supplier tidak diketahui"

            listOf(
                "Nomor faktur: ${row.nomorFaktur}",
                "Supplier: $supplierName",
                "Status pembayaran: ${paymentStatusLabel(row.outstandingRupiah)}",
            ).joinToString(separator = "\n")
        }
    }

    private fun renderSupplierInvoiceDetail(
        summary: MobileSupplierInvoiceSummary,
        lines: List<MobileSupplierInvoiceLine>,
    ): String {
        val supplierName = summary.supplierNamaPtPengirimCurrent
            ?: summary.supplierNamaPtPengirimSnapshot
            ?: "Supplier tidak diketahui"

        return listOf(
            listOf(
                "Nomor faktur: ${summary.nomorFaktur}",
                "Supplier: $supplierName",
                "Total: Rp ${rupiahFormat.format(summary.grandTotalRupiah)}",
                "Status pembayaran: ${paymentStatusLabel(summary.outstandingRupiah)}",
            ).joinToString(separator = "\n"),
            "Rincian barang:",
            renderSupplierInvoiceLines(lines),
        ).joinToString(separator = "\n\n")
    }

    private fun renderSupplierInvoiceLines(lines: List<MobileSupplierInvoiceLine>): String {
        if (lines.isEmpty()) {
            return "Tidak ada rincian barang."
        }

        return lines.take(5).joinToString(separator = "\n\n") { line ->
            listOf(
                line.namaBarang,
                "Qty: ${line.qtyPcs}",
                "Harga satuan: Rp ${rupiahFormat.format(line.unitCostRupiah)}",
                "Subtotal: Rp ${rupiahFormat.format(line.lineTotalRupiah)}",
            ).joinToString(separator = "\n")
        }
    }

    private fun paymentStatusLabel(outstandingRupiah: Long): String {
        return if (outstandingRupiah <= 0L) {
            "Lunas"
        } else {
            "Belum lunas"
        }
    }

    private companion object {
        private val SUPPLIER_INVOICE_PROOF_MIME_TYPES = arrayOf(
            "image/jpeg",
            "image/png",
            "application/pdf",
        )
        private const val MAX_SUPPLIER_INVOICE_PROOF_BYTES = 2 * 1024 * 1024
    }
}

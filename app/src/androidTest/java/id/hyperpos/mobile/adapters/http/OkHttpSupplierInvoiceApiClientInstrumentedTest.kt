package id.hyperpos.mobile.adapters.http

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import id.hyperpos.mobile.adapters.storage.AndroidKeystoreSessionTokenStore
import id.hyperpos.mobile.application.auth.LoginRequest
import id.hyperpos.mobile.application.auth.LoginResult
import id.hyperpos.mobile.application.auth.LoginUseCase
import id.hyperpos.mobile.application.procurement.GetSupplierInvoiceDetailUseCase
import id.hyperpos.mobile.application.procurement.ListSupplierInvoicesUseCase
import id.hyperpos.mobile.application.procurement.SupplierInvoiceDetailResult
import id.hyperpos.mobile.application.procurement.SupplierInvoiceListResult
import okhttp3.OkHttpClient
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OkHttpSupplierInvoiceApiClientInstrumentedTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val tokenStore = AndroidKeystoreSessionTokenStore(context)
    private val httpClient = OkHttpClient()
    private val config = MobileApiConfig(baseUrl = "http://127.0.0.1:8000/api/v1")
    private val authApi = OkHttpAuthApiClient(
        config = config,
        httpClient = httpClient,
    )
    private val supplierInvoiceApi = OkHttpSupplierInvoiceApiClient(
        config = config,
        httpClient = httpClient,
    )

    @After
    fun tearDown() {
        tokenStore.clear()
    }

    @Test
    fun adminCanReadSupplierInvoicesAndDetailUsingStoredToken() {
        tokenStore.clear()

        val login = LoginUseCase(
            authApi = authApi,
            tokenStore = tokenStore,
        ).execute(
            LoginRequest(
                email = "mobile-admin-android-supplier-invoice@example.test",
                password = "MobileAdminSmoke123!",
                deviceName = "android-supplier-invoice-proof",
            ),
        )

        when (login) {
            is LoginResult.Success -> assertEquals("admin", login.session.actor.role)
            is LoginResult.Failure -> fail("Expected admin login success for supplier invoice smoke: ${login.message}")
        }

        val listResult = ListSupplierInvoicesUseCase(
            supplierInvoiceApi = supplierInvoiceApi,
            tokenStore = tokenStore,
        ).execute(
            paymentStatus = "all",
            page = 1,
        )

        val firstSupplierInvoiceId = when (listResult) {
            is SupplierInvoiceListResult.Success -> {
                assertEquals(1, listResult.page)
                assertEquals(10, listResult.perPage)
                assertEquals("all", listResult.paymentStatus)
                assertTrue("Expected at least one supplier invoice row.", listResult.rows.isNotEmpty())

                val firstRow = listResult.rows.first()
                assertTrue("Expected supplier invoice id.", firstRow.supplierInvoiceId.isNotBlank())
                assertTrue("Expected nomor faktur.", firstRow.nomorFaktur.isNotBlank())
                assertTrue("Expected non-negative grand total.", firstRow.grandTotalRupiah >= 0L)
                assertTrue("Expected non-negative paid total.", firstRow.totalPaidRupiah >= 0L)
                assertTrue("Expected non-negative outstanding total.", firstRow.outstandingRupiah >= 0L)
                assertTrue("Expected policy state.", firstRow.policyState.isNotBlank())

                firstRow.supplierInvoiceId
            }
            is SupplierInvoiceListResult.Failure -> {
                fail("Expected supplier invoice list success using stored token: ${listResult.message}")
                return
            }
            is SupplierInvoiceListResult.Unauthenticated -> {
                fail("Expected authenticated supplier invoice list using stored token: ${listResult.message}")
                return
            }
        }

        val detailResult = GetSupplierInvoiceDetailUseCase(
            supplierInvoiceApi = supplierInvoiceApi,
            tokenStore = tokenStore,
        ).execute(firstSupplierInvoiceId)

        when (detailResult) {
            is SupplierInvoiceDetailResult.Success -> {
                assertEquals(firstSupplierInvoiceId, detailResult.summary.supplierInvoiceId)
                assertTrue("Expected nomor faktur in detail.", detailResult.summary.nomorFaktur.isNotBlank())
                assertTrue("Expected non-negative detail grand total.", detailResult.summary.grandTotalRupiah >= 0L)
                assertTrue("Expected non-negative detail outstanding total.", detailResult.summary.outstandingRupiah >= 0L)
                assertTrue("Expected detail policy state.", detailResult.summary.policyState.isNotBlank())
                assertTrue(
                    "Expected valid supplier invoice lines when present.",
                    detailResult.lines.all { line ->
                        line.supplierInvoiceId == firstSupplierInvoiceId &&
                            line.namaBarang.isNotBlank() &&
                            line.qtyPcs >= 0 &&
                            line.lineTotalRupiah >= 0L &&
                            line.unitCostRupiah >= 0L
                    },
                )
            }
            is SupplierInvoiceDetailResult.Failure -> {
                fail("Expected supplier invoice detail success using stored token: ${detailResult.message}")
            }
            is SupplierInvoiceDetailResult.Unauthenticated -> {
                fail("Expected authenticated supplier invoice detail using stored token: ${detailResult.message}")
            }
        }
    }
}

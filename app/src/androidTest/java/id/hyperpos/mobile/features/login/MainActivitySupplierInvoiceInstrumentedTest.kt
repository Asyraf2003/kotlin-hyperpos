package id.hyperpos.mobile.features.login

import android.content.Context
import android.os.SystemClock
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withSubstring
import androidx.test.ext.junit.runners.AndroidJUnit4
import id.hyperpos.mobile.R
import id.hyperpos.mobile.adapters.http.MobileApiConfig
import id.hyperpos.mobile.adapters.http.OkHttpAuthApiClient
import id.hyperpos.mobile.adapters.storage.AndroidKeystoreSessionTokenStore
import id.hyperpos.mobile.application.auth.LogoutResult
import okhttp3.OkHttpClient
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.not
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivitySupplierInvoiceInstrumentedTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val tokenStore = AndroidKeystoreSessionTokenStore(context)
    private val authApi = OkHttpAuthApiClient(
        config = MobileApiConfig(baseUrl = "http://127.0.0.1:8000/api/v1"),
        httpClient = OkHttpClient(),
    )

    @Before
    fun setUp() {
        tokenStore.clear()
    }

    @After
    fun tearDown() {
        tokenStore.clear()
    }

    @Test
    fun adminCanLoginLoadSupplierInvoicesAndLoadFirstInvoiceDetailFromUi() {
        ActivityScenario.launch(MainActivity::class.java).use {
            loginAsAdmin()

            waitUntil {
                onView(withId(R.id.logoutButton)).check(matches(isDisplayed()))
            }
            waitUntil {
                onView(withId(R.id.productSearchContainer)).check(matches(not(isDisplayed())))
            }
            waitUntil {
                onView(withId(R.id.supplierInvoiceContainer)).check(matches(isDisplayed()))
            }
            waitUntil {
                onView(withId(R.id.supplierInvoiceSearchInput)).check(matches(isDisplayed()))
            }

            waitUntil {
                onView(withId(R.id.supplierInvoiceListStatusText)).check(
                    matches(withSubstring("Nota supplier dimuat")),
                )
            }
            waitUntil {
                onView(withId(R.id.supplierInvoiceRowsContainer)).check(matches(isDisplayed()))
            }
            waitUntil {
                onView(allOf(withText(containsString("Nomor faktur:")), isDisplayed()))
                    .check(matches(isDisplayed()))
            }

            onView(allOf(withText(containsString("Nomor faktur:")), isDisplayed())).perform(
                scrollTo(),
                click(),
            )

            waitUntil {
                onView(withId(R.id.supplierInvoiceDetailStatusText)).check(
                    matches(withSubstring("Detail nota supplier dimuat")),
                )
            }
            waitUntil {
                onView(withId(R.id.supplierInvoiceDetailResultsText)).check(
                    matches(withSubstring("Nomor faktur:")),
                )
            }
            waitUntil {
                onView(withId(R.id.supplierInvoiceDetailResultsText)).check(
                    matches(withSubstring("Total: Rp")),
                )
            }
            waitUntil {
                onView(withId(R.id.supplierInvoiceDetailResultsText)).check(
                    matches(withSubstring("Status pembayaran:")),
                )
            }
            waitUntil {
                onView(withId(R.id.supplierInvoiceDetailResultsText)).check(
                    matches(withSubstring("Rincian barang:")),
                )
            }
        }
    }

    @Test
    fun adminCanSearchSupplierInvoicesByNomorFakturFromUi() {
        ActivityScenario.launch(MainActivity::class.java).use {
            loginAsAdmin()

            waitUntil {
                onView(withId(R.id.supplierInvoiceContainer)).check(matches(isDisplayed()))
            }
            waitUntil {
                onView(withId(R.id.productSearchContainer)).check(matches(not(isDisplayed())))
            }

            onView(withId(R.id.supplierInvoiceSearchInput)).perform(
                scrollTo(),
                replaceText("SI-BL-20260502-067"),
            )

            waitUntil {
                onView(withId(R.id.supplierInvoiceListStatusText)).check(
                    matches(withSubstring("Nota supplier dimuat")),
                )
            }
            waitUntil {
                onView(allOf(withText(containsString("Nomor faktur: SI-BL-20260502-067")), isDisplayed()))
                    .check(matches(isDisplayed()))
            }
            waitUntil {
                onView(withId(R.id.supplierInvoiceListResultsText)).check(
                    matches(withSubstring("Status pembayaran:")),
                )
            }
        }
    }


    @Test
    fun logoutClearsSupplierInvoiceListAndDetailUiState() {
        ActivityScenario.launch(MainActivity::class.java).use {
            loginAsAdmin()

            waitUntil {
                onView(withId(R.id.supplierInvoiceContainer)).check(matches(isDisplayed()))
            }

            waitUntil {
                onView(withId(R.id.supplierInvoiceListStatusText)).check(
                    matches(withSubstring("Nota supplier dimuat")),
                )
            }
            waitUntil {
                onView(allOf(withText(containsString("Nomor faktur:")), isDisplayed()))
                    .check(matches(isDisplayed()))
            }

            onView(allOf(withText(containsString("Nomor faktur:")), isDisplayed())).perform(
                scrollTo(),
                click(),
            )

            waitUntil {
                onView(withId(R.id.supplierInvoiceDetailStatusText)).check(
                    matches(withSubstring("Detail nota supplier dimuat")),
                )
            }

            onView(withId(R.id.logoutButton)).perform(
                scrollTo(),
                click(),
            )

            waitUntil {
                onView(withId(R.id.supplierInvoiceContainer)).check(matches(not(isDisplayed())))
            }
            waitUntil {
                onView(withId(R.id.productSearchContainer)).check(matches(not(isDisplayed())))
            }
            waitUntil {
                onView(withId(R.id.logoutButton)).check(matches(not(isDisplayed())))
            }

            assertNull(tokenStore.read())
        }
    }

    @Test
    fun revokedTokenClearsSupplierInvoiceUiState() {
        ActivityScenario.launch(MainActivity::class.java).use {
            loginAsAdmin()

            waitUntil {
                onView(withId(R.id.supplierInvoiceContainer)).check(matches(isDisplayed()))
            }

            val storedToken = tokenStore.read()
            assertNotNull(storedToken)

            val logoutResult = authApi.logout(storedToken!!)

            when (logoutResult) {
                is LogoutResult.Success -> Unit
                is LogoutResult.NoSession -> {
                    throw AssertionError("Expected backend logout success before supplier invoice invalid-session UI proof: ${logoutResult.message}")
                }
                is LogoutResult.Failure -> {
                    throw AssertionError("Expected backend logout success before supplier invoice invalid-session UI proof: ${logoutResult.message}")
                }
            }

            waitUntil {
                onView(withId(R.id.statusText)).check(
                    matches(withSubstring("Autentikasi diperlukan")),
                )
            }
            waitUntil {
                onView(withId(R.id.supplierInvoiceContainer)).check(matches(not(isDisplayed())))
            }
            waitUntil {
                onView(withId(R.id.productSearchContainer)).check(matches(not(isDisplayed())))
            }
            waitUntil {
                onView(withId(R.id.logoutButton)).check(matches(not(isDisplayed())))
            }

            assertNull(tokenStore.read())
        }
    }

    @Test
    fun cashierLoginDoesNotShowSupplierInvoicesUi() {
        ActivityScenario.launch(MainActivity::class.java).use {
            onView(withId(R.id.emailInput)).perform(
                replaceText("mobile-android-smoke@example.test"),
            )
            onView(withId(R.id.passwordInput)).perform(
                replaceText("MobileSmoke123!"),
            )
            onView(withId(R.id.loginButton)).perform(click())

            waitUntil {
                onView(withId(R.id.logoutButton)).check(matches(isDisplayed()))
            }
            waitUntil {
                onView(withId(R.id.productSearchContainer)).check(matches(isDisplayed()))
            }
            waitUntil {
                onView(withId(R.id.supplierInvoiceContainer)).check(matches(not(isDisplayed())))
            }
        }
    }

    private fun loginAsAdmin() {
        onView(withId(R.id.emailInput)).perform(
            replaceText("mobile-admin-android-supplier-invoice@example.test"),
        )
        onView(withId(R.id.passwordInput)).perform(
            replaceText("MobileAdminSmoke123!"),
        )
        onView(withId(R.id.loginButton)).perform(click())
    }

    private fun waitUntil(
        timeoutMs: Long = 20_000,
        assertion: () -> Unit,
    ) {
        val deadline = SystemClock.elapsedRealtime() + timeoutMs
        var lastError: Throwable? = null

        while (SystemClock.elapsedRealtime() < deadline) {
            try {
                assertion()
                return
            } catch (error: Throwable) {
                lastError = error
                SystemClock.sleep(250)
            }
        }

        val timeout = AssertionError("Timed out waiting for Supplier Invoice UI condition.")
        if (lastError != null) {
            timeout.initCause(lastError)
        }
        throw timeout
    }
}

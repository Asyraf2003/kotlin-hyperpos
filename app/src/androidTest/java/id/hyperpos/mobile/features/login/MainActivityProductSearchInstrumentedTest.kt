package id.hyperpos.mobile.features.login

import android.content.Context
import android.os.SystemClock
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.clearText
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withSubstring
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import id.hyperpos.mobile.R
import id.hyperpos.mobile.adapters.http.MobileApiConfig
import id.hyperpos.mobile.adapters.http.OkHttpAuthApiClient
import id.hyperpos.mobile.adapters.storage.AndroidKeystoreSessionTokenStore
import id.hyperpos.mobile.application.auth.LogoutResult
import org.hamcrest.Matchers.not
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import okhttp3.OkHttpClient
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityProductSearchInstrumentedTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val tokenStore = AndroidKeystoreSessionTokenStore(context)

    @Before
    fun setUp() {
        tokenStore.clear()
    }

    @After
    fun tearDown() {
        tokenStore.clear()
    }

    @Test
    fun cashierCanLoginAndSearchProductsFromUi() {
        ActivityScenario.launch(MainActivity::class.java).use {
            onView(withId(R.id.emailInput)).perform(
                replaceText("mobile-android-smoke@example.test"),
            )
            onView(withId(R.id.passwordInput)).perform(
                replaceText("MobileSmoke123!"),
            )
            onView(withId(R.id.deviceNameInput)).perform(
                clearText(),
                replaceText("android-product-search-ui-regression"),
            )

            onView(withId(R.id.loginButton)).perform(click())

            waitUntil {
                onView(withId(R.id.productSearchContainer)).check(matches(isDisplayed()))
            }
            waitUntil {
                onView(withId(R.id.supplierInvoiceContainer)).check(matches(not(isDisplayed())))
            }

            onView(withId(R.id.productSearchInput)).perform(
                scrollTo(),
                replaceText("pis"),
            )
            onView(withId(R.id.productSearchButton)).perform(
                scrollTo(),
                click(),
            )

            waitUntil {
                onView(withId(R.id.productSearchStatusText)).check(
                    matches(withSubstring("Hasil untuk \"pis\"")),
                )
            }
            waitUntil {
                onView(withId(R.id.productSearchResultsText)).check(
                    matches(withSubstring("Harga jual: Rp 185.000")),
                )
            }
            waitUntil {
                onView(withId(R.id.productSearchResultsText)).check(
                    matches(withSubstring("Harga jual: Rp 275.000")),
                )
            }
        }
    }


    @Test
    fun cashierCanLogoutFromUiAndClearLocalSession() {
        ActivityScenario.launch(MainActivity::class.java).use {
            onView(withId(R.id.emailInput)).perform(
                replaceText("mobile-android-smoke@example.test"),
            )
            onView(withId(R.id.passwordInput)).perform(
                replaceText("MobileSmoke123!"),
            )
            onView(withId(R.id.deviceNameInput)).perform(
                clearText(),
                replaceText("android-logout-ui-regression"),
            )

            onView(withId(R.id.loginButton)).perform(click())

            waitUntil {
                onView(withId(R.id.logoutButton)).check(matches(isDisplayed()))
            }
            waitUntil {
                onView(withId(R.id.productSearchContainer)).check(matches(isDisplayed()))
            }

            assertNotNull(tokenStore.read())

            onView(withId(R.id.logoutButton)).perform(
                scrollTo(),
                click(),
            )

            waitUntil {
                onView(withId(R.id.statusText)).check(matches(withText("Logout berhasil.")))
            }
            waitUntil {
                onView(withId(R.id.logoutButton)).check(matches(not(isDisplayed())))
            }
            waitUntil {
                onView(withId(R.id.productSearchContainer)).check(matches(not(isDisplayed())))
            }
            waitUntil {
                onView(withId(R.id.supplierInvoiceContainer)).check(matches(not(isDisplayed())))
            }

            assertNull(tokenStore.read())
        }
    }


    @Test
    fun revokedSessionFromProductSearchClearsLocalTokenAndRequiresLogin() {
        ActivityScenario.launch(MainActivity::class.java).use {
            onView(withId(R.id.emailInput)).perform(
                replaceText("mobile-android-smoke@example.test"),
            )
            onView(withId(R.id.passwordInput)).perform(
                replaceText("MobileSmoke123!"),
            )
            onView(withId(R.id.deviceNameInput)).perform(
                clearText(),
                replaceText("android-invalid-session-ui-regression"),
            )

            onView(withId(R.id.loginButton)).perform(click())

            waitUntil {
                onView(withId(R.id.logoutButton)).check(matches(isDisplayed()))
            }
            waitUntil {
                onView(withId(R.id.productSearchContainer)).check(matches(isDisplayed()))
            }

            val storedToken = tokenStore.read()
            assertNotNull(storedToken)

            val logout = OkHttpAuthApiClient(
                config = MobileApiConfig(baseUrl = "http://127.0.0.1:8000/api/v1"),
                httpClient = OkHttpClient(),
            ).logout(storedToken!!)

            when (logout) {
                is LogoutResult.Success -> Unit
                is LogoutResult.NoSession -> throw AssertionError("Expected backend logout success before invalid-session UI proof: ${logout.message}")
                is LogoutResult.Failure -> throw AssertionError("Expected backend logout success before invalid-session UI proof: ${logout.message}")
            }

            onView(withId(R.id.productSearchInput)).perform(
                scrollTo(),
                replaceText("ban"),
            )
            onView(withId(R.id.productSearchButton)).perform(
                scrollTo(),
                click(),
            )

            waitUntil {
                onView(withId(R.id.statusText)).check(matches(withText("Autentikasi diperlukan.")))
            }
            waitUntil {
                onView(withId(R.id.logoutButton)).check(matches(not(isDisplayed())))
            }
            waitUntil {
                onView(withId(R.id.productSearchContainer)).check(matches(not(isDisplayed())))
            }

            assertNull(tokenStore.read())
        }
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

        val timeout = AssertionError("Timed out waiting for Product Search UI condition.")
        if (lastError != null) {
            timeout.initCause(lastError)
        }
        throw timeout
    }
}

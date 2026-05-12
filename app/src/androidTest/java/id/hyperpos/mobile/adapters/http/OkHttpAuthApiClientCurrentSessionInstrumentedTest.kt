package id.hyperpos.mobile.adapters.http

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import id.hyperpos.mobile.adapters.storage.AndroidKeystoreSessionTokenStore
import id.hyperpos.mobile.application.auth.CurrentSessionResult
import id.hyperpos.mobile.application.auth.CurrentSessionUseCase
import id.hyperpos.mobile.application.auth.LoginRequest
import id.hyperpos.mobile.application.auth.LoginResult
import id.hyperpos.mobile.application.auth.LoginUseCase
import okhttp3.OkHttpClient
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OkHttpAuthApiClientCurrentSessionInstrumentedTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val tokenStore = AndroidKeystoreSessionTokenStore(context)
    private val authApi = OkHttpAuthApiClient(
        config = MobileApiConfig(baseUrl = "http://127.0.0.1:8000/api/v1"),
        httpClient = OkHttpClient(),
    )

    @After
    fun tearDown() {
        tokenStore.clear()
    }

    @Test
    fun loginSavesTokenAndCurrentSessionUsesStoredToken() {
        tokenStore.clear()

        val login = LoginUseCase(
            authApi = authApi,
            tokenStore = tokenStore,
        ).execute(
            LoginRequest(
                email = "mobile-android-smoke@example.test",
                password = "MobileSmoke123!",
                deviceName = "android-me-readback-proof",
            ),
        )

        when (login) {
            is LoginResult.Success -> Unit
            is LoginResult.Failure -> fail("Expected login success for Android current-session smoke: ${login.message}")
        }

        val storedToken = tokenStore.read()
        assertNotNull(storedToken)
        assertTrue(storedToken!!.isNotBlank())

        val currentSession = CurrentSessionUseCase(
            authApi = authApi,
            tokenStore = tokenStore,
        ).execute()

        when (currentSession) {
            is CurrentSessionResult.Success -> {
                assertEquals("Mobile Android Smoke", currentSession.actor.name)
                assertEquals("mobile-android-smoke@example.test", currentSession.actor.email)
                assertEquals("kasir", currentSession.actor.role)
            }
            is CurrentSessionResult.Failure -> {
                fail("Expected current session success using stored token: ${currentSession.message}")
            }
        }
    }
}

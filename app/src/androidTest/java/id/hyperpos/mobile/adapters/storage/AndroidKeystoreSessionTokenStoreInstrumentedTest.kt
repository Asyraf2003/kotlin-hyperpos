package id.hyperpos.mobile.adapters.storage

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AndroidKeystoreSessionTokenStoreInstrumentedTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val store = AndroidKeystoreSessionTokenStore(context)

    @After
    fun tearDown() {
        store.clear()
    }

    @Test
    fun readReturnsNullWhenNoTokenIsStored() {
        store.clear()

        assertNull(store.read())
    }

    @Test
    fun saveThenReadReturnsStoredToken() {
        store.clear()
        val fakeToken = "fake-token-for-keystore-readback-proof"

        store.save(fakeToken)

        assertEquals(fakeToken, store.read())
    }
}

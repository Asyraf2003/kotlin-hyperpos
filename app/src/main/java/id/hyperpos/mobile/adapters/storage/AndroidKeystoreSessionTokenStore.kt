package id.hyperpos.mobile.adapters.storage

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import id.hyperpos.mobile.application.ports.SessionTokenStore
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class AndroidKeystoreSessionTokenStore(
    context: Context,
) : SessionTokenStore {
    private val appContext = context.applicationContext
    private val preferences = appContext.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)

    override fun save(token: String) {
        require(token.isNotBlank()) { "Token must not be blank." }

        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateSecretKey())

        val cipherText = cipher.doFinal(token.toByteArray(Charsets.UTF_8))
        preferences.edit()
            .putString(KEY_IV, Base64.encodeToString(cipher.iv, Base64.NO_WRAP))
            .putString(KEY_CIPHER_TEXT, Base64.encodeToString(cipherText, Base64.NO_WRAP))
            .apply()
    }

    override fun read(): String? {
        val encodedIv = preferences.getString(KEY_IV, null) ?: return null
        val encodedCipherText = preferences.getString(KEY_CIPHER_TEXT, null) ?: return null

        return try {
            val iv = Base64.decode(encodedIv, Base64.NO_WRAP)
            val cipherText = Base64.decode(encodedCipherText, Base64.NO_WRAP)

            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, getOrCreateSecretKey(), GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv))

            String(cipher.doFinal(cipherText), Charsets.UTF_8)
        } catch (_: Exception) {
            clear()
            null
        }
    }

    override fun clear() {
        preferences.edit()
            .remove(KEY_IV)
            .remove(KEY_CIPHER_TEXT)
            .apply()
    }

    private fun getOrCreateSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply {
            load(null)
        }

        val existingKey = keyStore.getKey(KEY_ALIAS, null)
        if (existingKey is SecretKey) {
            return existingKey
        }

        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        val keySpec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(AES_KEY_SIZE_BITS)
            .build()

        keyGenerator.init(keySpec)
        return keyGenerator.generateKey()
    }

    private companion object {
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val KEY_ALIAS = "hyperpos_mobile_api_token_v1"
        private const val PREFERENCE_NAME = "hyperpos_secure_session"
        private const val KEY_IV = "token_iv"
        private const val KEY_CIPHER_TEXT = "token_cipher_text"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val GCM_TAG_LENGTH_BITS = 128
        private const val AES_KEY_SIZE_BITS = 256
    }
}

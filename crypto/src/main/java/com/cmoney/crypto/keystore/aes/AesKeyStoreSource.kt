package com.cmoney.crypto.keystore.aes

import android.util.Base64
import com.cmoney.crypto.keystore.KeyStoreSharedPreferencesHelper
import com.cmoney.crypto.model.CipherModel
import com.cmoney.crypto.model.CipherParam
import com.cmoney.crypto.variable.SecurityKeySet
import java.security.SecureRandom

class AesKeyStoreSource(
    keySets: List<SecurityKeySet.AesKeySet>,
    private val aesAlgorithm: String,
    private val sharedPreferences: KeyStoreSharedPreferencesHelper
) {

    private val keyMemoryMap = mutableMapOf<String, ByteArray>()

    init {
        keySets.forEach {
            val saveKey = sharedPreferences.getAesKey(it.aesKey)
            if (saveKey.isBlank()) {
                generateAESKey(it)
            }
        }
    }

    private fun generateAESKey(keySet: SecurityKeySet.AesKeySet) {
        // Generate AES-Key
        val aesKey = ByteArray(32)
        val secureRandom = SecureRandom()
        secureRandom.nextBytes(aesKey)

        // Generate 12 bytes iv then save to SharedPrefs
        val generated = secureRandom.generateSeed(16)
        val iv = Base64.encodeToString(generated, Base64.DEFAULT)
        sharedPreferences.setIv(keySet.ivKey, iv)

        // Encrypt AES-Key with RSA Public Key then save to SharedPrefs
        val encryptAESKey = Base64.encodeToString(aesKey, Base64.DEFAULT)
        sharedPreferences.setAESKey(keySet.aesKey, encryptAESKey)
    }

    private fun getAESKey(keySet: SecurityKeySet.AesKeySet): ByteArray {
        val encryptedKey = Base64.decode(sharedPreferences.getAesKey(keySet.aesKey), Base64.DEFAULT)
        keyMemoryMap[keySet.aesKey] = encryptedKey
        return encryptedKey
    }

    private fun getIv(keySet: SecurityKeySet.AesKeySet): ByteArray {
        return Base64.decode(sharedPreferences.getIv(keySet.ivKey), Base64.DEFAULT)
    }

    fun encryptAES(keySet: SecurityKeySet.AesKeySet, plainText: String): Result<String> {
        val cipherModel =
            CipherModel(
                CipherParam.Aes(
                    aesAlgorithm,
                    getAesKeyFormIOorCache(keySet),
                    getIv(keySet)
                )
            )
        return cipherModel.encrypt(plainText)
    }

    fun decryptAES(keySet: SecurityKeySet.AesKeySet, encryptedText: String): Result<String> {
        val cipherModel =
            CipherModel(
                CipherParam.Aes(
                    aesAlgorithm,
                    getAesKeyFormIOorCache(keySet),
                    getIv(keySet)
                )
            )
        return cipherModel.decrypt(encryptedText)
    }

    private fun getAesKeyFormIOorCache(keySet: SecurityKeySet.AesKeySet): ByteArray {
        return keyMemoryMap[keySet.aesKey] ?: getAESKey(keySet)
    }

    companion object {

        private const val TAG = "KeyStoreTest"
    }
}
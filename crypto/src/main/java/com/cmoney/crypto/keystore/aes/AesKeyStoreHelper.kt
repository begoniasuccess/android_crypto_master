package com.cmoney.crypto.keystore.aes

import androidx.annotation.WorkerThread
import com.cmoney.crypto.keystore.IKeyStoreHelper
import com.cmoney.crypto.keystore.KeyStoreSharedPreferencesHelper
import com.cmoney.crypto.variable.SecurityKeySet
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AesKeyStoreHelper (
    private val keyStoreSource: AesKeyStoreSource,
    private val preferencesHelper: KeyStoreSharedPreferencesHelper,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO):
    IKeyStoreHelper {

    @WorkerThread
    override fun storeText(keySet: SecurityKeySet, input: String) {
        require(keySet is SecurityKeySet.AesKeySet) {
            "Only Use SecurityKeySet.AesKeySet for keySet"
        }
        val encryptResult = keyStoreSource.encryptAES(keySet, input)
        encryptResult.fold(
            { encryptText ->
                preferencesHelper.setInput(keySet.dataKey, encryptText)
            },
            {
                it.printStackTrace()
            }
        )
    }

    override suspend fun storeTextAsync(keySet: SecurityKeySet, input: String) = withContext(dispatcher) {
        require(keySet is SecurityKeySet.AesKeySet) {
            "Only Use SecurityKeySet.AesKeySet for keySet"
        }
        val encryptResult = keyStoreSource.encryptAES(keySet, input)
        encryptResult.fold(
            { encryptString ->
                if (encryptString.isNotBlank()) {
                    preferencesHelper.setInput(keySet.dataKey, encryptString)
                }
            },
            {
                it.printStackTrace()
            }
        )
    }

    @WorkerThread
    override fun getStoreText(keySet: SecurityKeySet): String {
        require(keySet is SecurityKeySet.AesKeySet) {
            "Only Use SecurityKeySet.AesKeySet for keySet"
        }
        val encryptedText = preferencesHelper.getInput(keySet.dataKey)
        return if (encryptedText.isBlank()) {
            encryptedText
        } else {
            keyStoreSource.decryptAES(keySet, encryptedText).getOrNull().orEmpty()
        }
    }

    override suspend fun getStoreTextAsync(keySet: SecurityKeySet): String = withContext(dispatcher) {
        require(keySet is SecurityKeySet.AesKeySet) {
            "Only Use SecurityKeySet.AesKeySet for keySet"
        }
        val encryptedText = preferencesHelper.getInput(keySet.dataKey)
        if (encryptedText.isBlank()) {
            encryptedText
        } else {
            keyStoreSource.decryptAES(keySet, encryptedText).getOrNull().orEmpty()
        }
    }
}
package com.cmoney.crypto.keystore.rsa

import androidx.annotation.WorkerThread
import com.cmoney.crypto.keystore.IKeyStoreHelper
import com.cmoney.crypto.keystore.KeyStoreSharedPreferencesHelper
import com.cmoney.crypto.variable.SecurityKeySet
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RsaKeyStoreHelper(
    private val keyStoreSource: RsaKeyStoreSource,
    private val preferencesHelper: KeyStoreSharedPreferencesHelper,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : IKeyStoreHelper {

    @WorkerThread
    override fun storeText(keySet: SecurityKeySet, input: String) {
        require(keySet is SecurityKeySet.RsaKeySet) {
            "Only Use SecurityKeySet.RsaKeySet for keySet"
        }
        val encryptResult = keyStoreSource.encryptRSA(keySet.alias, input)
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

    override suspend fun storeTextAsync(keySet: SecurityKeySet, input: String) =
        withContext(dispatcher) {
            require(keySet is SecurityKeySet.RsaKeySet) {
                "Only Use SecurityKeySet.RsaKeySet for keySet"
            }
            val encryptResult = keyStoreSource.encryptRSA(keySet.alias, input)
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
        require(keySet is SecurityKeySet.RsaKeySet) {
            "Only Use SecurityKeySet.RsaKeySet for keySet"
        }
        val encryptedText = preferencesHelper.getInput(keySet.dataKey)
        val decodeResult = keyStoreSource.decryptRSA(keySet.alias, encryptedText)
        return decodeResult.getOrNull().orEmpty()
    }

    override suspend fun getStoreTextAsync(keySet: SecurityKeySet): String =
        withContext(dispatcher) {
            require(keySet is SecurityKeySet.RsaKeySet) {
                "Only Use SecurityKeySet.RsaKeySet for keySet"
            }
            val encryptedText = preferencesHelper.getInput(keySet.dataKey)
            val decodedResult = keyStoreSource.decryptRSA(keySet.alias, encryptedText)
            decodedResult.getOrNull().orEmpty()
        }
}
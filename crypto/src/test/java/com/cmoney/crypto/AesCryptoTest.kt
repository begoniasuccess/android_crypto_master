package com.cmoney.crypto

import com.cmoney.crypto.model.CipherModel
import com.cmoney.crypto.model.CipherParam
import com.google.common.truth.Truth
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner

@Suppress("RemoveRedundantBackticks", "NonAsciiCharacters")
@RunWith(ParameterizedRobolectricTestRunner::class)
class AesCryptoTest(
    private val plainText: String,
    private val expectedText: String
) {

    companion object {

        private val aesByteArray by lazy {
            "cmoneytestcmoneytestcmoneytestcm".toByteArray()
        }

        private val ivByteArray by lazy {
            "cmcryptocmcrypto".toByteArray()
        }

        @ParameterizedRobolectricTestRunner.Parameters
        @JvmStatic
        fun getData(): Iterable<Array<Any?>> {
            return listOf(
                arrayOf<Any?>(
                    "cmoney", "kkUCbVwj9l36V5cRlq0zug=="
                ),
                arrayOf<Any?>(
                    "crypto", "+mGfGK4JrF4QBPdUtm0XXg=="
                ),
                arrayOf<Any?>(
                    "helloworld", "TDiz/S+EQpJOvZpAMXIz6A=="
                )
            )
        }
    }

    private val cipherAlgorithm = CipherParam.Aes.CBC_PKCS7PADDING

    private lateinit var cipherModel: CipherModel

    @Before
    fun setUp() {
        cipherModel = CipherModel(CipherParam.Aes(cipherAlgorithm, aesByteArray, ivByteArray))
    }

    @Test
    fun `加密範例文字成功`() {
        val encryptedResult = cipherModel.encrypt(plainText)
        Truth.assertThat(encryptedResult.isSuccess).isTrue()
        val encryptedPlainText = encryptedResult.getOrNull()
        Truth.assertThat(encryptedPlainText).isNotNull()
        Truth.assertThat(encryptedPlainText).isEqualTo(expectedText)
    }

    @Test
    fun `解密範例密文成功`() {
        val decryptedResult = cipherModel.decrypt(expectedText)
        Truth.assertThat(decryptedResult.isSuccess).isTrue()
        val decryptedPlainText = decryptedResult.getOrNull()
        Truth.assertThat(decryptedPlainText).isNotNull()
        Truth.assertThat(decryptedPlainText).isEqualTo(plainText)
    }
}
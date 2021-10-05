package com.cmoney.crypto


import androidx.test.platform.app.InstrumentationRegistry
import com.cmoney.crypto.model.CipherModel
import com.cmoney.crypto.model.CipherParam
import com.google.common.truth.Truth
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner

@Suppress("RemoveRedundantBackticks", "NonAsciiCharacters")
@RunWith(ParameterizedRobolectricTestRunner::class)
class RsaCryptoTest(private val plainText: String, private val targetEncrypted: String) {

    companion object {

        @ParameterizedRobolectricTestRunner.Parameters
        @JvmStatic
        fun getData(): Iterable<Array<Any?>> {
            return listOf(
                arrayOf<Any?>(
                    "helloworld",
                    "K9jweeacJ3KSYbdU433APWn8ZnkW76VzrNnLUNnfqg+Nixi2DPWjd2wGSylXguSG2t/nx/oFyFW1M2zUUvQ3bTLf1yDyvp6AQ348Af0OFpErCFTM5fUfTaT8RiYEsx+/VjuXU/WyvCA5Qx/44/q6EnTmJpD8Mm6xsZ9gylk+Qb/BP9GPEdzQ05w0GwkkYw2H23cvQpAXJ/RA55zwgOIXm4ztojo88GGWqOfxIulMC45e3Y9gV1xG71ak+h7y/OQuiyKdMj+jFS6DgkP++PkRrVrrWSxGQ/w/W9QhqyfqU/GFw+rcSOWvi96TJ2r2FzQDWPJiFUmdpgFIDz1p4V6ngw=="
                ),
                arrayOf<Any?>(
                    "cmoney",
                    "T2Z4Xf+niRB7tTY3/mFSL1TlDSOJUJOGew9gqhgUZFSVvpB/xsM8cZKSe6TKzeo8VPrkykWIx1F+MQhb0+Heby0B9h6ECiAAQo9iDigy94+5/SbwZbDmgjy5uZLZleiLw4PadQNXGjIdsASrjiD51hgFH65FzJ2mvH1nX8b750cZoc4vphrWHO8UndYbzeyBz/cj0VGyYV9R2CXKVSgAkFUqRF/4z7b1glJGXBOC+ZwknCEvOqsiEV/DDJoz0UqcQcXaYydlPkMZ3rmf2Uh3l8Y8jWkPjXJtObKDdI/JEnq4hEOkNl40rvsW2AqGGT/TuVDjdZky/Le3vcHcb06tag=="
                )
            )
        }
    }

    private lateinit var keyProvider: RsaKeyTextProvider

    private lateinit var cipherModel: CipherModel

    private val algorithm = CipherParam.Rsa.ECB_PKCS1PADDING

    @Before
    fun setUp() {
        keyProvider = RsaKeyTextProvider(InstrumentationRegistry.getInstrumentation().context)
        cipherModel = CipherModel(
            CipherParam.Rsa(
                algorithm,
                CipherParam.Rsa.generateEncryptedKey(keyProvider.publicKeyText),
                CipherParam.Rsa.generateDecryptedKey(keyProvider.privateKeyText)
            )
        )
    }

    @Test
    fun `加解密成功`() {
        // 加密
        val encryptedResult = cipherModel.encrypt(plainText)

        Truth.assertThat(encryptedResult.isSuccess).isTrue()

        // 解密
        val encryptedText = encryptedResult.getOrNull()
        Truth.assertThat(encryptedText).isNotNull()
        val decryptedResult = cipherModel.decrypt(encryptedText!!)
        Truth.assertThat(decryptedResult.getOrNull()).isNotNull()
        Truth.assertThat(decryptedResult.getOrNull()).isEqualTo(plainText)
    }

    @Test
    fun `解密密文成功`() {
        val decryptedResult = cipherModel.decrypt(targetEncrypted)
        Truth.assertThat(decryptedResult.isSuccess).isTrue()
        val decryptedText = decryptedResult.getOrNull()
        Truth.assertThat(decryptedText).isNotNull()
        Truth.assertThat(decryptedText).isEqualTo(plainText)
    }
}
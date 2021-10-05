package com.cmoney.crypto.model

import android.util.Base64
import java.security.InvalidAlgorithmParameterException
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import javax.crypto.*
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class CipherModel(private val useParam: CipherParam) {

    /**
     * 加密字串
     * 可能會有Exception發生，
     * 會以[Result.failure]方式回傳
     *
     * @param plainText 要加密文字
     * @return 結果(失敗則Exception，成功是加密後文字)
     */
    fun encrypt(plainText: String): Result<String> {
        return kotlin.runCatching {
            when (useParam) {
                is CipherParam.Aes -> {
                    encryptAes(useParam, plainText)
                }
                is CipherParam.Rsa -> {
                    encryptRsa(useParam, plainText)
                }
            }
        }
    }

    @Throws(
        NoSuchAlgorithmException::class,
        NoSuchPaddingException::class,
        UnsupportedOperationException::class,
        InvalidAlgorithmParameterException::class,
        InvalidKeyException::class,
        IllegalStateException::class,
        BadPaddingException::class,
        IllegalBlockSizeException::class,
        AEADBadTagException::class
    )
    private fun encryptAes(param: CipherParam.Aes, plainText: String): String {
        val cipher = Cipher.getInstance(param.algorithm)
        cipher.init(
            Cipher.ENCRYPT_MODE,
            SecretKeySpec(param.aesKey, param.algorithm),
            IvParameterSpec(param.iv)
        )
        val encryptedText = cipher.doFinal(plainText.toByteArray())
        return Base64.encodeToString(encryptedText, Base64.NO_WRAP)
    }

    @Throws(
        NoSuchAlgorithmException::class,
        NoSuchPaddingException::class,
        UnsupportedOperationException::class,
        InvalidKeyException::class,
        IllegalStateException::class,
        BadPaddingException::class,
        IllegalBlockSizeException::class,
        AEADBadTagException::class
    )
    private fun encryptRsa(param: CipherParam.Rsa, plainText: String): String {
        val encryptCipher = Cipher.getInstance(param.algorithm)
        encryptCipher.init(Cipher.ENCRYPT_MODE, param.publicKey)
        val encryptedBytes = encryptCipher.doFinal(plainText.toByteArray())
        return Base64.encodeToString(encryptedBytes, Base64.DEFAULT)
    }

    /**
     * 解密加密的字串
     * 若有Exception則以
     * [Result.failure]形式回傳
     *
     * @param encryptedText 欲解密字串
     * @return 成功則是解密字串，失敗則Exception
     */
    fun decrypt(encryptedText: String): Result<String> {
        val encrypted = Base64.decode(encryptedText, Base64.NO_WRAP)
        return kotlin.runCatching {
            when (useParam) {
                is CipherParam.Aes -> {
                    decryptAes(useParam, encrypted)
                }
                is CipherParam.Rsa -> {
                    decryptRsa(useParam, encrypted)
                }
            }
        }
    }

    @Throws(
        NoSuchAlgorithmException::class,
        NoSuchPaddingException::class,
        UnsupportedOperationException::class,
        InvalidAlgorithmParameterException::class,
        InvalidKeyException::class,
        IllegalStateException::class,
        BadPaddingException::class,
        IllegalBlockSizeException::class,
        AEADBadTagException::class
    )
    private fun decryptAes(param: CipherParam.Aes, encryptedBytes: ByteArray): String {
        val cipher = Cipher.getInstance(param.algorithm)
        cipher.init(
            Cipher.DECRYPT_MODE,
            SecretKeySpec(param.aesKey, param.algorithm),
            IvParameterSpec(param.iv)
        )
        val decryptedBytes = cipher.doFinal(encryptedBytes)
        return String(decryptedBytes)
    }

    @Throws(
        NoSuchAlgorithmException::class,
        NoSuchPaddingException::class,
        UnsupportedOperationException::class,
        InvalidKeyException::class,
        IllegalStateException::class,
        BadPaddingException::class,
        IllegalBlockSizeException::class,
        AEADBadTagException::class
    )
    private fun decryptRsa(param: CipherParam.Rsa, encryptedBytes: ByteArray): String {
        val decryptedCipher = Cipher.getInstance(param.algorithm)
        decryptedCipher.init(Cipher.DECRYPT_MODE, param.privateKey)
        val decryptedBytes = decryptedCipher.doFinal(encryptedBytes)
        return String(decryptedBytes)
    }
}
package com.cmoney.crypto.model

import android.util.Base64
import java.security.Key
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.PublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher

sealed class CipherParam {

    /**
     * 使用於加解密 AES的 [Cipher] 相關設定
     *
     * @property algorithm
     * @property aesKey
     * @property iv
     */
    data class Aes(val algorithm: String, val aesKey: ByteArray, val iv: ByteArray) :
        CipherParam() {
        companion object {
            const val GCM_NOPADDING = "AES/GCM/NoPadding"

            const val CBC_PKCS7PADDING = "AES/CBC/PKCS7Padding"
        }

        override fun hashCode(): Int {
            return algorithm.hashCode() + aesKey.contentHashCode() + iv.contentHashCode()
        }

        override fun equals(other: Any?): Boolean {
            return when (other) {
                is Aes -> {
                    algorithm == other.algorithm &&
                            aesKey.contentEquals(other.aesKey) &&
                            iv.contentEquals(other.iv)
                }
                else -> {
                    false
                }
            }
        }
    }

    /**
     * 使用於加解密 RSA的 [Cipher] 相關設定
     *
     * @property algorithm
     * @property publicKey
     * @property privateKey
     */
    data class Rsa(val algorithm: String, val publicKey: Key, val privateKey: Key? = null) :
        CipherParam() {

        companion object {
            const val ECB_PKCS1PADDING = "RSA/ECB/PKCS1Padding"

            fun generateEncryptedKey(publicKeyText: String): PublicKey {
                val encodeEncoded = Base64.decode(publicKeyText, Base64.DEFAULT)
                val encodedKeySpec = X509EncodedKeySpec(encodeEncoded)
                val encodeKeyFactory = KeyFactory.getInstance("RSA")
                return encodeKeyFactory.generatePublic(encodedKeySpec)
            }

            fun generateDecryptedKey(privateKeyText: String): PrivateKey {
                val decodeEncoded = Base64.decode(privateKeyText, Base64.DEFAULT)
                val decryptedKeySpec = PKCS8EncodedKeySpec(decodeEncoded)
                val keyFactory = KeyFactory.getInstance("RSA")
                return keyFactory.generatePrivate(decryptedKeySpec)
            }
        }
    }
}
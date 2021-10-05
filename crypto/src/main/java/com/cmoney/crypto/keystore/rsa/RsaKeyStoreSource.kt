package com.cmoney.crypto.keystore.rsa

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.security.KeyPairGeneratorSpec
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import com.cmoney.crypto.model.CipherModel
import com.cmoney.crypto.model.CipherParam
import com.cmoney.crypto.variable.SecurityKeySet
import java.io.IOException
import java.math.BigInteger
import java.security.*
import java.util.*
import javax.security.auth.x500.X500Principal
import javax.security.cert.CertificateException

class RsaKeyStoreSource(
    context: Context,
    keySets: List<SecurityKeySet.RsaKeySet>,
    private val rsaAlgorithm: String
) {

    @Volatile
    private var keyStore: KeyStore? = null

    init {
        try {
            keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER)
            keyStore?.load(null)
            keySets.forEach { keySet ->
                if (keyStore?.containsAlias(keySet.alias) == false) {
                    generateKey(context, keySet.alias)
                }
            }
        } catch (e: KeyStoreException) {
            e.printStackTrace()
        } catch (e: CertificateException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }
    }

    private fun generateKey(context: Context, alias: String) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                generateRsaKeyAboveApi23(alias)
            } else {
                generateRsaKeyBelowApi23(context, alias)
            }
        } catch (e: GeneralSecurityException) {
            e.printStackTrace()
        } catch (e: ProviderException) {
            e.printStackTrace()
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Throws(
        GeneralSecurityException::class,
        ProviderException::class
    )
    private fun generateRsaKeyAboveApi23(alias: String) {
        val keyPairGenerator = KeyPairGenerator
            .getInstance(
                KeyProperties.KEY_ALGORITHM_RSA,
                KEYSTORE_PROVIDER
            )

        val keyGenParameterSpec =
            KeyGenParameterSpec.Builder(
                alias,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)
                .build()

        keyPairGenerator.initialize(keyGenParameterSpec)
        keyPairGenerator.generateKeyPair()
    }

    @Throws(GeneralSecurityException::class)
    private fun generateRsaKeyBelowApi23(context: Context, alias: String) {
        val start = Calendar.getInstance()
        val end = Calendar.getInstance()
        end.add(Calendar.YEAR, 100)

        val spec = KeyPairGeneratorSpec.Builder(context)
            .setAlias(alias)
            .setSubject(X500Principal("CN=$alias"))
            .setSerialNumber(BigInteger.TEN)
            .setStartDate(start.time)
            .setEndDate(end.time)
            .build()

        val keyPairGenerator = KeyPairGenerator
            .getInstance(
                KeyProperties.KEY_ALGORITHM_RSA,
                KEYSTORE_PROVIDER
            )

        keyPairGenerator.initialize(spec)
        keyPairGenerator.generateKeyPair()
    }

    /**
     * 加密
     *
     * @param alias
     * @param encryptText 純文字即可
     * @return
     */
    fun encryptRSA(alias: String, encryptText: String): Result<String> {
        val privateKeyEntry = getPrivateKeyEntry(alias)
            ?: return Result.failure(NullPointerException("Not have PrivateKeyEntry"))
        val cipherModel =
            CipherModel(CipherParam.Rsa(rsaAlgorithm, privateKeyEntry.certificate.publicKey, null))
        return cipherModel.encrypt(encryptText)
    }

    /**
     * 解密
     *
     * @param alias
     * @param encryptedText Base64 編碼
     * @return
     */
    fun decryptRSA(alias: String, encryptedText: String): Result<String> {
        val privateKeyEntry = getPrivateKeyEntry(alias)
            ?: return Result.failure(NullPointerException("Not have PrivateKeyEntry"))
        val cipherModel = CipherModel(
            CipherParam.Rsa(
                rsaAlgorithm,
                privateKeyEntry.certificate.publicKey,
                privateKeyEntry.privateKey
            )
        )
        return cipherModel.decrypt(encryptedText)
    }

    private fun getPrivateKeyEntry(alias: String): KeyStore.PrivateKeyEntry? {
        return try {
            keyStore?.getEntry(alias, null) as? KeyStore.PrivateKeyEntry ?: return null
        } catch (e: KeyStoreException) {
            e.printStackTrace()
            null
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
            null
        } catch (e: UnrecoverableEntryException) {
            e.printStackTrace()
            null
        }
    }

    companion object {

        private const val TAG = "KeyStoreTest"

        private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
    }
}
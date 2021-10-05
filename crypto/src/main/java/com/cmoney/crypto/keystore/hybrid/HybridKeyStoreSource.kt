package com.cmoney.crypto.keystore.hybrid

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.security.KeyPairGeneratorSpec
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import com.cmoney.crypto.keystore.KeyStoreSharedPreferencesHelper
import com.cmoney.crypto.model.CipherModel
import com.cmoney.crypto.model.CipherParam
import com.cmoney.crypto.variable.SecurityKeySet
import java.io.IOException
import java.math.BigInteger
import java.security.*
import java.util.*
import javax.security.auth.x500.X500Principal
import javax.security.cert.CertificateException

class HybridKeyStoreSource(
    context: Context,
    keySets: List<SecurityKeySet.HybridKeySet>,
    private val aesAlgorithm: String,
    private val rsaAlgorithm: String,
    private val sharedPreferences: KeyStoreSharedPreferencesHelper
) {

    @Volatile
    private var keyStore: KeyStore? = null
    private val keyMemoryMap = mutableMapOf<String, ByteArray>()

    init {
        try {
            keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER)
            keyStore?.load(null)
            keySets.forEach {
                if (keyStore?.containsAlias(it.alias) == false) {
                    generateKey(context, it.alias)
                    generateAESKey(it)
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
            .getInstance(KeyProperties.KEY_ALGORITHM_RSA,
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

    @Throws(
        GeneralSecurityException::class,
        ProviderException::class
    )
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
            .getInstance(KeyProperties.KEY_ALGORITHM_RSA,
                KEYSTORE_PROVIDER
            )

        keyPairGenerator.initialize(spec)
        keyPairGenerator.generateKeyPair()
    }

    private fun generateAESKey(keySet: SecurityKeySet.HybridKeySet) {
        // Generate AES-Key
        val aesKey = ByteArray(32)
        val secureRandom = SecureRandom()
        secureRandom.nextBytes(aesKey)

        // Generate 12 bytes iv then save to SharedPrefs
        val generated = secureRandom.generateSeed(16)
        val iv = Base64.encodeToString(generated, Base64.DEFAULT)
        sharedPreferences.setIv(keySet.ivKey, iv)


        // Encrypt AES-Key with RSA Public Key then save to SharedPrefs
        val encryptAESKey =
            encryptRSA(keySet.alias, Base64.encodeToString(aesKey, Base64.DEFAULT)).getOrNull()
                .orEmpty()
        sharedPreferences.setAESKey(keySet.aesKey, encryptAESKey)
    }

    private fun getAESKey(keySet: SecurityKeySet.HybridKeySet): ByteArray {
        val encryptedKey = sharedPreferences.getAesKey(keySet.aesKey)
        val decryptedString = decryptRSA(
            keySet.alias,
            encryptedKey
        ).getOrNull().orEmpty()
        val aesByteArray = Base64.decode(decryptedString, Base64.DEFAULT)
        keyMemoryMap[keySet.aesKey] = aesByteArray
        return aesByteArray
    }

    private fun getIv(keySet: SecurityKeySet.HybridKeySet): ByteArray {
        return Base64.decode(sharedPreferences.getIv(keySet.ivKey), Base64.DEFAULT)
    }

    fun encryptAES(keySet: SecurityKeySet.HybridKeySet, plainText: String): Result<String> {
        val cipherModel =
            CipherModel(CipherParam.Aes(aesAlgorithm, getAesKeyFormIOorCache(keySet), getIv(keySet)))
        return cipherModel.encrypt(plainText)
    }

    fun decryptAES(keySet: SecurityKeySet.HybridKeySet, encryptedText: String): Result<String> {
        val cipherModel =
            CipherModel(CipherParam.Aes(aesAlgorithm, getAesKeyFormIOorCache(keySet), getIv(keySet)))
        return cipherModel.decrypt(encryptedText)
    }

    private fun getAesKeyFormIOorCache(keySet: SecurityKeySet.HybridKeySet): ByteArray {
        return keyMemoryMap[keySet.aesKey] ?: getAESKey(keySet)
    }

    /**
     * 加密
     *
     * @param alias
     * @param encryptText 純文字即可
     * @return
     */
    private fun encryptRSA(alias: String, encryptText: String): Result<String> {
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
    private fun decryptRSA(alias: String, encryptedText: String): Result<String> {
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
package com.cmoney.crypto.tink

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import android.util.Log
import androidx.core.content.edit
import com.cmoney.crypto.BuildConfig
import com.google.crypto.tink.KeysetHandle
import com.google.crypto.tink.PublicKeySign
import com.google.crypto.tink.PublicKeyVerify
import com.google.crypto.tink.signature.SignatureKeyTemplates
import java.security.GeneralSecurityException

class SecureRsaPreferences(context: Context, name: String): SharedPreferences {

    companion object {
        private const val SIGNATURE_APPEND = "_sign"
        private const val TAG = "SecureRsaPreferences"

        private fun log(type: Int, str: String) {
            if (BuildConfig.DEBUG) {
                when (type) {
                    Log.WARN -> Log.w(TAG, str)
                    Log.ERROR -> Log.e(TAG, str)
                    Log.DEBUG -> Log.d(TAG, str)
                }
            }
        }
    }

    private val sharedPreferences: SharedPreferences =
        context.applicationContext.getSharedPreferences(name, Context.MODE_PRIVATE)
    private var keySigner: PublicKeySign? = null
    private var keyVerifier: PublicKeyVerify? = null

    init {
        try {
            val privateKeySetHandle =
                KeysetHandle.generateNew(SignatureKeyTemplates.RSA_SSA_PKCS1_4096_SHA512_F4)
            keySigner = privateKeySetHandle.getPrimitive(PublicKeySign::class.java)
            val publicKeySetHandle = privateKeySetHandle.publicKeysetHandle
            keyVerifier = publicKeySetHandle.getPrimitive(PublicKeyVerify::class.java)
        } catch (e: GeneralSecurityException) {
            e.printStackTrace()
        }
    }

    private fun signData(data: ByteArray): ByteArray? {
        return try {
            keySigner?.sign(data) ?: data
        } catch (e: GeneralSecurityException) {
            log(Log.ERROR, "signData $e")
            null
        }
    }

    private fun verifyData(signature: ByteArray, data: ByteArray): Boolean {
        return try {
            keyVerifier?.verify(signature, data)
            true
        } catch (e: GeneralSecurityException) {
            e.printStackTrace()
            false
        }
    }

    override fun contains(key: String?): Boolean {
        return sharedPreferences.contains(key)
    }

    override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
    }

    override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {
        return sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener)
    }

    override fun getBoolean(key: String?, defValue: Boolean): Boolean {
        return getString(key, null)?.toBoolean() ?: defValue
    }

    override fun getInt(key: String?, defValue: Int): Int {
        return getString(key, null)?.toIntOrNull() ?: defValue
    }

    override fun getAll(): MutableMap<String, *> {
        val all = sharedPreferences.all
        val valueMap = mutableMapOf<String, String?>()
        for ((key, value) in all) {
            if (!key.endsWith(SIGNATURE_APPEND)) {
                if (value != null) {
                    valueMap[key] = getString(key, null)
                }
            }
        }
        return valueMap
    }

    override fun edit(): SharedPreferences.Editor {
        return Editor()
    }

    override fun getLong(key: String?, defValue: Long): Long {
        return getString(key, null)?.toLongOrNull() ?: defValue
    }

    override fun getFloat(key: String?, defValue: Float): Float {
        return getString(key, null)?.toFloatOrNull() ?: defValue
    }

    override fun getStringSet(key: String?, defValues: MutableSet<String>?): MutableSet<String> {
        val value = getString(key, null) ?: return mutableSetOf()
        if (value.isBlank()) {
            return mutableSetOf()
        }
        return value.split(",").toMutableSet()
    }

    override fun getString(key: String?, defValue: String?): String? {
        if (key == null) {
            return defValue
        }
        val signature = getSavedSignature(key) ?: return defValue
        val saveString = sharedPreferences.getString(key, defValue) ?: return defValue
        val isVerify = verifyData(signature, saveString.toByteArray())
        return if (isVerify) {
            saveString
        } else {
            defValue
        }
    }

    private fun saveSignature(key: String, signature: ByteArray) {
        sharedPreferences.edit {
            putString(key + SIGNATURE_APPEND, Base64.encodeToString(signature, Base64.DEFAULT))
        }
    }

    private fun getSavedSignature(key: String): ByteArray? {
        return Base64.decode((sharedPreferences.getString(key + SIGNATURE_APPEND, null)) ?: return null, Base64.DEFAULT)
    }

    private inner class Editor : SharedPreferences.Editor {

        private val editor: SharedPreferences.Editor = sharedPreferences.edit()

        override fun putInt(key: String, value: Int): SharedPreferences.Editor {
            putString(key, value.toString())
            return this
        }

        override fun putLong(key: String, value: Long): SharedPreferences.Editor {
            putString(key, value.toString())
            return this
        }

        override fun putBoolean(key: String, value: Boolean): SharedPreferences.Editor {
            putString(key, value.toString())
            return this
        }

        override fun putFloat(key: String, value: Float): SharedPreferences.Editor {
            putString(key, value.toString())
            return this
        }

        override fun putString(key: String, value: String?): SharedPreferences.Editor {
            if (value == null) {
                return this
            }
            val signedData = value.toByteArray()
            val signature = signData(signedData) ?: return this
            saveSignature(key, signature)
            editor.putString(key, value)
            return this
        }

        override fun putStringSet(
            key: String,
            values: MutableSet<String>?
        ): SharedPreferences.Editor {
            val storeString = values?.joinToString(",").orEmpty()
            val signedData = storeString.toByteArray()
            val signature = signData(signedData) ?: return this
            saveSignature(key, signature)
            editor.putString(key, storeString)
            return this
        }

        override fun remove(key: String): SharedPreferences.Editor {
            editor.remove(key)
            return this
        }

        override fun clear(): SharedPreferences.Editor {
            editor.clear()
            return this
        }

        override fun commit(): Boolean {
            return editor.commit()
        }

        override fun apply() {
            editor.apply()
        }

    }

}
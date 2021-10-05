package com.cmoney.crypto.tink

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import android.util.Log
import com.cmoney.crypto.BuildConfig
import com.google.crypto.tink.Aead
import com.google.crypto.tink.aead.AeadKeyTemplates
import com.google.crypto.tink.integration.android.AndroidKeysetManager
import java.security.GeneralSecurityException

class SecureAesPreferences(context: Context, name: String) : SharedPreferences {

    private val sharedPreferences: SharedPreferences =
        context.applicationContext.getSharedPreferences(name, Context.MODE_PRIVATE)

    @Volatile
    private var aead: Aead? = null // Null is okay, the data will not be encrypted.

    init {
        try {
            val keySetHandle = AndroidKeysetManager.Builder()
                .withSharedPref(context.applicationContext, "key_set", "${name}_pref")
                .withKeyTemplate(AeadKeyTemplates.AES256_GCM)
                .withMasterKeyUri("android-keystore://${name}_master_key")
                .build()
                .keysetHandle
            aead = keySetHandle.getPrimitive(Aead::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        private const val TAG = "SecureAesPreferences"

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

    private fun encrypt(plainText: String?): String? {
        if (plainText.isNullOrEmpty()) {
            return plainText
        }

        var cipherText: ByteArray? = null
        try {
            val realAead = aead
            cipherText = if (realAead != null) {
                realAead.encrypt(plainText.toByteArray(), byteArrayOf())
            } else {
                plainText.toByteArray()
            }
        } catch (e: GeneralSecurityException) {
            log(
                Log.ERROR,
                "encrypt: $e"
            )
        }

        return cipherText?.let { Base64.encodeToString(cipherText, Base64.DEFAULT) }
    }

    private fun decrypt(encryptedText: String): String? {
        if (encryptedText.isEmpty()) {
            return encryptedText
        }

        var plainText: ByteArray? = null

        try {
            val realAead = aead
            plainText = if (realAead != null) {
                realAead.decrypt(Base64.decode(encryptedText, Base64.DEFAULT), byteArrayOf())
            } else {
                Base64.decode(encryptedText, Base64.DEFAULT)
            }
        } catch (e: GeneralSecurityException) {
            log(
                Log.ERROR,
                "decrypt: $e"
            )
        }
        return plainText?.let { String(it) }
    }

    override fun getInt(key: String, defValue: Int): Int {
        return getString(key, null)?.toIntOrNull() ?: defValue
    }

    override fun getLong(key: String, defValue: Long): Long {
        return getString(key, null)?.toLongOrNull() ?: defValue
    }

    override fun getBoolean(key: String, defValue: Boolean): Boolean {
        return getString(key, null)?.toBoolean() ?: defValue
    }

    override fun getFloat(key: String, defValue: Float): Float {
        return getString(key, null)?.toFloatOrNull() ?: defValue
    }

    override fun getString(key: String, defValue: String?): String? {
        val encryptedValue = sharedPreferences.getString(key, null) ?: return defValue
        return decrypt(encryptedValue) ?: defValue
    }

    override fun getStringSet(key: String, defValues: MutableSet<String>?): MutableSet<String>? {
        val encryptedSet = sharedPreferences.getStringSet(key, null) ?: return defValues
        val decryptedSet = mutableSetOf<String>()
        encryptedSet.forEach {
            decrypt(it)?.run {
                decryptedSet.add(this)
            }
        }
        return decryptedSet
    }

    override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
    }

    override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener)
    }

    override fun contains(key: String): Boolean {
        return sharedPreferences.contains(key)
    }

    override fun getAll(): MutableMap<String, *> {
        val encryptedMap = sharedPreferences.all
        val decryptedMap = mutableMapOf<String, String?>()

        for ((key, cipherText) in encryptedMap) {
            try {
                if (cipherText != null) {
                    decryptedMap[key] = decrypt(cipherText.toString())
                }
            } catch (e: Exception) {
                log(
                    Log.ERROR,
                    "error getAll: $e"
                )
                decryptedMap[key] = cipherText.toString()
            }

        }
        return decryptedMap
    }

    override fun edit(): SharedPreferences.Editor {
        return Editor()
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
            editor.putString(key, encrypt(value))
            return this
        }

        override fun putStringSet(
            key: String,
            values: MutableSet<String>?
        ): SharedPreferences.Editor {
            val encryptedValues = mutableSetOf<String>()
            values?.forEach {
                encrypt(it)?.run {
                    encryptedValues.add(this)
                }
            }
            editor.putStringSet(key, encryptedValues)
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

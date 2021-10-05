package com.cmoney.crypto.keystore

import android.content.Context
import android.content.SharedPreferences

class KeyStoreSharedPreferencesHelper(context: Context, name: String) {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(name, Context.MODE_PRIVATE)

    fun setIv(key: String, iv: String?) {
        sharedPreferences.edit().putString(key, iv).apply()
    }

    fun getIv(key: String): String {
        return sharedPreferences.getString(key, "") ?: ""
    }

    fun getAesKey(key: String): String {
        return sharedPreferences.getString(key, "") ?: ""
    }

    fun setAESKey(key: String, value: String) {
        sharedPreferences.edit().putString(key, value).apply()
    }

    fun setInput(key: String, encryptString: String) {
        sharedPreferences.edit().putString(key, encryptString).apply()
    }

    fun getInput(key: String): String {
        return sharedPreferences.getString(key, "") ?: ""
    }
}

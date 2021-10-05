package com.cmoney.crypto.di


/**
 * 使用Tink sharedPreferences的方法
 *
 * @param useRsa 是否使用 rsa加密 sharedPreferences
 * @param rsaSharedPreferencesName 若[useRsa]為true，請務必設定
 * @param useAes 是否使用 aes加密 sharedPreferences
 * @param aesSharedPreferencesName 若[useAes]為true，請務必設定
 */
data class UseTinkParam(
    val useRsa: Boolean = false,
    val rsaSharedPreferencesName: String? = null,
    val useAes: Boolean = false,
    val aesSharedPreferencesName: String? = null
)
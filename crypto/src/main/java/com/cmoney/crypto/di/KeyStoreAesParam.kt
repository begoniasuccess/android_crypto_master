package com.cmoney.crypto.di

import com.cmoney.crypto.model.CipherParam
import com.cmoney.crypto.variable.SecurityKeySet

data class KeyStoreAesParam(
    val aesSharedPreferencesName: String,
    val aesAlgorithm: String = CipherParam.Aes.CBC_PKCS7PADDING,
    val aesKeySets: List<SecurityKeySet.AesKeySet> = listOf()
)
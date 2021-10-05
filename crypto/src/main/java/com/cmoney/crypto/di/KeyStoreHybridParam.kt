package com.cmoney.crypto.di

import com.cmoney.crypto.model.CipherParam
import com.cmoney.crypto.variable.SecurityKeySet

data class KeyStoreHybridParam(
    val hybridSharedPreferencesName: String,
    val hybridAesAlgorithm: String = CipherParam.Aes.CBC_PKCS7PADDING,
    val hybridRsaAlgorithm: String = CipherParam.Rsa.ECB_PKCS1PADDING,
    val hybridKeySets: List<SecurityKeySet.HybridKeySet> = listOf()
)
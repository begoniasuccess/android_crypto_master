package com.cmoney.crypto.di

import com.cmoney.crypto.model.CipherParam
import com.cmoney.crypto.variable.SecurityKeySet

data class KeyStoreRsaParam(
    val rsaSharedPreferencesName: String,
    val rsaAlgorithm: String = CipherParam.Rsa.ECB_PKCS1PADDING,
    val rsaKeySets: List<SecurityKeySet.RsaKeySet> = listOf()
)
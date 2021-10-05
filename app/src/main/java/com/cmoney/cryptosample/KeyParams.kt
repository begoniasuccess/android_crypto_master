package com.cmoney.cryptosample

import com.cmoney.crypto.variable.SecurityKeySet

sealed class KeyParams {

    object InputRsa: KeyParams() {
        val keySet = SecurityKeySet.RsaKeySet("RsaKey", "RsaDataKey")
    }

    object InputAes: KeyParams() {
        val keySet = SecurityKeySet.AesKeySet("AesKey", "IvKey", "AesDataKey")
    }

    object InputHybrid: KeyParams() {
        val keySet = SecurityKeySet.HybridKeySet("HybridAlias", "HybridAesKey", "HybridIvKey", "HybridDataKey")
    }
}
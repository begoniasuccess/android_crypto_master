package com.cmoney.crypto.di

import com.cmoney.crypto.keystore.KeyStoreSharedPreferencesHelper
import com.cmoney.crypto.keystore.aes.AesKeyStoreHelper
import com.cmoney.crypto.keystore.aes.AesKeyStoreSource
import com.cmoney.crypto.keystore.hybrid.HybridKeyStoreHelper
import com.cmoney.crypto.keystore.hybrid.HybridKeyStoreSource
import com.cmoney.crypto.keystore.rsa.RsaKeyStoreHelper
import com.cmoney.crypto.keystore.rsa.RsaKeyStoreSource
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

fun getKeyStoreAesModule(param: KeyStoreAesParam, createAtStart: Boolean = true) =
    module(createdAtStart = createAtStart) {
        single {
            AesKeyStoreHelper(
                get(AES_KEY_SOURCE_NAMED), get(AES_SHAREDPREFERENCES_NAMED)
            )
        }
        single(qualifier = AES_KEY_SOURCE_NAMED, createdAtStart = true) {
            AesKeyStoreSource(
                param.aesKeySets,
                param.aesAlgorithm,
                get(AES_SHAREDPREFERENCES_NAMED)
            )
        }
        single(AES_SHAREDPREFERENCES_NAMED) {
            KeyStoreSharedPreferencesHelper(androidContext(), param.aesSharedPreferencesName)
        }
    }

fun getKeyStoreRsaModule(param: KeyStoreRsaParam, createAtStart: Boolean = true) =
    module(createdAtStart = createAtStart) {
        single {
            RsaKeyStoreHelper(
                get(
                    RSA_KEY_SOURCE_NAMED
                ), get(RSA_SHAREDPREFERENCES_NAMED)
            )
        }
        single(qualifier = RSA_KEY_SOURCE_NAMED, createdAtStart = true) {
            RsaKeyStoreSource(
                androidContext(),
                param.rsaKeySets,
                param.rsaAlgorithm
            )
        }
        single(RSA_SHAREDPREFERENCES_NAMED) {
            KeyStoreSharedPreferencesHelper(androidContext(), param.rsaSharedPreferencesName)
        }
    }

fun getKeyStoreHybridModule(param: KeyStoreHybridParam, createAtStart: Boolean = true) =
    module(createdAtStart = createAtStart) {
        single {
            HybridKeyStoreHelper(
                get(
                    HYBRID_KEY_SOURCE_NAMED
                ), get(HYBRID_SHAREDPREFERENCES_NAMED)
            )
        }
        single(qualifier = HYBRID_KEY_SOURCE_NAMED, createdAtStart = true) {
            HybridKeyStoreSource(
                androidContext(),
                param.hybridKeySets,
                param.hybridAesAlgorithm,
                param.hybridRsaAlgorithm,
                get(HYBRID_SHAREDPREFERENCES_NAMED)
            )
        }
        single(HYBRID_SHAREDPREFERENCES_NAMED) {
            KeyStoreSharedPreferencesHelper(
                androidContext(),
                param.hybridSharedPreferencesName
            )
        }
    }
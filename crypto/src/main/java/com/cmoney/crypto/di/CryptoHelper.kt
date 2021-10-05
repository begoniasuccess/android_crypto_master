package com.cmoney.crypto.di

import com.cmoney.crypto.keystore.aes.AesKeyStoreSource
import com.cmoney.crypto.keystore.hybrid.HybridKeyStoreSource
import com.cmoney.crypto.keystore.rsa.RsaKeyStoreSource
import com.google.crypto.tink.config.TinkConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.core.KoinComponent
import org.koin.core.get

object CryptoHelper : KoinComponent {

    /**
     * 註冊Tink
     */
    fun registerTink() {
        TinkConfig.register()
    }

    /**
     * 使用Coroutine非同步生成keyStore(AES)需要的資源
     */
    fun initKeyStoreAesSource() {
        GlobalScope.launch(Dispatchers.IO) {
            get<AesKeyStoreSource>(AES_KEY_SOURCE_NAMED)
        }
    }

    /**
     * 使用Coroutine非同步生成keyStore(RSA)需要的資源
     */
    fun initKeyStoreRsaSource() {
        GlobalScope.launch(Dispatchers.IO) {
            get<RsaKeyStoreSource>(RSA_KEY_SOURCE_NAMED)
        }
    }

    /**
     * 使用Coroutine非同步生成keyStore(Hybrid(RSA、AES))需要的資源
     */
    fun initKeyStoreHybridSource() {
        GlobalScope.launch(Dispatchers.IO) {
            get<HybridKeyStoreSource>(HYBRID_KEY_SOURCE_NAMED)
        }
    }
}
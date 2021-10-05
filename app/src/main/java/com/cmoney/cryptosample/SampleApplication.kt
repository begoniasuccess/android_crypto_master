package com.cmoney.cryptosample

import android.app.Application
import com.cmoney.crypto.di.*
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class SampleApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        CryptoHelper.registerTink()
        startKoin {
            androidContext(this@SampleApplication)
            if (BuildConfig.DEBUG) {
                androidLogger(Level.NONE)
            }
            modules(
                listOf(
                    // 視情況選用Module
                    getTinkAesModule("tink_sharedPreferences_aes"),
                    getTinkRsaModule("tink_sharedPreferences_rsa"),
                    getKeyStoreAesModule(
                        KeyStoreAesParam(
                            aesSharedPreferencesName = "key_store_sharedPreferences_aes",
                            aesKeySets = listOf(KeyParams.InputAes.keySet)
                        )
                    ),
                    getKeyStoreRsaModule(
                        KeyStoreRsaParam(
                            rsaSharedPreferencesName = "key_store_sharedPreferences_rsa",
                            rsaKeySets = listOf(KeyParams.InputRsa.keySet)
                        )
                    ),
                    getKeyStoreHybridModule(
                        KeyStoreHybridParam(
                            hybridSharedPreferencesName = "key_store_sharedPreferences_hybrid",
                            hybridKeySets = listOf(KeyParams.InputHybrid.keySet)
                        )
                    )
                )
            )
        }
        with(CryptoHelper) {
            initKeyStoreAesSource()
            initKeyStoreRsaSource()
            initKeyStoreHybridSource()
        }
    }
}
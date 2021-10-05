## Migrate

### 索引

- [Migrate 1.0.0->1.1.0](#Migrate_1.0.0~1.1.0)

#### Migrate_1.0.0~1.1.0

- 第一步: 升級Gradle引用版本到1.1.0

- 第二步: 確認是否使用Tink-原本呼叫`CryptoHelper.init`參數有UseTinkParam物件為有使用

    ```kotlin
    UseTinkParam(useAes=true)

    // 取代為
    CryptoHelper.registerTink()
    ```

    - 第二步之一: 確認使用的Tink為哪一種加密模式，添加到`Koin`管理的Module中，沒有使用則不需要添加module

        - Aes  
        
        ```kotlin
        startKoin {
            modules(
                listOf(
                    getTinkAesModule(sharedPreferencesName = "tink_aes_sharedPreferences")
                )
            )
        }
        ```  

        - Rsa

        ```kotlin
        startKoin {
            modules(
                listOf(
                    getTinkRsaModule(sharedPreferencesName = "tink_rsa_sharedPreferences")
                )
            )
        }
        ```

- 第三步: 確認是否使用Keystore，沒有使用不需要添加Module

    - 第三步之一: 確認使用的Keystore為哪一種加密模式，添加到`Koin`管理的Module中

        - Aes

        ```kotlin
        startKoin {
            modules(
                listOf(
                    getKeyStoreAesModule(
                        xxxSharedPreferencesName = "key_store_aes_sharedPrefences",
                        xxxkeySets = listOf(
                            SecurityKeySet.AesKeySet // 提前生成KeyStore的Key設定
                        )
                    )
                )
            )
        }
        // init KeyStore key
        CryptoHelper.initKeyStoreAesSource()
        ```

        - Rsa

        ```kotlin
        startKoin {
            modules(
                listOf(
                    getKeyStoreRsaModule(
                        xxxSharedPreferencesName = "key_store_rsa_sharedPrefences",
                        xxxkeySets = listOf(
                            SecurityKeySet.RsaKeySet // 提前生成KeyStore的Key設定
                        )
                    )
                )
            )
        }
        // init KeyStore key
        CryptoHelper.initKeyStoreRsaSource()
        ```

        - Hybrid

        ```kotlin
        startKoin {
            modules(
                listOf(
                    getKeyStoreHybridModule(
                        xxxSharedPreferencesName = "key_store_hybrid_sharedPrefences",
                        xxxkeySets = listOf(
                            SecurityKeySet.HybridKeySet // 提前生成KeyStore的Key
                        )
                    )
                )
            )
        }
         // init KeyStore key
        CryptoHelper.initKeyStoreHybridSource()
        ```

##### 下方為完整 Migrate

- 舊的設定

```kotlin
override fun onCreate() {
    super.onCreate()

    CryptoHelper.init(
        application = this,
        useTinkParam = UseTinkParam(
            useAes = true,
            aesSharedPreferencesName = "tink_sharedPreferences_aes",
            useRsa = true,
            rsaSharedPreferencesName = "tink_sharedPreferences_rsa"
        ),
        useKeyStoreParam = UseKeyStoreParam(
            useRsa = true,
            useAes = true,
            useHybrid = true,
            rsaKeySets = listOf(KeyParams.InputRsa.keySet),
            aesKeySets = listOf(KeyParams.InputAes.keySet),
            hybridKeySets = listOf(KeyParams.InputHybrid.keySet),
            rsaSharedPreferencesName = "key_store_sharedPreferences_rsa",
            aesSharedPreferencesName = "key_store_sharedPreferences_aes",
            hybridSharedPreferencesName = "key_store_sharedPreferences_hybrid"
        )
    )
}
```

- 新的設定

```kotlin
override fun onCreate() {
    super.onCreate()

    CryptoHelper.registerTink()
    startKoin {
        androidContext(this@SampleApplication)
        if (BuildConfig.DEBUG) {
            androidLogger()
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
```
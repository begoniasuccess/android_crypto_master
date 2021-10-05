# 加密模組

## 索引

- [環境](#環境)
- [架構](#架構)
- [類別](#類別)
- [使用方法](#使用方法)

## 環境

```
Android Studio 4.0.1
Build #AI-193.6911.18.40.6626763, built on June 25, 2020
Runtime version: 1.8.0_242-release-1644-b01 amd64
VM: OpenJDK 64-Bit Server VM by JetBrains s.r.o
Windows 10 10.0
GC: ParNew, ConcurrentMarkSweep
Memory: 1998M
Cores: 6
Registry: ide.new.welcome.screen.force=true
Non-Bundled Plugins: org.jetbrains.kotlin, cn.wjdghd.unique.plugin.id, com.developerphil.adbidea, com.google.services.firebase, com.huawei.deveco.hms, com.suusan2go.kotlin-fill-class, commit-template-idea-plugin, de.netnexus.camelcaseplugin, detekt, org.intellij.plugins.markdown, wu.seal.tool.jsontokotlin
```

## 架構

目前使用物件設計，並使用`Koin`進行物件注入，將Rsa、Aes、Hybrid方法包裝成Helper

## 類別

```
// KeyStore
AesKeyStoreHelper
RsaKeyStoreHelper
HybridKeyStoreHelper

KeyStoreSource

// Google/Tink
SecureAesPreferences
SecureRsaPreferences

// CipherModel
CipherParam
CipherModel

// SecurityKeySet
SecurityKeySet

```

## 使用方法

#### Gradle

- build.gradle

```groovy
allprojects {
    google()
    mavenCentral()
    jcenter()
    // 新增檔案路徑源
    maven { url 'http://192.168.99.70:8081/repository/maven-public/' }
}
```

- app/build.gradle

```groovy
android {
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }
}

dependencies {
    debugImplementation "com.cmoney.crypto:crypto-android-debug:1.3.0"
    releaseImplementation "com.cmoney.crypto:crypto-android:1.3.0"
}
```

#### 設定所需參數

```kotlin
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
```

### MIGRATE

- 請根據目前引用版本MIGRATE說明進行修正  

文件: [MIGRATE文件](./documents/MIGRATE.md)

#### Application啟動時添加

```kotlin
class SampleApplication : Application() {

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
            CryptoHelper.initKeyStoreAesSource()
            CryptoHelper.initKeyStoreRsaSource()
            CryptoHelper.initKeyStoreHybridSource()
        }
    }
}
```

#### Activity中使用

```kotlin
class KeyStoreTestCaseActivity : AppCompatActivity() {

    private val keyStoreAesHelper by inject<AesKeyStoreHelper>()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_key_store_test_case)
        
        // 加密
        test_encrypt_button.setOnClickListener {
            keyStoreAesHelper.storeTextAsync(KeyParams.InputAes.keySet, inputText.toString())
        }
        
        // 解密
        test_decrypt_button.setOnClickListener {
            keyStoreAesHelper.getStoreTextAsync(KeyParams.InputAes.keySet)
        }
    }
}
```

#### 加密的邏輯處理

- CipherParam 提供`CipherModel`所需的參數，常數為提供Cipher Algorithm的推薦演算邏輯

```kotlin
sealed class CipherParam {

    /**
     * 使用於加解密 AES的 [Cipher] 相關設定
     *
     * @property algorithm
     * @property aesKey
     * @property iv
     */
    data class Aes(val algorithm: String, val aesKey: ByteArray, val iv: ByteArray) :
        CipherParam() {
        companion object {
            const val GCM_NOPADDING = "AES/GCM/NoPadding"

            const val CBC_PKCS7PADDING = "AES/CBC/PKCS7Padding"
        }
    }

    /**
     * 使用於加解密 RSA的 [Cipher] 相關設定
     *
     * @property algorithm
     * @property publicKey
     * @property privateKey
     */
    data class Rsa(val algorithm: String, val publicKey: Key, val privateKey: Key? = null) :
        CipherParam() {

        companion object {
            const val ECB_PKCS1PADDING = "RSA/ECB/PKCS1Padding"

            fun generateEncryptedKey(publicKeyText: String): PublicKey

            fun generateDecryptedKey(privateKeyText: String): PrivateKey
        }
    }
}
```

- CipherModel 可以使用此物件自行處理需要的加密邏輯

```kotlin
class CipherModel(private val useParam: CipherParam) {

    /**
     * 加密字串
     * 可能會有Exception發生，
     * 會以[Result.failure]方式回傳
     * 
     * @param plainText 要加密文字
     * @return 結果(失敗則Exception，成功是加密後文字)
     */
    fun encrypt(plainText: String): Result<String> {
        return kotlin.runCatching {
            when (useParam) {
                is CipherParam.Aes -> {
                    encryptAes(useParam, plainText)
                }
                is CipherParam.Rsa -> {
                    encryptRsa(useParam, plainText)
                }
            }
        }
    }

    @Throws(
        NoSuchAlgorithmException::class,
        NoSuchPaddingException::class,
        UnsupportedOperationException::class,
        InvalidAlgorithmParameterException::class,
        InvalidKeyException::class,
        IllegalStateException::class,
        BadPaddingException::class,
        IllegalBlockSizeException::class,
        AEADBadTagException::class
    )
    private fun encryptAes(param: CipherParam.Aes, plainText: String): String {
    }

    @Throws(
        NoSuchAlgorithmException::class,
        NoSuchPaddingException::class,
        UnsupportedOperationException::class,
        InvalidKeyException::class,
        IllegalStateException::class,
        BadPaddingException::class,
        IllegalBlockSizeException::class,
        AEADBadTagException::class
    )
    private fun encryptRsa(param: CipherParam.Rsa, plainText: String): String {
    }

    /**
     * 解密加密的字串
     * 若有Exception則以
     * [Result.failure]形式回傳
     *
     * @param encryptedText 欲解密字串
     * @return 成功則是解密字串，失敗則Exception
     */
    fun decrypt(encryptedText: String): Result<String> {
        val encrypted = Base64.decode(encryptedText, Base64.NO_WRAP)
        return kotlin.runCatching {
            when (useParam) {
                is CipherParam.Aes -> {
                    decryptAes(useParam, encrypted)
                }
                is CipherParam.Rsa -> {
                    decryptRsa(useParam, encrypted)
                }
            }
        }
    }

    @Throws(
        NoSuchAlgorithmException::class,
        NoSuchPaddingException::class,
        UnsupportedOperationException::class,
        InvalidAlgorithmParameterException::class,
        InvalidKeyException::class,
        IllegalStateException::class,
        BadPaddingException::class,
        IllegalBlockSizeException::class,
        AEADBadTagException::class
    )
    private fun decryptAes(param: CipherParam.Aes, encryptedBytes: ByteArray): String {
    }

    @Throws(
        NoSuchAlgorithmException::class,
        NoSuchPaddingException::class,
        UnsupportedOperationException::class,
        InvalidKeyException::class,
        IllegalStateException::class,
        BadPaddingException::class,
        IllegalBlockSizeException::class,
        AEADBadTagException::class
    )
    private fun decryptRsa(param: CipherParam.Rsa, encryptedBytes: ByteArray): String {
    }
}
```

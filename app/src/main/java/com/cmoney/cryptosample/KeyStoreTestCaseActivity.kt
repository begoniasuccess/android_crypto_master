package com.cmoney.cryptosample

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cmoney.crypto.keystore.aes.AesKeyStoreHelper
import com.cmoney.crypto.keystore.hybrid.HybridKeyStoreHelper
import com.cmoney.crypto.keystore.rsa.RsaKeyStoreHelper
import kotlinx.android.synthetic.main.activity_key_store_test_case.*
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject

class KeyStoreTestCaseActivity : AppCompatActivity() {

    private var nowCryptoType = CryptoType.AES

    private val keyStoreAesHelper by inject<AesKeyStoreHelper>()

    private val keyStoreRsaHelper by inject<RsaKeyStoreHelper>()

    private val keyStoreHybridHelper by inject<HybridKeyStoreHelper>()

    private val ioScope by lazy {
        CoroutineScope(SupervisorJob() + Dispatchers.IO)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_key_store_test_case)

        crypto_type_radioGroup.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.aes_radio_button -> {
                    nowCryptoType = CryptoType.AES
                }
                R.id.rsa_radio_button -> {
                    nowCryptoType = CryptoType.RSA
                }
                R.id.hybrid_radio_button -> {
                    nowCryptoType = CryptoType.HYBRID
                }
            }
        }

        encrypt_button.setOnClickListener {
            val inputText = input_editText.text
            if (inputText.isBlank()) {
                Toast.makeText(this, "Input Is Blank.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            ioScope.launch {
                when(nowCryptoType) {
                    CryptoType.RSA -> {
                        keyStoreRsaHelper.storeTextAsync(KeyParams.InputRsa.keySet, inputText.toString())
                    }
                    CryptoType.AES -> {
                        keyStoreAesHelper.storeTextAsync(KeyParams.InputAes.keySet, inputText.toString())
                    }
                    CryptoType.HYBRID -> {
                        keyStoreHybridHelper.storeTextAsync(KeyParams.InputHybrid.keySet, inputText.toString())
                    }
                }
            }
        }

        decrypt_button.setOnClickListener {
            ioScope.launch {
                val decryptedText = when(nowCryptoType) {
                    CryptoType.RSA -> {
                        keyStoreRsaHelper.getStoreTextAsync(KeyParams.InputRsa.keySet)
                    }
                    CryptoType.AES -> {
                        keyStoreAesHelper.getStoreTextAsync(KeyParams.InputAes.keySet)
                    }
                    CryptoType.HYBRID -> {
                        keyStoreHybridHelper.getStoreTextAsync(KeyParams.InputHybrid.keySet)
                    }
                }
                withContext(Dispatchers.Main) {
                    output_textView.text = decryptedText
                }
            }
        }
    }

    override fun onDestroy() {
        ioScope.cancel()
        super.onDestroy()
    }

    enum class CryptoType {
        RSA,
        AES,
        HYBRID
    }
}

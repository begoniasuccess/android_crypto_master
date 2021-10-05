package com.cmoney.cryptosample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.cmoney.crypto.tink.SecureAesPreferences
import com.cmoney.crypto.tink.SecureRsaPreferences
import kotlinx.android.synthetic.main.activity_tink_test_case.*
import org.koin.android.ext.android.inject

class TinkTestCaseActivity : AppCompatActivity() {

    private val secureAesPreferences by inject<SecureAesPreferences>()

    private val secureRsaPreferences by inject<SecureRsaPreferences>()

    private var isRsa = false

    private val dataKey = "test_case_key"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tink_test_case)

        isRsa_switch.setOnCheckedChangeListener { _, isChecked ->
            isRsa = isChecked
        }

        encrypt_button.setOnClickListener {
            val inputText = input_editText.text
            if (inputText.isBlank()) {
                Toast.makeText(this, "Input Is Blank.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (isRsa) {
                secureRsaPreferences.edit()
                    .putString(dataKey, inputText.toString())
                    .apply()
            } else {
                secureAesPreferences.edit()
                    .putString(dataKey, inputText.toString())
                    .apply()
            }
        }

        decrypt_button.setOnClickListener {
            val decryptedText = if (isRsa) {
                secureRsaPreferences.getString(dataKey, "").orEmpty()
            } else {
                secureAesPreferences.getString(dataKey, "").orEmpty()
            }
            if (decryptedText.isBlank()) {
                Toast.makeText(this, "Decrypt Is Blank.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            output_textView.text = decryptedText
        }
    }
}

package com.cmoney.crypto

import android.content.Context

class RsaKeyTextProvider(context: Context) {

    val privateKeyText: String by lazy {
        context.assets.open("rsa_privatekey.txt")
            .use {
                val reader = it.reader()
                reader.readText()
                    .removePrefix("-----BEGIN RSA PRIVATE KEY-----")
                    .removeSuffix("-----END RSA PRIVATE KEY-----")
            }
    }

    val publicKeyText: String by lazy {
        context.assets.open("rsa_publickey.txt")
            .use {
                val reader = it.reader()
                reader.readText()
                    .removePrefix("-----BEGIN PUBLIC KEY-----")
                    .removeSuffix("-----END PUBLIC KEY-----")
            }
    }
}
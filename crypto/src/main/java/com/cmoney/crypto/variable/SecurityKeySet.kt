package com.cmoney.crypto.variable

sealed class SecurityKeySet {

    data class AesKeySet(val aesKey: String, val ivKey: String, val dataKey: String): SecurityKeySet()

    data class RsaKeySet(val alias: String, val dataKey: String): SecurityKeySet()

    data class HybridKeySet(val alias: String, val aesKey: String, val ivKey: String, val dataKey: String): SecurityKeySet()
}
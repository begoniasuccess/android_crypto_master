package com.cmoney.crypto.di

data class UseKeyStoreParam(
    val useAes: Boolean = false,
    val useRsa: Boolean = false,
    val useHybrid: Boolean = false
)
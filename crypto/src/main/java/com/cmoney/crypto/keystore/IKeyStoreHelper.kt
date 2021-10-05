package com.cmoney.crypto.keystore

import androidx.annotation.WorkerThread
import com.cmoney.crypto.variable.SecurityKeySet

interface IKeyStoreHelper {
    @WorkerThread
    fun storeText(keySet: SecurityKeySet, input: String)

    suspend fun storeTextAsync(keySet: SecurityKeySet, input: String)

    @WorkerThread
    fun getStoreText(keySet: SecurityKeySet): String

    suspend fun getStoreTextAsync(keySet: SecurityKeySet): String
}
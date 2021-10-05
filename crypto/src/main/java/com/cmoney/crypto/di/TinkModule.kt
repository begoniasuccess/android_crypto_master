package com.cmoney.crypto.di

import com.cmoney.crypto.tink.SecureAesPreferences
import com.cmoney.crypto.tink.SecureRsaPreferences
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

fun getTinkAesModule(sharedPreferencesName: String) = module {
    single {
        SecureAesPreferences(androidContext(), sharedPreferencesName)
    }
}

fun getTinkRsaModule(sharedPreferencesName: String) =
    module {
        single {
            SecureRsaPreferences(androidContext(), sharedPreferencesName)
        }
    }
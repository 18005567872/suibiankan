package com.suibiankan.tv

import android.app.Application
import com.suibiankan.tv.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import timber.log.Timber

class TVApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize Timber for logging
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        // Initialize Koin dependency injection
        startKoin {
            androidContext(this@TVApplication)
            modules(appModule)
        }
    }
}

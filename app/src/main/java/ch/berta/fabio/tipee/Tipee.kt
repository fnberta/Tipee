package ch.berta.fabio.tipee

import android.app.Application
import timber.log.Timber

class Tipee : Application() {

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}
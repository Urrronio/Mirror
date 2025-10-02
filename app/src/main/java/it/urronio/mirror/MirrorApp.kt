package it.urronio.mirror

import android.app.Application
import it.urronio.mirror.di.appModules
import it.urronio.mirror.di.dataModules
import it.urronio.mirror.di.viewModelModules
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MirrorApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@MirrorApp)
            modules(
                appModules,
                dataModules,
                viewModelModules
            )
        }
    }
}
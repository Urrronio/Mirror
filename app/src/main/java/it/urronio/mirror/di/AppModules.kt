package it.urronio.mirror.di

import android.content.Context
import android.hardware.usb.UsbManager
import android.location.LocationManager
import androidx.compose.foundation.layout.ContextualFlowRow
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val appModules = module {
    single<UsbManager> { androidContext().getSystemService(Context.USB_SERVICE) as UsbManager }
    single<LocationManager> { androidContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager }
}
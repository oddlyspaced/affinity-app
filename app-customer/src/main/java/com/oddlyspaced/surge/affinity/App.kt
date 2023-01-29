package com.oddlyspaced.surge.affinity

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App: Application() {
    companion object {
        private const val API_URL_LINK = "http://192.168.29.36"
        private const val API_URL_PORT = "4444"
        const val API_URL = "$API_URL_LINK:$API_URL_PORT"
    }
}
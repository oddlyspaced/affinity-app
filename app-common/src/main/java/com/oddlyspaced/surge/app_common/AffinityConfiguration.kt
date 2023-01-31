package com.oddlyspaced.surge.app_common

class AffinityConfiguration {
    companion object {
        private const val API_URL_LINK = "http://192.168.29.36"
        private const val API_URL_PORT = "4444"
        const val API_URL = "$API_URL_LINK:$API_URL_PORT"
        const val DEFAULT_MAP_ZOOM = 14
    }
}
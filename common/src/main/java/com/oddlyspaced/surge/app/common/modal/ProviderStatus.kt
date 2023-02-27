package com.oddlyspaced.surge.app.common.modal

enum class ProviderStatus {
    ACTIVE,
    INACTIVE,
    UNDEFINED,
}
fun ProviderStatus.flip(): ProviderStatus {
    return when (this) {
        ProviderStatus.ACTIVE -> ProviderStatus.INACTIVE
        ProviderStatus.INACTIVE -> ProviderStatus.ACTIVE
        ProviderStatus.UNDEFINED -> ProviderStatus.UNDEFINED
    }
}
package com.oddlyspaced.surge.app.common.modal

/**
 * enum for different states of provider
 */
enum class ProviderStatus {
    ACTIVE,
    INACTIVE,
    UNDEFINED,
}

/**
 * extension function to return the negation of a provider state
 * @return new state of provider
 */
fun ProviderStatus.flip(): ProviderStatus {
    return when (this) {
        ProviderStatus.ACTIVE -> ProviderStatus.INACTIVE
        ProviderStatus.INACTIVE -> ProviderStatus.ACTIVE
        ProviderStatus.UNDEFINED -> ProviderStatus.UNDEFINED
    }
}
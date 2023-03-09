package com.oddlyspaced.surge.app.common.modal

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * body class for post call to update status of a provider
 * @param id id of provider
 * @param newStatus new status of provider
 */
@Parcelize
data class StatusUpdateParameter(
    val id: Int,
    val newStatus: ProviderStatus,
): Parcelable
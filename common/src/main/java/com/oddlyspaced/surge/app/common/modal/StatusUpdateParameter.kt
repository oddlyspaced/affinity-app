package com.oddlyspaced.surge.app.common.modal

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class StatusUpdateParameter(
    val id: Int,
    val newStatus: ProviderStatus,
): Parcelable
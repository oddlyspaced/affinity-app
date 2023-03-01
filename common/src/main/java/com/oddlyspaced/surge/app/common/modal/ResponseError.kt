package com.oddlyspaced.surge.app.common.modal

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ResponseError(
    val message: String?,
    val error: Boolean = true,
): Parcelable
package com.oddlyspaced.surge.app.common.modal

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class AreaUpdateParameter(
    val id: Int,
    val newArea: AreaServed,
): Parcelable
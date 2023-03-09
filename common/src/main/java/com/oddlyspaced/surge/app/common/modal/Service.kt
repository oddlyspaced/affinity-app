package com.oddlyspaced.surge.app.common.modal

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * modal class to represent a service tag
 * @param id service id
 * @param tag service name
 * @param rank rank for comparison [less means the service is ranked higher]
 */
@Parcelize
data class Service(
    val id: Int,
    val tag: String,
    val rank: Int,
): Parcelable
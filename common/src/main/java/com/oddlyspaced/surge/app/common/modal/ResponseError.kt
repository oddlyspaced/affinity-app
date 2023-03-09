package com.oddlyspaced.surge.app.common.modal

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * modal class to represent the output of a message based api call
 * @param message message from server
 * @param error boolean to represent if the api call was successful
 */
@Parcelize
data class ResponseError(
    val message: String?,
    val error: Boolean = true,
): Parcelable
package com.oddlyspaced.surge.app.common.modal

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * modal class to store phone number representation
 * @param countryCode country code of phone number
 * @param phoneNumber phone number without country code
 */
@Parcelize
data class PhoneNumber(
    val countryCode: String,
    val phoneNumber: String,
): Parcelable
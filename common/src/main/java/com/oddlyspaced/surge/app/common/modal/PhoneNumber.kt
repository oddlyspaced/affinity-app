package com.oddlyspaced.surge.app.common.modal

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


/**
 * data class to store phone number representation
 */
@Parcelize
data class PhoneNumber(
    val countryCode: String,// country code
    val phoneNumber: String,// phone number
): Parcelable
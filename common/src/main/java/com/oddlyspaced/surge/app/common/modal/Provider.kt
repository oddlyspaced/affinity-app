package com.oddlyspaced.surge.app.common.modal

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


/**
 * data class to hold the properties of a Provider
 */
@Parcelize
data class Provider(
    val id: Int, // unique id for provider
    val name: String, // name of provider
    val phone: PhoneNumber, // phone number of provider
    val location: Location,
    val services: ArrayList<String>, // tags of all the services the provider offers
): Parcelable
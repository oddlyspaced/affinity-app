package com.oddlyspaced.surge.app.common.modal

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * modal class to hold the properties of a Provider
 * @param id id of provider
 * @param name of provider
 * @param phone phone number of provider
 * @param location location of provider
 * @param services services of provider
 * @param areaServed area served by provider
 * @param status status of provider [ACTIVE by default]
 */
@Parcelize
data class Provider(
    val id: Int = -1,
    val name: String,
    val phone: PhoneNumber,
    val location: Location,
    val services: ArrayList<String>,
    var areaServed: AreaServed,
    var status: ProviderStatus,
): Parcelable
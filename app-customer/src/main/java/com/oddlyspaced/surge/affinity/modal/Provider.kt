package com.oddlyspaced.surge.affinity.modal

/**
 * data class to hold the properties of a Provider
 */
data class Provider(
    val id: Int, // unique id for provider
    val name: String, // name of provider
    val phone: PhoneNumber, // phone number of provider
    val location: Location,
    val services: ArrayList<String>, // tags of all the services the provider offers
)
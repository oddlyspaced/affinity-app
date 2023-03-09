package com.oddlyspaced.surge.app.common.modal

/**
 * modal class to store address representation of a location
 * @param location location of point
 * @param address address of said location
 */
data class Address(
    val location: Location,
    val address: String,
)
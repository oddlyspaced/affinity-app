package com.oddlyspaced.surge.app_common.nominatim.modal

import com.google.gson.annotations.SerializedName


data class Address(
    @SerializedName("road") var road: String? = null,
    @SerializedName("hamlet") var hamlet: String? = null,
    @SerializedName("village") var village: String? = null,
    @SerializedName("county") var county: String? = null,
    @SerializedName("state_district") var stateDistrict: String? = null,
    @SerializedName("state") var state: String? = null,
    @SerializedName("postcode")
    var postcode: String? = null,
    @SerializedName("country")
    var country: String? = null,
    @SerializedName("country_code")
    var countryCode: String? = null
)
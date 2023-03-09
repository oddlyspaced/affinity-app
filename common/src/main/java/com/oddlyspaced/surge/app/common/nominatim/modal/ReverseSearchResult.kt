package com.oddlyspaced.surge.app.common.nominatim.modal

import com.google.gson.annotations.SerializedName

/**
 * search result from nominatim api
 * @param displayName fetched display name
 */
data class ReverseSearchResult(
    @SerializedName("display_name") var displayName: String? = null,
)
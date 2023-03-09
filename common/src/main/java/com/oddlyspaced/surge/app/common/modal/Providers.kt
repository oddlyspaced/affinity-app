package com.oddlyspaced.surge.app.common.modal

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * modal class to hold a list of providers
 * (mainly used for navigation params)
 * @param providers provider list to hold
 */
@Parcelize
data class Providers(
    val providers: List<Provider>
): Parcelable
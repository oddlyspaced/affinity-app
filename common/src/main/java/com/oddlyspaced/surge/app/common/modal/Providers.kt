package com.oddlyspaced.surge.app.common.modal

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Providers(
    val providers: ArrayList<Provider>
): Parcelable
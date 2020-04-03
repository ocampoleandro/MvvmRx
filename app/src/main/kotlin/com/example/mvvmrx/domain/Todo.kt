package com.example.mvvmrx.domain

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize

@JsonClass(generateAdapter = true)
@Parcelize
data class Todo(
    val id: Int = 0,
    val title: String = "",
    val completed: Boolean = false
): Parcelable
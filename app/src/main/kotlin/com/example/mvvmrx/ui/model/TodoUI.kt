package com.example.mvvmrx.ui.model

import android.os.Parcelable
import androidx.annotation.ColorRes
import kotlinx.android.parcel.Parcelize

@Parcelize
data class TodoUI(
    val id: Int,
    val title: String,
    @ColorRes
    val colorState: Int
): Parcelable
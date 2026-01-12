package com.example.myapplication.model

import android.graphics.drawable.Drawable

data class AppInfo(
    val name: String,
    val packageName: String,
    val icon: Drawable?,
    val sourceDir: String,
    val versionName: String
)

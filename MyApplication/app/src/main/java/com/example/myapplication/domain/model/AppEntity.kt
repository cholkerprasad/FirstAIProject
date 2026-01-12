package com.example.myapplication.domain.model

import android.graphics.drawable.Drawable

data class AppEntity(
    val name: String,
    val packageName: String,
    val icon: Drawable?,
    val sourceDir: String,
    val splitSourceDirs: Array<String>?,
    val versionName: String
) {
    val isSplit: Boolean get() = !splitSourceDirs.isNullOrEmpty()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AppEntity

        if (name != other.name) return false
        if (packageName != other.packageName) return false
        if (icon != other.icon) return false
        if (sourceDir != other.sourceDir) return false
        if (splitSourceDirs != null) {
            if (other.splitSourceDirs == null) return false
            if (!splitSourceDirs.contentEquals(other.splitSourceDirs)) return false
        } else if (other.splitSourceDirs != null) return false
        if (versionName != other.versionName) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + packageName.hashCode()
        result = 31 * result + (icon?.hashCode() ?: 0)
        result = 31 * result + sourceDir.hashCode()
        result = 31 * result + (splitSourceDirs?.contentHashCode() ?: 0)
        result = 31 * result + versionName.hashCode()
        return result
    }
}

package com.example.myapplication.domain.repository

import com.example.myapplication.domain.model.AppEntity
import kotlinx.coroutines.flow.Flow

interface AppRepository {
    suspend fun getInstalledApps(): List<AppEntity>
    suspend fun extractApk(app: AppEntity): String? // Returns the path of extracted APK
}

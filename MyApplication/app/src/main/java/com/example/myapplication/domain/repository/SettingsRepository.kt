package com.example.myapplication.domain.repository

import kotlinx.coroutines.flow.Flow

enum class DefaultAction {
    EXTRACT, SHARE
}

interface SettingsRepository {
    fun getDefaultAction(): Flow<DefaultAction>
    suspend fun setDefaultAction(action: DefaultAction)
}

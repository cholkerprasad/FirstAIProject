package com.example.myapplication.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.myapplication.domain.repository.DefaultAction
import com.example.myapplication.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

class SettingsRepositoryImpl(private val context: Context) : SettingsRepository {

    private val DEFAULT_ACTION_KEY = stringPreferencesKey("default_action")

    override fun getDefaultAction(): Flow<DefaultAction> {
        return context.dataStore.data.map { preferences ->
            val actionString = preferences[DEFAULT_ACTION_KEY] ?: DefaultAction.SHARE.name
            DefaultAction.valueOf(actionString)
        }
    }

    override suspend fun setDefaultAction(action: DefaultAction) {
        context.dataStore.edit { preferences ->
            preferences[DEFAULT_ACTION_KEY] = action.name
        }
    }
}

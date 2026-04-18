package com.utility.eldac

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "ldac_settings")

class SettingsRepository(private val context: Context) {

    private object Keys {
        val BIT_RATE = stringPreferencesKey("bit_rate")
        val BIT_DEPTH = stringPreferencesKey("bit_depth")
        val SAMPLING_RATE = stringPreferencesKey("sampling_rate")
    }

    val audioSettings: Flow<AudioSettings> = context.dataStore.data.map { prefs ->
        AudioSettings(
            bitRate = prefs[Keys.BIT_RATE] ?: AudioSettings.DEFAULT_BIT_RATE,
            bitDepth = prefs[Keys.BIT_DEPTH] ?: AudioSettings.DEFAULT_BIT_DEPTH,
            samplingRate = prefs[Keys.SAMPLING_RATE] ?: AudioSettings.DEFAULT_SAMPLING_RATE
        )
    }

    suspend fun saveBitRate(value: String) {
        context.dataStore.edit { it[Keys.BIT_RATE] = value }
    }

    suspend fun saveBitDepth(value: String) {
        context.dataStore.edit { it[Keys.BIT_DEPTH] = value }
    }

    suspend fun saveSamplingRate(value: String) {
        context.dataStore.edit { it[Keys.SAMPLING_RATE] = value }
    }
}

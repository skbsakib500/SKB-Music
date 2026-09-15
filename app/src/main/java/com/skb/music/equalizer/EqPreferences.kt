package com.skb.music.equalizer

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val Context.eqDataStore: DataStore<Preferences> by preferencesDataStore(name = "skb_eq")

data class EqSettings(
    val enabled: Boolean = false,
    val preset: Int = 0,
    val bandLevelsCsv: String = "",
    val bass: Int = 0,
    val virtualizer: Int = 0,
    val loudness: Int = 0
)

object EqPreferences {

    private val KEY_ENABLED = booleanPreferencesKey("eq_enabled")
    private val KEY_PRESET = intPreferencesKey("eq_preset")
    private val KEY_BANDS = stringPreferencesKey("eq_bands")
    private val KEY_BASS = intPreferencesKey("eq_bass")
    private val KEY_VIRT = intPreferencesKey("eq_virtualizer")
    private val KEY_LOUD = intPreferencesKey("eq_loudness")

    suspend fun load(context: Context): EqSettings {
        val prefs = context.eqDataStore.data.first()
        return EqSettings(
            enabled = prefs[KEY_ENABLED] ?: false,
            preset = prefs[KEY_PRESET] ?: 0,
            bandLevelsCsv = prefs[KEY_BANDS] ?: "",
            bass = prefs[KEY_BASS] ?: 0,
            virtualizer = prefs[KEY_VIRT] ?: 0,
            loudness = prefs[KEY_LOUD] ?: 0
        )
    }

    suspend fun saveEnabled(context: Context, enabled: Boolean) {
        context.eqDataStore.edit { it[KEY_ENABLED] = enabled }
    }

    suspend fun savePreset(context: Context, preset: Int) {
        context.eqDataStore.edit { it[KEY_PRESET] = preset }
    }

    suspend fun saveBands(context: Context, levels: List<Short>) {
        val csv = levels.joinToString(",")
        context.eqDataStore.edit { it[KEY_BANDS] = csv }
    }

    suspend fun saveBass(context: Context, value: Int) {
        context.eqDataStore.edit { it[KEY_BASS] = value }
    }

    suspend fun saveVirtualizer(context: Context, value: Int) {
        context.eqDataStore.edit { it[KEY_VIRT] = value }
    }

    suspend fun saveLoudness(context: Context, value: Int) {
        context.eqDataStore.edit { it[KEY_LOUD] = value }
    }

    suspend fun clear(context: Context) {
        context.eqDataStore.edit { it.clear() }
    }

    fun parseBands(csv: String): List<Short> {
        if (csv.isBlank()) return emptyList()
        return csv.split(",").mapNotNull { it.trim().toShortOrNull() }
    }
}

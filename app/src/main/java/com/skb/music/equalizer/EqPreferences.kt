package com.skb.music.equalizer

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

val Context.eqDataStore: DataStore<Preferences> by preferencesDataStore(name = "skb_eq")

data class EqSettings(
    val enabled: Boolean = false,
    val preset: Int = 0,
    val bandLevelsCsv: String = "",
    val bass: Int = 0,
    val virtualizer: Int = 0,
    val loudness: Int = 0,
    val replayGainEnabled: Boolean = false,
    val replayGainTargetDb: Float = -14f,
    val replayGainMaxBoostDb: Float = 6f,
    val replayGainPreventClip: Boolean = true
)

object EqPreferences {

    private val KEY_ENABLED = booleanPreferencesKey("eq_enabled")
    private val KEY_PRESET = intPreferencesKey("eq_preset")
    private val KEY_BANDS = stringPreferencesKey("eq_bands")
    private val KEY_BASS = intPreferencesKey("eq_bass")
    private val KEY_VIRT = intPreferencesKey("eq_virtualizer")
    private val KEY_LOUD = intPreferencesKey("eq_loudness")
    private val KEY_RG_ENABLED = booleanPreferencesKey("rg_enabled")
    private val KEY_RG_TARGET = floatPreferencesKey("rg_target")
    private val KEY_RG_MAXBOOST = floatPreferencesKey("rg_maxboost")
    private val KEY_RG_PREVENT = booleanPreferencesKey("rg_prevent")

    suspend fun load(context: Context): EqSettings {
        val p = context.eqDataStore.data.first()
        return EqSettings(
            enabled = p[KEY_ENABLED] ?: false,
            preset = p[KEY_PRESET] ?: 0,
            bandLevelsCsv = p[KEY_BANDS] ?: "",
            bass = p[KEY_BASS] ?: 0,
            virtualizer = p[KEY_VIRT] ?: 0,
            loudness = p[KEY_LOUD] ?: 0,
            replayGainEnabled = p[KEY_RG_ENABLED] ?: false,
            replayGainTargetDb = p[KEY_RG_TARGET] ?: -14f,
            replayGainMaxBoostDb = p[KEY_RG_MAXBOOST] ?: 6f,
            replayGainPreventClip = p[KEY_RG_PREVENT] ?: true
        )
    }

    suspend fun saveEnabled(c: Context, v: Boolean) { c.eqDataStore.edit { it[KEY_ENABLED] = v } }
    suspend fun savePreset(c: Context, v: Int) { c.eqDataStore.edit { it[KEY_PRESET] = v } }
    suspend fun saveBands(c: Context, levels: List<Short>) {
        c.eqDataStore.edit { it[KEY_BANDS] = levels.joinToString(",") }
    }
    suspend fun saveBass(c: Context, v: Int) { c.eqDataStore.edit { it[KEY_BASS] = v } }
    suspend fun saveVirtualizer(c: Context, v: Int) { c.eqDataStore.edit { it[KEY_VIRT] = v } }
    suspend fun saveLoudness(c: Context, v: Int) { c.eqDataStore.edit { it[KEY_LOUD] = v } }

    suspend fun saveReplayGainEnabled(c: Context, v: Boolean) {
        c.eqDataStore.edit { it[KEY_RG_ENABLED] = v }
    }
    suspend fun saveReplayGainTarget(c: Context, v: Float) {
        c.eqDataStore.edit { it[KEY_RG_TARGET] = v }
    }
    suspend fun saveReplayGainMaxBoost(c: Context, v: Float) {
        c.eqDataStore.edit { it[KEY_RG_MAXBOOST] = v }
    }
    suspend fun saveReplayGainPreventClip(c: Context, v: Boolean) {
        c.eqDataStore.edit { it[KEY_RG_PREVENT] = v }
    }

    fun parseBands(csv: String): List<Short> {
        if (csv.isBlank()) return emptyList()
        return csv.split(",").mapNotNull { it.trim().toShortOrNull() }
    }
}

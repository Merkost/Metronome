package com.merkost.metronome.model

import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import com.merkost.metronome.presets.InMemoryPreferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class AppDatastoreTrainerModeTest {
    @Test
    fun theStepModeIsRemembered() = runTest {
        val store = AppDatastoreImpl(InMemoryPreferencesDataStore())
        store.saveLastTrainerConfig(GradualTempoConfig(100, 120, 2, 3, TempoStepMode.GAP_CYCLE))
        assertEquals(TempoStepMode.GAP_CYCLE, store.lastTrainerConfig.first()?.stepMode)
        store.saveLastTrainerConfig(GradualTempoConfig(100, 120, 2, 4, TempoStepMode.MANUAL))
        assertEquals(TempoStepMode.MANUAL, store.lastTrainerConfig.first()?.stepMode)
    }

    @Test
    fun aStoredTrainerWithoutAModeReadsAsBars() = runTest {
        val preferences = InMemoryPreferencesDataStore()
        preferences.edit {
            it[intPreferencesKey("TRAINER_START_BPM")] = 90
            it[intPreferencesKey("TRAINER_END_BPM")] = 110
        }
        val config = AppDatastoreImpl(preferences).lastTrainerConfig.first()
        assertEquals(TempoStepMode.BARS, config?.stepMode)
        assertEquals(90, config?.startBpm)
    }
}

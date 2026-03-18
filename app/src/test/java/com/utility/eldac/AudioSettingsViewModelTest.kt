package com.utility.eldac

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AudioSettingsViewModelTest {

    private lateinit var viewModel: AudioSettingsViewModel

    @Before
    fun setUp() {
        viewModel = AudioSettingsViewModel()
    }

    @Test
    fun defaultAudioSettings_hasExpectedValues() {
        val settings = viewModel.audioSettings.value
        assertEquals("990 kbps", settings.bitRate)
        assertEquals("24 bit", settings.bitDepth)
        assertEquals("96 kHz", settings.samplingRate)
    }

    @Test
    fun defaultDeviceState_isNotConnected() {
        val state = viewModel.deviceState.value
        assertEquals("Not Connected", state.name)
        assertEquals(0, state.batteryLevel)
        assertEquals("N/A", state.signalStrength)
        assertFalse(state.isConnected)
    }

    @Test
    fun selectBitRate_updatesValue() {
        viewModel.selectBitRate("330 kbps")
        assertEquals("330 kbps", viewModel.audioSettings.value.bitRate)
    }

    @Test
    fun selectBitRate_doesNotAffectOtherSettings() {
        viewModel.selectBitRate("660 kbps")
        val settings = viewModel.audioSettings.value
        assertEquals("660 kbps", settings.bitRate)
        assertEquals("24 bit", settings.bitDepth)
        assertEquals("96 kHz", settings.samplingRate)
    }

    @Test
    fun selectBitDepth_updatesValue() {
        viewModel.selectBitDepth("16 bit")
        assertEquals("16 bit", viewModel.audioSettings.value.bitDepth)
    }

    @Test
    fun selectSamplingRate_updatesValue() {
        viewModel.selectSamplingRate("44.1 kHz")
        assertEquals("44.1 kHz", viewModel.audioSettings.value.samplingRate)
    }

    @Test
    fun updateDeviceState_partialUpdate() {
        viewModel.updateDeviceState(name = "Sony WH-1000XM5", isConnected = true)
        val state = viewModel.deviceState.value
        assertEquals("Sony WH-1000XM5", state.name)
        assertTrue(state.isConnected)
        assertEquals(0, state.batteryLevel)
        assertEquals("N/A", state.signalStrength)
    }

    @Test
    fun updateDeviceState_fullUpdate() {
        viewModel.updateDeviceState(
            name = "Sony WH-1000XM5",
            batteryLevel = 85,
            signalStrength = "Good",
            isConnected = true
        )
        val state = viewModel.deviceState.value
        assertEquals("Sony WH-1000XM5", state.name)
        assertEquals(85, state.batteryLevel)
        assertEquals("Good", state.signalStrength)
        assertTrue(state.isConnected)
    }

    @Test
    fun multipleSelections_lastWins() {
        viewModel.selectBitRate("330 kbps")
        viewModel.selectBitRate("660 kbps")
        viewModel.selectBitRate("990 kbps")
        assertEquals("990 kbps", viewModel.audioSettings.value.bitRate)
    }
}

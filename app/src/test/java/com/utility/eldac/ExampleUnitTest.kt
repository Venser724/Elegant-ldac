package com.utility.eldac

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Test

class AudioSettingsTest {

    @Test
    fun defaultAudioSettings_hasHighQualityDefaults() {
        val settings = AudioSettings()
        assertEquals("990 kbps", settings.bitRate)
        assertEquals("24 bit", settings.bitDepth)
        assertEquals("96 kHz", settings.samplingRate)
    }

    @Test
    fun audioSettings_copyPreservesUnchangedFields() {
        val original = AudioSettings()
        val modified = original.copy(bitRate = "330 kbps")
        assertEquals("330 kbps", modified.bitRate)
        assertEquals(original.bitDepth, modified.bitDepth)
        assertEquals(original.samplingRate, modified.samplingRate)
    }

    @Test
    fun audioSettings_equalityWorks() {
        val a = AudioSettings(bitRate = "660 kbps")
        val b = AudioSettings(bitRate = "660 kbps")
        assertEquals(a, b)
    }

    @Test
    fun audioSettings_inequalityWorks() {
        val a = AudioSettings(bitRate = "660 kbps")
        val b = AudioSettings(bitRate = "990 kbps")
        assertNotEquals(a, b)
    }

    @Test
    fun defaultDeviceState_isDisconnected() {
        val state = DeviceState()
        assertEquals("Not Connected", state.name)
        assertEquals(0, state.batteryLevel)
        assertEquals("N/A", state.signalStrength)
        assertFalse(state.isConnected)
    }

    @Test
    fun deviceState_copyUpdatesCorrectly() {
        val state = DeviceState()
        val connected = state.copy(
            name = "WH-1000XM5",
            isConnected = true,
            batteryLevel = 85,
            signalStrength = "Excellent"
        )
        assertEquals("WH-1000XM5", connected.name)
        assertEquals(85, connected.batteryLevel)
        assertEquals("Excellent", connected.signalStrength)
        assertEquals(true, connected.isConnected)
    }
}

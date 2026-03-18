package com.utility.eldac

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ApplyStatusTest {

    @Test
    fun idle_isDefaultState() {
        val status: ApplyStatus = ApplyStatus.Idle
        assertTrue(status is ApplyStatus.Idle)
    }

    @Test
    fun success_carriesMessage() {
        val status = ApplyStatus.Success("Applied")
        assertEquals("Applied", status.message)
    }

    @Test
    fun error_carriesMessage() {
        val status = ApplyStatus.Error("Failed")
        assertEquals("Failed", status.message)
    }

    @Test
    fun permissionRequired_carriesMessage() {
        val status = ApplyStatus.PermissionRequired("Need root")
        assertEquals("Need root", status.message)
    }

    @Test
    fun applying_isSingleton() {
        val a = ApplyStatus.Applying
        val b = ApplyStatus.Applying
        assertTrue(a === b)
    }
}

class AudioSettingsDefaultsTest {

    @Test
    fun defaultConstants_matchDefaultConstructor() {
        val settings = AudioSettings()
        assertEquals(AudioSettings.DEFAULT_BIT_RATE, settings.bitRate)
        assertEquals(AudioSettings.DEFAULT_BIT_DEPTH, settings.bitDepth)
        assertEquals(AudioSettings.DEFAULT_SAMPLING_RATE, settings.samplingRate)
    }

    @Test
    fun defaultValues_areHighQuality() {
        assertEquals("990 kbps", AudioSettings.DEFAULT_BIT_RATE)
        assertEquals("24 bit", AudioSettings.DEFAULT_BIT_DEPTH)
        assertEquals("96 kHz", AudioSettings.DEFAULT_SAMPLING_RATE)
    }
}

class DeviceStateDefaultsTest {

    @Test
    fun defaultState_isDisconnected() {
        val state = DeviceState()
        assertFalse(state.isConnected)
        assertEquals("Not Connected", state.name)
        assertEquals(0, state.batteryLevel)
        assertEquals("N/A", state.signalStrength)
    }

    @Test
    fun connectedState_hasAllFields() {
        val state = DeviceState(
            name = "WH-1000XM5",
            batteryLevel = 85,
            signalStrength = "Excellent",
            isConnected = true
        )
        assertTrue(state.isConnected)
        assertEquals("WH-1000XM5", state.name)
        assertEquals(85, state.batteryLevel)
    }
}

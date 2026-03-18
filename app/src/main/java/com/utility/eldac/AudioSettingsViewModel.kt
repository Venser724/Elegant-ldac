package com.utility.eldac

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class AudioSettings(
    val bitRate: String = DEFAULT_BIT_RATE,
    val bitDepth: String = DEFAULT_BIT_DEPTH,
    val samplingRate: String = DEFAULT_SAMPLING_RATE
) {
    companion object {
        const val DEFAULT_BIT_RATE = "990 kbps"
        const val DEFAULT_BIT_DEPTH = "24 bit"
        const val DEFAULT_SAMPLING_RATE = "96 kHz"
    }
}

data class DeviceState(
    val name: String = "Not Connected",
    val batteryLevel: Int = 0,
    val signalStrength: String = "N/A",
    val isConnected: Boolean = false
)

class AudioSettingsViewModel : ViewModel() {
    private val _audioSettings = MutableStateFlow(AudioSettings())
    val audioSettings: StateFlow<AudioSettings> = _audioSettings.asStateFlow()

    private val _deviceState = MutableStateFlow(DeviceState())
    val deviceState: StateFlow<DeviceState> = _deviceState.asStateFlow()

    fun selectBitRate(bitRate: String) {
        _audioSettings.update { it.copy(bitRate = bitRate) }
    }

    fun selectBitDepth(bitDepth: String) {
        _audioSettings.update { it.copy(bitDepth = bitDepth) }
    }

    fun selectSamplingRate(samplingRate: String) {
        _audioSettings.update { it.copy(samplingRate = samplingRate) }
    }

    fun updateDeviceState(
        name: String? = null,
        batteryLevel: Int? = null,
        signalStrength: String? = null,
        isConnected: Boolean? = null
    ) {
        _deviceState.update { current ->
            current.copy(
                name = name ?: current.name,
                batteryLevel = batteryLevel ?: current.batteryLevel,
                signalStrength = signalStrength ?: current.signalStrength,
                isConnected = isConnected ?: current.isConnected
            )
        }
    }
}

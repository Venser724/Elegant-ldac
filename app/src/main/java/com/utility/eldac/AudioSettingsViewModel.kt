package com.utility.eldac

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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

data class DiagnosticInfo(
    val requested: AudioSettings,
    val actualCodec: LdacCodecManager.CurrentCodecInfo?,
    val codecReadError: String?,
    val apiCallResult: String,
    val isAssociated: Boolean,
    val deviceName: String,
    val isActiveDevice: Boolean = false
)

sealed class ApplyStatus {
    data object Idle : ApplyStatus()
    data object Applying : ApplyStatus()
    data class Success(val message: String) : ApplyStatus()
    data class Error(val message: String) : ApplyStatus()
    data class Failed(val diagnostic: DiagnosticInfo) : ApplyStatus()
}

class AudioSettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val codecManager: LdacCodecManager = LdacCodecManager()
) : ViewModel() {

    val audioSettings: StateFlow<AudioSettings> = settingsRepository.audioSettings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AudioSettings())

    private val _applyStatus = MutableStateFlow<ApplyStatus>(ApplyStatus.Idle)
    val applyStatus: StateFlow<ApplyStatus> = _applyStatus.asStateFlow()

    private val _codecReadResult = MutableStateFlow(LdacCodecManager.CodecReadResult())

    private val _currentCodecInfo = MutableStateFlow<LdacCodecManager.CurrentCodecInfo?>(null)
    val currentCodecInfo: StateFlow<LdacCodecManager.CurrentCodecInfo?> =
        _currentCodecInfo.asStateFlow()

    fun selectBitRate(bitRate: String) {
        viewModelScope.launch { settingsRepository.saveBitRate(bitRate) }
    }

    fun selectBitDepth(bitDepth: String) {
        viewModelScope.launch { settingsRepository.saveBitDepth(bitDepth) }
    }

    fun selectSamplingRate(samplingRate: String) {
        viewModelScope.launch { settingsRepository.saveSamplingRate(samplingRate) }
    }

    fun applySettings(bluetoothViewModel: BluetoothViewModel) {
        val a2dp = bluetoothViewModel.getA2dpProxy()
        val device = bluetoothViewModel.getConnectedDevice()

        if (a2dp == null || device == null) {
            _applyStatus.value = ApplyStatus.Error("No Bluetooth device connected")
            return
        }

        val desired = audioSettings.value
        val isAssociated = bluetoothViewModel.isAssociated.value
        val deviceName = bluetoothViewModel.deviceState.value.name
        val isActiveDevice = bluetoothViewModel.isActiveDevice()

        _applyStatus.value = ApplyStatus.Applying
        viewModelScope.launch {
            val result = codecManager.applySettings(a2dp, device, desired)
            val apiCallResult = when (result) {
                is LdacCodecManager.Result.Success -> "OK (no exception)"
                is LdacCodecManager.Result.Error -> "Error: ${result.message}"
                is LdacCodecManager.Result.PermissionRequired -> "SecurityException: ${result.message}"
            }

            readCurrentCodec(bluetoothViewModel)
            val readResult = _codecReadResult.value
            val actualCodec = readResult.info

            val settingsMatch = actualCodec != null && actualCodec.isLdac &&
                actualCodec.bitRate == desired.bitRate &&
                actualCodec.sampleRate == desired.samplingRate &&
                actualCodec.bitsPerSample == desired.bitDepth

            if (result is LdacCodecManager.Result.Success && settingsMatch) {
                _applyStatus.value = ApplyStatus.Success("LDAC settings applied")
            } else {
                _applyStatus.value = ApplyStatus.Failed(
                    DiagnosticInfo(
                        requested = desired,
                        actualCodec = actualCodec,
                        codecReadError = readResult.error,
                        apiCallResult = apiCallResult,
                        isAssociated = isAssociated,
                        deviceName = deviceName,
                        isActiveDevice = isActiveDevice
                    )
                )
            }
        }
    }

    fun readCurrentCodec(bluetoothViewModel: BluetoothViewModel) {
        val a2dp = bluetoothViewModel.getA2dpProxy()
        val device = bluetoothViewModel.getConnectedDevice()
        if (a2dp != null && device != null) {
            val result = codecManager.readCurrentCodec(a2dp, device)
            _codecReadResult.value = result
            _currentCodecInfo.value = result.info
        } else {
            _codecReadResult.value = LdacCodecManager.CodecReadResult(error = "No A2DP proxy or device")
            _currentCodecInfo.value = null
        }
    }

    fun clearApplyStatus() {
        _applyStatus.value = ApplyStatus.Idle
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AudioSettingsViewModel::class.java)) {
                return AudioSettingsViewModel(
                    settingsRepository = SettingsRepository(context.applicationContext)
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}

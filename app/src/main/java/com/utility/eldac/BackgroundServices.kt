package com.utility.eldac

import android.bluetooth.BluetoothA2dp
import android.bluetooth.BluetoothDevice
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class BluetoothViewModel(context: Context) : ViewModel(),
    BluetoothHandler.BluetoothEventListener {

    private val bluetoothHandler = BluetoothHandler(context.applicationContext)

    private val _deviceState = MutableStateFlow(DeviceState())
    val deviceState: StateFlow<DeviceState> = _deviceState.asStateFlow()

    private val _isBluetoothReady = MutableStateFlow(false)
    val isBluetoothReady: StateFlow<Boolean> = _isBluetoothReady.asStateFlow()

    init {
        bluetoothHandler.setListener(this)
    }

    fun initialize() {
        if (bluetoothHandler.hasBluetoothPermission()) {
            bluetoothHandler.initialize()
        }
    }

    fun hasPermission(): Boolean = bluetoothHandler.hasBluetoothPermission()

    override fun onDeviceConnected(device: BluetoothDevice) {
        val deviceName = try {
            device.name ?: "Unknown Device"
        } catch (e: SecurityException) {
            "Unknown Device"
        }

        _deviceState.value = DeviceState(
            name = deviceName,
            batteryLevel = 0,
            signalStrength = "Good",
            isConnected = true
        )
    }

    override fun onDeviceDisconnected() {
        _deviceState.value = DeviceState()
    }

    override fun onA2dpReady(proxy: BluetoothA2dp) {
        _isBluetoothReady.value = true
    }

    override fun onCleared() {
        super.onCleared()
        bluetoothHandler.release()
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(BluetoothViewModel::class.java)) {
                return BluetoothViewModel(context) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}

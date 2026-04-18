package com.utility.eldac

import android.bluetooth.BluetoothA2dp
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.IntentSender
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

    private val _isAssociated = MutableStateFlow(false)
    val isAssociated: StateFlow<Boolean> = _isAssociated.asStateFlow()

    private var connectedDevice: BluetoothDevice? = null
    private var onDeviceConnectedCallback: (() -> Unit)? = null

    init {
        bluetoothHandler.setListener(this)
    }

    fun initialize() {
        if (bluetoothHandler.hasBluetoothPermission()) {
            bluetoothHandler.initialize()
        }
    }

    fun hasPermission(): Boolean = bluetoothHandler.hasBluetoothPermission()

    fun getA2dpProxy(): BluetoothA2dp? = bluetoothHandler.getA2dpProxy()

    fun refreshAssociationState() {
        val device = connectedDevice
        _isAssociated.value = if (device != null) {
            bluetoothHandler.isDeviceAssociated(device)
        } else {
            false
        }
    }

    fun requestAssociation(
        onIntentSender: (IntentSender) -> Unit,
        onError: (String) -> Unit
    ) {
        bluetoothHandler.requestAssociation(onIntentSender, onError)
    }

    fun getConnectedDevice(): BluetoothDevice? = connectedDevice

    fun isActiveDevice(): Boolean {
        val active = bluetoothHandler.getActiveDevice()
        return active != null && connectedDevice != null &&
            active.address == connectedDevice?.address
    }

    fun setOnDeviceConnectedCallback(callback: () -> Unit) {
        onDeviceConnectedCallback = callback
    }

    override fun onDeviceConnected(device: BluetoothDevice) {
        connectedDevice = device
        val deviceName = try {
            device.name ?: "Unknown Device"
        } catch (e: SecurityException) {
            "Unknown Device"
        }

        val batteryLevel = bluetoothHandler.getBatteryLevel(device)

        _deviceState.value = DeviceState(
            name = deviceName,
            batteryLevel = if (batteryLevel >= 0) batteryLevel else 0,
            signalStrength = "Good",
            isConnected = true
        )
        refreshAssociationState()
        onDeviceConnectedCallback?.invoke()
    }

    override fun onDeviceDisconnected() {
        connectedDevice = null
        _isAssociated.value = false
        _deviceState.value = DeviceState()
        onDeviceConnectedCallback?.invoke()
    }

    override fun onBatteryLevelChanged(level: Int) {
        _deviceState.value = _deviceState.value.copy(batteryLevel = level)
    }

    override fun onA2dpReady(proxy: BluetoothA2dp) {
        _isBluetoothReady.value = true
        val device = bluetoothHandler.getConnectedA2dpDevice()
        if (device != null) {
            onDeviceConnected(device)
        }
    }

    override fun onCleared() {
        super.onCleared()
        onDeviceConnectedCallback = null
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

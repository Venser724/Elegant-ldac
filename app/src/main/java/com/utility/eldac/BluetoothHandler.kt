package com.utility.eldac

import android.Manifest
import android.bluetooth.BluetoothA2dp
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.companion.BluetoothDeviceFilter
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.companion.AssociationRequest
import android.companion.CompanionDeviceManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

class BluetoothHandler(private val context: Context) {

    companion object {
        private const val ACTION_BATTERY_LEVEL_CHANGED =
            "android.bluetooth.device.action.BATTERY_LEVEL_CHANGED"
        private const val EXTRA_BATTERY_LEVEL = "android.bluetooth.device.extra.BATTERY_LEVEL"
    }

    private var bluetoothAdapter: BluetoothAdapter? = null
    private var a2dpProxy: BluetoothA2dp? = null
    private var listener: BluetoothEventListener? = null

    interface BluetoothEventListener {
        fun onDeviceConnected(device: BluetoothDevice)
        fun onDeviceDisconnected()
        fun onA2dpReady(proxy: BluetoothA2dp)
        fun onBatteryLevelChanged(level: Int)
    }

    private val a2dpServiceListener = object : BluetoothProfile.ServiceListener {
        override fun onServiceConnected(profile: Int, proxy: BluetoothProfile) {
            if (profile == BluetoothProfile.A2DP) {
                a2dpProxy = proxy as BluetoothA2dp
                listener?.onA2dpReady(proxy)
                updateConnectedDevice()
            }
        }

        override fun onServiceDisconnected(profile: Int) {
            if (profile == BluetoothProfile.A2DP) {
                a2dpProxy = null
            }
        }
    }

    private val bluetoothReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BluetoothDevice.ACTION_ACL_CONNECTED -> {
                    val device = intent.getParcelableExtra<BluetoothDevice>(
                        BluetoothDevice.EXTRA_DEVICE
                    )
                    if (device != null) {
                        listener?.onDeviceConnected(device)
                    }
                }
                BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                    listener?.onDeviceDisconnected()
                }
                ACTION_BATTERY_LEVEL_CHANGED -> {
                    val level = intent.getIntExtra(EXTRA_BATTERY_LEVEL, -1)
                    if (level in 0..100) {
                        listener?.onBatteryLevelChanged(level)
                    }
                }
            }
        }
    }

    fun initialize() {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE)
                as? BluetoothManager
        bluetoothAdapter = bluetoothManager?.adapter

        bluetoothAdapter?.getProfileProxy(
            context,
            a2dpServiceListener,
            BluetoothProfile.A2DP
        )

        val filter = IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
            addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
            addAction(ACTION_BATTERY_LEVEL_CHANGED)
        }
        context.registerReceiver(bluetoothReceiver, filter)
    }

    fun setListener(eventListener: BluetoothEventListener) {
        listener = eventListener
    }

    fun hasBluetoothPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun getConnectedA2dpDevice(): BluetoothDevice? {
        if (!hasBluetoothPermission()) return null

        return try {
            a2dpProxy?.connectedDevices?.firstOrNull()
        } catch (e: SecurityException) {
            null
        }
    }

    fun getA2dpProxy(): BluetoothA2dp? = a2dpProxy

    fun isDeviceAssociated(device: BluetoothDevice): Boolean {
        val cdm = context.getSystemService(Context.COMPANION_DEVICE_SERVICE)
                as? CompanionDeviceManager ?: return false
        return try {
            val address = device.address.uppercase()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                cdm.myAssociations.any {
                    it.deviceMacAddress?.toString()?.uppercase() == address
                }
            } else {
                @Suppress("DEPRECATION")
                cdm.associations.any { it.uppercase() == address }
            }
        } catch (e: Exception) {
            false
        }
    }

    fun requestAssociation(callback: (IntentSender) -> Unit, onError: (String) -> Unit) {
        val cdm = context.getSystemService(Context.COMPANION_DEVICE_SERVICE)
                as? CompanionDeviceManager
        if (cdm == null) {
            onError("CompanionDeviceManager not available")
            return
        }

        val filter = BluetoothDeviceFilter.Builder().build()
        val request = AssociationRequest.Builder()
            .addDeviceFilter(filter)
            .setSingleDevice(false)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            cdm.associate(
                request,
                { it.run() },
                object : CompanionDeviceManager.Callback() {
                    override fun onAssociationPending(intentSender: IntentSender) {
                        callback(intentSender)
                    }

                    override fun onFailure(errorMessage: CharSequence?) {
                        onError(errorMessage?.toString() ?: "Association failed")
                    }
                }
            )
        } else {
            @Suppress("DEPRECATION")
            cdm.associate(
                request,
                object : CompanionDeviceManager.Callback() {
                    @Deprecated("Deprecated in API 33")
                    override fun onDeviceFound(intentSender: IntentSender) {
                        callback(intentSender)
                    }

                    override fun onFailure(errorMessage: CharSequence?) {
                        onError(errorMessage?.toString() ?: "Association failed")
                    }
                },
                null
            )
        }
    }

    fun getBatteryLevel(device: BluetoothDevice): Int {
        return try {
            val method = BluetoothDevice::class.java.getMethod("getBatteryLevel")
            val level = method.invoke(device) as Int
            if (level in 0..100) level else -1
        } catch (e: Exception) {
            -1
        }
    }

    private fun updateConnectedDevice() {
        val device = getConnectedA2dpDevice()
        if (device != null) {
            listener?.onDeviceConnected(device)
        }
    }

    fun release() {
        try {
            context.unregisterReceiver(bluetoothReceiver)
        } catch (e: IllegalArgumentException) {
            // Receiver was not registered
        }
        a2dpProxy?.let {
            bluetoothAdapter?.closeProfileProxy(BluetoothProfile.A2DP, it)
        }
        a2dpProxy = null
        listener = null
    }
}

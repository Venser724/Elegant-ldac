package com.utility.eldac

import android.bluetooth.BluetoothA2dp
import android.bluetooth.BluetoothCodecConfig
import android.bluetooth.BluetoothCodecStatus
import android.bluetooth.BluetoothDevice
import android.os.Build

class LdacCodecManager {

    companion object {
        const val CODEC_TYPE_LDAC = 4

        const val LDAC_QUALITY_HIGH = 1000L
        const val LDAC_QUALITY_MID = 1001L
        const val LDAC_QUALITY_LOW = 1002L
        const val LDAC_QUALITY_ABR = 1003L
    }

    sealed class Result {
        data class Success(val message: String) : Result()
        data class Error(val message: String) : Result()
        data class PermissionRequired(val message: String) : Result()
    }

    data class CurrentCodecInfo(
        val codecName: String,
        val sampleRate: String,
        val bitsPerSample: String,
        val bitRate: String,
        val isLdac: Boolean
    )

    fun readCurrentCodec(a2dp: BluetoothA2dp, device: BluetoothDevice): CurrentCodecInfo? {
        return try {
            val status = getCodecStatus(a2dp, device) ?: return null
            val config = status.codecConfig ?: return null
            parseCodecConfig(config)
        } catch (e: SecurityException) {
            null
        } catch (e: Exception) {
            null
        }
    }

    fun applySettings(
        a2dp: BluetoothA2dp,
        device: BluetoothDevice,
        settings: AudioSettings
    ): Result {
        return try {
            val config = buildCodecConfig(settings)
            setCodecConfigPreference(a2dp, device, config)
            Result.Success("LDAC settings applied successfully")
        } catch (e: SecurityException) {
            Result.PermissionRequired(
                "Changing codec settings requires elevated privileges. " +
                        "Enable Developer Options > Bluetooth Audio Codec, or use a rooted device."
            )
        } catch (e: Exception) {
            Result.Error("Failed to apply settings: ${e.message}")
        }
    }

    private fun getCodecStatus(
        a2dp: BluetoothA2dp,
        device: BluetoothDevice
    ): BluetoothCodecStatus? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            a2dp.getCodecStatus(device)
        } else {
            try {
                val method = BluetoothA2dp::class.java.getMethod(
                    "getCodecStatus",
                    BluetoothDevice::class.java
                )
                method.invoke(a2dp, device) as? BluetoothCodecStatus
            } catch (e: Exception) {
                null
            }
        }
    }

    private fun setCodecConfigPreference(
        a2dp: BluetoothA2dp,
        device: BluetoothDevice,
        config: BluetoothCodecConfig
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            a2dp.setCodecConfigPreference(device, config)
        } else {
            val method = BluetoothA2dp::class.java.getMethod(
                "setCodecConfigPreference",
                BluetoothDevice::class.java,
                BluetoothCodecConfig::class.java
            )
            method.invoke(a2dp, device, config)
        }
    }

    private fun buildCodecConfig(settings: AudioSettings): BluetoothCodecConfig {
        val sampleRate = mapSampleRate(settings.samplingRate)
        val bitsPerSample = mapBitsPerSample(settings.bitDepth)
        val codecSpecific1 = mapBitRate(settings.bitRate)

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            BluetoothCodecConfig.Builder()
                .setCodecType(CODEC_TYPE_LDAC)
                .setCodecPriority(BluetoothCodecConfig.CODEC_PRIORITY_HIGHEST)
                .setSampleRate(sampleRate)
                .setBitsPerSample(bitsPerSample)
                .setChannelMode(BluetoothCodecConfig.CHANNEL_MODE_STEREO)
                .setCodecSpecific1(codecSpecific1)
                .build()
        } else {
            @Suppress("DEPRECATION")
            BluetoothCodecConfig(
                CODEC_TYPE_LDAC,
                BluetoothCodecConfig.CODEC_PRIORITY_HIGHEST,
                sampleRate,
                bitsPerSample,
                BluetoothCodecConfig.CHANNEL_MODE_STEREO,
                codecSpecific1,
                0L,
                0L,
                0L
            )
        }
    }

    private fun parseCodecConfig(config: BluetoothCodecConfig): CurrentCodecInfo {
        val codecName = when (config.codecType) {
            0 -> "SBC"
            1 -> "AAC"
            2 -> "aptX"
            3 -> "aptX HD"
            CODEC_TYPE_LDAC -> "LDAC"
            5 -> "LC3"
            6 -> "Opus"
            else -> "Unknown (${config.codecType})"
        }

        val sampleRate = when (config.sampleRate) {
            BluetoothCodecConfig.SAMPLE_RATE_44100 -> "44.1 kHz"
            BluetoothCodecConfig.SAMPLE_RATE_48000 -> "48 kHz"
            BluetoothCodecConfig.SAMPLE_RATE_88200 -> "88.2 kHz"
            BluetoothCodecConfig.SAMPLE_RATE_96000 -> "96 kHz"
            BluetoothCodecConfig.SAMPLE_RATE_176400 -> "176.4 kHz"
            BluetoothCodecConfig.SAMPLE_RATE_192000 -> "192 kHz"
            else -> "Unknown"
        }

        val bitsPerSample = when (config.bitsPerSample) {
            BluetoothCodecConfig.BITS_PER_SAMPLE_16 -> "16 bit"
            BluetoothCodecConfig.BITS_PER_SAMPLE_24 -> "24 bit"
            BluetoothCodecConfig.BITS_PER_SAMPLE_32 -> "32 bit"
            else -> "Unknown"
        }

        val bitRate = when (config.codecSpecific1) {
            LDAC_QUALITY_HIGH -> "990 kbps"
            LDAC_QUALITY_MID -> "660 kbps"
            LDAC_QUALITY_LOW -> "330 kbps"
            LDAC_QUALITY_ABR -> "Adaptive"
            else -> if (config.codecType == CODEC_TYPE_LDAC) "Adaptive" else "N/A"
        }

        return CurrentCodecInfo(
            codecName = codecName,
            sampleRate = sampleRate,
            bitsPerSample = bitsPerSample,
            bitRate = bitRate,
            isLdac = config.codecType == CODEC_TYPE_LDAC
        )
    }

    fun mapSampleRate(displayValue: String): Int = when (displayValue) {
        "44.1 kHz" -> BluetoothCodecConfig.SAMPLE_RATE_44100
        "48 kHz" -> BluetoothCodecConfig.SAMPLE_RATE_48000
        "88.2 kHz" -> BluetoothCodecConfig.SAMPLE_RATE_88200
        "96 kHz" -> BluetoothCodecConfig.SAMPLE_RATE_96000
        else -> BluetoothCodecConfig.SAMPLE_RATE_96000
    }

    fun mapBitsPerSample(displayValue: String): Int = when (displayValue) {
        "16 bit" -> BluetoothCodecConfig.BITS_PER_SAMPLE_16
        "24 bit" -> BluetoothCodecConfig.BITS_PER_SAMPLE_24
        "32 bit" -> BluetoothCodecConfig.BITS_PER_SAMPLE_32
        else -> BluetoothCodecConfig.BITS_PER_SAMPLE_24
    }

    fun mapBitRate(displayValue: String): Long = when (displayValue) {
        "330 kbps" -> LDAC_QUALITY_LOW
        "660 kbps" -> LDAC_QUALITY_MID
        "990 kbps" -> LDAC_QUALITY_HIGH
        else -> LDAC_QUALITY_HIGH
    }
}

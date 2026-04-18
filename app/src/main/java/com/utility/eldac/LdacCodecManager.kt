package com.utility.eldac

import android.bluetooth.BluetoothA2dp
import android.bluetooth.BluetoothCodecConfig
import android.bluetooth.BluetoothCodecStatus
import android.bluetooth.BluetoothDevice

class LdacCodecManager {

    companion object {
        const val CODEC_TYPE_LDAC = 4

        const val LDAC_QUALITY_HIGH = 1000L
        const val LDAC_QUALITY_MID = 1001L
        const val LDAC_QUALITY_LOW = 1002L
        const val LDAC_QUALITY_ABR = 1003L

        // BluetoothCodecConfig constants (duplicated to avoid SDK visibility issues)
        const val SAMPLE_RATE_44100 = 0x01
        const val SAMPLE_RATE_48000 = 0x02
        const val SAMPLE_RATE_88200 = 0x04
        const val SAMPLE_RATE_96000 = 0x08
        const val SAMPLE_RATE_176400 = 0x10
        const val SAMPLE_RATE_192000 = 0x20

        const val BITS_PER_SAMPLE_16 = 0x01
        const val BITS_PER_SAMPLE_24 = 0x02
        const val BITS_PER_SAMPLE_32 = 0x04

        const val CHANNEL_MODE_STEREO = 0x02
        const val CODEC_PRIORITY_HIGHEST = 1_000_000
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

    data class CodecReadResult(
        val info: CurrentCodecInfo? = null,
        val error: String? = null
    )

    fun readCurrentCodec(a2dp: BluetoothA2dp, device: BluetoothDevice): CodecReadResult {
        return try {
            val statusResult = getCodecStatus(a2dp, device)
            if (statusResult.second != null) {
                return CodecReadResult(error = statusResult.second)
            }
            val status = statusResult.first
                ?: return CodecReadResult(error = "getCodecStatus returned null (no error)")
            val config = status.codecConfig
                ?: return CodecReadResult(error = "codecConfig is null inside CodecStatus")
            CodecReadResult(info = parseCodecConfig(config))
        } catch (e: Exception) {
            CodecReadResult(error = "${e.javaClass.simpleName}: ${e.message}")
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
                "Requires BLUETOOTH_PRIVILEGED. Use Developer Options or a rooted device."
            )
        } catch (e: java.lang.reflect.InvocationTargetException) {
            val cause = e.cause
            if (cause is SecurityException) {
                Result.PermissionRequired(
                    "Requires BLUETOOTH_PRIVILEGED. Use Developer Options or a rooted device."
                )
            } else {
                Result.Error("Failed to apply: ${cause?.message ?: e.message}")
            }
        } catch (e: Exception) {
            Result.Error("Failed to apply settings: ${e.message}")
        }
    }

    private fun getCodecStatus(
        a2dp: BluetoothA2dp,
        device: BluetoothDevice
    ): Pair<BluetoothCodecStatus?, String?> {
        return try {
            val method = BluetoothA2dp::class.java.getMethod(
                "getCodecStatus",
                BluetoothDevice::class.java
            )
            val result = method.invoke(a2dp, device)
            if (result == null) {
                Pair(null, null)
            } else {
                Pair(result as BluetoothCodecStatus, null)
            }
        } catch (e: java.lang.reflect.InvocationTargetException) {
            val cause = e.cause
            Pair(null, "InvocationTarget: ${cause?.javaClass?.simpleName}: ${cause?.message}")
        } catch (e: Exception) {
            Pair(null, "${e.javaClass.simpleName}: ${e.message}")
        }
    }

    private fun setCodecConfigPreference(
        a2dp: BluetoothA2dp,
        device: BluetoothDevice,
        config: BluetoothCodecConfig
    ) {
        val method = BluetoothA2dp::class.java.getMethod(
            "setCodecConfigPreference",
            BluetoothDevice::class.java,
            BluetoothCodecConfig::class.java
        )
        method.invoke(a2dp, device, config)
    }

    private fun buildCodecConfig(settings: AudioSettings): BluetoothCodecConfig {
        val sampleRate = mapSampleRate(settings.samplingRate)
        val bitsPerSample = mapBitsPerSample(settings.bitDepth)
        val codecSpecific1 = mapBitRate(settings.bitRate)

        return BluetoothCodecConfig.Builder()
            .setCodecType(CODEC_TYPE_LDAC)
            .setCodecPriority(CODEC_PRIORITY_HIGHEST)
            .setSampleRate(sampleRate)
            .setBitsPerSample(bitsPerSample)
            .setChannelMode(CHANNEL_MODE_STEREO)
            .setCodecSpecific1(codecSpecific1)
            .build()
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
            SAMPLE_RATE_44100 -> "44.1 kHz"
            SAMPLE_RATE_48000 -> "48 kHz"
            SAMPLE_RATE_88200 -> "88.2 kHz"
            SAMPLE_RATE_96000 -> "96 kHz"
            SAMPLE_RATE_176400 -> "176.4 kHz"
            SAMPLE_RATE_192000 -> "192 kHz"
            else -> "Unknown"
        }

        val bitsPerSample = when (config.bitsPerSample) {
            BITS_PER_SAMPLE_16 -> "16 bit"
            BITS_PER_SAMPLE_24 -> "24 bit"
            BITS_PER_SAMPLE_32 -> "32 bit"
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
        "44.1 kHz" -> SAMPLE_RATE_44100
        "48 kHz" -> SAMPLE_RATE_48000
        "88.2 kHz" -> SAMPLE_RATE_88200
        "96 kHz" -> SAMPLE_RATE_96000
        else -> SAMPLE_RATE_96000
    }

    fun mapBitsPerSample(displayValue: String): Int = when (displayValue) {
        "16 bit" -> BITS_PER_SAMPLE_16
        "24 bit" -> BITS_PER_SAMPLE_24
        "32 bit" -> BITS_PER_SAMPLE_32
        else -> BITS_PER_SAMPLE_24
    }

    fun mapBitRate(displayValue: String): Long = when (displayValue) {
        "330 kbps" -> LDAC_QUALITY_LOW
        "660 kbps" -> LDAC_QUALITY_MID
        "990 kbps" -> LDAC_QUALITY_HIGH
        else -> LDAC_QUALITY_HIGH
    }
}

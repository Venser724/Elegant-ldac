package com.utility.eldac

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class LdacCodecManagerTest {

    private lateinit var manager: LdacCodecManager

    @Before
    fun setUp() {
        manager = LdacCodecManager()
    }

    @Test
    fun mapBitRate_330kbps_returnsLowQuality() {
        assertEquals(LdacCodecManager.LDAC_QUALITY_LOW, manager.mapBitRate("330 kbps"))
    }

    @Test
    fun mapBitRate_660kbps_returnsMidQuality() {
        assertEquals(LdacCodecManager.LDAC_QUALITY_MID, manager.mapBitRate("660 kbps"))
    }

    @Test
    fun mapBitRate_990kbps_returnsHighQuality() {
        assertEquals(LdacCodecManager.LDAC_QUALITY_HIGH, manager.mapBitRate("990 kbps"))
    }

    @Test
    fun mapBitRate_unknownValue_defaultsToHigh() {
        assertEquals(LdacCodecManager.LDAC_QUALITY_HIGH, manager.mapBitRate("unknown"))
    }

    @Test
    fun mapSampleRate_44100() {
        assertEquals(LdacCodecManager.SAMPLE_RATE_44100, manager.mapSampleRate("44.1 kHz"))
    }

    @Test
    fun mapSampleRate_48000() {
        assertEquals(LdacCodecManager.SAMPLE_RATE_48000, manager.mapSampleRate("48 kHz"))
    }

    @Test
    fun mapSampleRate_88200() {
        assertEquals(LdacCodecManager.SAMPLE_RATE_88200, manager.mapSampleRate("88.2 kHz"))
    }

    @Test
    fun mapSampleRate_96000() {
        assertEquals(LdacCodecManager.SAMPLE_RATE_96000, manager.mapSampleRate("96 kHz"))
    }

    @Test
    fun mapSampleRate_unknownValue_defaultsTo96000() {
        assertEquals(LdacCodecManager.SAMPLE_RATE_96000, manager.mapSampleRate("unknown"))
    }

    @Test
    fun mapBitsPerSample_16bit() {
        assertEquals(LdacCodecManager.BITS_PER_SAMPLE_16, manager.mapBitsPerSample("16 bit"))
    }

    @Test
    fun mapBitsPerSample_24bit() {
        assertEquals(LdacCodecManager.BITS_PER_SAMPLE_24, manager.mapBitsPerSample("24 bit"))
    }

    @Test
    fun mapBitsPerSample_32bit() {
        assertEquals(LdacCodecManager.BITS_PER_SAMPLE_32, manager.mapBitsPerSample("32 bit"))
    }

    @Test
    fun mapBitsPerSample_unknownValue_defaultsTo24bit() {
        assertEquals(LdacCodecManager.BITS_PER_SAMPLE_24, manager.mapBitsPerSample("unknown"))
    }

    @Test
    fun ldacQualityConstants_haveExpectedValues() {
        assertEquals(1000L, LdacCodecManager.LDAC_QUALITY_HIGH)
        assertEquals(1001L, LdacCodecManager.LDAC_QUALITY_MID)
        assertEquals(1002L, LdacCodecManager.LDAC_QUALITY_LOW)
        assertEquals(1003L, LdacCodecManager.LDAC_QUALITY_ABR)
    }

    @Test
    fun codecTypeLdac_isFour() {
        assertEquals(4, LdacCodecManager.CODEC_TYPE_LDAC)
    }

    @Test
    fun sampleRateConstants_matchBluetoothSpec() {
        assertEquals(0x01, LdacCodecManager.SAMPLE_RATE_44100)
        assertEquals(0x02, LdacCodecManager.SAMPLE_RATE_48000)
        assertEquals(0x04, LdacCodecManager.SAMPLE_RATE_88200)
        assertEquals(0x08, LdacCodecManager.SAMPLE_RATE_96000)
    }

    @Test
    fun bitsPerSampleConstants_matchBluetoothSpec() {
        assertEquals(0x01, LdacCodecManager.BITS_PER_SAMPLE_16)
        assertEquals(0x02, LdacCodecManager.BITS_PER_SAMPLE_24)
        assertEquals(0x04, LdacCodecManager.BITS_PER_SAMPLE_32)
    }
}

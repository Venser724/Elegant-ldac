package com.utility.eldac

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue



object BackgroundServices {
    var deviceName by mutableStateOf("Headphones Name")
    var deviceBatteryLvl by mutableStateOf(85)
    var signalLvl by mutableStateOf("Good")

}
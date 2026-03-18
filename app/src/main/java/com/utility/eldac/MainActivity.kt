package com.utility.eldac

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import com.utility.eldac.ui.theme.Blue10
import com.utility.eldac.ui.theme.Blue30
import com.utility.eldac.ui.theme.Blue60
import com.utility.eldac.ui.theme.Blue80
import com.utility.eldac.ui.theme.Black
import com.utility.eldac.ui.theme.ElegantLDACTheme

class MainActivity : ComponentActivity() {

    private lateinit var bluetoothViewModel: BluetoothViewModel
    private lateinit var audioViewModel: AudioSettingsViewModel

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            bluetoothViewModel.initialize()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bluetoothViewModel = ViewModelProvider(
            this,
            BluetoothViewModel.Factory(applicationContext)
        )[BluetoothViewModel::class.java]

        audioViewModel = ViewModelProvider(
            this,
            AudioSettingsViewModel.Factory(applicationContext)
        )[AudioSettingsViewModel::class.java]

        bluetoothViewModel.setOnDeviceConnectedCallback {
            audioViewModel.readCurrentCodec(bluetoothViewModel)
        }

        requestBluetoothPermission()

        setContent {
            ElegantLDACTheme {
                EldacApp(
                    audioViewModel = audioViewModel,
                    bluetoothViewModel = bluetoothViewModel
                )
            }
        }
    }

    private fun requestBluetoothPermission() {
        if (bluetoothViewModel.hasPermission()) {
            bluetoothViewModel.initialize()
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EldacApp(
    audioViewModel: AudioSettingsViewModel,
    bluetoothViewModel: BluetoothViewModel
) {
    val audioSettings by audioViewModel.audioSettings.collectAsState()
    val deviceState by bluetoothViewModel.deviceState.collectAsState()
    val applyStatus by audioViewModel.applyStatus.collectAsState()
    val codecInfo by audioViewModel.currentCodecInfo.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(applyStatus) {
        val message = when (val status = applyStatus) {
            is ApplyStatus.Success -> status.message
            is ApplyStatus.Error -> status.message
            is ApplyStatus.PermissionRequired -> status.message
            else -> null
        }
        if (message != null) {
            snackbarHostState.showSnackbar(message)
            audioViewModel.clearApplyStatus()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("LDAC Settings") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Blue80,
                    titleContentColor = Color.White
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Blue10)
                    .verticalScroll(rememberScrollState())
            ) {
                DeviceHeaderCard(
                    deviceState = deviceState,
                    codecInfo = codecInfo
                )
                AudioParametersCard(
                    audioSettings = audioSettings,
                    onBitRateSelected = audioViewModel::selectBitRate,
                    onBitDepthSelected = audioViewModel::selectBitDepth,
                    onSamplingRateSelected = audioViewModel::selectSamplingRate
                )
                ApplySettingsButton(
                    isConnected = deviceState.isConnected,
                    isApplying = applyStatus is ApplyStatus.Applying,
                    onApply = { audioViewModel.applySettings(bluetoothViewModel) }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    )
}

@Composable
fun DeviceHeaderCard(
    deviceState: DeviceState,
    codecInfo: LdacCodecManager.CurrentCodecInfo?
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(all = 12.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Blue60),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_bt),
                    contentDescription = "Bluetooth Icon",
                    tint = Color.White,
                    modifier = Modifier
                        .size(36.dp)
                        .background(Blue80, CircleShape)
                        .padding(8.dp)
                )
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        if (deviceState.isConnected) "Connected Device" else "No Device",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 14.sp
                    )
                    Text(
                        text = deviceState.name,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
                StatusBadge(isConnected = deviceState.isConnected)
            }
            HorizontalDivider(
                color = Color.White.copy(alpha = 0.2f),
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 16.dp)
            )
            Row {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Signal Strength",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 14.sp
                    )
                    Text(
                        text = deviceState.signalStrength,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Battery",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 14.sp
                    )
                    Text(
                        text = if (deviceState.isConnected) "${deviceState.batteryLevel} %" else "N/A",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            if (codecInfo != null) {
                HorizontalDivider(
                    color = Color.White.copy(alpha = 0.2f),
                    thickness = 1.dp,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
                Text(
                    "Active Codec",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Codec",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 12.sp
                        )
                        Text(
                            codecInfo.codecName,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Bit Rate",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 12.sp
                        )
                        Text(
                            codecInfo.bitRate,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Sample Rate",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 12.sp
                        )
                        Text(
                            codecInfo.sampleRate,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Bit Depth",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 12.sp
                        )
                        Text(
                            codecInfo.bitsPerSample,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatusBadge(isConnected: Boolean) {
    Button(
        onClick = { },
        enabled = false,
        shape = RoundedCornerShape(50),
        colors = ButtonDefaults.buttonColors(
            disabledContainerColor = if (isConnected) {
                Color.White.copy(alpha = 0.2f)
            } else {
                Color.Red.copy(alpha = 0.3f)
            },
            disabledContentColor = Color.White
        ),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Text(if (isConnected) "Connected" else "Disconnected")
    }
}

@Composable
fun AudioParametersCard(
    audioSettings: AudioSettings,
    onBitRateSelected: (String) -> Unit,
    onBitDepthSelected: (String) -> Unit,
    onSamplingRateSelected: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(all = 12.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_settings),
                contentDescription = "Audio Icon",
                tint = Blue60,
                modifier = Modifier
                    .size(36.dp)
                    .padding(8.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Audio Parameters",
                    color = Color.Black,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(start = 8.dp),
                )
                Text(
                    "Configure LDAC audio settings",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
        Column(
            modifier = Modifier.padding(all = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            LdacSelectionGroup(
                label = "Bit Rate",
                options = listOf("330 kbps", "660 kbps", "990 kbps"),
                selectedOption = audioSettings.bitRate,
                onOptionSelected = onBitRateSelected
            )
        }
        HorizontalDivider(
            modifier = Modifier.padding(vertical = 12.dp),
            thickness = 1.dp,
            color = Color.Gray.copy(alpha = 0.3f)
        )
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            LdacSelectionGroup(
                label = "Bit Depth",
                options = listOf("16 bit", "24 bit", "32 bit"),
                selectedOption = audioSettings.bitDepth,
                onOptionSelected = onBitDepthSelected
            )
        }
        HorizontalDivider(
            modifier = Modifier.padding(vertical = 12.dp),
            thickness = 1.dp,
            color = Color.Gray.copy(alpha = 0.3f)
        )
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            LdacSelectionGroup(
                label = "Sampling Rate",
                options = listOf("44.1 kHz", "48 kHz", "88.2 kHz", "96 kHz"),
                selectedOption = audioSettings.samplingRate,
                onOptionSelected = onSamplingRateSelected
            )
        }
    }
}

@Composable
fun ApplySettingsButton(
    isConnected: Boolean,
    isApplying: Boolean,
    onApply: () -> Unit
) {
    Button(
        onClick = onApply,
        enabled = isConnected && !isApplying,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Blue80,
            contentColor = Color.White,
            disabledContainerColor = Blue80.copy(alpha = 0.4f),
            disabledContentColor = Color.White.copy(alpha = 0.5f)
        ),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        Text(
            text = when {
                isApplying -> "Applying..."
                !isConnected -> "Connect a device to apply"
                else -> "Apply LDAC Settings"
            },
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
    }
}

@Composable
fun LdacSelectionGroup(
    label: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    Column {
        Text(label, style = MaterialTheme.typography.labelMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            options.forEach { option ->
                val isSelected = option == selectedOption
                Button(
                    onClick = { onOptionSelected(option) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) Blue60 else Blue30,
                        contentColor = if (isSelected) Color.White else Black
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(option)
                }
            }
        }
    }
}

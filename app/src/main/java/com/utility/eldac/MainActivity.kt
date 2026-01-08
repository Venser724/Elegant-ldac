package com.utility.eldac

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.utility.eldac.ui.theme.Black
import com.utility.eldac.ui.theme.Blue10
import com.utility.eldac.ui.theme.Blue30
import com.utility.eldac.ui.theme.Blue60
import com.utility.eldac.ui.theme.Blue80


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ScaffoldBG()
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun ScaffoldBG() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("LDAC Settings") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Blue80,
                    titleContentColor = Color.White // Recommended for contrast

                )
            )
        },
        // The content lambda for the official Scaffold provides padding values
        content = { paddingValues ->
            // Apply the padding to your content's root Column
            Column(
                modifier = Modifier
                    .fillMaxSize() // Fill the available space
                    .padding(paddingValues) // Use the padding from the Scaffold
                    .background(Blue10) // I see AppBg in your theme imports
            ) {
                DeviceHeaderCard()
                AudioParametersCard()
                //PresetsCard()
            }
        }
    )
}

@Composable
fun DeviceHeaderCard() {
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
                        "Connected Device",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 14.sp
                    )
                    Text(
                        "Sony WH-1000XM5",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
                Button(
                    onClick = { /*TODO*/ },
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.2f)),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Text("Connected", color = Color.White)
                }
            }
            HorizontalDivider(
                color = Color.White.copy(alpha = 0.2f),
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 16.dp)
            )
            Row {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Signal Strength",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 14.sp
                    )
                    Text("Excellent", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Battery", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
                    Text("85%", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}


@Composable
fun AudioParametersCard() {



    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(all = 12.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)) {
            Icon(
                painter = painterResource(id = R.drawable.ic_settings),
                contentDescription = "Audio Icon",
                tint = Blue60,
                modifier = Modifier
                    .size(36.dp)
                    .padding(8.dp)
            )
            Column(modifier = Modifier.weight(1f),
                ) {
                Text(
                    "Audio Parameters",
                    color = Color.Black,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .padding(start = 8.dp),
                )
                Text(
                    "Configure LDAC audio settings",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .padding(start = 8.dp)

                )
            }
        }
        Column(
            modifier = Modifier.padding(all = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            LdacBitrateSelectionGroup(
                label = "Bit rate",
                options = listOf("330 kbps", "660 kbps", "990 kbps"),
                selectedOption = "",
                onOptionSelected = { /* Handle selection */ }
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
                selectedOption = "",
                onOptionSelected = { /* Handle selection */ })
        }
        HorizontalDivider(
            modifier = Modifier.padding(vertical = 16.dp),
            thickness = 1.dp,
            color = Color.Gray.copy(alpha = 0.3f)
        )
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            LdacSamplingRateSelectionGroup(
                label = "Sampling Rate",
                options = listOf("44.1 kHz", "48 kHz", "88.2 kHz", "96 kHz"),
                selectedOption = "",
                onOptionSelected = { /* Handle selection */ })
        }

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
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
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

@Composable
fun LdacBitrateSelectionGroup(
    label: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: () -> Unit
) {
    Column {
        Text(label, style = MaterialTheme.typography.labelMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            options.forEach { option ->
                val isSelected = option == selectedOption
                Button(
                    onClick = { onOptionSelected() },
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

@Composable
fun LdacSamplingRateSelectionGroup(
    label: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    Column {
        Text(label, style = MaterialTheme.typography.labelMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
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
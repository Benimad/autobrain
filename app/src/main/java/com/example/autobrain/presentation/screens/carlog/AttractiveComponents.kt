package com.example.autobrain.presentation.screens.carlog

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.StickyNote2
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.outlined.BatteryChargingFull
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.OilBarrel
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.TireRepair
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.autobrain.domain.model.MaintenanceType
import com.example.autobrain.presentation.theme.DeepNavy
import com.example.autobrain.presentation.theme.ElectricTeal
import com.example.autobrain.presentation.theme.TextMuted
import com.example.autobrain.presentation.theme.TextPrimary
import com.example.autobrain.presentation.theme.TextSecondary

@Composable
fun AttractiveTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: ImageVector,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    trailingIcon: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    readOnly: Boolean = false,
    minLines: Int = 1,
    suffix: @Composable (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontWeight = FontWeight.Medium) },
        placeholder = { if (placeholder.isNotEmpty()) Text(placeholder) else null },
        leadingIcon = {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = ElectricTeal,
                modifier = Modifier.size(20.dp)
            )
        },
        trailingIcon = trailingIcon,
        suffix = suffix,
        modifier = modifier.fillMaxWidth(),
        readOnly = readOnly,
        minLines = minLines,
        keyboardOptions = keyboardOptions,
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary,
            focusedBorderColor = ElectricTeal,
            unfocusedBorderColor = Color(0xFF30363D),
            focusedLabelColor = ElectricTeal,
            unfocusedLabelColor = TextSecondary,
            cursorColor = ElectricTeal,
            focusedContainerColor = DeepNavy.copy(alpha = 0.7f),
            unfocusedContainerColor = DeepNavy.copy(alpha = 0.4f),
            focusedPlaceholderColor = TextMuted,
            unfocusedPlaceholderColor = TextMuted,
            focusedLeadingIconColor = ElectricTeal,
            unfocusedLeadingIconColor = TextSecondary
        )
    )
}

fun getMaintenanceTypeIcon(type: MaintenanceType): ImageVector {
    return when (type) {
        MaintenanceType.OIL_CHANGE -> Icons.Outlined.OilBarrel
        MaintenanceType.TIRE_ROTATION -> Icons.Outlined.TireRepair
        MaintenanceType.BRAKE_SERVICE -> Icons.Outlined.Build
        MaintenanceType.ENGINE_TUNE_UP -> Icons.Outlined.Settings
        MaintenanceType.BATTERY_REPLACEMENT -> Icons.Outlined.BatteryChargingFull
        MaintenanceType.AIR_FILTER -> Icons.Outlined.Settings
        MaintenanceType.TRANSMISSION_SERVICE -> Icons.Outlined.Build
        MaintenanceType.COOLANT_FLUSH -> Icons.Outlined.WaterDrop
        MaintenanceType.GENERAL_INSPECTION -> Icons.Outlined.CheckCircle
        MaintenanceType.REPAIR -> Icons.Outlined.Build
        MaintenanceType.OTHER -> Icons.AutoMirrored.Filled.StickyNote2
    }
}

fun getMaintenanceTypeLabel(type: MaintenanceType): String {
    return when (type) {
        MaintenanceType.OIL_CHANGE -> "Oil Change"
        MaintenanceType.TIRE_ROTATION -> "Tire Rotation"
        MaintenanceType.BRAKE_SERVICE -> "Brake Service"
        MaintenanceType.ENGINE_TUNE_UP -> "Engine Tune-Up"
        MaintenanceType.BATTERY_REPLACEMENT -> "Battery Replacement"
        MaintenanceType.AIR_FILTER -> "Air Filter"
        MaintenanceType.TRANSMISSION_SERVICE -> "Transmission Service"
        MaintenanceType.COOLANT_FLUSH -> "Coolant Flush"
        MaintenanceType.GENERAL_INSPECTION -> "General Inspection"
        MaintenanceType.REPAIR -> "Repair"
        MaintenanceType.OTHER -> "Other"
    }
}

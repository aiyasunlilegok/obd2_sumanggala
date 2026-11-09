package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GaugeStyle
import com.example.model.SpeedUnit
import com.example.model.TempUnit
import com.example.model.VehicleTelemetry
import com.example.ui.components.ArcGauge
import com.example.ui.components.BatteryGauge
import com.example.ui.components.EngineTempGauge
import com.example.ui.components.ShiftLightBar
import com.example.ui.components.SpeedometerCard
import com.example.ui.theme.CockpitColorScheme
import com.example.ui.theme.HeadUnitThemeColors

@Composable
fun DashboardClusterView(
    telemetry: VehicleTelemetry,
    gaugeStyle: GaugeStyle,
    speedUnit: SpeedUnit,
    tempUnit: TempUnit,
    themeColor: Color,
    warningTempC: Float,
    criticalTempC: Float,
    redlineRpm: Int,
    minBatteryV: Float,
    maxSpeedLimitKmh: Float,
    colorScheme: CockpitColorScheme = HeadUnitThemeColors.DarkScheme,
    modifier: Modifier = Modifier
) {
    val isOverSpeed = telemetry.speedKmh > maxSpeedLimitKmh

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(colorScheme.screenBackground)
            .padding(8.dp)
            .testTag("dashboard_cluster_view")
    ) {
        val isWidescreenHeadUnit = maxWidth > maxHeight || maxWidth >= 680.dp

        if (isWidescreenHeadUnit && gaugeStyle == GaugeStyle.SPORTS_ARC) {
            // Panoramic 3-Wing layout optimized for 7", 9", 10", 12" Android Head Units
            WidescreenHeadUnitCluster(
                telemetry = telemetry,
                speedUnit = speedUnit,
                tempUnit = tempUnit,
                themeColor = themeColor,
                warningTempC = warningTempC,
                criticalTempC = criticalTempC,
                redlineRpm = redlineRpm,
                minBatteryV = minBatteryV,
                isOverSpeed = isOverSpeed,
                colorScheme = colorScheme
            )
        } else {
            when (gaugeStyle) {
                GaugeStyle.SPORTS_ARC -> {
                    SportsArcLayout(
                        telemetry = telemetry,
                        speedUnit = speedUnit,
                        tempUnit = tempUnit,
                        themeColor = themeColor,
                        warningTempC = warningTempC,
                        criticalTempC = criticalTempC,
                        redlineRpm = redlineRpm,
                        minBatteryV = minBatteryV,
                        isOverSpeed = isOverSpeed,
                        colorScheme = colorScheme
                    )
                }
                GaugeStyle.DUAL_DIAL -> {
                    DualDialLayout(
                        telemetry = telemetry,
                        speedUnit = speedUnit,
                        tempUnit = tempUnit,
                        themeColor = themeColor,
                        warningTempC = warningTempC,
                        criticalTempC = criticalTempC,
                        redlineRpm = redlineRpm,
                        minBatteryV = minBatteryV,
                        colorScheme = colorScheme
                    )
                }
                GaugeStyle.MINIMAL_HUD -> {
                    MinimalHudLayout(
                        telemetry = telemetry,
                        speedUnit = speedUnit,
                        tempUnit = tempUnit,
                        themeColor = themeColor,
                        warningTempC = warningTempC,
                        criticalTempC = criticalTempC,
                        redlineRpm = redlineRpm,
                        minBatteryV = minBatteryV,
                        colorScheme = colorScheme
                    )
                }
                GaugeStyle.MODULAR_GRID -> {
                    ModularGridLayout(
                        telemetry = telemetry,
                        speedUnit = speedUnit,
                        tempUnit = tempUnit,
                        themeColor = themeColor,
                        warningTempC = warningTempC,
                        criticalTempC = criticalTempC,
                        redlineRpm = redlineRpm,
                        minBatteryV = minBatteryV,
                        colorScheme = colorScheme
                    )
                }
            }
        }
    }
}

/**
 * Panoramic 3-Wing Layout specifically designed for 7" to 12" Landscape Android Head Units.
 * Left Wing: Engine Temp + Battery Gauges
 * Center: Speedometer HUD + Shift Lights + Gear
 * Right Wing: Tachometer (RPM Dial) + Throttle & Fuel stats
 */
@Composable
private fun WidescreenHeadUnitCluster(
    telemetry: VehicleTelemetry,
    speedUnit: SpeedUnit,
    tempUnit: TempUnit,
    themeColor: Color,
    warningTempC: Float,
    criticalTempC: Float,
    redlineRpm: Int,
    minBatteryV: Float,
    isOverSpeed: Boolean,
    colorScheme: CockpitColorScheme
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Shift light bar spans across the top of the head unit display
        ShiftLightBar(
            currentRpm = telemetry.rpm,
            maxRpm = 7500,
            redlineRpm = redlineRpm
        )

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Wing: Vitals (Engine Temp & Battery)
            Column(
                modifier = Modifier
                    .weight(0.95f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                EngineTempGauge(
                    tempC = telemetry.engineTempC,
                    tempUnit = tempUnit,
                    warningThresholdC = warningTempC,
                    criticalThresholdC = criticalTempC,
                    themeColor = themeColor,
                    cardBg = colorScheme.cardBackground,
                    textColor = colorScheme.textPrimary,
                    strokeColor = colorScheme.borderStroke
                )
                Spacer(modifier = Modifier.height(6.dp))
                BatteryGauge(
                    voltage = telemetry.batteryVoltage,
                    minThreshold = minBatteryV,
                    themeColor = themeColor,
                    cardBg = colorScheme.cardBackground,
                    textColor = colorScheme.textPrimary,
                    strokeColor = colorScheme.borderStroke
                )
            }

            // Center Stage: Speedometer HUD
            Box(
                modifier = Modifier
                    .weight(1.25f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                SpeedometerCard(
                    speedKmh = telemetry.speedKmh,
                    speedUnit = speedUnit,
                    currentGear = telemetry.currentGear,
                    maxSpeedKmh = telemetry.maxSpeedKmh,
                    tripDistanceKm = telemetry.tripDistanceKm,
                    themeColor = themeColor,
                    isSpeedWarning = isOverSpeed,
                    cardBg = colorScheme.cardBackground,
                    textColor = colorScheme.textPrimary,
                    strokeColor = colorScheme.borderStroke,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Right Wing: Tachometer Dial + Secondary Stats
            Column(
                modifier = Modifier
                    .weight(0.95f)
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                ArcGauge(
                    value = telemetry.rpm.toFloat(),
                    minValue = 0f,
                    maxValue = 8000f,
                    title = "RPM MESIN",
                    unit = "RPM",
                    gaugeColor = themeColor,
                    dangerThreshold = redlineRpm.toFloat(),
                    textColor = colorScheme.textPrimary,
                    size = 140.dp
                )

                // Secondary Stats Pill
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(colorScheme.cardBackground)
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "GAS: ${telemetry.throttlePct.toInt()}%",
                        style = MaterialTheme.typography.labelSmall.copy(color = colorScheme.textMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    )
                    Box(modifier = Modifier.width(1.dp).height(12.dp).background(colorScheme.borderStroke))
                    Text(
                        text = "BBM: ${telemetry.fuelLevelPct.toInt()}%",
                        style = MaterialTheme.typography.labelSmall.copy(color = themeColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}

/**
 * Standard Stacked Sports Arc Layout (for Portrait or narrow split).
 */
@Composable
private fun SportsArcLayout(
    telemetry: VehicleTelemetry,
    speedUnit: SpeedUnit,
    tempUnit: TempUnit,
    themeColor: Color,
    warningTempC: Float,
    criticalTempC: Float,
    redlineRpm: Int,
    minBatteryV: Float,
    isOverSpeed: Boolean,
    colorScheme: CockpitColorScheme
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ShiftLightBar(
            currentRpm = telemetry.rpm,
            maxRpm = 7500,
            redlineRpm = redlineRpm
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SpeedometerCard(
                speedKmh = telemetry.speedKmh,
                speedUnit = speedUnit,
                currentGear = telemetry.currentGear,
                maxSpeedKmh = telemetry.maxSpeedKmh,
                tripDistanceKm = telemetry.tripDistanceKm,
                themeColor = themeColor,
                isSpeedWarning = isOverSpeed,
                cardBg = colorScheme.cardBackground,
                textColor = colorScheme.textPrimary,
                strokeColor = colorScheme.borderStroke,
                modifier = Modifier.weight(1.15f)
            )

            ArcGauge(
                value = telemetry.rpm.toFloat(),
                minValue = 0f,
                maxValue = 8000f,
                title = "RPM",
                unit = "x1000",
                gaugeColor = themeColor,
                dangerThreshold = redlineRpm.toFloat(),
                textColor = colorScheme.textPrimary,
                size = 145.dp,
                modifier = Modifier.weight(0.85f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            EngineTempGauge(
                tempC = telemetry.engineTempC,
                tempUnit = tempUnit,
                warningThresholdC = warningTempC,
                criticalThresholdC = criticalTempC,
                themeColor = themeColor,
                cardBg = colorScheme.cardBackground,
                textColor = colorScheme.textPrimary,
                strokeColor = colorScheme.borderStroke,
                modifier = Modifier.weight(1f)
            )

            BatteryGauge(
                voltage = telemetry.batteryVoltage,
                minThreshold = minBatteryV,
                themeColor = themeColor,
                cardBg = colorScheme.cardBackground,
                textColor = colorScheme.textPrimary,
                strokeColor = colorScheme.borderStroke,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(colorScheme.cardBackground)
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "THROTTLE: ${telemetry.throttlePct.toInt()}%",
                style = MaterialTheme.typography.labelSmall.copy(color = colorScheme.textMuted, fontWeight = FontWeight.Bold)
            )
            Box(modifier = Modifier.width(1.dp).height(14.dp).background(colorScheme.borderStroke))
            Text(
                text = "INTAKE: ${telemetry.intakeTempC.toInt()}°C",
                style = MaterialTheme.typography.labelSmall.copy(color = colorScheme.textMuted, fontWeight = FontWeight.Bold)
            )
            Box(modifier = Modifier.width(1.dp).height(14.dp).background(colorScheme.borderStroke))
            Text(
                text = "FUEL: ${telemetry.fuelLevelPct.toInt()}%",
                style = MaterialTheme.typography.labelSmall.copy(color = themeColor, fontWeight = FontWeight.Bold)
            )
        }
    }
}

@Composable
private fun DualDialLayout(
    telemetry: VehicleTelemetry,
    speedUnit: SpeedUnit,
    tempUnit: TempUnit,
    themeColor: Color,
    warningTempC: Float,
    criticalTempC: Float,
    redlineRpm: Int,
    minBatteryV: Float,
    colorScheme: CockpitColorScheme
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ArcGauge(
                value = telemetry.speedKmh * speedUnit.factorToKmh,
                minValue = 0f,
                maxValue = 220f,
                title = "SPEED",
                unit = speedUnit.label,
                gaugeColor = themeColor,
                textColor = colorScheme.textPrimary,
                size = 150.dp
            )

            ArcGauge(
                value = telemetry.rpm.toFloat(),
                minValue = 0f,
                maxValue = 8000f,
                title = "TACHO",
                unit = "RPM",
                gaugeColor = themeColor,
                dangerThreshold = redlineRpm.toFloat(),
                textColor = colorScheme.textPrimary,
                size = 150.dp
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            EngineTempGauge(
                tempC = telemetry.engineTempC,
                tempUnit = tempUnit,
                warningThresholdC = warningTempC,
                criticalThresholdC = criticalTempC,
                themeColor = themeColor,
                cardBg = colorScheme.cardBackground,
                textColor = colorScheme.textPrimary,
                strokeColor = colorScheme.borderStroke,
                modifier = Modifier.weight(1f)
            )
            BatteryGauge(
                voltage = telemetry.batteryVoltage,
                minThreshold = minBatteryV,
                themeColor = themeColor,
                cardBg = colorScheme.cardBackground,
                textColor = colorScheme.textPrimary,
                strokeColor = colorScheme.borderStroke,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun MinimalHudLayout(
    telemetry: VehicleTelemetry,
    speedUnit: SpeedUnit,
    tempUnit: TempUnit,
    themeColor: Color,
    warningTempC: Float,
    criticalTempC: Float,
    redlineRpm: Int,
    minBatteryV: Float,
    colorScheme: CockpitColorScheme
) {
    val displaySpeed = telemetry.speedKmh * speedUnit.factorToKmh

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "GEAR ${telemetry.currentGear}",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Black,
                color = themeColor
            )
        )

        Text(
            text = "${displaySpeed.toInt()}",
            style = MaterialTheme.typography.displayLarge.copy(
                fontWeight = FontWeight.Black,
                color = colorScheme.textPrimary,
                fontSize = 96.sp,
                letterSpacing = (-4).sp
            )
        )
        Text(
            text = speedUnit.label.uppercase(),
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = themeColor
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Text(
                text = "RPM: ${telemetry.rpm}",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (telemetry.rpm >= redlineRpm) Color(0xFFFF1744) else colorScheme.textPrimary
                )
            )
            Text(
                text = "SUHU: ${telemetry.engineTempC.toInt()}°C",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (telemetry.engineTempC >= warningTempC) Color(0xFFFF1744) else colorScheme.textPrimary
                )
            )
            Text(
                text = "AKI: ${String.format("%.1f", telemetry.batteryVoltage)}V",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (telemetry.batteryVoltage < minBatteryV) Color(0xFFFF1744) else colorScheme.textPrimary
                )
            )
        }
    }
}

@Composable
private fun ModularGridLayout(
    telemetry: VehicleTelemetry,
    speedUnit: SpeedUnit,
    tempUnit: TempUnit,
    themeColor: Color,
    warningTempC: Float,
    criticalTempC: Float,
    redlineRpm: Int,
    minBatteryV: Float,
    colorScheme: CockpitColorScheme
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SpeedometerCard(
                speedKmh = telemetry.speedKmh,
                speedUnit = speedUnit,
                currentGear = telemetry.currentGear,
                maxSpeedKmh = telemetry.maxSpeedKmh,
                tripDistanceKm = telemetry.tripDistanceKm,
                themeColor = themeColor,
                isSpeedWarning = false,
                cardBg = colorScheme.cardBackground,
                textColor = colorScheme.textPrimary,
                strokeColor = colorScheme.borderStroke,
                modifier = Modifier.weight(1f)
            )
            ArcGauge(
                value = telemetry.rpm.toFloat(),
                minValue = 0f,
                maxValue = 8000f,
                title = "RPM MESIN",
                unit = "RPM",
                gaugeColor = themeColor,
                dangerThreshold = redlineRpm.toFloat(),
                textColor = colorScheme.textPrimary,
                size = 145.dp,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            EngineTempGauge(
                tempC = telemetry.engineTempC,
                tempUnit = tempUnit,
                warningThresholdC = warningTempC,
                criticalThresholdC = criticalTempC,
                themeColor = themeColor,
                cardBg = colorScheme.cardBackground,
                textColor = colorScheme.textPrimary,
                strokeColor = colorScheme.borderStroke,
                modifier = Modifier.weight(1f)
            )

            BatteryGauge(
                voltage = telemetry.batteryVoltage,
                minThreshold = minBatteryV,
                themeColor = themeColor,
                cardBg = colorScheme.cardBackground,
                textColor = colorScheme.textPrimary,
                strokeColor = colorScheme.borderStroke,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

package com.example.data.model

data class DeviceInfo(
    val manufacturer: String,
    val model: String,
    val hardware: String,
    val androidVersion: String,
    val apiLevel: Int,
    val resolution: String,
    val screenDpi: Int,
    val refreshRateHz: Int,
    val totalRamGb: Double,
    val availableRamGb: Double,
    val processorArch: String
) {
    val fullDeviceName: String
        get() = "${manufacturer.replaceFirstChar { it.uppercase() }} $model"
}

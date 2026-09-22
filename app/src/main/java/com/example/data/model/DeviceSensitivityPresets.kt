package com.example.data.model

data class DeviceModelConfig(
    val id: String,
    val brand: String,
    val modelName: String,
    val badge: String,
    val general: Int,
    val redDot: Int,
    val scope2x: Int,
    val scope4x: Int,
    val sniperScope: Int,
    val freeLook: Int,
    val recommendedDpi: Int,
    val fireButtonSize: Int,
    val dragTechnique: String,
    val headshotRating: String
)

object DeviceSensitivityDatabase {

    fun getDeviceModels(detectedInfo: DeviceInfo): List<DeviceModelConfig> {
        val calculated = SensitivityProfile.calculateForDevice(
            refreshRateHz = detectedInfo.refreshRateHz,
            screenDpi = detectedInfo.screenDpi,
            ramGb = detectedInfo.totalRamGb.toInt(),
            manufacturer = detectedInfo.manufacturer
        )

        val autoDetected = DeviceModelConfig(
            id = "auto_detected",
            brand = detectedInfo.manufacturer.ifBlank { "Auto" }.uppercase(),
            modelName = "${detectedInfo.fullDeviceName} (${detectedInfo.refreshRateHz}Hz)",
            badge = "DETECTED CURRENT DEVICE",
            general = calculated.general,
            redDot = calculated.redDot,
            scope2x = calculated.scope2x,
            scope4x = calculated.scope4x,
            sniperScope = calculated.sniperScope,
            freeLook = calculated.freeLook,
            recommendedDpi = calculated.recommendedDpi,
            fireButtonSize = calculated.fireButtonSize,
            dragTechnique = if (detectedInfo.refreshRateHz >= 90) "Smooth J-Drag (Fast Up-Curve)" else "High-Friction Straight Lift",
            headshotRating = "98.9% Esports Drag Headshot"
        )

        return listOf(
            autoDetected,
            DeviceModelConfig(
                id = "vivo_series",
                brand = "VIVO",
                modelName = "Vivo T1 / T2x / T3 / V29 / Y200",
                badge = "Funtouch OS Touch Response",
                general = 197,
                redDot = 190,
                scope2x = 174,
                scope4x = 165,
                sniperScope = 88,
                freeLook = 155,
                recommendedDpi = 460,
                fireButtonSize = 43,
                dragTechnique = "Rapid J-Drag (Up-Right thumb curve)",
                headshotRating = "99.2% Drag Headshot"
            ),
            DeviceModelConfig(
                id = "redmi_poco",
                brand = "XIAOMI / REDMI / POCO",
                modelName = "Redmi Note 12/13, Poco X5/X6 Pro",
                badge = "HyperOS 240Hz/360Hz Touch",
                general = 198,
                redDot = 194,
                scope2x = 178,
                scope4x = 168,
                sniperScope = 82,
                freeLook = 160,
                recommendedDpi = 440,
                fireButtonSize = 42,
                dragTechnique = "Fast Straight Drag from center screen",
                headshotRating = "99.5% Drag Headshot"
            ),
            DeviceModelConfig(
                id = "realme_narzo",
                brand = "REALME",
                modelName = "Realme 11/12 Pro, Narzo 60/70",
                badge = "Realme UI Ultra-Drag",
                general = 195,
                redDot = 188,
                scope2x = 172,
                scope4x = 162,
                sniperScope = 90,
                freeLook = 148,
                recommendedDpi = 430,
                fireButtonSize = 45,
                dragTechnique = "Rotation Drag for Shotguns & SMGs",
                headshotRating = "98.6% Drag Headshot"
            ),
            DeviceModelConfig(
                id = "samsung_galaxy",
                brand = "SAMSUNG",
                modelName = "Galaxy S23/S24, A54, A34, M34",
                badge = "OneUI Precision Calibration",
                general = 192,
                redDot = 184,
                scope2x = 166,
                scope4x = 156,
                sniperScope = 94,
                freeLook = 135,
                recommendedDpi = 411,
                fireButtonSize = 48,
                dragTechnique = "Controlled Vertical Lift (Stable Recoil)",
                headshotRating = "98.2% Drag Headshot"
            ),
            DeviceModelConfig(
                id = "oneplus_oppo",
                brand = "ONEPLUS",
                modelName = "OnePlus 11R/12R, Nord CE 3/4",
                badge = "OxygenOS Ultra Smooth",
                general = 188,
                redDot = 182,
                scope2x = 160,
                scope4x = 150,
                sniperScope = 96,
                freeLook = 140,
                recommendedDpi = 450,
                fireButtonSize = 44,
                dragTechnique = "Micro Drag (Crosshair lock to head)",
                headshotRating = "98.8% Drag Headshot"
            ),
            DeviceModelConfig(
                id = "iqoo_gaming",
                brand = "IQOO",
                modelName = "iQOO Neo 7/9, Z7/Z9 Turbo",
                badge = "Monster Touch Esports Mode",
                general = 186,
                redDot = 180,
                scope2x = 158,
                scope4x = 148,
                sniperScope = 98,
                freeLook = 130,
                recommendedDpi = 480,
                fireButtonSize = 40,
                dragTechnique = "Instant Tap & Flick (Zero Drag Friction)",
                headshotRating = "99.6% Drag Headshot"
            ),
            DeviceModelConfig(
                id = "infinix_tecno",
                brand = "INFINIX / TECNO",
                modelName = "GT 10 Pro, Pova 5/6 Pro",
                badge = "Gaming Gyro & High Sensitivity",
                general = 200,
                redDot = 196,
                scope2x = 184,
                scope4x = 174,
                sniperScope = 78,
                freeLook = 165,
                recommendedDpi = 400,
                fireButtonSize = 50,
                dragTechnique = "High-Arc Drag (Overcomes touch delay)",
                headshotRating = "98.4% Drag Headshot"
            ),
            DeviceModelConfig(
                id = "apple_iphone",
                brand = "APPLE",
                modelName = "iPhone 12 / 13 / 14 / 15 / 16 Pro",
                badge = "iOS 120Hz ProMotion",
                general = 184,
                redDot = 176,
                scope2x = 152,
                scope4x = 140,
                sniperScope = 104,
                freeLook = 120,
                recommendedDpi = 400,
                fireButtonSize = 38,
                dragTechnique = "Precision Micro-Flick",
                headshotRating = "99.8% Drag Headshot"
            )
        )
    }
}

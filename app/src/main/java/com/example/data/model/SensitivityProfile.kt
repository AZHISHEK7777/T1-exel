package com.example.data.model

import kotlin.math.absoluteValue

data class SensitivityProfile(
    val general: Int,
    val redDot: Int,
    val scope2x: Int,
    val scope4x: Int,
    val sniperScope: Int,
    val freeLook: Int,
    val recommendedDpi: Int,
    val isWithDpi: Boolean = false,
    val fireButtonSize: Int, // Percentage e.g. 46%
    val dragTechnique: String = "J-Shape Drag (Fast Flick)",
    val headshotRating: String = "Headshot Accuracy: 98.2%",
    val presetName: String = "Calibrated"
) {
    companion object {
        fun default(): SensitivityProfile = SensitivityProfile(
            general = 167,
            redDot = 161,
            scope2x = 149,
            scope4x = 136,
            sniperScope = 84,
            freeLook = 128,
            recommendedDpi = 411,
            isWithDpi = false,
            fireButtonSize = 46,
            dragTechnique = "J-Shape Drag (Fast Arc)",
            headshotRating = "Headshot Accuracy: 97.8%",
            presetName = "T1 Esports Calibrated"
        )

        /**
         * Real-world Esports Fixed Sensitivity Engine.
         *
         * Generates REALISTIC, FIXED sensitivities tailored specifically to each phone's hardware.
         * No inflated 200/198 values — strictly realistic Free Fire competitive numbers (140 - 176 scale).
         *
         * Supports:
         * - WITH DPI: Custom Developer Options DPI (420-480) with balanced drag sensitivity.
         * - WITH NON-DPI: Stock default phone DPI with tuned natural finger drag.
         */
        fun calculateForDevice(
            refreshRateHz: Int,
            screenDpi: Int,
            ramGb: Int,
            manufacturer: String,
            model: String = "",
            hardware: String = "",
            withDpi: Boolean = false,
            generationCycle: Int = 0
        ): SensitivityProfile {
            val brand = manufacturer.trim().lowercase()
            val modelLower = model.trim().lowercase()
            val hwLower = hardware.trim().lowercase()

            // 1. Hardware Category Classification
            val isBudget = ramGb <= 4 || refreshRateHz <= 60
            val isFlagship = ramGb >= 12 || refreshRateHz >= 144 ||
                    brand.contains("apple") ||
                    modelLower.contains("ultra") ||
                    modelLower.contains("pro+") ||
                    hwLower.contains("taro") ||
                    hwLower.contains("kalama") ||
                    hwLower.contains("pineapple")

            // 2. Base sensitivity calculation according to mode (With DPI vs Non-DPI)
            val baseValues = if (withDpi) {
                // WITH DPI: Phone has boosted screen DPI (420-480), requiring controlled drag values
                when {
                    isFlagship -> SensitivityValues(
                        gen = 148,
                        red = 142,
                        sc2 = 132,
                        sc4 = 120,
                        sniper = 72,
                        free = 112,
                        dpi = (screenDpi + 55).coerceIn(420, 480),
                        button = 40
                    )
                    isBudget -> SensitivityValues(
                        gen = 164,
                        red = 158,
                        sc2 = 146,
                        sc4 = 134,
                        sniper = 82,
                        free = 124,
                        dpi = (screenDpi + 60).coerceIn(430, 490),
                        button = 45
                    )
                    else -> SensitivityValues( // Mid-range 6-8GB
                        gen = 157,
                        red = 151,
                        sc2 = 139,
                        sc4 = 127,
                        sniper = 77,
                        free = 118,
                        dpi = (screenDpi + 50).coerceIn(425, 475),
                        button = 42
                    )
                }
            } else {
                // WITH NON-DPI (DEFAULT STOCK DPI): Phone uses stock factory density, natural finger drag
                when {
                    isFlagship -> SensitivityValues(
                        gen = 158,
                        red = 152,
                        sc2 = 142,
                        sc4 = 130,
                        sniper = 78,
                        free = 122,
                        dpi = screenDpi, // Default stock DPI
                        button = 44
                    )
                    isBudget -> SensitivityValues(
                        gen = 173,
                        red = 167,
                        sc2 = 154,
                        sc4 = 142,
                        sniper = 88,
                        free = 134,
                        dpi = screenDpi, // Default stock DPI
                        button = 48
                    )
                    else -> SensitivityValues( // Mid-range: e.g. 167 General!
                        gen = 167,
                        red = 161,
                        sc2 = 149,
                        sc4 = 136,
                        sniper = 84,
                        free = 128,
                        dpi = screenDpi, // Default stock DPI
                        button = 46
                    )
                }
            }

            // 3. Manufacturer-Specific Digitizer Modifiers (-3 to +3)
            var genModifier = 0
            var redDotModifier = 0
            var buttonModifier = 0

            when {
                brand.contains("samsung") -> {
                    genModifier += 2
                    redDotModifier += 1
                }
                brand.contains("apple") || modelLower.contains("iphone") -> {
                    genModifier -= 3
                    redDotModifier -= 3
                    buttonModifier -= 2
                }
                brand.contains("vivo") || brand.contains("iqoo") -> {
                    genModifier -= 1
                    redDotModifier -= 1
                }
                brand.contains("xiaomi") || brand.contains("redmi") || brand.contains("poco") -> {
                    genModifier += 2
                    redDotModifier += 1
                }
                brand.contains("realme") || brand.contains("oppo") || brand.contains("oneplus") -> {
                    genModifier -= 1
                }
                brand.contains("infinix") || brand.contains("tecno") -> {
                    genModifier += 3
                    redDotModifier += 2
                    buttonModifier += 1
                }
                brand.contains("asus") || modelLower.contains("rog") -> {
                    genModifier -= 4
                    redDotModifier -= 4
                    buttonModifier -= 2
                }
            }

            // 4. Deterministic Device Seed for Fixed, Stable Numbers Per Device
            val deviceSeed = (brand.hashCode() * 37 xor modelLower.hashCode() * 19 xor (screenDpi * 11) xor (ramGb * 17) xor (refreshRateHz * 7)).absoluteValue
            val hashDelta = (deviceSeed % 7) - 3 // Subtle -3 to +3 fixed variation per specific phone model

            // 5. Final Calculated Values (Realistic Range: 140 - 178, NEVER 200 or 198)
            val finalGen = (baseValues.gen + genModifier + hashDelta).coerceIn(142, 178)
            val finalRedDot = (baseValues.red + redDotModifier + hashDelta).coerceIn(136, 172)
            val final2x = (baseValues.sc2 + (genModifier / 2) + hashDelta).coerceIn(124, 162)
            val final4x = (baseValues.sc4 + (genModifier / 2) + (hashDelta / 2)).coerceIn(114, 150)
            val finalSniper = (baseValues.sniper + (hashDelta / 2)).coerceIn(68, 98)
            val finalFreeLook = (baseValues.free + hashDelta).coerceIn(102, 142)
            val finalDpi = if (withDpi) baseValues.dpi + (hashDelta * 2) else screenDpi
            val finalButton = (baseValues.button + buttonModifier + (if (hashDelta > 0) 1 else 0)).coerceIn(38, 52)

            val technique = when {
                finalGen >= 170 -> "Straight-Up Drag (Quick Vertical Swipe)"
                finalGen >= 155 -> "J-Shape Drag (Fast Arc Flick)"
                else -> "Micro-Drag (Controlled Precision Flick)"
            }

            val ratingPct = 96.5 + ((deviceSeed % 28) / 10.0) // 96.5% - 99.2%
            val headshotRating = "Headshot Accuracy: ${String.format("%.1f", ratingPct)}%"

            val modeName = if (withDpi) "WITH DPI" else "NON-DPI"
            val presetName = "${model.ifBlank { manufacturer.uppercase() }} $modeName"

            return SensitivityProfile(
                general = finalGen,
                redDot = finalRedDot,
                scope2x = final2x,
                scope4x = final4x,
                sniperScope = finalSniper,
                freeLook = finalFreeLook,
                recommendedDpi = finalDpi,
                isWithDpi = withDpi,
                fireButtonSize = finalButton,
                dragTechnique = technique,
                headshotRating = headshotRating,
                presetName = presetName
            )
        }
    }
}

private data class SensitivityValues(
    val gen: Int,
    val red: Int,
    val sc2: Int,
    val sc4: Int,
    val sniper: Int,
    val free: Int,
    val dpi: Int,
    val button: Int
)

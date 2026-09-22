package com.example.util

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.util.DisplayMetrics
import android.view.WindowManager
import com.example.data.model.DeviceInfo
import java.io.File
import kotlin.math.roundToInt

object DeviceDetector {

    fun getDeviceInfo(context: Context): DeviceInfo {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
        val display = windowManager?.defaultDisplay

        val metrics = DisplayMetrics()
        display?.getRealMetrics(metrics)

        val refreshRate = display?.refreshRate?.roundToInt() ?: 60

        val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        actManager?.getMemoryInfo(memInfo)

        val totalRamGb = memInfo.totalMem / (1024.0 * 1024.0 * 1024.0)
        val availRamGb = memInfo.availMem / (1024.0 * 1024.0 * 1024.0)

        val resolution = "${metrics.widthPixels} x ${metrics.heightPixels}"

        val processorArch = if (Build.SUPPORTED_ABIS.isNotEmpty()) {
            Build.SUPPORTED_ABIS[0]
        } else {
            System.getProperty("os.arch") ?: "arm64-v8a"
        }

        return DeviceInfo(
            manufacturer = Build.MANUFACTURER ?: "Generic",
            model = Build.MODEL ?: "Android Device",
            hardware = Build.HARDWARE ?: Build.BOARD ?: "Qualcomm / MediaTek",
            androidVersion = Build.VERSION.RELEASE ?: "14",
            apiLevel = Build.VERSION.SDK_INT,
            resolution = resolution,
            screenDpi = metrics.densityDpi,
            refreshRateHz = refreshRate,
            totalRamGb = (totalRamGb * 10).roundToInt() / 10.0,
            availableRamGb = (availRamGb * 10).roundToInt() / 10.0,
            processorArch = processorArch
        )
    }
}

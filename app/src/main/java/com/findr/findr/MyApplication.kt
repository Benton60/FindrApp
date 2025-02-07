package com.findr.findr
import android.app.Application
import androidx.camera.core.CameraXConfig

class MyApplication : Application(), CameraXConfig.Provider {
    override fun getCameraXConfig(): CameraXConfig {
        return CameraXConfig.Builder()
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()
    }
}

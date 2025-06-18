package com.factordev.traslator

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

object PermissionManager {
    
    const val RECORD_AUDIO_PERMISSION = Manifest.permission.RECORD_AUDIO
    
    fun hasAudioPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            RECORD_AUDIO_PERMISSION
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    fun shouldShowRationale(activity: android.app.Activity): Boolean {
        return activity.shouldShowRequestPermissionRationale(RECORD_AUDIO_PERMISSION)
    }
} 
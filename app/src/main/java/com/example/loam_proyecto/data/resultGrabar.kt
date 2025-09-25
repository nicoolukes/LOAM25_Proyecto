package com.example.loam_proyecto.data

import android.media.MediaRecorder

data class StartResult(
    val recorder: MediaRecorder?,
    val filePath: String,
    val success: Boolean,
    val message: String
)

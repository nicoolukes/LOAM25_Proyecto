package com.example.loam_proyecto.Screen

import androidx.compose.runtime.Composable
import android.content.Context
import android.hardware.camera2.CameraManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun LinternaScreen(){

    val context = LocalContext.current
    var isFlashOn by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Button(
            onClick = {
                toggleFlash(context, !isFlashOn)
                isFlashOn = !isFlashOn
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isFlashOn) "Apagar linterna" else "Encender linterna")
        }
    }
}



// Función para manejar la linterna
fun toggleFlash(context: Context, turnOn: Boolean) {
    val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    val cameraId = cameraManager.cameraIdList[0] // normalmente la trasera

    try {
        cameraManager.setTorchMode(cameraId, turnOn)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}


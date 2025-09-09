package com.example.loam_proyecto.Screen

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat

@Composable
fun Camara() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context) }

    // Estado para saber si los permisos han sido concedidos
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.RECORD_AUDIO
                    ) == PackageManager.PERMISSION_GRANTED
        )
    }

    // Launcher para solicitar permisos
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasCameraPermission = permissions[Manifest.permission.CAMERA] == true &&
                permissions[Manifest.permission.RECORD_AUDIO] == true

        if (!hasCameraPermission) {
            // Los permisos fueron denegados
            // Aquí podrías manejar el caso de permisos denegados permanentemente
        }
    }

    // Inicializa ManejadorCamara solo cuando tengamos permisos
    val manejadorCamara = remember(context, previewView, lifecycleOwner) {
        ManejadorCamara(context, previewView, lifecycleOwner)
    }

    // Efecto para solicitar permisos si no se tienen y prender la cámara si se tienen
    LaunchedEffect(hasCameraPermission) {
        if (hasCameraPermission) {
            manejadorCamara.prenderCamara()
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO
                )
            )
        }
    }

    // Liberar recursos cuando el Composable se va de la composición
    DisposableEffect(Unit) {
        onDispose {
            manejadorCamara.liberarRecursos()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        if (hasCameraPermission) {
            // Mostrar preview de la cámara
            AndroidView(
                factory = { previewView },
                modifier = Modifier.weight(1f)
            )

            // Botones de control
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(onClick = { manejadorCamara.tomarFoto() }) {
                    Text("Foto")
                }
                Button(onClick = { manejadorCamara.empezarGrabacion() }) {
                    Text("Grabar")
                }
                Button(onClick = { manejadorCamara.detenerGrabacion() }) {
                    Text("Detener")
                }
            }
        } else {
            // Mostrar mensaje de permisos
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Se necesitan permisos de cámara y audio para usar esta función.")
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = {
                        permissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.CAMERA,
                                Manifest.permission.RECORD_AUDIO
                            )
                        )
                    }) {
                        Text("Otorgar Permisos")
                    }
                }
            }
        }
    }
}
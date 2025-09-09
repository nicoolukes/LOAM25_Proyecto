package com.example.loam_proyecto.Screen


import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat

@Composable
fun Camara (){
    val context = LocalContext.current
    val activity = context as Activity
    val permisos = remember { ManejadorPermisos(activity) }
    val previewView = remember {
        @Composable
        fun CamaraScreen() { // Renombrado a CamaraScreen para seguir convenciones
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
                // Aquí podrías mostrar un mensaje si los permisos son denegados permanentemente
                if (!hasCameraPermission) {
                    // Informar al usuario que los permisos son necesarios
                }
            }

            // Inicializa ManejadorCamara solo una vez y cuando los permisos estén disponibles
            // Usamos `produceState` o un `LaunchedEffect` con `key` para manejar la creación.
            // O, más simple, crearlo directamente y pasar el lifecycleOwner.
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
                    AndroidView(factory = { previewView }, modifier = Modifier.weight(1f))
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
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Se necesitan permisos de cámara y audio para usar esta función.")
                            Spacer(modifier = Modifier.height(8.dp))
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
        }PreviewView(context) }
    val manejadorCamara = remember { ManejadorCamara(context, previewView) }

    LaunchedEffect(Unit) {
        if (permisos.checarPermisosCamara()) {
            manejadorCamara.prenderCamara()
        } else {
            permisos.pedirPermisosCamara()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        AndroidView(factory = { previewView }, modifier = Modifier.weight(1f))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Button(onClick = { manejadorCamara.empezarGrabacion() }) {
                Text("Grabar")
            }
            Button(onClick = { manejadorCamara.detenerGrabacion() }) {
                Text("Detener")
            }
        }
    }
}
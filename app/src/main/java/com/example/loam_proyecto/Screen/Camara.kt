package com.example.loam_proyecto.Screen

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Camara() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val vistaPrev = remember { PreviewView(context) }
    // Estados
    var tienePermiso by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }
    var isGrabando by remember { mutableStateOf(false) }
    var isFlashOn by remember { mutableStateOf(false) }
    var isBackCamara by remember { mutableStateOf(true) }

    val permisoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permisos ->
        tienePermiso = permisos[Manifest.permission.CAMERA] == true &&
                permisos[Manifest.permission.RECORD_AUDIO] == true
    }

    // Inicializa manejador
    val manejadorCamara = remember(context, vistaPrev, lifecycleOwner) {
        ManejadorCamara(context, vistaPrev, lifecycleOwner)
    }

    LaunchedEffect(tienePermiso) {
        if (tienePermiso) {
            manejadorCamara.prenderCamara()
        } else {
            permisoLauncher.launch(
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO
                )
            )
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            manejadorCamara.liberarRecursos()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (tienePermiso) {
            // Vista previa de la cámara
            AndroidView(
                factory = {vistaPrev},
                modifier = Modifier.fillMaxSize()
            )

            // Overlay superior con controles
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.TopCenter),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Flash
                IconButton(
                    onClick = {
                        manejadorCamara.toggleFlash()
                        isFlashOn = !isFlashOn
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = Color.Black.copy(alpha = 0.5f),
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = if (isFlashOn) Icons.Filled.FlashOn else Icons.Filled.FlashOff,
                        contentDescription = if (isFlashOn) "Flash activado" else "Flash desactivado",
                        tint = if (isFlashOn) Color.Yellow else Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Indicador de grabación
                if (isGrabando) {
                    Card(
                        modifier = Modifier
                            .background(Color.Red, RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Red)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(Color.White, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "REC",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Controles inferiores
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    // Respetar navigation bar y un poco más de separación
                    .navigationBarsPadding()
                    .padding(bottom = 8.dp)
                    .background(
                        Color.Black.copy(alpha = 0.7f),
                        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                    )
                    .padding(20.dp)
            ) {
                // Fila principal de controles
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    // Botón central (Foto/Video)
                    Box(contentAlignment = Alignment.Center) {
                        // Anillo exterior
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .background(
                                    color = if (isGrabando) Color.Red else Color.White,
                                    shape = CircleShape
                                )
                        )

                        // Botón de captura
                        IconButton(
                            onClick = {
                                if (isGrabando) {
                                    manejadorCamara.detenerGrabacion()
                                    isGrabando = false
                                } else {
                                    manejadorCamara.tomarFoto()
                                }
                            },
                            modifier = Modifier
                                .size(68.dp)
                                .background(
                                    color = if (isGrabando) Color.White else Color.Transparent,
                                    shape = if (isGrabando) RoundedCornerShape(8.dp) else CircleShape
                                )
                        ) {
                            if (!isGrabando) {
                                Box(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .background(Color.White, CircleShape)
                                        .clip(CircleShape)
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .background(Color.Red, RoundedCornerShape(4.dp))
                                )
                            }
                        }
                    }

                    // Cambiar cámara
                    IconButton(
                        onClick = {
                            manejadorCamara.cambiarCamara()
                            isBackCamara = !isBackCamara
                        },
                        modifier = Modifier
                            .size(50.dp)
                            .background(
                                color = Color.White.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(12.dp)
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Cameraswitch,
                            contentDescription = "Cambiar cámara",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Fila de modos y video
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Modo Video
                    TextButton(
                        onClick = {
                            if (!isGrabando) {
                                manejadorCamara.empezarGrabacion()
                                isGrabando = true
                            }
                        },
                        modifier = Modifier
                            .background(
                                color = if (isGrabando) Color.Red.copy(alpha = 0.3f) else Color.Transparent,
                                shape = RoundedCornerShape(20.dp)
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Videocam,
                            contentDescription = "Video",
                            tint = if (isGrabando) Color.Red else Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "VIDEO",
                            color = if (isGrabando) Color.Red else Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Modo Foto (seleccionado por defecto)
                    TextButton(
                        onClick = { /* Ya en modo foto */ },
                        modifier = Modifier
                            .background(
                                color = if (!isGrabando) Color.Yellow.copy(alpha = 0.3f) else Color.Transparent,
                                shape = RoundedCornerShape(20.dp)
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PhotoCamera,
                            contentDescription = "Foto",
                            tint = if (!isGrabando) Color.Yellow else Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "FOTO",
                            color = if (!isGrabando) Color.Yellow else Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

        } else {
            // Pantalla de permisos
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.padding(32.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CameraAlt,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Permisos de Cámara",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Se necesitan permisos de cámara y audio para usar esta función.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = {
                                permisoLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.CAMERA,
                                        Manifest.permission.RECORD_AUDIO
                                    )
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Lock,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Otorgar Permisos")
                        }
                    }
                }
            }
        }
    }
}
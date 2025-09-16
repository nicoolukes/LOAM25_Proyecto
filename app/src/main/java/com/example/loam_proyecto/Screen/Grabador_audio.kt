package com.example.loam_proyecto.Screen

import android.Manifest
import android.app.Activity
import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavHostController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Grabador_audio(
    navController: NavHostController,
    manejadorPermisos: ManejadorPermisos? = null
) {
    val context = LocalContext.current
    val activity = LocalContext.current as? Activity
    val permisos = manejadorPermisos ?: activity?.let { ManejadorPermisos(it) }

    val scope = rememberCoroutineScope()

    var isRecording by remember { mutableStateOf(false) }
    var mediaRecorder by remember { mutableStateOf<MediaRecorder?>(null) }
    var audioFilePath by remember { mutableStateOf("") }
    var recordingTime by remember { mutableStateOf(0L) }
    var hasPermission by remember { mutableStateOf(permisos?.checarPermisosAudio() ?: false) }
    var pendingStartAfterPermission by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    // Playback
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var isPlaying by remember { mutableStateOf(false) }

    // Launcher para pedir permisos y recibir resultado aquí mismo (no necesitás ir hacia atrás)
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        val allGranted = perms.values.all { it }
        hasPermission = allGranted
        if (allGranted && pendingStartAfterPermission) {
            pendingStartAfterPermission = false
            // arrancamos la grabación automáticamente
            scope.launch {
                val result = startRecordingAsync(context)
                withContext(Dispatchers.Main) {
                    if (result.success && result.recorder != null) {
                        mediaRecorder = result.recorder
                        audioFilePath = result.filePath
                        isRecording = true
                        errorMessage = ""
                    } else {
                        errorMessage = result.message
                    }
                }
            }
        } else if (!allGranted) {
            pendingStartAfterPermission = false
            Toast.makeText(context, "Permisos denegados. No se puede grabar.", Toast.LENGTH_SHORT).show()
        }
    }

    // Re-check permisos al volver al foreground
    val lifecycleOwner = LocalContext.current as? androidx.lifecycle.LifecycleOwner
    DisposableEffect(lifecycleOwner) {
        val owner = lifecycleOwner
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasPermission = permisos?.checarPermisosAudio() ?: false
            }
        }
        owner?.lifecycle?.addObserver(observer)
        onDispose { owner?.lifecycle?.removeObserver(observer) }
    }

    // Timer de grabación
    LaunchedEffect(isRecording) {
        if (isRecording) {
            val startTime = System.currentTimeMillis()
            while (isRecording) {
                recordingTime = System.currentTimeMillis() - startTime
                kotlinx.coroutines.delay(100)
            }
        } else {
            recordingTime = 0L
        }
    }

    // Liberar recursos al salir de la pantalla
    DisposableEffect(Unit) {
        onDispose {
            try { mediaRecorder?.stop() } catch (_: Exception) {}
            try { mediaRecorder?.release() } catch (_: Exception) {}
            try { mediaPlayer?.stop(); mediaPlayer?.release() } catch (_: Exception) {}
            mediaRecorder = null
            mediaPlayer = null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Grabadora de Audio", fontSize = 24.sp, fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary)

                Spacer(modifier = Modifier.height(24.dp))

                if (isRecording) {
                    Text("Grabando...", fontSize = 18.sp, color = Color.Red, fontWeight = FontWeight.Medium)
                    Text(formatTime(recordingTime), fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color.Red)
                } else {
                    Text("Listo para grabar", fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
                }

                Spacer(modifier = Modifier.height(32.dp))

                FloatingActionButton(
                    onClick = {
                        Log.d("GrabadorAudio", "Botón presionado. hasPermission=$hasPermission, isRecording=$isRecording")
                        if (hasPermission) {
                            if (isRecording) {
                                // detener
                                scope.launch {
                                    val (ok, msg) = stopRecordingAsync(mediaRecorder)
                                    withContext(Dispatchers.Main) {
                                        if (ok) {
                                            mediaRecorder = null
                                            isRecording = false
                                            errorMessage = ""
                                            Toast.makeText(context, "Audio guardado: ${File(audioFilePath).name}", Toast.LENGTH_SHORT).show()
                                        } else {
                                            errorMessage = msg
                                        }
                                    }
                                }
                            } else {
                                // iniciar
                                scope.launch {
                                    val result = startRecordingAsync(context)
                                    withContext(Dispatchers.Main) {
                                        if (result.success && result.recorder != null) {
                                            mediaRecorder = result.recorder
                                            audioFilePath = result.filePath
                                            isRecording = true
                                            errorMessage = ""
                                        } else {
                                            errorMessage = result.message
                                        }
                                    }
                                }
                            }
                        } else {
                            // pedimos permisos con el launcher; si el usuario da ok, arrancamos automáticamente
                            pendingStartAfterPermission = true
                            val permsToAsk = mutableListOf(Manifest.permission.RECORD_AUDIO)
                            // pedimos storage solo para APIs antiguas (no hace daño pedirlo)
                            permsToAsk.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            permsToAsk.add(Manifest.permission.READ_EXTERNAL_STORAGE)
                            permissionLauncher.launch(permsToAsk.toTypedArray())
                        }
                    },
                    modifier = Modifier.size(80.dp),
                    containerColor = if (isRecording) Color.Red else MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                        contentDescription = if (isRecording) "Detener grabación" else "Iniciar grabación",
                        modifier = Modifier.size(32.dp),
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (audioFilePath.isNotEmpty() && !isRecording) {
                    Text("Audio guardado: ${File(audioFilePath).name}", fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                        // Play / Pause
                        IconButton(onClick = {
                            if (!isPlaying) {
                                // start playback
                                scope.launch {
                                    val playOk = startPlayback(context, audioFilePath) { mp ->
                                        mediaPlayer = mp
                                        isPlaying = true
                                    }
                                    if (!playOk) {
                                        Toast.makeText(context, "Error reproduciendo audio", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } else {
                                // pause
                                try {
                                    mediaPlayer?.let {
                                        if (it.isPlaying) {
                                            it.pause()
                                            isPlaying = false
                                        } else {
                                            it.start()
                                            isPlaying = true
                                        }
                                    }
                                } catch (e: Exception) {
                                    Log.e("GrabadorAudio", "Error pause/resume", e)
                                }
                            }
                        }) {
                            Icon(imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "Pausar" else "Reproducir"
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Stop playback (release)
                        IconButton(onClick = {
                            try {
                                mediaPlayer?.stop()
                            } catch (_: Exception) {}
                            try { mediaPlayer?.release() } catch (_: Exception) {}
                            mediaPlayer = null
                            isPlaying = false
                        }) {
                            Icon(imageVector = Icons.Default.Stop, contentDescription = "Detener reproducción")
                        }
                    }
                }

                if (errorMessage.isNotEmpty()) {
                    Text("Error: $errorMessage", color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = { navController.popBackStack() }, modifier = Modifier.fillMaxWidth()) {
            Text("Volver")
        }
    }
}

/** --- Helpers asincrónicos --- **/

private data class StartResult(val recorder: MediaRecorder?, val filePath: String, val success: Boolean, val message: String)

private suspend fun startRecordingAsync(context: Context): StartResult = withContext(Dispatchers.IO) {
    try {
        val mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }

        val audioDir = File(context.filesDir, "AudioRecordings")
        if (!audioDir.exists()) audioDir.mkdirs()
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val audioFile = File(audioDir, "AUDIO_${timeStamp}.3gp")

        mediaRecorder.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            setOutputFile(audioFile.absolutePath)
        }

        try {
            mediaRecorder.prepare()
            mediaRecorder.start()
        } catch (ise: IllegalStateException) {
            try { mediaRecorder.release() } catch (_: Exception) {}
            return@withContext StartResult(null, "", false, "IllegalState al iniciar: ${ise.message}")
        } catch (se: SecurityException) {
            try { mediaRecorder.release() } catch (_: Exception) {}
            return@withContext StartResult(null, "", false, "Falta permiso de micrófono: ${se.message}")
        }

        return@withContext StartResult(mediaRecorder, audioFile.absolutePath, true, "")
    } catch (e: Exception) {
        Log.e("GrabadorAudio", "Error al iniciar grabación", e)
        return@withContext StartResult(null, "", false, e.message ?: "Error desconocido al iniciar")
    }
}

private suspend fun stopRecordingAsync(mediaRecorder: MediaRecorder?): Pair<Boolean, String> = withContext(Dispatchers.IO) {
    if (mediaRecorder == null) return@withContext Pair(false, "MediaRecorder es null")
    try {
        try {
            mediaRecorder.stop()
        } catch (ise: IllegalStateException) {
            try { mediaRecorder.reset() } catch (_: Exception) {}
            try { mediaRecorder.release() } catch (_: Exception) {}
            return@withContext Pair(false, "stop() falló (no estaba grabando): ${ise.message}")
        }
        mediaRecorder.reset()
        mediaRecorder.release()
        return@withContext Pair(true, "")
    } catch (se: SecurityException) {
        try { mediaRecorder.release() } catch (_: Exception) {}
        return@withContext Pair(false, "Falta permiso al detener: ${se.message}")
    } catch (e: Exception) {
        try { mediaRecorder.release() } catch (_: Exception) {}
        return@withContext Pair(false, e.message ?: "Error desconocido al detener")
    }
}

// Playback: prepara y arranca en background, retorna true si ok y ejecuta el callback con el MediaPlayer ya listo
private suspend fun startPlayback(context: Context, filePath: String, onReady: (MediaPlayer) -> Unit): Boolean {
    return try {
        val mp = MediaPlayer()
        withContext(Dispatchers.IO) {
            mp.setDataSource(filePath)
            mp.prepare() // bloqueante en IO
        }
        mp.setOnCompletionListener {
            try { mp.release() } catch (_: Exception) {}
        }
        mp.start()
        onReady(mp)
        true
    } catch (e: Exception) {
        Log.e("GrabadorAudio", "Error reproduciendo audio", e)
        false
    }
}

private fun formatTime(timeMs: Long): String {
    val seconds = (timeMs / 1000) % 60
    val minutes = (timeMs / 1000) / 60
    return String.format("%02d:%02d", minutes, seconds)
}

package com.example.loam_proyecto.Screen

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.*
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import androidx.camera.core.TorchState

class ManejadorCamara(
    private val context: Context,
    private val previewView: PreviewView,
    private val lifecycleOwner: LifecycleOwner
) {
    private var cameraProvider: ProcessCameraProvider? = null
    private var preview: Preview? = null
    private var imageCapture: ImageCapture? = null
    private var videoCapture: VideoCapture<Recorder>? = null
    private var recording: Recording? = null
    private var camera: Camera? = null

    // Executor para tareas de cámara
    private lateinit var cameraExecutor: ExecutorService

    // Estado de lente: BACK por defecto
    private var lensFacing: Int = CameraSelector.LENS_FACING_BACK

    init {
        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    fun prenderCamara() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener(Runnable {
            try {
                cameraProvider = cameraProviderFuture.get()
                bindCameraUseCases()
                Log.d("ManejadorCamara", "Cámara iniciada exitosamente")
            } catch (exc: Exception) {
                Log.e("ManejadorCamara", "Fallo al vincular casos de uso", exc)
                Toast.makeText(
                    context,
                    "Error al iniciar la cámara: ${exc.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }, ContextCompat.getMainExecutor(context))
    }

    private fun bindCameraUseCases() {
        val cameraProvider = cameraProvider ?: return

        // Unbind antes de bind
        cameraProvider.unbindAll()

        // Preview
        preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }

        // ImageCapture
        imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()

        // VideoCapture (Recorder)
        val recorder = Recorder.Builder()
            .setQualitySelector(QualitySelector.from(Quality.HIGHEST))
            .build()
        videoCapture = VideoCapture.withOutput(recorder)

        // Selector por lensFacing (BACK / FRONT)
        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(lensFacing)
            .build()

        // Bind y guardar el Camera instance
        camera = cameraProvider.bindToLifecycle(
            lifecycleOwner,
            cameraSelector,
            preview,
            imageCapture,
            videoCapture
        )
    }

    fun cambiarCamara() {
        // No permitir cambio mientras se esté grabando
        if (recording != null) {
            Toast.makeText(context, "Detener grabación antes de cambiar la cámara", Toast.LENGTH_SHORT).show()
            return
        }

        lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK)
            CameraSelector.LENS_FACING_FRONT
        else
            CameraSelector.LENS_FACING_BACK

        try {
            bindCameraUseCases()
            val nombre = if (lensFacing == CameraSelector.LENS_FACING_BACK) "trasera" else "frontal"
            Toast.makeText(context, "Cámara cambiada a $nombre", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e("ManejadorCamara", "Error al cambiar cámara", e)
            Toast.makeText(context, "Error al cambiar cámara: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    fun toggleFlash() {
        val cam = camera
        if (cam == null) {
            Toast.makeText(context, "Cámara no inicializada", Toast.LENGTH_SHORT).show()
            return
        }

        // Verificar si el sensor tiene flash
        val hasFlash = try {
            cam.cameraInfo.hasFlashUnit()
        } catch (e: Exception) {
            false
        }

        if (!hasFlash) {
            Toast.makeText(context, "Flash no disponible en esta cámara", Toast.LENGTH_SHORT).show()
            return
        }

        // Obtener estado actual y togglear
        val currentTorch = cam.cameraInfo.torchState.value == TorchState.ON
        cam.cameraControl.enableTorch(!currentTorch)
        Toast.makeText(context, if (!currentTorch) "Flash activado" else "Flash desactivado", Toast.LENGTH_SHORT).show()
    }

    fun tomarFoto() {
        val imageCapture = imageCapture ?: run {
            Toast.makeText(context, "Cámara no inicializada", Toast.LENGTH_SHORT).show()
            return
        }

        val name = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US)
            .format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX-Image")
            }
        }

        val outputOptions = ImageCapture.OutputFileOptions
            .Builder(
                context.contentResolver,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            )
            .build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e("ManejadorCamara", "Error al tomar foto: ${exc.message}", exc)
                    Toast.makeText(
                        context,
                        "Error al guardar foto: ${exc.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val msg = "Foto guardada correctamente"
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    Log.d("ManejadorCamara", "Foto guardada: ${output.savedUri}")
                }
            }
        )
    }

    @SuppressLint("MissingPermission")
    fun empezarGrabacion() {
        val videoCapture = this.videoCapture ?: run {
            Toast.makeText(context, "Cámara no inicializada para video", Toast.LENGTH_SHORT).show()
            return
        }

        if (recording != null) {
            Toast.makeText(context, "Ya hay una grabación en curso", Toast.LENGTH_SHORT).show()
            return
        }

        val name = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US)
            .format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/CameraX-Video")
            }
        }

        val mediaStoreOutputOptions = MediaStoreOutputOptions
            .Builder(context.contentResolver, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
            .setContentValues(contentValues)
            .build()

        recording = videoCapture.output
            .prepareRecording(context, mediaStoreOutputOptions)
            .withAudioEnabled()
            .start(ContextCompat.getMainExecutor(context)) { recordEvent ->
                when (recordEvent) {
                    is VideoRecordEvent.Start -> {
                        Toast.makeText(context, "Grabación iniciada", Toast.LENGTH_SHORT).show()
                        Log.d("ManejadorCamara", "Grabación iniciada")
                    }
                    is VideoRecordEvent.Finalize -> {
                        if (!recordEvent.hasError()) {
                            val msg = "Video guardado correctamente"
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            Log.d("ManejadorCamara", "Video guardado: ${recordEvent.outputResults.outputUri}")
                        } else {
                            recording?.close()
                            recording = null
                            Log.e("ManejadorCamara", "Error de grabación: ${recordEvent.error}")
                            Toast.makeText(
                                context,
                                "Error de grabación: ${recordEvent.cause?.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        recording = null
                    }
                }
            }
    }

    fun detenerGrabacion() {
        recording?.stop()
        Log.d("ManejadorCamara", "Detener grabación solicitado")
    }

    fun liberarRecursos() {
        try {
            recording?.stop()
            recording = null
            cameraProvider?.unbindAll()
            if (::cameraExecutor.isInitialized) {
                cameraExecutor.shutdown()
            }
            Log.d("ManejadorCamara", "Recursos liberados correctamente")
        } catch (e: Exception) {
            Log.e("ManejadorCamara", "Error al liberar recursos", e)
        }
    }
}

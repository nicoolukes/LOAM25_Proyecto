package com.example.loam_proyecto.Screen // Asegúrate de que el paquete sea el correcto

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
import androidx.compose.ui.semantics.error
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.privacysandbox.tools.core.generator.build
import androidx.transition.addListener
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

private val privacysandbox: Any

class ManejadorCamara(
    private val context: Context,
    private val previewView: PreviewView,
    private val lifecycleOwner: LifecycleOwner // Necesario para vincular el ciclo de vida de la cámara
) {
    private var cameraProvider: ProcessCameraProvider? = null
    private var preview: Preview? = null
    private var imageCapture: androidx.camera.core.ImageCapture? = null
    private var videoCapture: VideoCapture<androidx.camera.video.Recorder>? = null
    private var recording: androidx.camera.video.Recording? = null
    private lateinit var cameraExecutor: ExecutorService

    init {
        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    fun prenderCamara() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()

            // Configuración del Preview
            preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            // Configuración de ImageCapture (para fotos)
            imageCapture = androidx.camera.core.ImageCapture.Builder().build()

            // Configuración de VideoCapture (para videos)
            val recorder = androidx.camera.video.Recorder.Builder()
                .setQualitySelector(androidx.camera.video.QualitySelector.from(androidx.camera.video.Quality.HIGHEST))
                .build()
            videoCapture = androidx.camera.video.VideoCapture.withOutput(recorder)

            // Seleccionar la cámara trasera por defecto
            val cameraSelector = androidx.camera.core.CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Desvincular todos los casos de uso antes de volver a vincular
                cameraProvider?.unbindAll()

                // Vincular casos de uso a la cámara
                cameraProvider?.bindToLifecycle(
                    lifecycleOwner, // Usar el LifecycleOwner pasado
                    cameraSelector,
                    preview,
                    imageCapture, // Añadir imageCapture si vas a tomar fotos
                    videoCapture  // Añadir videoCapture si vas a grabar videos
                )
            } catch (exc: Exception) {
                Log.e("ManejadorCamara", "Fallo al vincular casos de uso", exc)
                Toast.makeText(context, "Error al iniciar la cámara: ${exc.message}", Toast.LENGTH_LONG).show()
            }
        }, ContextCompat.getMainExecutor(context))
    }

    fun tomarFoto() {
        val imageCapture = imageCapture ?: return // Salir si imageCapture no está inicializado

        // Crear nombre de archivo con marca de tiempo
        val name = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US)
            .format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX-Image")
            }
        }

        // Configurar opciones de salida
        val outputOptions = androidx.camera.core.ImageCapture.OutputFileOptions
            .Builder(
                context.contentResolver,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            )
            .build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : androidx.camera.core.ImageCapture.OnImageSavedCallback {
                override fun onError(exc: androidx.camera.core.ImageCaptureException) {
                    Log.e("ManejadorCamara", "Error al tomar foto: ${exc.message}", exc)
                    Toast.makeText(context, "Error al guardar foto: ${exc.message}", Toast.LENGTH_SHORT).show()
                }

                override fun onImageSaved(output: androidx.camera.core.ImageCapture.OutputFileResults) {
                    val msg = "Foto guardada: ${output.savedUri}"
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    Log.d("ManejadorCamara", msg)
                }
            }
        )
    }


    @SuppressLint("MissingPermission") // Los permisos se deben chequear antes de llamar a esta función
    fun empezarGrabacion() {
        val videoCapture = this.videoCapture ?: return // Salir si videoCapture no está inicializado

        // Si ya hay una grabación en curso, detenerla primero o simplemente salir
        if (recording != null) {
            Log.w("ManejadorCamara", "Ya hay una grabación en curso.")
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

        val mediaStoreOutputOptions = androidx.camera.video.MediaStoreOutputOptions
            .Builder(context.contentResolver, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
            .setContentValues(contentValues)
            .build()

        recording = videoCapture.output
            .prepareRecording(context, mediaStoreOutputOptions)
            // Habilitar la grabación de audio si tienes el permiso RECORD_AUDIO
            .withAudioEnabled() // Asegúrate de tener el permiso RECORD_AUDIO
            .start(ContextCompat.getMainExecutor(context)) { recordEvent ->
                when (recordEvent) {
                    is androidx.camera.video.VideoRecordEvent.Start -> {
                        Toast.makeText(context, "Grabación iniciada", Toast.LENGTH_SHORT).show()
                        Log.d("ManejadorCamara", "Grabación iniciada")
                    }
                    is androidx.camera.video.VideoRecordEvent.Finalize -> {
                        if (!recordEvent.hasError()) {
                            val msg = "Video guardado: ${recordEvent.outputResults.outputUri}"
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            Log.d("ManejadorCamara", msg)
                        } else {
                            recording?.close()
                            recording = null
                            Log.e("ManejadorCamara", "Error de grabación: ${recordEvent.error}")
                            Toast.makeText(context, "Error de grabación: ${recordEvent.cause?.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                    is androidx.camera.video.VideoRecordEvent.Status -> {
                        // Puedes usar esto para mostrar el progreso de la grabación
                        // val stats: RecordingStats = recordEvent.recordingStats
                        // Log.i("ManejadorCamara", "Progreso de grabación: ${stats.numBytesRecorded} bytes")
                    }
                }
            }
    }

    fun detenerGrabacion() {
        recording?.stop()
        recording = null
        Log.d("ManejadorCamara", "Grabación detenida solicitada.")
    }

    // Es importante liberar los recursos cuando la cámara ya no se necesite.
    // Podrías llamarlo en el onDispose de tu Composable o en el onDestroy de tu Activity/Fragment.
    fun liberarRecursos() {
        cameraExecutor.shutdown()
        cameraProvider?.unbindAll() // Desvincula todos los casos de uso
        Log.d("ManejadorCamara", "Recursos de la cámara liberados.")
    }
}

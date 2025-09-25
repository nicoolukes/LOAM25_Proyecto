package com.example.loam_proyecto.Screen

import android.Manifest
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

class ManejadorCamara(private val context: Context, private val previewView: PreviewView, private val lifecycleOwner: LifecycleOwner) {
    companion object {
        private const val TAG = "ManejadorCamara"
    }

    private var cameraProvider: ProcessCameraProvider? = null
    private var preview: Preview? = null
    private var imageCapture: ImageCapture? = null
    private var videoCapture: VideoCapture<Recorder>? = null
    private var recording: Recording? = null
    private var camera: Camera? = null

    // Estado de lente: BACK por defecto
    private var lensFacing: Int = CameraSelector.LENS_FACING_BACK

    // Estados de flash y grabación
    private var isFlashOn: Boolean = false
    private var isRecording: Boolean = false

    fun prenderCamara() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()
                bindCameraUseCases()
            } catch (exc: Exception) {
                Toast.makeText(context, "Error al iniciar la cámara: ${exc.message}", Toast.LENGTH_LONG).show()
            }
        }, ContextCompat.getMainExecutor(context))
    }

    //para ver si tiene las camaras
    private fun hasCameraFor(lens: Int): Boolean {
        val provider = cameraProvider ?: return false
        return try {
            provider.hasCamera(CameraSelector.Builder().requireLensFacing(lens).build())
        } catch (_: Exception) {
            false
        }
    }

    private fun bindCameraUseCases() {
        val provider = cameraProvider
        if (provider == null) {
            return
        }

        val selector = CameraSelector.Builder().requireLensFacing(lensFacing).build()

        // Verificar que la cámara solicitada exista
        if (!hasCameraFor(lensFacing)) {
            val nombre = if (lensFacing == CameraSelector.LENS_FACING_BACK) "trasera" else "frontal"
            Toast.makeText(context, "Cámara $nombre no disponible en el dispositivo", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            provider.unbindAll()

            //visualizar
            preview = Preview.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_16_9)
                .apply {
                    previewView.display?.rotation?.let { setTargetRotation(it) }
                }
                .build()
                .also { it.setSurfaceProvider(previewView.surfaceProvider) }

            //para tomar foto
            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .setTargetAspectRatio(AspectRatio.RATIO_16_9)
                .setJpegQuality(95)
                .build()


            //para grabar video
            val recorder = Recorder.Builder()
                .setQualitySelector(
                    QualitySelector.from(
                        Quality.HIGHEST,
                        FallbackStrategy.lowerQualityOrHigherThan(Quality.SD)
                    )
                )
                .build()
            videoCapture = VideoCapture.withOutput(recorder)

            //unirtodo
            camera = provider.bindToLifecycle(
                lifecycleOwner,
                selector,
                preview,
                imageCapture,
                videoCapture
            )

            // Restaurar estado del flash si corresponde
            if (isFlashOn && camera?.cameraInfo?.hasFlashUnit() == true) {
                camera?.cameraControl?.enableTorch(true)
            }

        } catch (exc: Exception) {
            Toast.makeText(context, "Error al inicializar cámara: ${exc.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun cambiarCamara() {
        if (isRecording) {
            Toast.makeText(context, "Detener grabación antes de cambiar la cámara", Toast.LENGTH_SHORT).show()
            return
        }

        val nueva = if (lensFacing == CameraSelector.LENS_FACING_BACK)
            CameraSelector.LENS_FACING_FRONT
        else
            CameraSelector.LENS_FACING_BACK

        // Verificar existenca de la nueva cámara antes de setear
        if (!hasCameraFor(nueva)) {
            Toast.makeText(context, "La cámara solicitada no está disponible", Toast.LENGTH_SHORT).show()
            return
        }
        lensFacing = nueva
        bindCameraUseCases()
    }

    fun toggleFlash() {
        val cam = camera
        if (cam == null) { 
            Toast.makeText(context, "Cámara no inicializada", Toast.LENGTH_SHORT).show()
            return
        }

        val hasFlash = try {
            cam.cameraInfo.hasFlashUnit()
        } catch (_: Exception) {
            false
        }

        if (!hasFlash) {
            Toast.makeText(context, "Flash no disponible en esta cámara", Toast.LENGTH_SHORT).show()
            return
        }


        isFlashOn = !isFlashOn

        try {
            cam.cameraControl.enableTorch(isFlashOn)
        } catch (_: Exception) {
            Toast.makeText(context, "No se pudo cambiar el flash", Toast.LENGTH_SHORT).show()
            isFlashOn = !isFlashOn
        }
    }

    fun tomarFoto() {
        val ic = imageCapture
        if (ic == null) {
            Toast.makeText(context, "Cámara no inicializada", Toast.LENGTH_SHORT).show()
            return
        }

        val name = "IMG_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(System.currentTimeMillis())}"
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraApp")
            }
        }

        val outputOptions = ImageCapture.OutputFileOptions
            .Builder(context.contentResolver, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            .build()

        try {
            ic.takePicture(
                outputOptions,
                ContextCompat.getMainExecutor(context),
                object : ImageCapture.OnImageSavedCallback {
                    override fun onError(exc: ImageCaptureException) {
                        Toast.makeText(context, "Error al guardar foto", Toast.LENGTH_SHORT).show()
                    }

                    override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                        Toast.makeText(context, "Foto guardada", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        } catch (_: Exception) {
            Toast.makeText(context, "No se pudo tomar la foto", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("MissingPermission")
    fun empezarGrabacion() {
        val vc = videoCapture
        if (vc == null) {
            Toast.makeText(context, "Cámara no inicializada para video", Toast.LENGTH_SHORT).show()
            return
        }

        if (isRecording) {
            Toast.makeText(context, "Ya hay una grabación en curso", Toast.LENGTH_SHORT).show()
            return
        }



        val name = "VID_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(System.currentTimeMillis())}"
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/CameraApp")
            }
        }

        val mediaStoreOutputOptions = MediaStoreOutputOptions
            .Builder(context.contentResolver, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
            .setContentValues(contentValues)
            .build()

        try {
            val pending = vc.output.prepareRecording(context, mediaStoreOutputOptions)

            // Solo habilitar audio si tenemos permiso
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                pending.withAudioEnabled()
            } else {
                Toast.makeText(context, "Grabando sin audio (permiso no otorgado)", Toast.LENGTH_SHORT).show()
            }

            recording = pending.start(ContextCompat.getMainExecutor(context)) { event ->
                when (event) {
                    is VideoRecordEvent.Start -> {
                        isRecording = true
                        Log.d(TAG, "Grabación iniciada")
                    }
                    is VideoRecordEvent.Finalize -> {
                        isRecording = false
                        if (!event.hasError()) {
                            Toast.makeText(context, "Video guardado", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Error al grabar video", Toast.LENGTH_SHORT).show()
                        }
                        recording?.close()
                        recording = null
                    }

                }
            }
        } catch (_: Exception) {
            Toast.makeText(context, "No se pudo iniciar la grabación", Toast.LENGTH_SHORT).show()
            recording = null
            isRecording = false
        }
    }

    fun detenerGrabacion() {
        if (!isRecording && recording == null) return

        try {
            recording?.stop()
        } catch (_: Exception) {

        } finally {
            recording = null
            isRecording = false

        }
    }

    fun liberarRecursos() {
        try {
            try { recording?.stop() } catch (_: Exception) {}
            recording = null
            isRecording = false
            cameraProvider?.unbindAll()

        } catch (_: Exception) {

        }
    }
}

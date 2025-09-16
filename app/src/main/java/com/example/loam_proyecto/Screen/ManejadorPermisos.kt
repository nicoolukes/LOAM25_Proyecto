package com.example.loam_proyecto.Screen

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class ManejadorPermisos(private val activity: Activity) {

    companion object {
        const val CAMERA_PERMISSION_REQUEST_CODE = 1001
        const val AUDIO_PERMISSION_REQUEST_CODE = 1002
        const val CAMERA_AUDIO_PERMISSION_REQUEST_CODE = 1003
    }

    // Verificar permisos de cámara
    fun checarPermisosCamara(): Boolean {
        return ContextCompat.checkSelfPermission(
            activity,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    // Verificar permisos de audio
    fun checarPermisosAudio(): Boolean {
        return ContextCompat.checkSelfPermission(
            activity,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    // Verificar permisos de cámara y audio juntos
    fun checarPermisosCamaraYAudio(): Boolean {
        return checarPermisosCamara() && checarPermisosAudio()
    }

    // Verificar permisos de almacenamiento
    fun checarPermisosAlmacenamiento(): Boolean {
        return ContextCompat.checkSelfPermission(
            activity,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    // Pedir solo permisos de cámara
    fun pedirPermisosCamara() {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(Manifest.permission.CAMERA),
            CAMERA_PERMISSION_REQUEST_CODE
        )
    }

    // Pedir solo permisos de audio
    fun pedirPermisosAudio() {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ),
            AUDIO_PERMISSION_REQUEST_CODE
        )
    }

    // Pedir permisos de cámara y audio (para video con audio)
    fun pedirPermisosCamaraYAudio() {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ),
            CAMERA_AUDIO_PERMISSION_REQUEST_CODE
        )
    }

    // Manejar resultado de permisos
    fun manejarResultadoPermisos(requestCode: Int, grantResults: IntArray): Boolean {
        return when (requestCode) {
            CAMERA_PERMISSION_REQUEST_CODE -> {
                grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
            }
            AUDIO_PERMISSION_REQUEST_CODE -> {
                grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }
            }
            CAMERA_AUDIO_PERMISSION_REQUEST_CODE -> {
                grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }
            }
            else -> false
        }
    }

    // Función helper para obtener mensaje de error específico
    fun obtenerMensajePermisos(requestCode: Int): String {
        return when (requestCode) {
            CAMERA_PERMISSION_REQUEST_CODE -> "Se necesita permiso de cámara para tomar fotos y videos"
            AUDIO_PERMISSION_REQUEST_CODE -> "Se necesita permiso de micrófono y almacenamiento para grabar audio"
            CAMERA_AUDIO_PERMISSION_REQUEST_CODE -> "Se necesitan permisos de cámara, micrófono y almacenamiento para grabar videos con audio"
            else -> "Se necesitan permisos para continuar"
        }
    }
}
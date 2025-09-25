package com.example.loam_proyecto.Screen // Asegúrate de que el paquete sea el correcto

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class ManejadorPermisos(private val activity: Activity) {

    companion object {
        const val CAMERA_PERMISSION_REQUEST_CODE = 1001
        const val AUDIO_PERMISSION_REQUEST_CODE = 1002
    }

    fun checarPermisosCamara(): Boolean {
        return ContextCompat.checkSelfPermission(
            activity,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    activity,
                    Manifest.permission.RECORD_AUDIO
                ) == PackageManager.PERMISSION_GRANTED
    }

    fun pedirPermisosCamara() {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
            ),
            CAMERA_PERMISSION_REQUEST_CODE
        )
    }

    // Función solicitada para verificar permisos de audio
    fun checarPermisosAudio(): Boolean {
        val audioPermission = ContextCompat.checkSelfPermission(
            activity,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        // Para Android 10 y anteriores, también necesitamos permisos de almacenamiento
        val storagePermission = if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(
                        activity,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED
        } else {
            // En Android 11+ no necesitamos estos permisos si guardamos en almacenamiento interno
            true
        }

        return audioPermission && storagePermission
    }

    fun pedirPermisosAudio() {
        val permisos = mutableListOf(Manifest.permission.RECORD_AUDIO)

        // Solo agregar permisos de almacenamiento para Android 10 y anteriores
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            permisos.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            permisos.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        ActivityCompat.requestPermissions(
            activity,
            permisos.toTypedArray(),
            AUDIO_PERMISSION_REQUEST_CODE
        )
    }

    fun manejarResultadoPermisos(requestCode: Int, grantResults: IntArray): Boolean {
        when (requestCode) {
            CAMERA_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    return true
                }
            }
            AUDIO_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    return true
                }
            }
        }
        return false
    }

    // Funciones auxiliares para verificar permisos individuales
    fun tienePermisoAudio(): Boolean {
        return ContextCompat.checkSelfPermission(
            activity,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun tienePermisoAlmacenamiento(): Boolean {
        return if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }
}
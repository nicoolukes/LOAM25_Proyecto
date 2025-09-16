package com.example.loam_proyecto.Screen // Asegúrate de que el paquete sea el correcto

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class ManejadorPermisos(private val activity: Activity) {

    companion object {
        const val CAMERA_PERMISSION_REQUEST_CODE = 1001
    }

    fun checarPermisosCamara(): Boolean {
        return ContextCompat.checkSelfPermission(
            activity,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    activity,
                    Manifest.permission.RECORD_AUDIO // Necesario si también grabas audio
                ) == PackageManager.PERMISSION_GRANTED
    }

    fun pedirPermisosCamara() {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO // Necesario si también grabas audio
            ),
            CAMERA_PERMISSION_REQUEST_CODE
        )
    }

    // Opcional: Puedes añadir una función para manejar el resultado de la solicitud de permisos
    // Esto se llamaría desde tu Activity o Fragment donde se recibe el callback onRequestPermissionsResult
    fun manejarResultadoPermisos(requestCode: Int, grantResults: IntArray): Boolean {
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                // Permisos concedidos
                return true
            } else {
                // Permisos denegados
                // Aquí podrías mostrar un mensaje al usuario explicando por qué necesitas los permisos
            }
        }
        return false
    }
}


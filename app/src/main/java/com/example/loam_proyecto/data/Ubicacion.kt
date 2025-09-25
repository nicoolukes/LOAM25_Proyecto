package com.example.loam_proyecto.data

import android.content.Context
import android.location.Geocoder
import android.util.Log
import android.util.Log.e
import android.widget.Toast
import com.google.firebase.database.FirebaseDatabase
import org.osmdroid.util.GeoPoint
import java.util.Locale
import kotlin.math.log

data class Ubicacion(
    val lat: Double = 0.0,
    val lon: Double = 0.0,
    val dereccion: String = "",
    val referencia: String = ""
)

fun guardarUbicacionEnFirebase(geoPoint: GeoPoint, direccion: String, context: Context) {
    val database = FirebaseDatabase.getInstance()
    val ref = database.getReference("ubicaciones")

    val datos = Ubicacion(
        lat = geoPoint.latitude,
        lon = geoPoint.longitude,
        dereccion = direccion,
        referencia = "referencia del ingeniero"

    )

    ref.push().setValue(datos)
        .addOnSuccessListener {
            Toast.makeText(context, "Ubicación guardada", Toast.LENGTH_SHORT).show()
        }
        .addOnFailureListener { e ->
            Toast.makeText(context, "Error al guardar ubicación", Toast.LENGTH_SHORT).show()
        }

}

fun getDireccion(context: Context, geoPoint: GeoPoint): String{
    val geocoder = Geocoder(context, Locale.getDefault())
    val direcciones = geocoder.getFromLocation(geoPoint.latitude, geoPoint.longitude, 1)
    return if (!direcciones.isNullOrEmpty()){
        direcciones[0].getAddressLine(0) ?: "direccion no encontrada"
    }else{
        "direccion no encontrada"
    }
}
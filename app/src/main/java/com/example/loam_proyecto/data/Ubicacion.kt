package com.example.loam_proyecto.data

import android.content.Context
import android.location.Geocoder
import android.util.Log
import android.util.Log.e
import com.google.firebase.database.FirebaseDatabase
import org.osmdroid.util.GeoPoint
import java.util.Locale
import kotlin.math.log

data class Ubicacion(
    val lat: Double = 0.0,
    val lon: Double = 0.0,
    val referencia: String = ""
)

fun guardarUbicacionEnFirebase(geoPoint: GeoPoint, direccion: String) {
    val database = FirebaseDatabase.getInstance()
    var ref = database.getReference("ubicaciones")

    val datos = hashMapOf(
        "latitud" to geoPoint.latitude,
        "longitud" to geoPoint.longitude,
        "dereccion" to direccion,
        "referencia" to "referencia del ingeniero"

    )

    ref.push().setValue(datos)
        .addOnSuccessListener {
            Log.d("firebase", "Ubicacion guardada en la base de datos")
        }
        .addOnFailureListener { e ->
            Log.e("firebase", "no se pudo guardar", e)
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
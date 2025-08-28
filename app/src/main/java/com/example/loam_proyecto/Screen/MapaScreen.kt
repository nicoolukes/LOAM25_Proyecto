package com.example.loam_proyecto.Screen

import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

import android.Manifest
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.loam_proyecto.data.guardarUbicacionEnFirebase

import com.example.loam_proyecto.data.getDireccion

@Composable
fun MapaScreen() {
    val context = LocalContext.current
    var error by remember { mutableStateOf<String?>(null) }
    var ultimaUbicacion by remember { mutableStateOf<GeoPoint?>(null) }
    var direc by remember { mutableStateOf("buscando direccion") }


    val mapView = remember{
        MapView(context).apply{
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)

            controller.setZoom(15.0)
            controller.setCenter(GeoPoint(-34.6037, -58.3816))
        }
    }
    val permisoUbicacion = remember {
        mutableStateOf(
            ActivityCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted -> permisoUbicacion.value = isGranted }

    LaunchedEffect(Unit) {
        if (!permisoUbicacion.value)
            launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    LaunchedEffect(Unit) {
        if (permisoUbicacion.value){
            val locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(context), mapView)
            locationOverlay.enableMyLocation()
            locationOverlay.enableFollowLocation()
            mapView.overlays.add(locationOverlay)

            locationOverlay.runOnFirstFix {
                ultimaUbicacion = locationOverlay.myLocation
                ultimaUbicacion?.let{
                    direc = getDireccion(context, it)
                }
            }
        }else{
            error = "El GPS no esta activado o no hay permisos"
        }
    }
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ){
        Text(
            text = error?: direc,
            fontSize = 22.sp,
            modifier = Modifier.padding(16.dp)
        )
        Button(
            onClick = {
                ultimaUbicacion?.let{
                    guardarUbicacionEnFirebase(it, direc)
                } ?: run{
                    error = "no se pudo obtener ubicacion"
                }
            },
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Text("Guardar ubicacion")
        }
        AndroidView(factory = {mapView}, modifier = Modifier.weight(1f))
    }

}
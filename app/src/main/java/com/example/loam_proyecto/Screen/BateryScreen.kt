package com.example.loam_proyecto.Screen

import android.content.Intent
import android.content.IntentFilter
import android.icu.util.Calendar
import android.os.BatteryManager
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun BateryScreen() {
    val context = LocalContext.current
    var bateria by remember { mutableStateOf(0) }
    var cargar by remember {mutableStateOf(false)}

    LaunchedEffect(Unit) {
        val filtrar = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val estadoBateria: Intent? = context.registerReceiver(null, filtrar)
        val nivel = estadoBateria?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val escala = estadoBateria?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        bateria = (nivel * 100 / escala.toFloat()).toInt()
        val estado = estadoBateria?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        cargar = estado == BatteryManager.BATTERY_STATUS_CHARGING || estado == BatteryManager.BATTERY_STATUS_FULL
    }
    val minutosPorcentaje = 3L
    val minutosEstimado = bateria * minutosPorcentaje
    val now = Calendar.getInstance()
    now.add(Calendar.MINUTE, minutosEstimado.toInt())
    val estimacionVaciadoHora = "${now.get(Calendar.HOUR_OF_DAY)}:${now.get(Calendar.MINUTE).toString().padStart(2, '0')}"

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ){
        Text("Duracion de Bateria", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Nivel actual: $bateria %")
        Text("Estad: ${if (cargar) "Cargando" else "Descargando"}")

        Spacer(modifier = Modifier.height(8.dp))

        if(!cargar){
            Text("Duracion estimada: $minutosEstimado minutos")
            Text("Se agotaria aprox. a los $estimacionVaciadoHora")
        }else{
            Text("El dispositivo esta cargando, no se clacula estimacion.")
        }
    }
}
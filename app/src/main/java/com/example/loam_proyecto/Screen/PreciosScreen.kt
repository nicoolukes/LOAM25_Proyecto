package com.example.loam_proyecto.Screen

import android.health.connect.datatypes.ExercisePerformanceGoal
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.loam_proyecto.data.Dolar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.example.loam_proyecto.data.PreciosReferencia
import com.example.loam_proyecto.apis.client
import io.ktor.client.call.body
import io.ktor.client.request.get

@Composable
fun PreciosScreen() {
    val database = FirebaseDatabase.getInstance().getReference("preciosReferencia")
    val precios = remember { mutableStateOf(PreciosReferencia()) }//inicializa con lo que hay en PreciosReferencia

    val dolarOC = remember { mutableStateOf(0.0) }


    LaunchedEffect(Unit) { //hace que se ejecute cuando hay una pantalla
        database.addValueEventListener(object : ValueEventListener{//escucha la base de datos
            override fun onDataChange(snapshot: DataSnapshot) { // se ejecuta cuanfo los datos cambian
                precios.value = snapshot.getValue(PreciosReferencia::class.java) ?: PreciosReferencia() //pasa del nodo (firebase) a un objeto y se actualiza el estado
            }
            override fun onCancelled(error: DatabaseError) {}
        })
        try {
            val respuesta: List<Dolar> = client.get("https://dolarapi.com/v1/dolares").body()
            val oficial = respuesta.firstOrNull { it.casa == "oficial" }
            dolarOC.value = oficial?.compra ?: 0.1

        }catch (e: Exception){
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp))
    {
        Text("Honorarios: ${precios.value.precio_honorarios ?: "-"}")
        Text("Precio m2: ${precios.value.precio_m2 ?: "-"}")
        Text("Materiales: ${precios.value.precio_materiales ?: "-"}")
        Text("Pintura: ${precios.value.precio_pintura ?: "-"}")

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Cotización dólar: ${dolarOC.value}",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }


}
package com.example.loam_proyecto.Screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.loam_proyecto.repo.Chat
import com.example.loam_proyecto.data.Mensaje

@Composable
fun ChatScreen() {
    val chat = Chat()
    var mensaje by remember { mutableStateOf("") }
    var listaMensaje by remember { mutableStateOf(listOf<Mensaje>()) }

    LaunchedEffect(Unit) {
        chat.recivirMensaje { mensaj -> listaMensaje = mensaj }
    }
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            reverseLayout = false
        ) {
            items(listaMensaje) { mens ->
                val color : Color
                if (mens.autor == "usuario") {
                    color = Color.Green
                }else {
                    color = Color.Blue
                }
                Text(
                    text = "${mens.autor}: ${mens.texto}",
                    color = color,
                    modifier = Modifier.padding(4.dp)
                )
            }
        }

        Row {
            TextField(
                value = mensaje,
                onValueChange = { mensaje = it },
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick = {
                    if (mensaje.isNotEmpty()) {
                        chat.enviarMensaje(Mensaje(mensaje, "usuario"))
                        mensaje = ""
                    }
                },
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text("Enviar")
            }
        }
    }
}
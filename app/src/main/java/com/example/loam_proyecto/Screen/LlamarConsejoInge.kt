package com.example.loam_proyecto.Screen

import android.net.Uri
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


@Composable
fun LlamarConsejoInge() {
    val context = LocalContext.current //se guarda el contexto, para poder ejecutar operaciones de sistema
    val nroTel = "2954112233"

    Button(
        onClick = {
            val intent = Intent(Intent.ACTION_DIAL) // esto indica que queremo abrir una app del celu
            intent.data = Uri.parse("tel:$nroTel")
            context.startActivity(intent) // en este contexto se inicia la app
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
    ){
        Text(text = "Llamar al Consejo de Ingeniería")
    }
}
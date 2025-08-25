package com.example.loam_proyecto

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.loam_proyecto.ui.theme.LOAMProyectoTheme
import com.google.firebase.database.FirebaseDatabase
import com.example.loam_proyecto.Navegation.AppNavegation

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LOAMProyectoTheme {
                AppNavegation()
            }
        }
        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference("mensaje")

        // Escribe un valor en la base
        myRef.setValue("Hola")
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    LOAMProyectoTheme {
        Greeting("Android")
    }
}
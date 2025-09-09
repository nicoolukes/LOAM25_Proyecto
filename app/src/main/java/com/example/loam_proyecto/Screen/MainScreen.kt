package com.example.loam_proyecto.Screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController

@Composable
fun MainScreen(navController: NavHostController) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = { navController.navigate("precios") }) {
            Text("Ver Precios")
        }
        LlamarConsejoInge()

        Button(onClick = {navController.navigate("mapa")} ) {
            Text("Ver Mapa")
        }

        Button(onClick = {navController.navigate("camara")} ) {
            Text("Camara")
        }
        Camara()
    }
}

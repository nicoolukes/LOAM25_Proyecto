package com.example.loam_proyecto.Navegation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.loam_proyecto.Screen.BateryScreen
import com.example.loam_proyecto.Screen.ChatScreen
import com.example.loam_proyecto.Screen.MainScreen
import com.example.loam_proyecto.Screen.MapaScreen
import com.example.loam_proyecto.Screen.PreciosScreen



@Composable
fun AppNavegation (){
    val navController = rememberNavController() //nos permite ir entre distintas pantallas
    // remember asegura que no se pierda el estado del controlador

    //contiene todas las pantallas para navegar
    NavHost(navController = navController, startDestination = "main") {
        composable("main") { MainScreen(navController) } // es una pantalla navegable la "ruta" es <main>
        composable ("precios"){PreciosScreen()  }
        composable ("mapa"){ MapaScreen()  }
        composable ("bateria"){ BateryScreen() }
        composable ("Chat"){ ChatScreen() }

    }



}

package com.example.loam_proyecto.Navegation

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.loam_proyecto.Screen.BateryScreen
import com.example.loam_proyecto.Screen.Camara
import com.example.loam_proyecto.Screen.ChatScreen
import com.example.loam_proyecto.Screen.Grabador_audio
import com.example.loam_proyecto.Screen.MainScreen
import com.example.loam_proyecto.Screen.ManejadorPermisos
import com.example.loam_proyecto.Screen.MapaScreen
import com.example.loam_proyecto.Screen.PreciosScreen
import com.example.loam_proyecto.Screen.SirenaButton
import com.example.loam_proyecto.Screen.LinternaScreen


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
      composable("audio") {
            val context = LocalContext.current
            val activity = context as? Activity
            val manejador = activity?.let { ManejadorPermisos(it) }

            Grabador_audio(
                navController = navController,
                manejadorPermisos = manejador
            )
        }

        composable ("camara"){ Camara() }
        composable ("SOS") { SirenaButton() }
        composable ("linterna") { LinternaScreen()  }

}



}

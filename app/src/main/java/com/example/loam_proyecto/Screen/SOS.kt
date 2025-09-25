package com.example.loam_proyecto.Screen

import android.Manifest

import android.content.Context
import android.media.MediaPlayer
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.annotation.RequiresPermission
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.example.loam_proyecto.R




@Composable
fun SirenaButton() {
    val context = LocalContext.current
    var reproducMulti by remember { mutableStateOf<MediaPlayer?>(null) }

    Button(onClick =  {
        // Si ya estaba sonando, detenemos primero
        reproducMulti?.stop()
        reproducMulti?.release()

        // Creamos un nuevo MediaPlayer con el sonido de la sirena
        reproducMulti = MediaPlayer.create(context, R.raw.sonido)
        reproducMulti?.start()

        vibrar(context, 6000)
    }) {
        Text("Activar Sirena")
    }
}


fun vibrar(context: Context, milisegundos: Long){
    val vibrator = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
        val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vm.defaultVibrator
    }else{
        //@Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrator.vibrate(VibrationEffect.createOneShot(milisegundos, VibrationEffect.DEFAULT_AMPLITUDE))
    } else {
       // @Suppress("DEPRECATION")
        vibrator.vibrate(milisegundos)
    }
}
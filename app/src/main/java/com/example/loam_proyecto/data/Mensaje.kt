package com.example.loam_proyecto.data

data class Mensaje(
    val texto: String = "",
    val autor: String = "",
    val tiempo: Long = System.currentTimeMillis()

)

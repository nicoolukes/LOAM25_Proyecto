package com.example.loam_proyecto.data

import kotlinx.serialization.Serializable

@Serializable //permite convertir a un objeto o a un JSON
data class Dolar(
    var moneda: String? = null,
    var casa: String? = null,
    var nombre: String? = null,
    var compra: Double? = null,
    var venta: Double? = null,
    var fechaActualizacion: String? = null

)

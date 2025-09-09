package com.example.loam_proyecto.repo

import com.example.loam_proyecto.data.Mensaje
import com.google.firebase.firestore.FirebaseFirestore

class Chat {
    private val db = FirebaseFirestore.getInstance()

    fun enviarMensaje(mensaje: Mensaje){
        db.collection("mensajes").add(mensaje)
    }
    fun recivirMensaje(onChange: (List<Mensaje>)-> Unit){
        db.collection("mensajes")
            .orderBy("tiempo")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null){
                    val lista= snapshot.toObjects(Mensaje::class.java)
                    onChange(lista)
                }
            }
    }
}
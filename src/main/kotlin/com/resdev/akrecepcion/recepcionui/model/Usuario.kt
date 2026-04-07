package com.resdev.akrecepcion.recepcionui.model

data class Usuario(
    val idUsuario: Int,
    val usuario: String,
    val nombreUsuario: String,
    val idNivel: Int,
    val foto: ByteArray? = null,
)


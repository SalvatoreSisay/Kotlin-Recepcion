package com.resdev.akrecepcion.recepcionui.db

/**
 * Ejecutable simple para verificar conectividad.
 *
 * Se puede correr desde IntelliJ (Run DbPingKt) o desde un task Gradle si lo agregamos.
 */
fun main() {
    Env.preload()

    val cfg = DatabaseConfigLoader.tryLoad()
    if (cfg == null) {
        System.err.println("DB no configurada. Revisa .env o variables de entorno.")
        return
    }

    println("DB_URL=${cfg.jdbcUrl}")
    println("DB_USER=${cfg.username}")

    val res = DbHealthcheck.ping()
    res.onSuccess {
        println("OK: conexion a MariaDB y SELECT 1 exitoso.")
    }.onFailure { e ->
        System.err.println("ERROR: no se pudo conectar/validar DB: ${e.message}")
        e.printStackTrace()
    }
}


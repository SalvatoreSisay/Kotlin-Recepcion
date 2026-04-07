package com.resdev.akrecepcion.recepcionui.db

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

/**
 * Loader minimalista de ".env" para desarrollo local.
 *
 * Importante:
 * - En Java/Kotlin, un archivo ".env" NO se carga automaticamente.
 * - En produccion, lo normal es configurar variables de entorno reales del sistema/launcher.
 * - Aqui damos una forma simple de trabajar local sin dependencias extra (dotenv libs).
 */
object Env {
    private val cachedDotenv: Map<String, String> by lazy { loadDotenvFile(Paths.get(".env")) }

    fun preload() {
        // Fuerza la carga del archivo en startup (opcional), pero sin fallar si no existe.
        cachedDotenv.size
    }

    fun get(key: String): String? {
        // Orden de prioridad:
        // 1) System properties (-DDB_HOST=...)
        // 2) Variables de entorno reales (export DB_HOST=...)
        // 3) Archivo local .env (solo dev)
        return System.getProperty(key)
            ?: System.getenv(key)
            ?: cachedDotenv[key]
    }

    fun getRequired(key: String): String =
        get(key) ?: error("Falta configuracion requerida: $key (definela como env var, -D$key, o en .env)")

    private fun loadDotenvFile(path: Path): Map<String, String> {
        if (!Files.exists(path)) return emptyMap()
        return Files.readAllLines(path)
            .asSequence()
            .map { it.trim() }
            .filter { it.isNotBlank() && !it.startsWith("#") }
            .mapNotNull { line ->
                val normalized =
                    if (line.startsWith("export ")) line.removePrefix("export ").trim()
                    else line

                val idx = normalized.indexOf('=')
                if (idx <= 0) return@mapNotNull null

                val k = normalized.substring(0, idx).trim()
                var v = normalized.substring(idx + 1).trim()

                // Permite comillas simples o dobles: DB_PASSWORD="Admin#D3V"
                if ((v.startsWith("\"") && v.endsWith("\"")) || (v.startsWith("'") && v.endsWith("'"))) {
                    v = v.substring(1, v.length - 1)
                }
                k to v
            }
            .toMap()
    }
}


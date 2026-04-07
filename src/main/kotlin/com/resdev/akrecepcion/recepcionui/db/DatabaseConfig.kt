package com.resdev.akrecepcion.recepcionui.db

data class DatabaseConfig(
    val jdbcUrl: String,
    val username: String,
    val password: String,
    val poolName: String = "RecepcionUI-Hikari",
)

object DatabaseConfigLoader {
    /**
     * Construye la configuracion desde variables de entorno / system properties / .env:
     *
     * Opcion A (recomendada): DB_URL + DB_USER + DB_PASSWORD
     * Opcion B: DB_HOST + DB_PORT + DB_NAME + DB_USER + DB_PASSWORD
     */
    fun load(): DatabaseConfig {
        val url =
            Env.get("DB_URL")
                ?: buildUrlFromParts(
                    host = Env.getRequired("DB_HOST"),
                    port = Env.get("DB_PORT")?.toIntOrNull() ?: 3306,
                    dbName = Env.getRequired("DB_NAME"),
                )

        val user = Env.getRequired("DB_USER")
        val password = Env.getRequired("DB_PASSWORD")

        return DatabaseConfig(
            jdbcUrl = url,
            username = user,
            password = password,
        )
    }

    fun tryLoad(): DatabaseConfig? =
        runCatching { load() }.getOrNull()

    private fun buildUrlFromParts(host: String, port: Int, dbName: String): String {
        // Parametros minimos. Se pueden agregar mas adelante si se requiere (timezone, SSL, etc.).
        return "jdbc:mariadb://$host:$port/$dbName"
    }
}


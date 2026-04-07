package com.resdev.akrecepcion.recepcionui.db

import java.sql.Connection

object DbHealthcheck {
    /**
     * Intenta ejecutar un SELECT 1 usando el pool.
     *
     * No hace throw por defecto: regresa Result para que la UI decida si muestra un banner,
     * fallback a "modo demo", etc.
     */
    fun ping(queryTimeoutSeconds: Int = 2): Result<Unit> {
        val ds = DataSourceProvider.getOrNull()
            ?: return Result.failure(IllegalStateException("DB no configurada: define DB_URL/DB_HOST/DB_NAME/DB_USER/DB_PASSWORD"))

        return runCatching {
            ds.connection.use { conn ->
                validateConnection(conn, queryTimeoutSeconds)
            }
        }
    }

    private fun validateConnection(conn: Connection, queryTimeoutSeconds: Int) {
        conn.createStatement().use { st ->
            st.queryTimeout = queryTimeoutSeconds
            st.executeQuery("SELECT 1").use { rs ->
                if (!rs.next()) error("Healthcheck inesperado: SELECT 1 no retorno filas")
            }
        }
    }
}


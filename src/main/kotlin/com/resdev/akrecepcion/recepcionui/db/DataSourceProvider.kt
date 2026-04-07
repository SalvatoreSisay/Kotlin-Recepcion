package com.resdev.akrecepcion.recepcionui.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource

/**
 * Proveedor singleton del pool de conexiones (HikariCP).
 *
 * La app (UI) no debe crear conexiones directas; todo pasa por este pool.
 */
object DataSourceProvider {
    @Volatile
    private var dataSource: HikariDataSource? = null

    /**
     * Devuelve el DataSource si y solo si la configuracion existe. No lanza error si faltan variables.
     * Util para no "romper" la app mientras migramos pantallas a datos reales.
     */
    fun getOrNull(): HikariDataSource? {
        if (dataSource != null) return dataSource
        val cfg = DatabaseConfigLoader.tryLoad() ?: return null
        return getOrCreate(cfg)
    }

    /**
     * Devuelve el DataSource y falla con mensaje claro si falta configuracion.
     * Ideal cuando ya tengamos DAOs activos y queramos fallar rapido si no hay DB.
     */
    fun getRequired(): HikariDataSource {
        if (dataSource != null) return dataSource!!
        val cfg = DatabaseConfigLoader.load()
        return getOrCreate(cfg)
    }

    @Synchronized
    private fun getOrCreate(cfg: DatabaseConfig): HikariDataSource {
        dataSource?.let { return it }

        val hc = HikariConfig().apply {
            poolName = cfg.poolName
            jdbcUrl = cfg.jdbcUrl
            username = cfg.username
            password = cfg.password

            // Driver explicito para evitar autodeteccion inconsistente en entornos modularizados.
            driverClassName = "org.mariadb.jdbc.Driver"

            // Tuning seguro para app de escritorio (pocas conexiones concurrentes).
            maximumPoolSize = 6
            minimumIdle = 1
            connectionTimeout = 5_000 // ms: cuanto esperar por una conexion del pool
            validationTimeout = 2_500
            idleTimeout = 60_000
            maxLifetime = 30 * 60_000L

            // Evita "validaciones sorpresa" sin query especifica.
            connectionTestQuery = "SELECT 1"

            // MariaDB/MySQL suelen ir bien con auto-commit para operaciones simples CRUD.
            isAutoCommit = true
        }

        val ds = HikariDataSource(hc)
        dataSource = ds
        return ds
    }

    fun close() {
        val ds = dataSource ?: return
        dataSource = null
        ds.close()
    }
}


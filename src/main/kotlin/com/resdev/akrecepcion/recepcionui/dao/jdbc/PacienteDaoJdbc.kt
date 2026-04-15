package com.resdev.akrecepcion.recepcionui.dao.jdbc

import com.resdev.akrecepcion.recepcionui.dao.PacienteDao
import com.resdev.akrecepcion.recepcionui.dao.PacienteNuevo
import com.resdev.akrecepcion.recepcionui.db.DataSourceProvider
import java.sql.Statement
import java.sql.Types
import java.time.LocalDate

class PacienteDaoJdbc : PacienteDao {
    override fun insert(paciente: PacienteNuevo, quienRegistra: String?): Int {
        val ds = DataSourceProvider.getRequired()
        ds.connection.use { conn ->
            conn.prepareStatement(
                """
                INSERT INTO `01pacientesce`
                    (`nombre`, `fechanacimiento`, `sexo`, `dpi`, `telefono`, `etnia`, `estadocivil`, `ocupacion`,
                     `DIRECCION`, `direccionaviso`, `pais`, `departamento`, `municipio`, `canton`, `lugarnacimiento`,
                     `programa`, `igss`, `pacientealtoriesgo`,
                     `OBSERVACIONES`, `OBSERVA1`, `OBSERVA2`, `quienregistra`, `fecharegistro`)
                VALUES
                    (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """.trimIndent(),
                Statement.RETURN_GENERATED_KEYS,
            ).use { ps ->
                fun setVarchar(idx: Int, value: String?) {
                    val v = value?.trim().orEmpty()
                    if (v.isBlank()) ps.setNull(idx, Types.VARCHAR) else ps.setString(idx, v)
                }

                fun setDate(idx: Int, value: LocalDate?) {
                    if (value == null) ps.setNull(idx, Types.DATE) else ps.setDate(idx, java.sql.Date.valueOf(value))
                }

                setVarchar(1, paciente.nombre)
                setDate(2, paciente.fechaNacimiento)
                setVarchar(3, paciente.sexo)
                setVarchar(4, paciente.dpi)
                setVarchar(5, paciente.telefono)
                setVarchar(6, paciente.etnia)
                setVarchar(7, paciente.estadoCivil)
                setVarchar(8, paciente.ocupacion)
                setVarchar(9, paciente.direccion)
                setVarchar(10, paciente.direccionAviso)
                setVarchar(11, paciente.pais)
                setVarchar(12, paciente.departamento)
                setVarchar(13, paciente.municipio)
                setVarchar(14, paciente.canton)
                setVarchar(15, paciente.lugarNacimiento)
                setVarchar(16, paciente.programa)
                setVarchar(17, paciente.igss)
                setVarchar(18, paciente.pacienteAltoRiesgo)
                setVarchar(19, paciente.observaciones)
                setVarchar(20, paciente.observa1)
                setVarchar(21, paciente.observa2)
                setVarchar(22, quienRegistra)
                setDate(23, LocalDate.now())

                ps.executeUpdate()

                ps.generatedKeys.use { rs ->
                    if (rs.next()) return rs.getInt(1)
                }
            }
        }

        error("No se pudo obtener el ID generado para el paciente.")
    }
}

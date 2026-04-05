package com.resdev.akrecepcion.recepcionui

/**
 * Store demo en memoria para Paciente Recurrente.
 *
 * Hoy la app no está conectada a un backend en esta sección, así que este store
 * funciona como "fuente de verdad" para compartir estado entre la vista de listado
 * y la vista de edición.
 */
object PacienteRecurrenteStore {
    data class Medicamento(
        var nombre: String,
        var dosis: String,
        var frecuencia: String,
    )

    data class VisitaNota(
        val fecha: String,
        val tipo: String,
        val titulo: String,
        val detalle: String,
        val estado: String,
    )

    data class PacienteRecord(
        var nombre: String,
        val pacienteId: String,
        var ultimaVisita: String,
        var fechaNacimiento: String,
        var tipoSangre: String,
        var seguro: String,
        var telefono: String,
        var email: String,
        var direccion: String,
        var identidadVerificada: Boolean,
        var notaRapida: String,
        var alergias: MutableList<String>,
        var condicionesCronicas: MutableList<String>,
        var medicamentos: MutableList<Medicamento>,
        var visitasNotas: List<VisitaNota>,
    )

    private val records: MutableList<PacienteRecord> =
        mutableListOf(
            PacienteRecord(
                nombre = "Ricardo Casares",
                pacienteId = "AK-MED-8829-01",
                ultimaVisita = "12 oct, 2023",
                fechaNacimiento = "24/05/1958",
                tipoSangre = "O+",
                seguro = "BlueCross Shield",
                telefono = "+502 55 43 210 987",
                email = "r.casares@proveedor.com",
                direccion = "Zona 10, Guatemala",
                identidadVerificada = true,
                notaRapida = "Paciente solicito copia impresa de resultados (sept.).",
                alergias = mutableListOf("Penicilina", "Latex"),
                condicionesCronicas = mutableListOf("Hipertension cronica (controlada)", "Diabetes tipo 2 (monitoreo)"),
                medicamentos =
                    mutableListOf(
                        Medicamento(nombre = "Metformina", dosis = "500mg", frecuencia = "2 veces al dia (con comidas)"),
                        Medicamento(nombre = "Lisinopril", dosis = "10mg", frecuencia = "1 vez al dia (manana)"),
                    ),
                visitasNotas =
                    listOf(
                        VisitaNota(
                            fecha = "12 oct, 2023",
                            tipo = "Revision anual",
                            titulo = "Control cardiologico",
                            detalle = "Seguimiento. Indicaciones de dieta y ejercicio. Se solicitan labs.",
                            estado = "COMPLETADO",
                        ),
                        VisitaNota(
                            fecha = "04 sep, 2023",
                            tipo = "Consulta",
                            titulo = "Chequeo general",
                            detalle = "Paciente refiere fatiga leve. Se programo control.",
                            estado = "ARCHIVADO",
                        ),
                    ),
            ),
            PacienteRecord(
                nombre = "Elena Rodriguez",
                pacienteId = "AK-MED-1044-22",
                ultimaVisita = "06 ene, 2024",
                fechaNacimiento = "03/11/1989",
                tipoSangre = "A-",
                seguro = "Seguros Orion",
                telefono = "+502 41 11 902 114",
                email = "e.rodriguez@correo.com",
                direccion = "Mixco, Guatemala",
                identidadVerificada = true,
                notaRapida = "Prefiere atencion por la manana.",
                alergias = mutableListOf("Polen"),
                condicionesCronicas = mutableListOf("Asma leve intermitente"),
                medicamentos =
                    mutableListOf(
                        Medicamento(nombre = "Salbutamol", dosis = "2 puffs", frecuencia = "SOS"),
                    ),
                visitasNotas =
                    listOf(
                        VisitaNota(
                            fecha = "06 ene, 2024",
                            tipo = "Consulta",
                            titulo = "Control respiratorio",
                            detalle = "Asma estable. Se revisa uso de inhalador.",
                            estado = "COMPLETADO",
                        ),
                    ),
            ),
            PacienteRecord(
                nombre = "Marcus Chen",
                pacienteId = "AK-MED-3301-07",
                ultimaVisita = "19 feb, 2024",
                fechaNacimiento = "18/09/1976",
                tipoSangre = "B+",
                seguro = "Plan Familiar Vida",
                telefono = "+502 50 09 441 330",
                email = "m.chen@correo.com",
                direccion = "Villa Nueva, Guatemala",
                identidadVerificada = false,
                notaRapida = "Traer resultados de imagen anteriores.",
                alergias = mutableListOf(),
                condicionesCronicas = mutableListOf("Dolor lumbar recurrente", "Fisioterapia en seguimiento"),
                medicamentos =
                    mutableListOf(
                        Medicamento(nombre = "Ibuprofeno", dosis = "400mg", frecuencia = "cada 8h por 3 dias"),
                    ),
                visitasNotas =
                    listOf(
                        VisitaNota(
                            fecha = "19 feb, 2024",
                            tipo = "Consulta",
                            titulo = "Dolor lumbar",
                            detalle = "Se recomiendan ejercicios y control en 2 semanas.",
                            estado = "COMPLETADO",
                        ),
                    ),
            ),
        )

    fun all(): List<PacienteRecord> = records.toList()

    fun findById(pacienteId: String): PacienteRecord? =
        records.firstOrNull { it.pacienteId.equals(pacienteId.trim(), ignoreCase = true) }

    fun update(record: PacienteRecord) {
        val idx = records.indexOfFirst { it.pacienteId.equals(record.pacienteId, ignoreCase = true) }
        if (idx >= 0) records[idx] = record else records.add(record)
    }
}


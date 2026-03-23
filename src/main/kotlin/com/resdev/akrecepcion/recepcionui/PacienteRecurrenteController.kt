package com.resdev.akrecepcion.recepcionui

import javafx.fxml.FXML
import javafx.geometry.Insets
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.RadioButton
import javafx.scene.control.TextField
import javafx.scene.control.ToggleGroup
import javafx.scene.input.MouseEvent
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import javafx.scene.layout.VBox

class PacienteRecurrenteController {
    private data class PacienteModel(
        val nombre: String,
        val pacienteId: String,
        val ultimaVisita: String,
        val fechaNacimiento: String,
        val tipoSangre: String,
        val seguro: String,
        val telefono: String,
        val email: String,
        val direccion: String,
        val resumenClinico: List<String>,
        val identidadVerificada: Boolean,
        val notaRapida: String,
    )

    @FXML private lateinit var txtBuscar: TextField
    @FXML private lateinit var btnBuscar: Button
    @FXML private lateinit var lblEncontrados: Label
    @FXML private lateinit var chipIdentidad: Label
    @FXML private lateinit var resultsContainer: VBox

    // Acciones esenciales
    @FXML private lateinit var lblPacienteSelNombre: Label
    @FXML private lateinit var lblPacienteSelId: Label
    @FXML private lateinit var lblAccionEstado: Label
    @FXML private lateinit var btnCheckIn: Button
    @FXML private lateinit var btnSeguro: Button
    @FXML private lateinit var btnImprimir: Button
    @FXML private lateinit var btnHistorial: Button

    @FXML private lateinit var radioAgendar: RadioButton
    @FXML private lateinit var radioYaTiene: RadioButton
    @FXML private lateinit var btnContinuar: Button

    @FXML private lateinit var lblNotaRapida: Label
    @FXML private lateinit var btnAgregarNota: Button

    private val pacientes = listOf(
        PacienteModel(
            nombre = "Ricardo Casares",
            pacienteId = "AK-MED-8829-01",
            ultimaVisita = "12 oct, 2023",
            fechaNacimiento = "24/05/1958",
            tipoSangre = "O+",
            seguro = "BlueCross Shield",
            telefono = "+502 55 43 210 987",
            email = "r.casares@proveedor.com",
            direccion = "Zona 10, Guatemala",
            resumenClinico = listOf(
                "Hipertension cronica (controlada)",
                "Monitoreo de diabetes tipo 2",
                "Ultimo lab: Glucosa 110 mg/dL",
            ),
            identidadVerificada = true,
            notaRapida = "Paciente solicito copia impresa de resultados (sept.).",
        ),
        PacienteModel(
            nombre = "Elena Rodriguez",
            pacienteId = "AK-MED-1044-22",
            ultimaVisita = "06 ene, 2024",
            fechaNacimiento = "03/11/1989",
            tipoSangre = "A-",
            seguro = "Seguros Orion",
            telefono = "+502 41 11 902 114",
            email = "e.rodriguez@correo.com",
            direccion = "Mixco, Guatemala",
            resumenClinico = listOf(
                "Alergia estacional",
                "Asma leve intermitente",
                "Ultimo lab: Espirometria normal",
            ),
            identidadVerificada = true,
            notaRapida = "Prefiere atencion por la manana.",
        ),
        PacienteModel(
            nombre = "Marcus Chen",
            pacienteId = "AK-MED-3301-07",
            ultimaVisita = "19 feb, 2024",
            fechaNacimiento = "18/09/1976",
            tipoSangre = "B+",
            seguro = "Plan Familiar Vida",
            telefono = "+502 50 09 441 330",
            email = "m.chen@correo.com",
            direccion = "Villa Nueva, Guatemala",
            resumenClinico = listOf(
                "Dolor lumbar recurrente",
                "Fisioterapia en seguimiento",
                "Ultimo lab: Rayos X sin hallazgos",
            ),
            identidadVerificada = false,
            notaRapida = "Traer resultados de imagen anteriores.",
        ),
    )

    private var filtrados: List<PacienteModel> = pacientes
    private var seleccionado: PacienteModel? = null
    private var selectedCard: Node? = null

    @FXML
    private fun initialize() {
        val group = ToggleGroup()
        radioAgendar.toggleGroup = group
        radioYaTiene.toggleGroup = group
        radioAgendar.isSelected = true

        btnBuscar.setOnAction { aplicarFiltro() }
        txtBuscar.setOnAction { aplicarFiltro() }
        btnContinuar.setOnAction { mostrarMensajeContinuar() }
        btnAgregarNota.setOnAction { agregarNotaDemo() }
        btnCheckIn.setOnAction { accion("Registrar llegada") }
        btnSeguro.setOnAction { accion("Validar seguro") }
        btnImprimir.setOnAction { accion("Imprimir ficha") }
        btnHistorial.setOnAction { accion("Ver historial") }

        renderCards(pacientes)
        seleccionar(pacientes.first())
    }

    private fun aplicarFiltro() {
        val q = txtBuscar.text?.trim()?.lowercase().orEmpty()
        filtrados =
            if (q.isBlank()) pacientes
            else pacientes.filter {
                it.nombre.lowercase().contains(q) ||
                    it.pacienteId.lowercase().contains(q) ||
                    it.fechaNacimiento.lowercase().contains(q)
            }

        renderCards(filtrados)
        if (filtrados.isNotEmpty()) seleccionar(filtrados.first()) else limpiarSeleccion()
    }

    private fun renderCards(items: List<PacienteModel>) {
        resultsContainer.children.clear()
        lblEncontrados.text = "Se encontraron ${items.size} registros"

        items.forEach { p ->
            resultsContainer.children.add(createPacienteCard(p))
        }
    }

    private fun createPacienteCard(p: PacienteModel): VBox {
        val avatar = Region().apply {
            styleClass.add("rp-avatar-shape")
            minWidth = 46.0
            minHeight = 46.0
            maxWidth = 46.0
            maxHeight = 46.0
        }

        val title = Label(p.nombre).apply { styleClass.add("rp-card-title-text-state-primary") }
        val subtitle = Label("ID Paciente: ${p.pacienteId}").apply { styleClass.add("muted") }

        val lastVisit = Label("ULTIMA VEZ QUE VINO\n${p.ultimaVisita}").apply {
            styleClass.add("rp-card-meta-text-state-primary")
        }

        val header = HBox(12.0).apply {
            alignmentProperty().set(javafx.geometry.Pos.CENTER_LEFT)
            children.addAll(
                avatar,
                VBox(2.0).apply { children.addAll(title, subtitle) }.also { HBox.setHgrow(it, Priority.ALWAYS) },
                lastVisit,
            )
        }

        fun chip(label: String, value: String): VBox =
            VBox(2.0).apply {
                styleClass.add("rp-chip-card")
                padding = Insets(8.0, 10.0, 8.0, 10.0)
                children.addAll(
                    Label(label).apply { styleClass.add("rp-chip-label-text-state-muted") },
                    Label(value).apply { styleClass.add("rp-chip-value-text-state-primary") },
                )
            }

        val chips = HBox(10.0).apply {
            children.addAll(
                chip("NACIMIENTO", p.fechaNacimiento),
                chip("SANGRE", p.tipoSangre),
                chip("SEGURO", p.seguro),
            )
        }

        val leftList = VBox(6.0).apply {
            children.add(Label("RESUMEN CLINICO").apply { styleClass.add("rp-subtitle-text-state-accent") })
            children.addAll(p.resumenClinico.map { Label("• $it").apply { styleClass.add("rp-list-text-state-primary") } })
        }

        val rightList = VBox(6.0).apply {
            children.add(Label("CONTACTO PRINCIPAL").apply { styleClass.add("rp-subtitle-text-state-accent") })
            children.addAll(
                Label(p.telefono).apply { styleClass.add("rp-list-text-state-primary") },
                Label(p.email).apply { styleClass.add("rp-list-text-state-primary") },
                Label(p.direccion).apply { styleClass.add("rp-list-text-state-primary") },
            )
        }

        val body = HBox(18.0).apply {
            children.addAll(leftList, rightList)
            HBox.setHgrow(leftList, Priority.ALWAYS)
            HBox.setHgrow(rightList, Priority.ALWAYS)
        }

        val card = VBox(12.0).apply {
            styleClass.add("rp-patient-card")
            padding = Insets(14.0, 14.0, 14.0, 14.0)
            children.addAll(header, chips, Region().apply { minHeight = 2.0 }, body)
            userData = p
            addEventHandler(MouseEvent.MOUSE_CLICKED) { seleccionar(p) }
        }

        return card
    }

    private fun seleccionar(p: PacienteModel) {
        seleccionado = p
        chipIdentidad.text = if (p.identidadVerificada) "IDENTIDAD VERIFICADA" else "IDENTIDAD PENDIENTE"
        chipIdentidad.styleClass.remove("rp-chip-identity-tone-ok")
        chipIdentidad.styleClass.remove("rp-chip-identity-tone-warn")
        chipIdentidad.styleClass.add(if (p.identidadVerificada) "rp-chip-identity-tone-ok" else "rp-chip-identity-tone-warn")

        lblPacienteSelNombre.text = p.nombre
        lblPacienteSelId.text = "ID Paciente: ${p.pacienteId}"
        lblAccionEstado.text = ""
        lblNotaRapida.text = p.notaRapida
        setAccionesEnabled(true)

        // resaltar card seleccionada
        selectedCard?.styleClass?.remove("rp-patient-card-state-selected")
        val newSelected = resultsContainer.children.firstOrNull { (it.userData as? PacienteModel) == p }
        newSelected?.styleClass?.add("rp-patient-card-state-selected")
        selectedCard = newSelected
    }

    private fun limpiarSeleccion() {
        seleccionado = null
        chipIdentidad.text = ""
        lblPacienteSelNombre.text = "-"
        lblPacienteSelId.text = "-"
        lblAccionEstado.text = "No hay paciente seleccionado."
        lblNotaRapida.text = "No hay notas."
        setAccionesEnabled(false)
    }

    private fun mostrarMensajeContinuar() {
        val p = seleccionado ?: return
        val paso = if (radioAgendar.isSelected) "Agendar cita" else "Ya tiene cita"
        lblNotaRapida.text = "Accion: $paso para ${p.nombre} (demo)."
    }

    private fun agregarNotaDemo() {
        val p = seleccionado ?: return
        lblNotaRapida.text = "Nota agregada para ${p.nombre} (demo)."
    }

    private fun accion(nombreAccion: String) {
        val p = seleccionado ?: return
        lblAccionEstado.text = "$nombreAccion: ${p.nombre} (demo)."
    }

    private fun setAccionesEnabled(enabled: Boolean) {
        btnCheckIn.isDisable = !enabled
        btnSeguro.isDisable = !enabled
        btnImprimir.isDisable = !enabled
        btnHistorial.isDisable = !enabled
        btnContinuar.isDisable = !enabled
        btnAgregarNota.isDisable = !enabled
        radioAgendar.isDisable = !enabled
        radioYaTiene.isDisable = !enabled
    }
}

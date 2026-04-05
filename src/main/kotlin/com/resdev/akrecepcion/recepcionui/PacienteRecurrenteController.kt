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
    var onEditarPaciente: ((pacienteId: String) -> Unit)? = null

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

    private var filtrados: List<PacienteRecurrenteStore.PacienteRecord> = PacienteRecurrenteStore.all()
    private var seleccionado: PacienteRecurrenteStore.PacienteRecord? = null
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

        renderCards(PacienteRecurrenteStore.all())
        PacienteRecurrenteStore.all().firstOrNull()?.let { seleccionar(it) } ?: limpiarSeleccion()
    }

    /**
     * Punto de entrada para navegación desde otras vistas (por ejemplo, “Actividad de hoy”).
     * Intenta localizar por `pacienteId` (exacto) y luego por `nombre`.
     */
    fun openPaciente(pacienteId: String?, nombre: String?) {
        lblAccionEstado.text = ""
        val id = pacienteId?.trim().orEmpty()
        val n = nombre?.trim().orEmpty()

        fun tryOpen(query: String, preferExactId: Boolean) {
            txtBuscar.text = query
            aplicarFiltro()
            if (filtrados.isEmpty()) return

            val preferred =
                if (preferExactId) filtrados.firstOrNull { it.pacienteId.equals(id, ignoreCase = true) }
                else filtrados.firstOrNull { it.nombre.equals(n, ignoreCase = true) }
            if (preferred != null) seleccionar(preferred)
        }

        if (id.isNotBlank()) {
            tryOpen(id, preferExactId = true)
            if (seleccionado != null) return
        }

        if (n.isNotBlank()) {
            tryOpen(n, preferExactId = false)
            if (seleccionado != null) return
        }

        lblAccionEstado.text = "No se encontró el paciente solicitado."
    }

    fun refresh() {
        // Re-render según el query actual, pero leyendo datos desde el store (por ejemplo, luego de editar).
        val keepId = seleccionado?.pacienteId
        aplicarFiltro()
        if (!keepId.isNullOrBlank()) {
            filtrados.firstOrNull { it.pacienteId.equals(keepId, ignoreCase = true) }?.let { seleccionar(it) }
        }
    }

    private fun aplicarFiltro() {
        val q = txtBuscar.text?.trim()?.lowercase().orEmpty()
        filtrados =
            if (q.isBlank()) PacienteRecurrenteStore.all()
            else PacienteRecurrenteStore.all().filter {
                it.nombre.lowercase().contains(q) ||
                    it.pacienteId.lowercase().contains(q) ||
                    it.fechaNacimiento.lowercase().contains(q)
            }

        renderCards(filtrados)
        if (filtrados.isNotEmpty()) seleccionar(filtrados.first()) else limpiarSeleccion()
    }

    private fun renderCards(items: List<PacienteRecurrenteStore.PacienteRecord>) {
        resultsContainer.children.clear()
        lblEncontrados.text = "Se encontraron ${items.size} registros"

        items.forEach { p ->
            resultsContainer.children.add(createPacienteCard(p))
        }
    }

    private fun createPacienteCard(p: PacienteRecurrenteStore.PacienteRecord): VBox {
        val avatar = Region().apply {
            styleClass.add("rp-avatar-shape")
            minWidth = 46.0
            minHeight = 46.0
            maxWidth = 46.0
            maxHeight = 46.0
        }

        val title = Label(p.nombre).apply { styleClass.add("rp-card-title-text-state-primary") }
        val subtitle = Label("ID Paciente: ${p.pacienteId}").apply { styleClass.add("muted") }

        val lastVisit = Label("ULTIMA VEZ QUE VINO\n${p.ultimaVisita}").apply { styleClass.add("rp-card-meta-text-state-primary") }

        val btnEditar = Button("Editar").apply {
            styleClass.add("rpe-card-edit-button")
            setOnAction {
                seleccionar(p)
                onEditarPaciente?.invoke(p.pacienteId)
            }
        }

        val metaBox = VBox(6.0).apply {
            children.addAll(lastVisit, btnEditar)
        }

        val header = HBox(12.0).apply {
            alignmentProperty().set(javafx.geometry.Pos.CENTER_LEFT)
            children.addAll(
                avatar,
                VBox(2.0).apply { children.addAll(title, subtitle) }.also { HBox.setHgrow(it, Priority.ALWAYS) },
                metaBox,
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
            children.addAll(
                p.condicionesCronicas.take(3).map { Label("• $it").apply { styleClass.add("rp-list-text-state-primary") } },
            )
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

    private fun seleccionar(p: PacienteRecurrenteStore.PacienteRecord) {
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
        val newSelected = resultsContainer.children.firstOrNull { (it.userData as? PacienteRecurrenteStore.PacienteRecord) == p }
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

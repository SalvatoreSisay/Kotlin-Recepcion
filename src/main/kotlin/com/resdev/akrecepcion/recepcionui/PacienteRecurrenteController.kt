package com.resdev.akrecepcion.recepcionui

import com.resdev.akrecepcion.recepcionui.dao.jdbc.ConsultaExternaDaoJdbc
import com.resdev.akrecepcion.recepcionui.dao.jdbc.PacienteRecurrenteDaoJdbc
import com.resdev.akrecepcion.recepcionui.repository.PacienteRecurrenteCard
import com.resdev.akrecepcion.recepcionui.repository.PacienteRecurrenteRepository
import com.resdev.akrecepcion.recepcionui.repository.impl.PacienteRecurrenteRepositoryImpl
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.javafx.JavaFx
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

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

    private val repo: PacienteRecurrenteRepository =
        PacienteRecurrenteRepositoryImpl(
            pacienteDao = PacienteRecurrenteDaoJdbc(),
            consultaDao = ConsultaExternaDaoJdbc(),
        )

    private val scope = CoroutineScope(Dispatchers.JavaFx + SupervisorJob())
    private var fetchJob: Job? = null

    private val esLocale = Locale("es", "GT")
    private val fmtVisit = DateTimeFormatter.ofPattern("dd MMM, yyyy", esLocale)
    private val fmtNac = DateTimeFormatter.ofPattern("dd/MM/yyyy", esLocale)

    private var filtrados: List<PacienteRecurrenteCard> = emptyList()
    private var seleccionado: PacienteRecurrenteCard? = null
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

        onViewShown()
    }

    /**
     * Se llama al entrar a la vista: carga los últimos 3 registrados.
     * No hay polling; solo se vuelve a cargar al re-entrar o al usar búsqueda explícita.
     */
    fun onViewShown() {
        txtBuscar.clear()
        lblAccionEstado.text = ""
        loadLatest()
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
                if (preferExactId) filtrados.firstOrNull { it.dpi.equals(id, ignoreCase = true) || it.codigoUsuario.toString() == id }
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
        // Re-render según el query actual (si hay).
        val keep = seleccionado?.codigoUsuario
        aplicarFiltro()
        if (keep != null) {
            filtrados.firstOrNull { it.codigoUsuario == keep }?.let { seleccionar(it) }
        }
    }

    private fun aplicarFiltro() {
        val q = txtBuscar.text?.trim().orEmpty()
        if (q.isBlank()) {
            loadLatest()
        } else {
            search(q)
        }
    }

    private fun loadLatest() {
        fetchJob?.cancel()
        fetchJob =
            scope.launch {
                try {
                    val items =
                        withContext(Dispatchers.IO) {
                            repo.latestCards(limit = 3)
                        }
                    applyResults(items)
                } catch (e: Exception) {
                    lblAccionEstado.text = "Error consultando pacientes: ${e.message ?: e::class.simpleName}"
                    applyResults(emptyList())
                }
            }
    }

    private fun search(q: String) {
        fetchJob?.cancel()
        fetchJob =
            scope.launch {
                try {
                    val items =
                        withContext(Dispatchers.IO) {
                            repo.searchCards(query = q, limit = 3)
                        }
                    applyResults(items)
                } catch (e: Exception) {
                    lblAccionEstado.text = "Error consultando pacientes: ${e.message ?: e::class.simpleName}"
                    applyResults(emptyList())
                }
            }
    }

    private fun applyResults(items: List<PacienteRecurrenteCard>) {
        filtrados = items
        renderCards(items)
        if (items.isNotEmpty()) seleccionar(items.first()) else limpiarSeleccion()
    }

    private fun renderCards(items: List<PacienteRecurrenteCard>) {
        resultsContainer.children.clear()
        lblEncontrados.text = "Se encontraron ${items.size} registros"

        items.forEach { p ->
            resultsContainer.children.add(createPacienteCard(p))
        }
    }

    private fun createPacienteCard(p: PacienteRecurrenteCard): VBox {
        val avatar = Region().apply {
            styleClass.add("rp-avatar-shape")
            minWidth = 46.0
            minHeight = 46.0
            maxWidth = 46.0
            maxHeight = 46.0
        }

        val title = Label(p.nombre).apply { styleClass.add("rp-card-title-text-state-primary") }
        val subtitle = Label("DPI: ${p.dpi.ifBlank { "-" }}").apply { styleClass.add("muted") }

        val lastVisit = Label("ULTIMA VEZ QUE VINO\n${ultimaVisitaLabel(p.fechaRegistro)}").apply { styleClass.add("rp-card-meta-text-state-primary") }

        val btnEditar = Button("Editar").apply {
            styleClass.add("rpe-card-edit-button")
            setOnAction {
                seleccionar(p)
                lblAccionEstado.text = "Edición pendiente para registros reales."
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
                chip("NACIMIENTO", nacimientoLabel(p.fechaNacimiento)),
                chip("IMC", imcLabel(p.imc)),
                chip("PROGRAMA", p.programa?.trim().orEmpty().ifBlank { "-" }),
            )
        }

        val leftList = VBox(6.0).apply {
            children.add(Label("RESUMEN CLINICO").apply { styleClass.add("rp-subtitle-text-state-accent") })
            children.addAll(
                resumenClinicoLines(p.historiaEnfermedad).map { Label("• $it").apply { styleClass.add("rp-list-text-state-primary") } },
            )
        }

        val rightList = VBox(6.0).apply {
            children.add(Label("CONTACTO PRINCIPAL").apply { styleClass.add("rp-subtitle-text-state-accent") })
            children.addAll(
                Label(p.telefono?.trim().orEmpty().ifBlank { "-" }).apply { styleClass.add("rp-list-text-state-primary") },
                Label(p.email?.trim().orEmpty().ifBlank { "-" }).apply { styleClass.add("rp-list-text-state-primary") },
                Label(p.direccion?.trim().orEmpty().ifBlank { "-" }).apply { styleClass.add("rp-list-text-state-primary") },
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

    private fun seleccionar(p: PacienteRecurrenteCard) {
        seleccionado = p
        val verificada = p.dpi.isNotBlank()
        chipIdentidad.text = if (verificada) "REGISTRADO" else "DPI PENDIENTE"
        chipIdentidad.styleClass.remove("rp-chip-identity-tone-ok")
        chipIdentidad.styleClass.remove("rp-chip-identity-tone-warn")
        chipIdentidad.styleClass.add(if (verificada) "rp-chip-identity-tone-ok" else "rp-chip-identity-tone-warn")

        lblPacienteSelNombre.text = p.nombre
        lblPacienteSelId.text =
            buildString {
                append("DPI: ")
                append(p.dpi.ifBlank { "-" })
                append(" · Código: ")
                append(p.codigoUsuario)
            }
        lblAccionEstado.text = ""
        lblNotaRapida.text = "No hay notas."
        setAccionesEnabled(true)

        // resaltar card seleccionada
        selectedCard?.styleClass?.remove("rp-patient-card-state-selected")
        val newSelected = resultsContainer.children.firstOrNull { (it.userData as? PacienteRecurrenteCard) == p }
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
        lblNotaRapida.text = "Accion: $paso para ${p.nombre}."
    }

    private fun agregarNotaDemo() {
        val p = seleccionado ?: return
        lblNotaRapida.text = "Notas de recepción pendientes de implementar."
    }

    private fun accion(nombreAccion: String) {
        val p = seleccionado ?: return
        lblAccionEstado.text = "$nombreAccion: ${p.nombre}."
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

    private fun ultimaVisitaLabel(fechaRegistro: LocalDate?): String {
        val fr = fechaRegistro ?: return "-"
        val today = LocalDate.now()
        if (fr == today) return "Hoy"
        return fmtVisit.format(fr).lowercase(esLocale)
    }

    private fun nacimientoLabel(fn: LocalDate?): String =
        fn?.let { fmtNac.format(it) } ?: "-"

    private fun imcLabel(imc: Int?): String =
        imc?.toString() ?: "-"

    private fun resumenClinicoLines(historia: String?): List<String> {
        val raw = historia?.trim().orEmpty()
        if (raw.isBlank()) return listOf("Sin resumen clínico")

        // Divide de manera tolerante: líneas, punto y coma o punto.
        val parts =
            raw.split("\n", ";", ".")
                .map { it.trim() }
                .filter { it.isNotBlank() }

        return (if (parts.isEmpty()) listOf(raw) else parts).take(3)
    }
}

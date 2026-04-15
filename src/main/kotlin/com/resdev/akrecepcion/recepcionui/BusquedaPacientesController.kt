package com.resdev.akrecepcion.recepcionui

import com.resdev.akrecepcion.recepcionui.dao.jdbc.PacienteBusquedaDaoJdbc
import com.resdev.akrecepcion.recepcionui.model.Paciente
import com.resdev.akrecepcion.recepcionui.repository.PacienteRepository
import com.resdev.akrecepcion.recepcionui.repository.impl.PacienteRepositoryImpl
import javafx.animation.FadeTransition
import javafx.animation.ParallelTransition
import javafx.animation.PauseTransition
import javafx.animation.TranslateTransition
import javafx.fxml.FXML
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.control.Tooltip
import javafx.scene.input.KeyCode
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import javafx.util.Duration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.javafx.JavaFx
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
import java.util.Locale

class BusquedaPacientesController {
    private enum class EstadoFiltro(val label: String) {
        TODOS("Todos los estados"),
        ACTIVO("Activo"),
    }

    private enum class RangoFiltro(val label: String) {
        H24("Últimas 24 horas"),
        D7("Últimos 7 días"),
        D30("Últimos 30 días"),
    }

    private data class PacienteRow(
        val nombre: String,
        val pacienteId: String,
        val edad: String,
        val sexo: String,
        val telefono: String,
        val ultimaVisita: String,
        val estado: EstadoFiltro,
    )

    @FXML private lateinit var txtBuscar: TextField
    @FXML private lateinit var btnBuscar: Button
    @FXML private lateinit var btnEstado: Button
    @FXML private lateinit var btnDepartamento: Button
    @FXML private lateinit var btnRango: Button
    @FXML private lateinit var lblMostrando: Label
    @FXML private lateinit var resultsContainer: VBox
    @FXML private lateinit var lblPagina: Label
    @FXML private lateinit var paginationContainer: HBox

    private var estadoFiltro: EstadoFiltro = EstadoFiltro.TODOS
    private var rangoFiltro: RangoFiltro = RangoFiltro.D30

    private val pageSize = 6
    private var currentPage = 1

    private val debounce = PauseTransition(Duration.millis(160.0))
    private var runningSwap: ParallelTransition? = null
    private var fetchJob: Job? = null

    private val repo: PacienteRepository = PacienteRepositoryImpl(PacienteBusquedaDaoJdbc())
    private val scope = CoroutineScope(Dispatchers.JavaFx + SupervisorJob())

    private val esLocale = Locale("es", "GT")
    private val fmtFecha = DateTimeFormatter.ofPattern("dd MMM, yyyy", esLocale)

    @FXML
    private fun initialize() {
        btnRango.text = rangoFiltro.label
        btnBuscar.setOnAction { aplicarFiltro(immediate = true) }
        txtBuscar.setOnAction { aplicarFiltro(immediate = true) }
        txtBuscar.textProperty().addListener { _, _, _ -> scheduleFiltro() }
        txtBuscar.setOnKeyPressed { e ->
            if (e.code == KeyCode.ESCAPE) {
                txtBuscar.clear()
                aplicarFiltro(immediate = true)
            }
        }

        btnEstado.setOnAction { cycleEstado() }
        btnDepartamento.isDisable = true
        btnRango.setOnAction { cycleRango() }

        Tooltip.install(btnEstado, Tooltip("Filtra por estado del paciente"))
        Tooltip.install(btnDepartamento, Tooltip("Filtro pendiente de definición"))
        Tooltip.install(btnRango, Tooltip("Acota la búsqueda por recencia"))

        debounce.setOnFinished { aplicarFiltro(immediate = false) }
        aplicarFiltro(immediate = true, firstRender = true)
    }

    private fun scheduleFiltro() {
        debounce.stop()
        currentPage = 1
        debounce.playFromStart()
    }

    private fun cycleEstado() {
        estadoFiltro = EstadoFiltro.entries[(estadoFiltro.ordinal + 1) % EstadoFiltro.entries.size]
        btnEstado.text = estadoFiltro.label
        currentPage = 1
        aplicarFiltro(immediate = true)
    }

    private fun cycleRango() {
        rangoFiltro = RangoFiltro.entries[(rangoFiltro.ordinal + 1) % RangoFiltro.entries.size]
        btnRango.text = rangoFiltro.label
        currentPage = 1
        aplicarFiltro(immediate = true)
    }

    private fun aplicarFiltro(immediate: Boolean, firstRender: Boolean = false) {
        val query = txtBuscar.text?.trim().orEmpty()
        // Si hay texto de búsqueda, se busca en todos los registros.
        // Si no hay texto, se aplica el rango seleccionado (por defecto: últimos 30 días).
        val desde = if (query.isBlank()) cutoffDesde(rangoFiltro) else null
        val requestedPage = currentPage

        fetchJob?.cancel()
        fetchJob =
            scope.launch {
                try {
                    if (immediate) lblMostrando.text = "Buscando..."

                    val page =
                        withContext(Dispatchers.IO) {
                            repo.search(
                                query = query,
                                desde = desde,
                                page = currentPage,
                                pageSize = pageSize,
                            )
                        }

                    val total = page.total
                    val totalPages = maxOf(1, ((total + pageSize - 1) / pageSize))
                    currentPage = currentPage.coerceIn(1, totalPages)

                    val finalPage =
                        if (currentPage != requestedPage) {
                            withContext(Dispatchers.IO) {
                                repo.search(
                                    query = query,
                                    desde = desde,
                                    page = currentPage,
                                    pageSize = pageSize,
                                )
                            }
                        } else {
                            page
                        }

                    val rows = finalPage.items.map { toRow(it) }

                    lblMostrando.text = "Mostrando ${finalPage.total} resultados"
                    lblPagina.text = "Página $currentPage de $totalPages"
                    renderPagination(totalPages)

                    swapRows(rows, animate = !firstRender)
                } catch (_: Exception) {
                    lblMostrando.text = "Error consultando pacientes."
                    lblPagina.text = "Página 1 de 1"
                    paginationContainer.children.clear()
                    swapRows(emptyList(), animate = !firstRender)
                }
            }
    }

    private fun cutoffDesde(rango: RangoFiltro): LocalDate {
        val today = LocalDate.now()
        return when (rango) {
            RangoFiltro.H24 -> today.minusDays(1)
            RangoFiltro.D7 -> today.minusDays(7)
            RangoFiltro.D30 -> today.minusDays(30)
        }
    }

    private fun toRow(p: Paciente): PacienteRow {
        val nombre = p.nombre?.trim().orEmpty().ifBlank { "-" }
        val dpi = p.dpi?.trim().orEmpty().ifBlank { "-" }
        val sexo = sexoLabel(p.sexo)
        val edad = edadLabel(p.fechaNacimiento)
        val telefono = p.telefono?.trim().orEmpty().ifBlank { "-" }
        val ultima = ultimaVisitaLabel(p.fechaRegistro)

        return PacienteRow(
            nombre = nombre,
            pacienteId = dpi,
            edad = edad,
            sexo = sexo,
            telefono = telefono,
            ultimaVisita = ultima,
            estado = EstadoFiltro.ACTIVO, // Por ahora: cualquier registro encontrado es "Activo".
        )
    }

    private fun edadLabel(fechaNacimiento: LocalDate?): String {
        val fn = fechaNacimiento ?: return "-"
        val today = LocalDate.now()
        if (fn.isAfter(today)) return "-"
        val years = Period.between(fn, today).years
        return years.toString()
    }

    private fun sexoLabel(sexo: String?): String =
        when (sexo?.trim()?.uppercase()) {
            "M" -> "Masculino"
            "F" -> "Femenino"
            else -> "-"
        }

    private fun ultimaVisitaLabel(fechaRegistro: LocalDate?): String {
        val fr = fechaRegistro ?: return "-"
        val today = LocalDate.now()
        if (fr == today) return "Hoy"
        return fmtFecha.format(fr).lowercase(esLocale)
    }

    private fun renderPagination(totalPages: Int) {
        paginationContainer.children.clear()

        fun pill(text: String, selected: Boolean, onClick: () -> Unit): Button =
            Button(text).apply {
                styleClass.add("ps-page-pill")
                if (selected) styleClass.add("ps-page-pill-state-selected")
                setOnAction {
                    onClick()
                }
            }

        val prev = pill("‹", selected = false) {
            currentPage = (currentPage - 1).coerceAtLeast(1)
            aplicarFiltro(immediate = true)
        }.apply { isDisable = currentPage <= 1 }

        val next = pill("›", selected = false) {
            currentPage = (currentPage + 1).coerceAtMost(totalPages)
            aplicarFiltro(immediate = true)
        }.apply { isDisable = currentPage >= totalPages }

        paginationContainer.children.add(prev)
        (1..totalPages.coerceAtMost(4)).forEach { p ->
            paginationContainer.children.add(
                pill(p.toString(), selected = p == currentPage) {
                    currentPage = p
                    aplicarFiltro(immediate = true)
                },
            )
        }
        if (totalPages > 4) {
            paginationContainer.children.add(Label("…").apply { styleClass.add("muted") })
            paginationContainer.children.add(
                pill(totalPages.toString(), selected = totalPages == currentPage) {
                    currentPage = totalPages
                    aplicarFiltro(immediate = true)
                },
            )
        }
        paginationContainer.children.add(next)
    }

    private fun swapRows(items: List<PacienteRow>, animate: Boolean) {
        runningSwap?.stop()
        runningSwap = null

        val newNodes = items.map { createRow(it) }

        if (!animate || resultsContainer.children.isEmpty()) {
            resultsContainer.children.setAll(newNodes)
            if (animate) animateIn(newNodes)
            return
        }

        val out = ParallelTransition().apply {
            children.addAll(resultsContainer.children.map { fadeSlide(it, out = true) })
            setOnFinished {
                resultsContainer.children.setAll(newNodes)
                animateIn(newNodes)
            }
        }
        runningSwap = out
        out.play()
    }

    private fun animateIn(nodes: List<Node>) {
        val inAnim = ParallelTransition().apply {
            children.addAll(nodes.map { fadeSlide(it, out = false) })
        }
        runningSwap = inAnim
        inAnim.play()
    }

    private fun fadeSlide(node: Node, out: Boolean): ParallelTransition {
        val fade = FadeTransition(Duration.millis(170.0), node).apply {
            if (out) {
                fromValue = node.opacity
                toValue = 0.0
            } else {
                node.opacity = 0.0
                fromValue = 0.0
                toValue = 1.0
            }
        }
        val slide = TranslateTransition(Duration.millis(170.0), node).apply {
            if (out) {
                fromY = 0.0
                toY = -6.0
            } else {
                node.translateY = 8.0
                fromY = 8.0
                toY = 0.0
            }
        }
        return ParallelTransition(fade, slide)
    }

    private fun createRow(p: PacienteRow): HBox {
        val initials = p.nombre
            .split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .map { it.trim().first().uppercaseChar() }
            .joinToString("")

        val avatar = StackPane(Label(initials).apply { styleClass.add("ps-avatar-text") }).apply {
            styleClass.add("ps-avatar")
            minWidth = 40.0
            minHeight = 40.0
            maxWidth = 40.0
            maxHeight = 40.0
        }

        val patientBlock = HBox(12.0).apply {
            children.addAll(
                avatar,
                VBox(2.0).apply {
                    children.addAll(
                        Label(p.nombre).apply { styleClass.add("ps-patient-name") },
                        Label("ID: ${p.pacienteId}").apply { styleClass.add("muted") },
                    )
                },
            )
            HBox.setHgrow(this, Priority.ALWAYS)
        }

        val demo = VBox(2.0).apply {
            prefWidth = 160.0
            children.addAll(
                Label(if (p.edad == "-") "-" else "${p.edad} años").apply { styleClass.add("ps-cell-strong") },
                Label(p.sexo).apply { styleClass.add("muted") },
            )
        }

        val contact = VBox(2.0).apply {
            prefWidth = 220.0
            children.addAll(
                Label(p.telefono).apply { styleClass.add("ps-cell-strong") },
                Label("Última visita: ${p.ultimaVisita}").apply { styleClass.add("muted") },
            )
        }

        val statusChip = Label(p.estado.label).apply {
            styleClass.addAll("ps-status-chip", when (p.estado) {
                EstadoFiltro.ACTIVO -> "ps-status-chip-tone-ok"
                EstadoFiltro.TODOS -> "ps-status-chip-tone-muted"
            })
        }
        val statusCell = HBox(statusChip).apply {
            prefWidth = 150.0
            alignmentProperty().set(javafx.geometry.Pos.CENTER_LEFT)
        }

        val actions = HBox(8.0).apply {
            prefWidth = 120.0
            alignmentProperty().set(javafx.geometry.Pos.CENTER_RIGHT)
            children.addAll(
                Button("Ver").apply {
                    styleClass.add("ps-action-button")
                    setOnAction {
                        // Demo: no navegación real por ahora.
                        btnBuscar.requestFocus()
                    }
                },
            )
        }

        val row = HBox(0.0).apply {
            styleClass.add("ps-row")
            children.addAll(
                patientBlock,
                demo,
                contact,
                statusCell,
                actions,
            )
        }
        return row
    }
}

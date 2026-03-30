package com.resdev.akrecepcion.recepcionui

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
//import javafx.scene.layout.Region
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import javafx.util.Duration

class BusquedaPacientesController {
    private enum class EstadoFiltro(val label: String) {
        TODOS("Todos los estados"),
        ACTIVO("Activo"),
        OBSERVACION("En observación"),
        EGRESADO("Egresado"),
    }

    private enum class RangoFiltro(val label: String) {
        H24("Últimas 24 horas"),
        D7("Últimos 7 días"),
        D30("Últimos 30 días"),
    }

    private data class PacienteRow(
        val nombre: String,
        val pacienteId: String,
        val edad: Int,
        val sexo: String,
        val telefono: String,
        val ultimaVisita: String,
        val estado: EstadoFiltro,
        val departamento: String,
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

    private val pacientes = listOf(
        PacienteRow("Elena Rodríguez", "CS-88219", 34, "Femenino", "+1 (555) 012-984", "12 oct, 2023", EstadoFiltro.ACTIVO, "Pediatría"),
        PacienteRow("Marcus Kinsley", "CS-91102", 58, "Masculino", "+1 (555) 882-0122", "29 sept, 2023", EstadoFiltro.OBSERVACION, "Emergencias"),
        PacienteRow("Anita Wu", "CS-77420", 22, "Femenino", "+1 (555) 301-7721", "15 ago, 2023", EstadoFiltro.EGRESADO, "Laboratorio"),
        PacienteRow("Jonathan Thorne", "CS-82210", 45, "Masculino", "+1 (555) 443-1109", "Hoy", EstadoFiltro.ACTIVO, "Medicina interna"),
        PacienteRow("Ricardo Casares", "AK-8829-01", 66, "Masculino", "+502 55 43 210 987", "19 feb, 2024", EstadoFiltro.ACTIVO, "Medicina interna"),
        PacienteRow("Sarah Jenkins", "AK-1044-22", 36, "Femenino", "+502 41 11 902 114", "06 ene, 2024", EstadoFiltro.ACTIVO, "Pediatría"),
        PacienteRow("Marcus Chen", "AK-3301-07", 49, "Masculino", "+502 50 09 441 330", "12 nov, 2023", EstadoFiltro.OBSERVACION, "Emergencias"),
        PacienteRow("Lucía Arévalo", "AK-4411-09", 29, "Femenino", "+502 33 90 110 771", "01 dic, 2023", EstadoFiltro.EGRESADO, "Laboratorio"),
        PacienteRow("Diego Pérez", "AK-1201-03", 40, "Masculino", "+502 49 80 201 998", "10 oct, 2023", EstadoFiltro.ACTIVO, "Medicina interna"),
        PacienteRow("Andrea Gómez", "AK-8711-16", 51, "Femenino", "+502 22 10 902 441", "21 ene, 2024", EstadoFiltro.OBSERVACION, "Emergencias"),
        PacienteRow("Sofía Castillo", "AK-7731-12", 19, "Femenino", "+502 55 12 331 001", "15 feb, 2024", EstadoFiltro.ACTIVO, "Pediatría"),
        PacienteRow("Jorge López", "AK-3400-22", 62, "Masculino", "+502 55 09 113 222", "03 mar, 2024", EstadoFiltro.EGRESADO, "Medicina interna"),
        PacienteRow("Valeria Méndez", "AK-9011-55", 27, "Femenino", "+502 40 11 902 771", "18 dic, 2023", EstadoFiltro.ACTIVO, "Laboratorio"),
        PacienteRow("Pedro Aguilar", "AK-6622-18", 38, "Masculino", "+502 31 09 100 700", "07 ene, 2024", EstadoFiltro.ACTIVO, "Emergencias"),
    )

    private var estadoFiltro: EstadoFiltro = EstadoFiltro.TODOS
    private var departamentoFiltro: String? = null
    private var rangoFiltro: RangoFiltro = RangoFiltro.H24

    private val pageSize = 6
    private var currentPage = 1

    private val debounce = PauseTransition(Duration.millis(160.0))
    private var runningSwap: ParallelTransition? = null

    @FXML
    private fun initialize() {
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
        btnDepartamento.setOnAction { cycleDepartamento() }
        btnRango.setOnAction { cycleRango() }

        Tooltip.install(btnEstado, Tooltip("Filtra por estado del paciente"))
        Tooltip.install(btnDepartamento, Tooltip("Filtra por área/departamento"))
        Tooltip.install(btnRango, Tooltip("Acota la búsqueda por recencia"))

        debounce.setOnFinished { aplicarFiltro(immediate = false) }
        aplicarFiltro(immediate = true, firstRender = true)
    }

    private fun scheduleFiltro() {
        debounce.stop()
        debounce.playFromStart()
    }

    private fun cycleEstado() {
        estadoFiltro = EstadoFiltro.entries[(estadoFiltro.ordinal + 1) % EstadoFiltro.entries.size]
        btnEstado.text = estadoFiltro.label
        currentPage = 1
        aplicarFiltro(immediate = true)
    }

    private val departamentos = listOf("Todos", "Emergencias", "Pediatría", "Laboratorio", "Medicina interna")

    private fun cycleDepartamento() {
        val actual = departamentoFiltro ?: "Todos"
        val idx = departamentos.indexOf(actual).coerceAtLeast(0)
        val next = departamentos[(idx + 1) % departamentos.size]
        departamentoFiltro = if (next == "Todos") null else next
        btnDepartamento.text = departamentoFiltro ?: "Departamento"
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
        val q = txtBuscar.text?.trim()?.lowercase().orEmpty()

        val filtrados = pacientes
            .asSequence()
            .filter { p ->
                if (q.isBlank()) true
                else p.nombre.lowercase().contains(q) || p.pacienteId.lowercase().contains(q) || p.telefono.lowercase().contains(q)
            }
            .filter { p ->
                if (estadoFiltro == EstadoFiltro.TODOS) true else p.estado == estadoFiltro
            }
            .filter { p ->
                departamentoFiltro?.let { p.departamento == it } ?: true
            }
            // El rango es demo visual (la data no tiene fecha estructurada): se mantiene como selector sin afectar resultados.
            .toList()

        val total = filtrados.size
        val totalPages = maxOf(1, ((total + pageSize - 1) / pageSize))
        currentPage = currentPage.coerceIn(1, totalPages)

        val pageItems = filtrados.drop((currentPage - 1) * pageSize).take(pageSize)
        lblMostrando.text = "Mostrando $total resultados"
        lblPagina.text = "Página $currentPage de $totalPages"
        renderPagination(totalPages)

        if (firstRender || immediate) {
            swapRows(pageItems, animate = !firstRender)
        } else {
            swapRows(pageItems, animate = true)
        }
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
                Label("${p.edad} años").apply { styleClass.add("ps-cell-strong") },
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
                EstadoFiltro.OBSERVACION -> "ps-status-chip-tone-warn"
                EstadoFiltro.EGRESADO -> "ps-status-chip-tone-muted"
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

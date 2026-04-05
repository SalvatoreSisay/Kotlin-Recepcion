package com.resdev.akrecepcion.recepcionui

import javafx.collections.FXCollections
import javafx.fxml.FXML
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.chart.BarChart
import javafx.scene.chart.CategoryAxis
import javafx.scene.chart.PieChart
import javafx.scene.chart.XYChart
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.ProgressBar
import javafx.geometry.Side
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import kotlin.math.roundToInt

class ReportesController {
    private enum class Periodo(val label: String) {
        MENSUAL("Mensual"),
        TRIMESTRAL("Trimestral"),
        ANUAL("Anual"),
    }

    private data class ProcRow(
        val departamento: String,
        val procedimiento: String,
        val pacientes: Int,
    )

    private data class ReporteData(
        val ingresos: Int,
        val hombres: Int,
        val mujeres: Int,
        val nuevos: Int,
        val recurrentes: Int,
        val procedimientos: Int,
        val tiempoPromAtencion: String,
        val topDiagnosticos: List<Pair<String, Int>>,
        val topMedicos: List<Pair<String, Int>>,
        val enfermedadesRecientes: List<ProcRow>,
        val operacionesRecientes: List<ProcRow>,
    )

    @FXML private lateinit var btnMensual: Button
    @FXML private lateinit var btnTrimestral: Button
    @FXML private lateinit var btnAnual: Button

    @FXML private lateinit var lblIngresosValue: Label
    @FXML private lateinit var lblNuevosValue: Label
    @FXML private lateinit var lblRecurrentesValue: Label
    @FXML private lateinit var lblProcedimientosValue: Label

    @FXML private lateinit var lblDistribucionMeta: Label
    @FXML private lateinit var pieDistribucion: PieChart

    @FXML private lateinit var lblMedicosMeta: Label
    @FXML private lateinit var barMedicos: BarChart<String, Number>

    @FXML private lateinit var topDiagnosticosContainer: VBox
    @FXML private lateinit var lblDiagnosticosMeta: Label

    @FXML private lateinit var pbNuevos: ProgressBar
    @FXML private lateinit var pbRecurrentes: ProgressBar
    @FXML private lateinit var lblNuevosPct: Label
    @FXML private lateinit var lblRecurrentesPct: Label
    @FXML private lateinit var lblTiempoProm: Label
    @FXML private lateinit var lblDiagPrincipal: Label

    @FXML private lateinit var enfermedadesRowsContainer: VBox
    @FXML private lateinit var operacionesRowsContainer: VBox

    private var periodoActual: Periodo = Periodo.MENSUAL

    private val dataset: Map<Periodo, ReporteData> =
        mapOf(
            Periodo.MENSUAL to
                ReporteData(
                    ingresos = 284,
                    hombres = 131,
                    mujeres = 153,
                    nuevos = 96,
                    recurrentes = 188,
                    procedimientos = 412,
                    tiempoPromAtencion = "14 min",
                    topDiagnosticos =
                        listOf(
                            "Infección respiratoria aguda" to 64,
                            "Gastritis" to 41,
                            "Hipertensión" to 37,
                            "Control pediátrico" to 29,
                            "Diabetes tipo 2" to 24,
                        ),
                    topMedicos =
                        listOf(
                            "Dra. C. Morales" to 58,
                            "Dr. J. López" to 52,
                            "Dra. M. Castillo" to 44,
                            "Dr. A. Herrera" to 39,
                            "Dra. S. Pérez" to 33,
                            "Dr. D. Fuentes" to 29,
                        ),
                    enfermedadesRecientes =
                        listOf(
                            ProcRow("Emergencias", "Gripe / IRA", 18),
                            ProcRow("Medicina interna", "Hipertensión", 14),
                            ProcRow("Pediatría", "Control de niño sano", 12),
                            ProcRow("Laboratorio", "Hemograma", 11),
                            ProcRow("Ginecología", "Control prenatal", 9),
                        ),
                    operacionesRecientes =
                        listOf(
                            ProcRow("Cirugía", "Apendicectomía", 7),
                            ProcRow("Trauma", "Sutura de heridas", 11),
                            ProcRow("Ginecología", "Cesárea", 6),
                            ProcRow("Urología", "Cistoscopía", 4),
                            ProcRow("Ortopedia", "Reducción de fractura", 5),
                        ),
                ),
            Periodo.TRIMESTRAL to
                ReporteData(
                    ingresos = 781,
                    hombres = 372,
                    mujeres = 409,
                    nuevos = 241,
                    recurrentes = 540,
                    procedimientos = 1143,
                    tiempoPromAtencion = "16 min",
                    topDiagnosticos =
                        listOf(
                            "Infección respiratoria aguda" to 182,
                            "Gastritis" to 117,
                            "Hipertensión" to 106,
                            "Control pediátrico" to 90,
                            "Diabetes tipo 2" to 77,
                        ),
                    topMedicos =
                        listOf(
                            "Dr. J. López" to 156,
                            "Dra. C. Morales" to 149,
                            "Dra. M. Castillo" to 132,
                            "Dr. A. Herrera" to 121,
                            "Dra. S. Pérez" to 98,
                            "Dr. D. Fuentes" to 86,
                        ),
                    enfermedadesRecientes =
                        listOf(
                            ProcRow("Emergencias", "Gripe / IRA", 51),
                            ProcRow("Medicina interna", "Hipertensión", 43),
                            ProcRow("Pediatría", "Control de niño sano", 39),
                            ProcRow("Laboratorio", "Química sanguínea", 36),
                            ProcRow("Ginecología", "Control prenatal", 31),
                        ),
                    operacionesRecientes =
                        listOf(
                            ProcRow("Trauma", "Sutura de heridas", 32),
                            ProcRow("Cirugía", "Apendicectomía", 19),
                            ProcRow("Ortopedia", "Reducción de fractura", 17),
                            ProcRow("Ginecología", "Cesárea", 14),
                            ProcRow("Urología", "Cistoscopía", 9),
                        ),
                ),
            Periodo.ANUAL to
                ReporteData(
                    ingresos = 3124,
                    hombres = 1512,
                    mujeres = 1612,
                    nuevos = 982,
                    recurrentes = 2142,
                    procedimientos = 4921,
                    tiempoPromAtencion = "15 min",
                    topDiagnosticos =
                        listOf(
                            "Infección respiratoria aguda" to 724,
                            "Gastritis" to 503,
                            "Hipertensión" to 468,
                            "Control pediátrico" to 391,
                            "Diabetes tipo 2" to 335,
                        ),
                    topMedicos =
                        listOf(
                            "Dr. J. López" to 634,
                            "Dra. C. Morales" to 611,
                            "Dra. M. Castillo" to 552,
                            "Dr. A. Herrera" to 497,
                            "Dra. S. Pérez" to 451,
                            "Dr. D. Fuentes" to 408,
                        ),
                    enfermedadesRecientes =
                        listOf(
                            ProcRow("Emergencias", "Gripe / IRA", 208),
                            ProcRow("Medicina interna", "Hipertensión", 173),
                            ProcRow("Pediatría", "Control de niño sano", 161),
                            ProcRow("Laboratorio", "Hemograma", 149),
                            ProcRow("Ginecología", "Control prenatal", 137),
                        ),
                    operacionesRecientes =
                        listOf(
                            ProcRow("Trauma", "Sutura de heridas", 124),
                            ProcRow("Cirugía", "Apendicectomía", 91),
                            ProcRow("Ortopedia", "Reducción de fractura", 86),
                            ProcRow("Ginecología", "Cesárea", 71),
                            ProcRow("Urología", "Cistoscopía", 44),
                        ),
                ),
        )

    @FXML
    private fun initialize() {
        btnMensual.userData = Periodo.MENSUAL
        btnTrimestral.userData = Periodo.TRIMESTRAL
        btnAnual.userData = Periodo.ANUAL

        listOf(btnMensual, btnTrimestral, btnAnual).forEach { b ->
            b.setOnAction {
                val periodo = b.userData as? Periodo ?: return@setOnAction
                setPeriodo(periodo)
            }
        }

        pieDistribucion.labelsVisible = false
        pieDistribucion.legendSide = Side.RIGHT
        pieDistribucion.startAngle = 90.0
        pieDistribucion.data = FXCollections.observableArrayList()

        barMedicos.isLegendVisible = false
        barMedicos.animated = false
        barMedicos.barGap = 2.0
        barMedicos.categoryGap = 16.0
        (barMedicos.xAxis as? CategoryAxis)?.tickLabelRotation = -25.0

        setPeriodo(Periodo.MENSUAL)
    }

    fun refresh() {
        applyData(dataset[periodoActual] ?: return)
    }

    private fun setPeriodo(periodo: Periodo) {
        periodoActual = periodo
        updatePeriodoButtons()
        applyData(dataset[periodo] ?: return)
    }

    private fun updatePeriodoButtons() {
        fun update(btn: Button, periodo: Periodo) {
            btn.styleClass.remove("re-period-button-active")
            if (periodoActual == periodo && !btn.styleClass.contains("re-period-button-active")) {
                btn.styleClass.add("re-period-button-active")
            }
        }

        update(btnMensual, Periodo.MENSUAL)
        update(btnTrimestral, Periodo.TRIMESTRAL)
        update(btnAnual, Periodo.ANUAL)
    }

    private fun applyData(d: ReporteData) {
        lblIngresosValue.text = d.ingresos.toString()
        lblNuevosValue.text = d.nuevos.toString()
        lblRecurrentesValue.text = d.recurrentes.toString()
        lblProcedimientosValue.text = d.procedimientos.toString()

        val total = (d.hombres + d.mujeres).coerceAtLeast(1)
        lblDistribucionMeta.text = "${pct(d.hombres, total)}% H / ${pct(d.mujeres, total)}% M"
        pieDistribucion.data.setAll(
            PieChart.Data("Hombres (${d.hombres})", d.hombres.toDouble()),
            PieChart.Data("Mujeres (${d.mujeres})", d.mujeres.toDouble()),
        )

        lblMedicosMeta.text = "Top ${d.topMedicos.size}"
        renderBarMedicos(d.topMedicos)

        val diagMax = d.topDiagnosticos.maxOfOrNull { it.second } ?: 1
        lblDiagnosticosMeta.text = "Top ${d.topDiagnosticos.size}"
        topDiagnosticosContainer.children.setAll(d.topDiagnosticos.map { (name, count) -> createDiagnosticoRow(name, count, diagMax) })

        val totalPac = (d.nuevos + d.recurrentes).coerceAtLeast(1)
        val nuevosP = d.nuevos.toDouble() / totalPac.toDouble()
        val recurrentesP = d.recurrentes.toDouble() / totalPac.toDouble()
        pbNuevos.progress = nuevosP
        pbRecurrentes.progress = recurrentesP
        lblNuevosPct.text = "${pct(d.nuevos, totalPac)}%"
        lblRecurrentesPct.text = "${pct(d.recurrentes, totalPac)}%"
        lblTiempoProm.text = d.tiempoPromAtencion
        lblDiagPrincipal.text = d.topDiagnosticos.firstOrNull()?.first ?: "N/D"

        enfermedadesRowsContainer.children.setAll(d.enfermedadesRecientes.map { createProcRow(it) })
        operacionesRowsContainer.children.setAll(d.operacionesRecientes.map { createProcRow(it) })
    }

    private fun renderBarMedicos(items: List<Pair<String, Int>>) {
        barMedicos.data.clear()
        val series = XYChart.Series<String, Number>()
        items.forEach { (doctor, count) ->
            series.data.add(XYChart.Data(doctor, count))
        }
        barMedicos.data.add(series)
    }

    private fun createDiagnosticoRow(nombre: String, count: Int, max: Int): Node {
        val header = HBox(10.0).apply {
            alignment = Pos.CENTER_LEFT
            children.addAll(
                Label(nombre).apply {
                    styleClass.add("re-diagnostico-name")
                    isWrapText = true
                    HBox.setHgrow(this, Priority.ALWAYS)
                },
                Label(count.toString()).apply { styleClass.add("re-metric-strong") },
            )
        }

        val pb = ProgressBar((count.toDouble() / max.toDouble()).coerceIn(0.0, 1.0)).apply {
            styleClass.add("re-progress")
            maxWidth = Double.MAX_VALUE
        }

        return VBox(6.0).apply {
            styleClass.add("re-diagnostico-row")
            children.addAll(header, pb)
        }
    }

    private fun createProcRow(r: ProcRow): Node {
        val dept = Label(r.departamento).apply {
            styleClass.add("re-cell-muted")
            prefWidth = 130.0
        }
        val proc = Label(r.procedimiento).apply {
            styleClass.add("re-cell-strong")
            isWrapText = true
            HBox.setHgrow(this, Priority.ALWAYS)
        }
        val count = Label(r.pacientes.toString()).apply {
            styleClass.add("re-cell-strong")
            prefWidth = 90.0
            alignment = Pos.CENTER_RIGHT
        }

        return HBox(10.0).apply {
            styleClass.addAll("ps-row", "re-proc-row")
            alignment = Pos.CENTER_LEFT
            children.addAll(dept, proc, count)
        }
    }

    private fun pct(part: Int, total: Int): Int = ((part.toDouble() / total.toDouble()) * 100.0).roundToInt()
}

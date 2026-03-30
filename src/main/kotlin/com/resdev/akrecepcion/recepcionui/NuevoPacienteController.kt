package com.resdev.akrecepcion.recepcionui

import javafx.animation.Interpolator
import javafx.animation.ParallelTransition
import javafx.animation.TranslateTransition
import javafx.fxml.FXML
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.ComboBox
import javafx.scene.control.Label
import javafx.scene.control.TextArea
import javafx.scene.control.TextField
import javafx.scene.control.TextInputControl
import javafx.scene.layout.Region
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import javafx.scene.shape.Rectangle
import javafx.util.Duration

class NuevoPacienteController {
    private enum class Paso(val idx: Int) {
        PERSONAL(0),
        UBICACION(1),
        AREA_MEDICA(2),
        RESUMEN(3),
    }

    @FXML private lateinit var pagesHost: StackPane
    @FXML private lateinit var pagePersonal: VBox
    @FXML private lateinit var pageUbicacion: VBox
    @FXML private lateinit var pageAreaMedica: VBox
    @FXML private lateinit var pageResumen: VBox

    @FXML private lateinit var step1Circle: Region
    @FXML private lateinit var step2Circle: Region
    @FXML private lateinit var step3Circle: Region
    @FXML private lateinit var step1Label: Label
    @FXML private lateinit var step2Label: Label
    @FXML private lateinit var step3Label: Label
    @FXML private lateinit var stepLine12: Region
    @FXML private lateinit var stepLine23: Region

    @FXML private lateinit var btnAnterior: Button
    @FXML private lateinit var btnSiguiente: Button
    @FXML private lateinit var btnGuardarBorrador: Button
    @FXML private lateinit var lblWizardStatus: Label
    @FXML private lateinit var wizardNavRow: javafx.scene.layout.HBox

    // Paso 1
    @FXML private lateinit var txtNombre: TextField
    @FXML private lateinit var txtApellidos: TextField
    @FXML private lateinit var txtEdad: TextField
    @FXML private lateinit var cmbGenero: ComboBox<String>

    // Paso 2
    @FXML private lateinit var txtDireccion: TextField
    @FXML private lateinit var txtCiudad: TextField
    @FXML private lateinit var txtDepartamento: TextField
    @FXML private lateinit var txtPais: TextField
    @FXML private lateinit var txtCodigoPostal: TextField

    // Paso 3
    @FXML private lateinit var cmbEspecialidad: ComboBox<String>
    @FXML private lateinit var txtMotivo: TextArea
    @FXML private lateinit var cmbPrioridad: ComboBox<String>

    // Resumen
    @FXML private lateinit var lblResumenNombre: Label
    @FXML private lateinit var lblResumenEdadGenero: Label
    @FXML private lateinit var lblResumenDireccion: Label
    @FXML private lateinit var lblResumenCiudadDepto: Label
    @FXML private lateinit var lblResumenPaisPostal: Label
    @FXML private lateinit var lblResumenEspecialidad: Label
    @FXML private lateinit var lblResumenPrioridadChip: Label
    @FXML private lateinit var lblResumenMotivo: Label

    private var pasoActual: Paso = Paso.PERSONAL
    private var animando = false

    @FXML
    private fun initialize() {
        // Clip para que el slide no se vea fuera del contenedor.
        val clip = Rectangle()
        clip.widthProperty().bind(pagesHost.widthProperty())
        clip.heightProperty().bind(pagesHost.heightProperty())
        pagesHost.clip = clip

        cmbGenero.items.setAll("Femenino", "Masculino", "Otro", "Prefiero no decir")
        cmbEspecialidad.items.setAll(
            "Medicina General",
            "Pediatria",
            "Ginecologia",
            "Dermatologia",
            "Odontologia",
            "Cardiologia",
            "Traumatologia",
        )
        cmbPrioridad.items.setAll("Normal", "Alta", "Urgente")
        cmbPrioridad.selectionModel.selectFirst()

        // Estado inicial.
        lblWizardStatus.text = ""
        showOnly(pagePersonal)
        updateStepUI()
        updateButtons()

        btnAnterior.setOnAction { goPrev() }
        btnSiguiente.setOnAction { goNext() }
        btnGuardarBorrador.setOnAction { guardarBorrador() }
    }

    private fun guardarBorrador() {
        lblWizardStatus.text = "Borrador guardado."
        lblWizardStatus.styleClass.remove("wizard-status-tone-error")
        if (!lblWizardStatus.styleClass.contains("wizard-status-tone-ok")) {
            lblWizardStatus.styleClass.add("wizard-status-tone-ok")
        }
    }

    private fun goPrev() {
        if (animando) return
        lblWizardStatus.text = ""
        val next = when (pasoActual) {
            Paso.PERSONAL -> Paso.PERSONAL
            Paso.UBICACION -> Paso.PERSONAL
            Paso.AREA_MEDICA -> Paso.UBICACION
            Paso.RESUMEN -> Paso.AREA_MEDICA
        }
        if (next == pasoActual) return
        transicionar(next)
    }

    private fun goNext() {
        if (animando) return
        lblWizardStatus.text = ""

        if (!validarPaso(pasoActual)) {
            lblWizardStatus.text = "Completa los campos obligatorios marcados con *."
            lblWizardStatus.styleClass.remove("wizard-status-tone-ok")
            if (!lblWizardStatus.styleClass.contains("wizard-status-tone-error")) {
                lblWizardStatus.styleClass.add("wizard-status-tone-error")
            }
            return
        }

        val next = when (pasoActual) {
            Paso.PERSONAL -> Paso.UBICACION
            Paso.UBICACION -> Paso.AREA_MEDICA
            Paso.AREA_MEDICA -> Paso.RESUMEN
            Paso.RESUMEN -> Paso.RESUMEN
        }
        if (next == pasoActual) return

        transicionar(next)
    }

    private fun validarPaso(paso: Paso): Boolean {
        fun req(control: TextInputControl): Boolean {
            val ok = control.text?.trim()?.isNotEmpty() == true
            setError(control, !ok)
            return ok
        }

        fun reqCombo(control: ComboBox<*>): Boolean {
            val ok = control.value != null
            setError(control, !ok)
            return ok
        }

        return when (paso) {
            Paso.PERSONAL -> {
                val okNombre = req(txtNombre)
                val okApellidos = req(txtApellidos)
                val okEdad = req(txtEdad)
                val okGenero = reqCombo(cmbGenero)
                okNombre && okApellidos && okEdad && okGenero
            }
            Paso.UBICACION -> {
                val okDireccion = req(txtDireccion)
                val okCiudad = req(txtCiudad)
                val okDepartamento = req(txtDepartamento)
                val okPais = req(txtPais)
                okDireccion && okCiudad && okDepartamento && okPais
            }
            Paso.AREA_MEDICA -> {
                val okEspecialidad = reqCombo(cmbEspecialidad)
                val okMotivo = req(txtMotivo)
                okEspecialidad && okMotivo
            }
            Paso.RESUMEN -> true
        }
    }

    private fun setError(node: Node, error: Boolean) {
        node.styleClass.remove("wizard-field-state-error")
        if (error) node.styleClass.add("wizard-field-state-error")
    }

    private fun transicionar(nextPaso: Paso) {
        val actualNode = nodeFor(pasoActual)
        val nextNode = nodeFor(nextPaso)
        val w = pagesHost.width.takeIf { it > 1.0 } ?: 760.0

        val dir = if (nextPaso.idx > pasoActual.idx) 1 else -1

        nextNode.translateX = w * dir
        nextNode.isVisible = true
        nextNode.isManaged = true
        actualNode.isVisible = true
        actualNode.isManaged = true

        animando = true

        val tActual = TranslateTransition(Duration.millis(240.0), actualNode).apply {
            fromX = 0.0
            toX = -w * dir
            interpolator = Interpolator.EASE_BOTH
        }
        val tNext = TranslateTransition(Duration.millis(240.0), nextNode).apply {
            fromX = w * dir
            toX = 0.0
            interpolator = Interpolator.EASE_BOTH
        }

        ParallelTransition(tActual, tNext).apply {
            setOnFinished {
                actualNode.translateX = 0.0
                showOnly(nextNode)
                pasoActual = nextPaso
                updateStepUI()
                updateButtons()
                if (pasoActual == Paso.RESUMEN) {
                    renderResumen()
                }
                animando = false
            }
            play()
        }
    }

    private fun nodeFor(paso: Paso): VBox = when (paso) {
        Paso.PERSONAL -> pagePersonal
        Paso.UBICACION -> pageUbicacion
        Paso.AREA_MEDICA -> pageAreaMedica
        Paso.RESUMEN -> pageResumen
    }

    private fun showOnly(node: VBox) {
        listOf(pagePersonal, pageUbicacion, pageAreaMedica, pageResumen).forEach {
            it.isVisible = it === node
            it.isManaged = it === node
            it.translateX = 0.0
        }
    }

    private fun updateButtons() {
        btnAnterior.isDisable = pasoActual == Paso.PERSONAL
        btnSiguiente.text =
            when (pasoActual) {
                Paso.AREA_MEDICA -> "Finalizar"
                else -> "Siguiente paso  \u2192"
            }

        // En resumen, usamos el panel derecho de acciones (estructura de referencia).
        val inResumen = pasoActual == Paso.RESUMEN
        wizardNavRow.isVisible = !inResumen
        wizardNavRow.isManaged = !inResumen
    }

    private fun updateStepUI() {
        // Reset
        val circles = listOf(step1Circle, step2Circle, step3Circle)
        val labels = listOf(step1Label, step2Label, step3Label)
        val lines = listOf(stepLine12, stepLine23)
        circles.forEach {
            it.styleClass.remove("wizard-step-circle-state-active")
            it.styleClass.remove("wizard-step-circle-state-done")
            if (!it.styleClass.contains("wizard-step-circle-state-idle")) it.styleClass.add("wizard-step-circle-state-idle")
        }
        labels.forEach {
            it.styleClass.remove("wizard-step-text-state-active")
            it.styleClass.remove("wizard-step-text-state-done")
            if (!it.styleClass.contains("wizard-step-text-state-idle")) it.styleClass.add("wizard-step-text-state-idle")
        }
        lines.forEach {
            it.styleClass.remove("wizard-step-line-state-done")
            if (!it.styleClass.contains("wizard-step-line-state-idle")) it.styleClass.add("wizard-step-line-state-idle")
        }

        fun setActive(circle: Region, label: Label) {
            circle.styleClass.remove("wizard-step-circle-state-idle")
            label.styleClass.remove("wizard-step-text-state-idle")
            circle.styleClass.add("wizard-step-circle-state-active")
            label.styleClass.add("wizard-step-text-state-active")
        }

        fun setDone(circle: Region, label: Label) {
            circle.styleClass.remove("wizard-step-circle-state-idle")
            label.styleClass.remove("wizard-step-text-state-idle")
            circle.styleClass.add("wizard-step-circle-state-done")
            label.styleClass.add("wizard-step-text-state-done")
        }

        when (pasoActual) {
            Paso.PERSONAL -> setActive(step1Circle, step1Label)
            Paso.UBICACION -> {
                setDone(step1Circle, step1Label)
                stepLine12.styleClass.remove("wizard-step-line-state-idle")
                stepLine12.styleClass.add("wizard-step-line-state-done")
                setActive(step2Circle, step2Label)
            }
            Paso.AREA_MEDICA -> {
                setDone(step1Circle, step1Label)
                setDone(step2Circle, step2Label)
                stepLine12.styleClass.remove("wizard-step-line-state-idle")
                stepLine12.styleClass.add("wizard-step-line-state-done")
                stepLine23.styleClass.remove("wizard-step-line-state-idle")
                stepLine23.styleClass.add("wizard-step-line-state-done")
                setActive(step3Circle, step3Label)
            }
            Paso.RESUMEN -> {
                setDone(step1Circle, step1Label)
                setDone(step2Circle, step2Label)
                stepLine12.styleClass.remove("wizard-step-line-state-idle")
                stepLine12.styleClass.add("wizard-step-line-state-done")
                stepLine23.styleClass.remove("wizard-step-line-state-idle")
                stepLine23.styleClass.add("wizard-step-line-state-done")
                // Mantenemos el último paso como "activo" para el cierre del flujo.
                setActive(step3Circle, step3Label)
            }
        }
    }

    private fun renderResumen() {
        val nombre = "${txtNombre.text.orEmpty().trim()} ${txtApellidos.text.orEmpty().trim()}".trim().ifEmpty { "-" }
        val edad = txtEdad.text.orEmpty().trim().ifEmpty { "-" }
        val genero = (cmbGenero.value ?: "-").trim().ifEmpty { "-" }

        val direccion = txtDireccion.text.orEmpty().trim().ifEmpty { "-" }
        val ciudad = txtCiudad.text.orEmpty().trim().ifEmpty { "-" }
        val depto = txtDepartamento.text.orEmpty().trim().ifEmpty { "-" }
        val pais = txtPais.text.orEmpty().trim().ifEmpty { "-" }
        val postal = txtCodigoPostal.text.orEmpty().trim().ifEmpty { "-" }

        val esp = (cmbEspecialidad.value ?: "-").trim().ifEmpty { "-" }
        val prioridad = (cmbPrioridad.value ?: "Normal").trim().ifEmpty { "Normal" }
        val motivo = txtMotivo.text.orEmpty().trim().ifEmpty { "-" }

        lblResumenNombre.text = nombre
        lblResumenEdadGenero.text = "$edad años · $genero"
        lblResumenDireccion.text = direccion
        lblResumenCiudadDepto.text = "$ciudad · $depto"
        lblResumenPaisPostal.text = "$pais · $postal"
        lblResumenEspecialidad.text = esp
        lblResumenPrioridadChip.text = prioridad.uppercase()
        lblResumenMotivo.text = motivo

        lblWizardStatus.text = ""
    }

    @FXML
    private fun onEditarResumen(@Suppress("UNUSED_PARAMETER") event: javafx.event.ActionEvent) {
        if (animando) return
        transicionar(Paso.AREA_MEDICA)
    }

    @FXML
    private fun onConfirmarRegistro(@Suppress("UNUSED_PARAMETER") event: javafx.event.ActionEvent) {
        lblWizardStatus.text = "Registro confirmado. Expediente creado (demo)."
        lblWizardStatus.styleClass.remove("wizard-status-tone-error")
        if (!lblWizardStatus.styleClass.contains("wizard-status-tone-ok")) {
            lblWizardStatus.styleClass.add("wizard-status-tone-ok")
        }

        limpiarFormulario()
        showOnly(pagePersonal)
        pasoActual = Paso.PERSONAL
        updateStepUI()
        updateButtons()
    }

    @FXML
    private fun onDescartarRegistro(@Suppress("UNUSED_PARAMETER") event: javafx.event.ActionEvent) {
        lblWizardStatus.text = "Registro descartado."
        lblWizardStatus.styleClass.remove("wizard-status-tone-ok")
        if (!lblWizardStatus.styleClass.contains("wizard-status-tone-error")) {
            lblWizardStatus.styleClass.add("wizard-status-tone-error")
        }

        limpiarFormulario()
        showOnly(pagePersonal)
        pasoActual = Paso.PERSONAL
        updateStepUI()
        updateButtons()
    }

    private fun limpiarFormulario() {
        txtNombre.clear()
        txtApellidos.clear()
        txtEdad.clear()
        cmbGenero.selectionModel.clearSelection()

        txtDireccion.clear()
        txtCiudad.clear()
        txtDepartamento.clear()
        txtPais.clear()
        txtCodigoPostal.clear()

        cmbEspecialidad.selectionModel.clearSelection()
        cmbPrioridad.selectionModel.selectFirst()
        txtMotivo.clear()
    }
}

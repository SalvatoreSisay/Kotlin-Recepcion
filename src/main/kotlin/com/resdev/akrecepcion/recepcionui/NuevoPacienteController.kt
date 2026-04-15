package com.resdev.akrecepcion.recepcionui

import com.resdev.akrecepcion.recepcionui.dao.PacienteNuevo
import com.resdev.akrecepcion.recepcionui.dao.jdbc.PacienteDaoJdbc
import javafx.animation.Interpolator
import javafx.animation.ParallelTransition
import javafx.animation.TranslateTransition
import javafx.fxml.FXML
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.ComboBox
import javafx.scene.control.DatePicker
import javafx.scene.control.Label
import javafx.scene.control.TextArea
import javafx.scene.control.TextField
import javafx.scene.control.TextInputControl
import javafx.scene.layout.Region
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import javafx.scene.shape.Rectangle
import javafx.util.Duration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.javafx.JavaFx
import java.time.LocalDate
import java.time.Period

class NuevoPacienteController {
    private enum class Paso(val idx: Int) {
        PERSONAL(0),
        UBICACION(1),
        HOSPITALARIO(2),
        AREA_MEDICA(3),
        RESUMEN(4),
    }

    @FXML private lateinit var pagesHost: StackPane
    @FXML private lateinit var pagePersonal: VBox
    @FXML private lateinit var pageUbicacion: VBox
    @FXML private lateinit var pageHospitalario: VBox
    @FXML private lateinit var pageAreaMedica: VBox
    @FXML private lateinit var pageResumen: VBox

    @FXML private lateinit var step1Circle: Region
    @FXML private lateinit var step2Circle: Region
    @FXML private lateinit var step3Circle: Region
    @FXML private lateinit var step4Circle: Region
    @FXML private lateinit var step1Label: Label
    @FXML private lateinit var step2Label: Label
    @FXML private lateinit var step3Label: Label
    @FXML private lateinit var step4Label: Label
    @FXML private lateinit var stepLine12: Region
    @FXML private lateinit var stepLine23: Region
    @FXML private lateinit var stepLine34: Region

    @FXML private lateinit var btnAnterior: Button
    @FXML private lateinit var btnSiguiente: Button
    @FXML private lateinit var btnGuardarBorrador: Button
    @FXML private lateinit var lblWizardStatus: Label
    @FXML private lateinit var wizardNavRow: javafx.scene.layout.HBox

    // Paso 1
    @FXML private lateinit var txtNombre: TextField
    @FXML private lateinit var dpFechaNacimiento: DatePicker
    @FXML private lateinit var txtEdad: TextField
    @FXML private lateinit var cmbGenero: ComboBox<String>
    @FXML private lateinit var txtDpi: TextField
    @FXML private lateinit var txtTelefono: TextField
    @FXML private lateinit var txtEtnia: TextField
    @FXML private lateinit var txtEstadoCivil: TextField
    @FXML private lateinit var txtOcupacion: TextField

    // Paso 2
    @FXML private lateinit var txtDireccion: TextField
    @FXML private lateinit var txtCiudad: TextField
    @FXML private lateinit var txtDepartamento: TextField
    @FXML private lateinit var txtMunicipio: TextField
    @FXML private lateinit var txtPais: TextField
    @FXML private lateinit var txtCodigoPostal: TextField
    @FXML private lateinit var txtLugarNacimiento: TextField

    // Paso 3 (Hospitalario)
    @FXML private lateinit var txtPrograma: TextField
    @FXML private lateinit var txtIgss: TextField
    @FXML private lateinit var txtPacienteAltoRiesgo: TextField
    @FXML private lateinit var txtQuienRegistra: TextField

    // Paso 4
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
    private var guardando = false

    private val scope = CoroutineScope(Dispatchers.JavaFx + SupervisorJob())
    private val pacienteDao = PacienteDaoJdbc()

    @FXML
    private fun initialize() {
        // Clip para que el slide no se vea fuera del contenedor.
        val clip = Rectangle()
        clip.widthProperty().bind(pagesHost.widthProperty())
        clip.heightProperty().bind(pagesHost.heightProperty())
        pagesHost.clip = clip

        cmbGenero.items.setAll("F", "M")
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

        // Quien registra: se llena automáticamente con el usuario logeado.
        txtQuienRegistra.text = AppSession.currentUser.usuario
        txtQuienRegistra.isEditable = false
        txtQuienRegistra.isFocusTraversable = false

        txtEdad.isEditable = false
        txtEdad.isFocusTraversable = false

        dpFechaNacimiento.valueProperty().addListener { _, _, newValue ->
            updateEdadFromFecha(newValue)
        }

        // DPI: máximo 15 caracteres (la columna es varchar(15)).
        txtDpi.textProperty().addListener { _, _, newValue ->
            if (newValue != null && newValue.length > 15) {
                txtDpi.text = newValue.take(15)
            }
        }

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
        if (guardando) return
        lblWizardStatus.text = ""
        val next = when (pasoActual) {
            Paso.PERSONAL -> Paso.PERSONAL
            Paso.UBICACION -> Paso.PERSONAL
            Paso.HOSPITALARIO -> Paso.UBICACION
            Paso.AREA_MEDICA -> Paso.HOSPITALARIO
            Paso.RESUMEN -> Paso.AREA_MEDICA
        }
        if (next == pasoActual) return
        transicionar(next)
    }

    private fun goNext() {
        if (animando) return
        if (guardando) return
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
            Paso.UBICACION -> Paso.HOSPITALARIO
            Paso.HOSPITALARIO -> Paso.AREA_MEDICA
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

        fun reqDate(control: DatePicker): Boolean {
            val d = control.value
            val ok = d != null && !d.isAfter(LocalDate.now())
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
                val okFecha = reqDate(dpFechaNacimiento)
                val okGenero = reqCombo(cmbGenero)
                okNombre && okFecha && okGenero
            }
            Paso.UBICACION -> {
                val okDireccion = req(txtDireccion)
                val okCiudad = req(txtCiudad)
                val okDepartamento = req(txtDepartamento)
                val okMunicipio = req(txtMunicipio)
                val okPais = req(txtPais)
                okDireccion && okCiudad && okDepartamento && okMunicipio && okPais
            }
            Paso.HOSPITALARIO -> true
            Paso.AREA_MEDICA -> {
                val okEspecialidad = reqCombo(cmbEspecialidad)
                val okMotivo = req(txtMotivo)
                okEspecialidad && okMotivo
            }
            Paso.RESUMEN -> true
        }
    }

    private fun updateEdadFromFecha(fecha: LocalDate?) {
        if (fecha == null) {
            txtEdad.text = ""
            setError(dpFechaNacimiento, false)
            return
        }

        val hoy = LocalDate.now()
        if (fecha.isAfter(hoy)) {
            txtEdad.text = ""
            setError(dpFechaNacimiento, true)
            return
        }

        val years = Period.between(fecha, hoy).years
        txtEdad.text = years.toString()
        setError(dpFechaNacimiento, false)
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

    private fun nodeFor(paso: Paso): VBox =
        when (paso) {
            Paso.PERSONAL -> pagePersonal
            Paso.UBICACION -> pageUbicacion
            Paso.HOSPITALARIO -> pageHospitalario
            Paso.AREA_MEDICA -> pageAreaMedica
            Paso.RESUMEN -> pageResumen
        }

    private fun showOnly(node: VBox) {
        listOf(pagePersonal, pageUbicacion, pageHospitalario, pageAreaMedica, pageResumen).forEach {
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
        val circles = listOf(step1Circle, step2Circle, step3Circle, step4Circle)
        val labels = listOf(step1Label, step2Label, step3Label, step4Label)
        val lines = listOf(stepLine12, stepLine23, stepLine34)
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
            Paso.HOSPITALARIO -> {
                setDone(step1Circle, step1Label)
                setDone(step2Circle, step2Label)
                stepLine12.styleClass.remove("wizard-step-line-state-idle")
                stepLine12.styleClass.add("wizard-step-line-state-done")
                stepLine23.styleClass.remove("wizard-step-line-state-idle")
                stepLine23.styleClass.add("wizard-step-line-state-done")
                setActive(step3Circle, step3Label)
            }
            Paso.AREA_MEDICA -> {
                setDone(step1Circle, step1Label)
                setDone(step2Circle, step2Label)
                setDone(step3Circle, step3Label)
                stepLine12.styleClass.remove("wizard-step-line-state-idle")
                stepLine12.styleClass.add("wizard-step-line-state-done")
                stepLine23.styleClass.remove("wizard-step-line-state-idle")
                stepLine23.styleClass.add("wizard-step-line-state-done")
                stepLine34.styleClass.remove("wizard-step-line-state-idle")
                stepLine34.styleClass.add("wizard-step-line-state-done")
                setActive(step4Circle, step4Label)
            }
            Paso.RESUMEN -> {
                setDone(step1Circle, step1Label)
                setDone(step2Circle, step2Label)
                stepLine12.styleClass.remove("wizard-step-line-state-idle")
                stepLine12.styleClass.add("wizard-step-line-state-done")
                stepLine23.styleClass.remove("wizard-step-line-state-idle")
                stepLine23.styleClass.add("wizard-step-line-state-done")
                setDone(step3Circle, step3Label)
                stepLine34.styleClass.remove("wizard-step-line-state-idle")
                stepLine34.styleClass.add("wizard-step-line-state-done")
                // Mantenemos el último paso como "activo" para el cierre del flujo.
                setActive(step4Circle, step4Label)
            }
        }
    }

    private fun renderResumen() {
        val nombre = txtNombre.text.orEmpty().trim().ifEmpty { "-" }
        val edad = txtEdad.text.orEmpty().trim().ifEmpty { "-" }
        val genero = (cmbGenero.value ?: "-").trim().ifEmpty { "-" }

        val direccion = txtDireccion.text.orEmpty().trim().ifEmpty { "-" }
        val canton = txtCiudad.text.orEmpty().trim().ifEmpty { "-" }
        val municipio = txtMunicipio.text.orEmpty().trim().ifEmpty { "-" }
        val pais = txtPais.text.orEmpty().trim().ifEmpty { "-" }
        val direccionAviso = txtCodigoPostal.text.orEmpty().trim().ifEmpty { "-" }

        val esp = (cmbEspecialidad.value ?: "-").trim().ifEmpty { "-" }
        val prioridad = (cmbPrioridad.value ?: "Normal").trim().ifEmpty { "Normal" }
        val motivo = txtMotivo.text.orEmpty().trim().ifEmpty { "-" }

        lblResumenNombre.text = nombre
        lblResumenEdadGenero.text = "$edad años · $genero"
        lblResumenDireccion.text = direccion
        lblResumenCiudadDepto.text = "$canton · $municipio"
        lblResumenPaisPostal.text = "$pais · $direccionAviso"
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
        if (animando || guardando) return

        // Redundante: al llegar al resumen ya se validó, pero aquí volvemos a validar para evitar inserts incompletos.
        val ok =
            validarPaso(Paso.PERSONAL) &&
                validarPaso(Paso.UBICACION) &&
                validarPaso(Paso.HOSPITALARIO) &&
                validarPaso(Paso.AREA_MEDICA)
        if (!ok) {
            lblWizardStatus.text = "Hay campos obligatorios incompletos. Revisa los pasos anteriores."
            lblWizardStatus.styleClass.remove("wizard-status-tone-ok")
            if (!lblWizardStatus.styleClass.contains("wizard-status-tone-error")) {
                lblWizardStatus.styleClass.add("wizard-status-tone-error")
            }
            return
        }

        val payload =
            PacienteNuevo(
                nombre = txtNombre.text.trim(),
                fechaNacimiento = requireNotNull(dpFechaNacimiento.value) { "Fecha de nacimiento requerida" },
                sexo = (cmbGenero.value ?: "").trim(),
                dpi = txtDpi.text?.trim().orEmpty(),
                telefono = txtTelefono.text?.trim().orEmpty(),
                etnia = txtEtnia.text?.trim().orEmpty(),
                estadoCivil = txtEstadoCivil.text?.trim().orEmpty(),
                ocupacion = txtOcupacion.text?.trim().orEmpty(),
                direccion = txtDireccion.text.trim(),
                pais = txtPais.text.trim(),
                departamento = txtDepartamento.text?.trim().orEmpty(),
                municipio = txtMunicipio.text.trim(),
                canton = txtCiudad.text.trim(),
                direccionAviso = txtCodigoPostal.text?.trim().orEmpty(),
                lugarNacimiento = txtLugarNacimiento.text?.trim().orEmpty(),
                programa = txtPrograma.text?.trim().orEmpty(),
                igss = txtIgss.text?.trim().orEmpty(),
                pacienteAltoRiesgo = txtPacienteAltoRiesgo.text?.trim().orEmpty(),
                observaciones = (cmbEspecialidad.value ?: "").trim(),
                observa1 = (cmbPrioridad.value ?: "Normal").trim(),
                observa2 = txtMotivo.text?.trim().orEmpty(),
            )

        setGuardando(true)
        lblWizardStatus.text = "Guardando paciente..."
        lblWizardStatus.styleClass.remove("wizard-status-tone-error")
        lblWizardStatus.styleClass.remove("wizard-status-tone-ok")

        scope.launch {
            val res =
                withContext(Dispatchers.IO) {
                    runCatching {
                        pacienteDao.insert(
                            paciente = payload,
                            quienRegistra = AppSession.currentUser.usuario,
                        )
                    }
                }

            res.fold(
                onSuccess = { nuevoId ->
                    lblWizardStatus.text = "Registro creado correctamente. ID: $nuevoId"
                    lblWizardStatus.styleClass.remove("wizard-status-tone-error")
                    if (!lblWizardStatus.styleClass.contains("wizard-status-tone-ok")) {
                        lblWizardStatus.styleClass.add("wizard-status-tone-ok")
                    }

                    limpiarFormulario()
                    showOnly(pagePersonal)
                    pasoActual = Paso.PERSONAL
                    updateStepUI()
                    updateButtons()
                },
                onFailure = { err ->
                    lblWizardStatus.text = "Error al registrar: ${err.message ?: err::class.simpleName}"
                    lblWizardStatus.styleClass.remove("wizard-status-tone-ok")
                    if (!lblWizardStatus.styleClass.contains("wizard-status-tone-error")) {
                        lblWizardStatus.styleClass.add("wizard-status-tone-error")
                    }
                },
            )

            setGuardando(false)
        }
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
        dpFechaNacimiento.value = null
        txtEdad.clear()
        cmbGenero.selectionModel.clearSelection()
        txtDpi.clear()
        txtTelefono.clear()
        txtEtnia.clear()
        txtEstadoCivil.clear()
        txtOcupacion.clear()

        txtDireccion.clear()
        txtCiudad.clear()
        txtDepartamento.clear()
        txtMunicipio.clear()
        txtPais.clear()
        txtCodigoPostal.clear()
        txtLugarNacimiento.clear()

        txtPrograma.clear()
        txtIgss.clear()
        txtPacienteAltoRiesgo.clear()
        // Se mantiene autollenado para cada registro.
        txtQuienRegistra.text = AppSession.currentUser.usuario

        cmbEspecialidad.selectionModel.clearSelection()
        cmbPrioridad.selectionModel.selectFirst()
        txtMotivo.clear()
    }

    private fun setGuardando(value: Boolean) {
        guardando = value
        pagesHost.isDisable = value
        btnAnterior.isDisable = value || pasoActual == Paso.PERSONAL
        btnSiguiente.isDisable = value
        btnGuardarBorrador.isDisable = value
    }
}

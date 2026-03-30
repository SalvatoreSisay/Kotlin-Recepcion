package com.resdev.akrecepcion.recepcionui

import javafx.animation.FadeTransition
import javafx.animation.Interpolator
import javafx.animation.ParallelTransition
import javafx.animation.PauseTransition
import javafx.animation.TranslateTransition
import javafx.fxml.FXML
import javafx.geometry.Insets
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.ComboBox
import javafx.scene.control.DatePicker
import javafx.scene.control.Label
import javafx.scene.control.TextArea
import javafx.scene.control.TextField
import javafx.scene.control.Tooltip
import javafx.scene.input.KeyCode
import javafx.scene.input.MouseEvent
import javafx.scene.layout.GridPane
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import javafx.scene.shape.Rectangle
import javafx.util.Duration
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class AgendarCitaController {
    private enum class Paso(val idx: Int) {
        PACIENTE(0),
        HORARIO(1),
        RESUMEN(2),
    }

    private data class Paciente(
        val nombre: String,
        val pacienteId: String,
        val edad: Int,
        val telefono: String,
    )

    private data class Doctor(
        val nombre: String,
        val especialidad: String,
        val proxima: String,
        val fee: Double,
    )

    @FXML private lateinit var pagesHost: StackPane
    @FXML private lateinit var pagePaciente: VBox
    @FXML private lateinit var pageHorario: VBox
    @FXML private lateinit var pageResumen: VBox

    @FXML private lateinit var step1Circle: Region
    @FXML private lateinit var step2Circle: Region
    @FXML private lateinit var step3Circle: Region
    @FXML private lateinit var step1Label: Label
    @FXML private lateinit var step2Label: Label
    @FXML private lateinit var step3Label: Label
    @FXML private lateinit var stepLine12: Region
    @FXML private lateinit var stepLine23: Region

    @FXML private lateinit var lblCardTitle: Label
    @FXML private lateinit var lblCardSubtitle: Label
    @FXML private lateinit var lblWizardStatus: Label

    @FXML private lateinit var txtBuscarPaciente: TextField
    @FXML private lateinit var btnLimpiarPaciente: Button
    @FXML private lateinit var lblPacienteCount: Label
    @FXML private lateinit var patientsContainer: VBox

    @FXML private lateinit var btnFiltroEspecialidad: Button
    @FXML private lateinit var doctorsGrid: GridPane
    @FXML private lateinit var datePicker: DatePicker
    @FXML private lateinit var cmbProcedimiento: ComboBox<String>
    @FXML private lateinit var txtNotas: TextArea
    @FXML private lateinit var slotsManana: HBox
    @FXML private lateinit var slotsTarde: HBox
    @FXML private lateinit var slotsNoche: HBox

    @FXML private lateinit var lblResumenPaciente: Label
    @FXML private lateinit var lblResumenDoctor: Label
    @FXML private lateinit var lblResumenFecha: Label
    @FXML private lateinit var lblResumenHora: Label
    @FXML private lateinit var lblResumenProcedimiento: Label
    @FXML private lateinit var lblResumenNotas: Label

    @FXML private lateinit var btnGuardar: Button
    @FXML private lateinit var btnAnterior: Button
    @FXML private lateinit var btnSiguiente: Button

    @FXML private lateinit var lblSelPaciente: Label
    @FXML private lateinit var lblSelPacienteId: Label
    @FXML private lateinit var lblSelDoctor: Label
    @FXML private lateinit var lblSelEspecialidad: Label
    @FXML private lateinit var lblSelFecha: Label
    @FXML private lateinit var lblSelHora: Label
    @FXML private lateinit var lblSelProcedimiento: Label
    @FXML private lateinit var lblFee: Label
    @FXML private lateinit var btnConfirmar: Button
    @FXML private lateinit var lblConfirmStatus: Label

    private val pacientes = listOf(
        Paciente("Arthur Morgan", "AK-8821", 33, "+502 55 11 220 909"),
        Paciente("Elena Rodríguez", "AK-1044", 34, "+502 41 11 902 114"),
        Paciente("Ricardo Casares", "AK-8829", 66, "+502 55 43 210 987"),
        Paciente("Marcus Chen", "AK-3301", 49, "+502 50 09 441 330"),
        Paciente("Lucía Arévalo", "AK-4411", 29, "+502 33 90 110 771"),
        Paciente("Andrea Gómez", "AK-8711", 51, "+502 22 10 902 441"),
        Paciente("Sofía Castillo", "AK-7731", 19, "+502 55 12 331 001"),
    )

    private val doctors = listOf(
        Doctor("Dr. Julián Vance", "Cardiología", "Próxima: 12 oct, 09:00", 120.0),
        Doctor("Dra. Sarah Chen", "Neurología", "Próxima: 14 oct, 11:30", 145.0),
        Doctor("Dr. Marcus Thorne", "Traumatología", "Próxima: 12 oct, 14:00", 130.0),
        Doctor("Dra. Elena Rodríguez", "Medicina general", "Próxima: 13 oct, 10:00", 95.0),
    )

    private val procedimientos = listOf(
        "Consulta general",
        "Control post-operatorio",
        "Electrocardiograma",
        "Evaluación neurológica",
        "Curación",
        "Laboratorio: toma de muestras",
    )

    private val fmtFecha = DateTimeFormatter.ofPattern("EEE, d MMM yyyy")

    private var pasoActual: Paso = Paso.PACIENTE
    private var animando = false

    private var pacienteSel: Paciente? = null
    private var doctorSel: Doctor? = null
    private var horaSel: String? = null
    private var especialidadFiltroIdx = 0

    private var selectedPacienteCard: Node? = null
    private var selectedDoctorCard: Node? = null
    private var selectedSlotButton: Button? = null

    private val debounce = PauseTransition(Duration.millis(160.0))

    @FXML
    private fun initialize() {
        // Clip para que el slide no se vea fuera del contenedor.
        val clip = Rectangle()
        clip.widthProperty().bind(pagesHost.widthProperty())
        clip.heightProperty().bind(pagesHost.heightProperty())
        pagesHost.clip = clip

        lblWizardStatus.text = ""
        lblConfirmStatus.text = ""

        Tooltip.install(btnFiltroEspecialidad, Tooltip("Cambia el filtro de especialidad (demo)"))
        Tooltip.install(btnConfirmar, Tooltip("Confirma la cita con la selección actual"))

        cmbProcedimiento.items.setAll(procedimientos)

        datePicker.value = LocalDate.now().plusDays(1)
        datePicker.valueProperty().addListener { _, _, _ ->
            syncSide()
            syncResumen()
            // Cuando cambia fecha, el slot elegido puede no aplicar.
            clearSlotSelection(keepMessage = false)
        }

        txtNotas.textProperty().addListener { _, _, _ -> syncResumen() }
        cmbProcedimiento.valueProperty().addListener { _, _, _ ->
            syncSide()
            syncResumen()
        }

        buildSlots()

        btnGuardar.setOnAction { guardarBorrador() }
        btnAnterior.setOnAction { goPrev() }
        btnSiguiente.setOnAction { goNext() }
        btnConfirmar.setOnAction { confirmarCita() }

        btnConfirmar.isDisable = true

        txtBuscarPaciente.setOnAction { renderPacientes() }
        txtBuscarPaciente.textProperty().addListener { _, _, _ -> schedulePacientes() }
        txtBuscarPaciente.setOnKeyPressed { e ->
            if (e.code == KeyCode.ESCAPE) {
                txtBuscarPaciente.clear()
                renderPacientes()
            }
        }
        btnLimpiarPaciente.setOnAction {
            txtBuscarPaciente.clear()
            renderPacientes()
        }
        debounce.setOnFinished { renderPacientes(animate = true) }

        btnFiltroEspecialidad.setOnAction { cycleEspecialidadFiltro() }

        renderPacientes(firstRender = true)
        renderDoctores(firstRender = true)

        showOnly(pagePaciente)
        updateStepUI()
        updateButtons()
        updateTitles()
        syncSide()
        syncResumen()
    }

    private fun schedulePacientes() {
        debounce.stop()
        debounce.playFromStart()
    }

    private fun renderPacientes(firstRender: Boolean = false, animate: Boolean = !firstRender) {
        val q = txtBuscarPaciente.text?.trim()?.lowercase().orEmpty()
        val filtered = if (q.isBlank()) pacientes else pacientes.filter {
            it.nombre.lowercase().contains(q) || it.pacienteId.lowercase().contains(q)
        }
        lblPacienteCount.text = "${filtered.size} pacientes"

        val newNodes = filtered.map { createPacienteCard(it) }
        if (!animate) {
            patientsContainer.children.setAll(newNodes)
            return
        }
        swapContainer(patientsContainer, newNodes)
    }

    private fun createPacienteCard(p: Paciente): VBox {
        val avatar = Region().apply {
            styleClass.add("ap-avatar")
            minWidth = 42.0
            minHeight = 42.0
            maxWidth = 42.0
            maxHeight = 42.0
        }

        val title = Label(p.nombre).apply { styleClass.add("ap-card-title") }
        val subtitle = Label("ID: ${p.pacienteId}").apply { styleClass.add("muted") }
        val meta = Label("${p.edad} años  •  ${p.telefono}").apply { styleClass.add("ap-card-meta") }

        val left = VBox(2.0).apply { children.addAll(title, subtitle, meta) }
        val header = HBox(12.0).apply {
            alignmentProperty().set(javafx.geometry.Pos.CENTER_LEFT)
            children.addAll(avatar, left)
            HBox.setHgrow(left, Priority.ALWAYS)
        }

        return VBox(10.0).apply {
            styleClass.add("ap-patient-card")
            padding = Insets(12.0, 12.0, 12.0, 12.0)
            children.addAll(header)
            userData = p
            addEventHandler(MouseEvent.MOUSE_CLICKED) { seleccionarPaciente(p, this) }
        }
    }

    private fun seleccionarPaciente(p: Paciente, card: Node) {
        pacienteSel = p
        lblWizardStatus.text = ""
        lblConfirmStatus.text = ""
        selectedPacienteCard?.styleClass?.remove("ap-card-state-selected")
        card.styleClass.add("ap-card-state-selected")
        selectedPacienteCard = card
        syncSide(animate = true)
        syncResumen()
        btnConfirmar.isDisable = true
    }

    private val especialidadesFiltro = listOf("Todas", "Cardiología", "Neurología", "Traumatología", "Medicina general")

    private fun cycleEspecialidadFiltro() {
        especialidadFiltroIdx = (especialidadFiltroIdx + 1) % especialidadesFiltro.size
        val label = especialidadesFiltro[especialidadFiltroIdx]
        btnFiltroEspecialidad.text = if (label == "Todas") "Filtrar por especialidad" else label
        renderDoctores()
    }

    private fun renderDoctores(firstRender: Boolean = false) {
        doctorsGrid.children.clear()

        val filtro = especialidadesFiltro[especialidadFiltroIdx]
        val list = if (filtro == "Todas") doctors else doctors.filter { it.especialidad == filtro }

        list.forEachIndexed { idx, d ->
            val node = createDoctorCard(d)
            GridPane.setRowIndex(node, idx / 2)
            GridPane.setColumnIndex(node, idx % 2)
            doctorsGrid.children.add(node)
        }

        if (firstRender) return
        // Si el doctor seleccionado no está en el filtro actual, limpiar selección.
        val stillVisible = list.any { it == doctorSel }
        if (!stillVisible) {
            doctorSel = null
            selectedDoctorCard = null
            syncSide()
            syncResumen()
        }
    }

    private fun createDoctorCard(d: Doctor): VBox {
        val avatar = Region().apply {
            styleClass.add("ap-doctor-avatar")
            minWidth = 44.0
            minHeight = 44.0
            maxWidth = 44.0
            maxHeight = 44.0
        }

        val title = Label(d.nombre).apply { styleClass.add("ap-card-title") }
        val spec = Label(d.especialidad).apply { styleClass.add("muted") }
        val next = Label(d.proxima).apply { styleClass.add("ap-card-meta") }

        val header = HBox(12.0).apply {
            alignmentProperty().set(javafx.geometry.Pos.CENTER_LEFT)
            children.addAll(avatar, VBox(2.0).apply { children.addAll(title, spec, next) })
        }

        val chip = Label("SELECCIONADO").apply {
            styleClass.addAll("ap-chip", "ap-chip-state-hidden")
        }

        return VBox(10.0).apply {
            styleClass.add("ap-doctor-card")
            padding = Insets(12.0, 12.0, 12.0, 12.0)
            children.addAll(HBox(10.0).apply {
                children.addAll(header, Region().apply { HBox.setHgrow(this, Priority.ALWAYS) }, chip)
            })
            userData = Pair(d, chip)
            addEventHandler(MouseEvent.MOUSE_CLICKED) { seleccionarDoctor(d, this) }

            if (doctorSel == d) {
                styleClass.add("ap-card-state-selected")
                chip.styleClass.remove("ap-chip-state-hidden")
            }
        }
    }

    private fun seleccionarDoctor(d: Doctor, card: Node) {
        doctorSel = d
        lblWizardStatus.text = ""
        lblConfirmStatus.text = ""

        // limpiar visual de selección previa
        selectedDoctorCard?.let { prev ->
            prev.styleClass.remove("ap-card-state-selected")
            val chip = (prev.userData as? Pair<*, *>)?.second as? Label
            chip?.styleClass?.add("ap-chip-state-hidden")
        }

        card.styleClass.add("ap-card-state-selected")
        val chip = (card.userData as? Pair<*, *>)?.second as? Label
        chip?.styleClass?.remove("ap-chip-state-hidden")
        selectedDoctorCard = card

        // actualizar fee según doctor
        syncSide(animate = true)
        syncResumen()
        btnConfirmar.isDisable = true
    }

    private fun buildSlots() {
        fun btnSlot(text: String): Button =
            Button(text).apply {
                styleClass.add("ap-slot")
                setOnAction {
                    selectSlot(this, text)
                }
            }

        slotsManana.children.setAll(btnSlot("09:00"), btnSlot("10:30"))
        slotsTarde.children.setAll(btnSlot("14:00"), btnSlot("15:30"), btnSlot("16:15"))
        slotsNoche.children.setAll(btnSlot("18:30"), btnSlot("19:00"))
    }

    private fun selectSlot(button: Button, hora: String) {
        lblWizardStatus.text = ""
        lblConfirmStatus.text = ""
        selectedSlotButton?.styleClass?.remove("ap-slot-state-selected")
        button.styleClass.add("ap-slot-state-selected")
        selectedSlotButton = button
        horaSel = hora
        syncSide(animate = true)
        syncResumen()
        btnConfirmar.isDisable = true
    }

    private fun clearSlotSelection(keepMessage: Boolean) {
        selectedSlotButton?.styleClass?.remove("ap-slot-state-selected")
        selectedSlotButton = null
        horaSel = null
        if (!keepMessage) lblWizardStatus.text = ""
        syncSide()
        syncResumen()
        btnConfirmar.isDisable = true
    }

    private fun guardarBorrador() {
        lblWizardStatus.text = "Borrador guardado."
        lblWizardStatus.styleClass.remove("wizard-status-tone-error")
        if (!lblWizardStatus.styleClass.contains("wizard-status-tone-ok")) lblWizardStatus.styleClass.add("wizard-status-tone-ok")
    }

    private fun goPrev() {
        if (animando) return
        lblWizardStatus.text = ""
        val next = when (pasoActual) {
            Paso.PACIENTE -> Paso.PACIENTE
            Paso.HORARIO -> Paso.PACIENTE
            Paso.RESUMEN -> Paso.HORARIO
        }
        if (next == pasoActual) return
        transicionar(next)
    }

    private fun goNext() {
        if (animando) return
        lblWizardStatus.text = ""
        lblWizardStatus.styleClass.remove("wizard-status-tone-ok")
        lblWizardStatus.styleClass.remove("wizard-status-tone-error")

        if (!validarPaso(pasoActual)) {
            lblWizardStatus.text = "Completa la selección requerida para continuar."
            if (!lblWizardStatus.styleClass.contains("wizard-status-tone-error")) lblWizardStatus.styleClass.add("wizard-status-tone-error")
            return
        }

        val next = when (pasoActual) {
            Paso.PACIENTE -> Paso.HORARIO
            Paso.HORARIO -> Paso.RESUMEN
            Paso.RESUMEN -> Paso.RESUMEN
        }

        if (next == pasoActual) {
            // En resumen, habilitar confirmación.
            btnConfirmar.isDisable = !isReadyToConfirm()
            lblWizardStatus.text = if (btnConfirmar.isDisable) "Falta completar datos para confirmar." else "Listo para confirmar."
            if (!lblWizardStatus.styleClass.contains("wizard-status-tone-ok") && !btnConfirmar.isDisable) {
                lblWizardStatus.styleClass.add("wizard-status-tone-ok")
            }
            return
        }

        transicionar(next)
    }

    private fun validarPaso(paso: Paso): Boolean = when (paso) {
        Paso.PACIENTE -> pacienteSel != null
        Paso.HORARIO -> doctorSel != null && datePicker.value != null && horaSel != null && cmbProcedimiento.value != null
        Paso.RESUMEN -> isReadyToConfirm()
    }

    private fun isReadyToConfirm(): Boolean =
        pacienteSel != null && doctorSel != null && datePicker.value != null && horaSel != null && cmbProcedimiento.value != null

    private fun transicionar(nextPaso: Paso) {
        val actualNode = nodeFor(pasoActual)
        val nextNode = nodeFor(nextPaso)
        val w = pagesHost.width.takeIf { it > 1.0 } ?: 860.0
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
                updateTitles()
                syncResumen()
                btnConfirmar.isDisable = pasoActual != Paso.RESUMEN || !isReadyToConfirm()
                animando = false
            }
            play()
        }
    }

    private fun nodeFor(paso: Paso): VBox = when (paso) {
        Paso.PACIENTE -> pagePaciente
        Paso.HORARIO -> pageHorario
        Paso.RESUMEN -> pageResumen
    }

    private fun showOnly(node: VBox) {
        listOf(pagePaciente, pageHorario, pageResumen).forEach {
            it.isVisible = it === node
            it.isManaged = it === node
            it.translateX = 0.0
        }
    }

    private fun updateButtons() {
        btnAnterior.isDisable = pasoActual == Paso.PACIENTE
        btnSiguiente.text = when (pasoActual) {
            Paso.PACIENTE -> "Siguiente paso  \u2192"
            Paso.HORARIO -> "Revisar  \u2192"
            Paso.RESUMEN -> "Listo"
        }
    }

    private fun updateTitles() {
        when (pasoActual) {
            Paso.PACIENTE -> {
                lblCardTitle.text = "Selecciona un paciente"
                lblCardSubtitle.text = "Busca y elige a quién deseas agendar la cita."
            }
            Paso.HORARIO -> {
                lblCardTitle.text = "Selecciona especialista y horario"
                lblCardSubtitle.text = "Elige doctor, fecha, hora y procedimiento."
            }
            Paso.RESUMEN -> {
                lblCardTitle.text = "Resumen de la cita"
                lblCardSubtitle.text = "Confirma que los datos sean correctos."
            }
        }
    }

    private fun updateStepUI() {
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
            Paso.PACIENTE -> setActive(step1Circle, step1Label)
            Paso.HORARIO -> {
                setDone(step1Circle, step1Label)
                stepLine12.styleClass.remove("wizard-step-line-state-idle")
                stepLine12.styleClass.add("wizard-step-line-state-done")
                setActive(step2Circle, step2Label)
            }
            Paso.RESUMEN -> {
                setDone(step1Circle, step1Label)
                setDone(step2Circle, step2Label)
                stepLine12.styleClass.remove("wizard-step-line-state-idle")
                stepLine12.styleClass.add("wizard-step-line-state-done")
                stepLine23.styleClass.remove("wizard-step-line-state-idle")
                stepLine23.styleClass.add("wizard-step-line-state-done")
                setActive(step3Circle, step3Label)
            }
        }
    }

    private fun syncSide(animate: Boolean = false) {
        val p = pacienteSel
        val d = doctorSel
        val fecha = datePicker.value
        val proc = cmbProcedimiento.value

        fun setLabel(label: Label, text: String) {
            if (label.text == text) return
            if (!animate) {
                label.text = text
                return
            }
            FadeTransition(Duration.millis(140.0), label).apply {
                fromValue = 0.35
                toValue = 1.0
                setOnFinished { label.opacity = 1.0 }
                play()
            }
            label.text = text
        }

        setLabel(lblSelPaciente, p?.nombre ?: "-")
        setLabel(lblSelPacienteId, p?.let { "ID: ${it.pacienteId}" } ?: "-")
        setLabel(lblSelDoctor, d?.nombre ?: "-")
        setLabel(lblSelEspecialidad, d?.especialidad ?: "-")
        setLabel(lblSelFecha, fecha?.format(fmtFecha) ?: "-")
        setLabel(lblSelHora, horaSel?.let { "Hora: $it" } ?: "-")
        setLabel(lblSelProcedimiento, proc ?: "-")

        val fee = d?.fee ?: 120.0
        lblFee.text = "Costo estimado  Q${"%.2f".format(fee)}"
    }

    private fun syncResumen() {
        val p = pacienteSel
        val d = doctorSel
        val fecha = datePicker.value
        val proc = cmbProcedimiento.value
        val notas = txtNotas.text?.trim().orEmpty()

        lblResumenPaciente.text = p?.let { "${it.nombre} (${it.pacienteId})" } ?: "-"
        lblResumenDoctor.text = d?.let { "${it.nombre} (${it.especialidad})" } ?: "-"
        lblResumenFecha.text = fecha?.format(fmtFecha) ?: "-"
        lblResumenHora.text = horaSel ?: "-"
        lblResumenProcedimiento.text = proc ?: "-"
        lblResumenNotas.text = if (notas.isBlank()) "Sin notas." else "Notas: $notas"
    }

    private fun confirmarCita() {
        lblConfirmStatus.text = ""
        if (!isReadyToConfirm()) {
            lblConfirmStatus.text = "Falta completar selección para confirmar."
            lblConfirmStatus.styleClass.remove("wizard-status-tone-ok")
            if (!lblConfirmStatus.styleClass.contains("wizard-status-tone-error")) lblConfirmStatus.styleClass.add("wizard-status-tone-error")
            return
        }

        val p = pacienteSel!!
        val d = doctorSel!!
        val fecha = datePicker.value!!
        val hora = horaSel!!
        val proc = cmbProcedimiento.value!!

        lblConfirmStatus.styleClass.remove("wizard-status-tone-error")
        if (!lblConfirmStatus.styleClass.contains("wizard-status-tone-ok")) lblConfirmStatus.styleClass.add("wizard-status-tone-ok")
        lblConfirmStatus.text = "Cita confirmada: ${p.nombre} con ${d.nombre} el ${fecha.format(fmtFecha)} a las $hora. ($proc)"

        FadeTransition(Duration.millis(180.0), lblConfirmStatus).apply {
            fromValue = 0.35
            toValue = 1.0
            interpolator = Interpolator.EASE_OUT
            play()
        }
    }

    private fun swapContainer(container: VBox, newNodes: List<Node>) {
        val out = ParallelTransition().apply {
            children.addAll(container.children.map { fadeSlide(it, out = true) })
            setOnFinished {
                container.children.setAll(newNodes)
                ParallelTransition().apply {
                    children.addAll(newNodes.map { fadeSlide(it, out = false) })
                    play()
                }
            }
        }
        out.play()
    }

    private fun fadeSlide(node: Node, out: Boolean): ParallelTransition {
        val fade = FadeTransition(Duration.millis(160.0), node).apply {
            if (out) {
                fromValue = node.opacity
                toValue = 0.0
            } else {
                node.opacity = 0.0
                fromValue = 0.0
                toValue = 1.0
            }
        }
        val slide = TranslateTransition(Duration.millis(160.0), node).apply {
            if (out) {
                fromY = 0.0
                toY = -6.0
            } else {
                node.translateY = 8.0
                fromY = 8.0
                toY = 0.0
            }
            interpolator = Interpolator.EASE_BOTH
        }
        return ParallelTransition(fade, slide)
    }
}


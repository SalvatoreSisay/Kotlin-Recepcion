package com.resdev.akrecepcion.recepcionui

import javafx.fxml.FXML
import javafx.geometry.Insets
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.layout.FlowPane
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import javafx.scene.layout.VBox

class PacienteRecurrenteEditController {
    @FXML private lateinit var lblTitulo: Label
    @FXML private lateinit var lblSubtitulo: Label
    @FXML private lateinit var lblEstado: Label

    @FXML private lateinit var btnCancelar: Button
    @FXML private lateinit var btnGuardar: Button

    // Info personal
    @FXML private lateinit var txtNombre: TextField
    @FXML private lateinit var txtPacienteId: TextField
    @FXML private lateinit var txtFechaNac: TextField
    @FXML private lateinit var txtTelefono: TextField
    @FXML private lateinit var txtEmail: TextField
    @FXML private lateinit var txtDireccion: TextField

    // Resumen visita
    @FXML private lateinit var lblUltimaVisitaFecha: Label
    @FXML private lateinit var lblUltimaVisitaTipo: Label
    @FXML private lateinit var lblUltimaVisitaTitulo: Label
    @FXML private lateinit var btnVerHistorial: Button

    // Alergias / Condiciones
    @FXML private lateinit var flowAlergias: FlowPane
    @FXML private lateinit var txtNuevaAlergia: TextField
    @FXML private lateinit var btnAgregarAlergia: Button

    @FXML private lateinit var flowCondiciones: FlowPane
    @FXML private lateinit var txtNuevaCondicion: TextField
    @FXML private lateinit var btnAgregarCondicion: Button

    // Medicamentos
    @FXML private lateinit var medsContainer: VBox
    @FXML private lateinit var txtMedNombre: TextField
    @FXML private lateinit var txtMedDosis: TextField
    @FXML private lateinit var txtMedFrecuencia: TextField
    @FXML private lateinit var btnAgregarMed: Button

    // Visitas (read-only)
    @FXML private lateinit var visitasContainer: VBox

    var onCancel: (() -> Unit)? = null
    var onSaved: ((pacienteId: String) -> Unit)? = null
    var onOpenHistorial: ((pacienteId: String) -> Unit)? = null

    private var current: PacienteRecurrenteStore.PacienteRecord? = null
    private var dirty: Boolean = false
    private var loading: Boolean = false

    @FXML
    private fun initialize() {
        btnCancelar.setOnAction {
            if (dirty) {
                lblEstado.text = "Tienes cambios sin guardar. Presiona Cancelar de nuevo para salir."
                dirty = false // segundo click sale sin bloquear
                return@setOnAction
            }
            onCancel?.invoke()
        }
        btnGuardar.setOnAction { guardar() }
        btnVerHistorial.setOnAction {
            val p = current ?: return@setOnAction
            onOpenHistorial?.invoke(p.pacienteId)
        }

        btnAgregarAlergia.setOnAction { agregarChip(tipo = ChipTipo.ALERGIA) }
        txtNuevaAlergia.setOnAction { agregarChip(tipo = ChipTipo.ALERGIA) }

        btnAgregarCondicion.setOnAction { agregarChip(tipo = ChipTipo.CONDICION) }
        txtNuevaCondicion.setOnAction { agregarChip(tipo = ChipTipo.CONDICION) }

        btnAgregarMed.setOnAction { agregarMedicamento() }
        txtMedFrecuencia.setOnAction { agregarMedicamento() }

        // marcar dirty cuando se editen campos principales
        listOf(txtNombre, txtFechaNac, txtTelefono, txtEmail, txtDireccion).forEach { tf ->
            tf.textProperty().addListener { _, _, _ -> markDirty() }
        }
    }

    fun openPaciente(pacienteId: String) {
        loading = true
        val rec = PacienteRecurrenteStore.findById(pacienteId)
        current = rec
        dirty = false
        lblEstado.text = ""

        if (rec == null) {
            lblTitulo.text = "Editar paciente"
            lblSubtitulo.text = "No se encontró el paciente con ID: $pacienteId"
            btnGuardar.isDisable = true
            loading = false
            return
        }

        btnGuardar.isDisable = false
        lblTitulo.text = "Editar información de paciente"
        lblSubtitulo.text = "Actualizando registro para ID: ${rec.pacienteId}"

        txtNombre.text = rec.nombre
        txtPacienteId.text = rec.pacienteId
        txtFechaNac.text = rec.fechaNacimiento
        txtTelefono.text = rec.telefono
        txtEmail.text = rec.email
        txtDireccion.text = rec.direccion

        lblUltimaVisitaFecha.text = rec.ultimaVisita
        lblUltimaVisitaTipo.text = if (rec.identidadVerificada) "Identidad verificada" else "Identidad pendiente"
        lblUltimaVisitaTitulo.text = rec.notaRapida

        txtNuevaAlergia.clear()
        txtNuevaCondicion.clear()
        paintAllChips()

        renderMedicamentos()
        renderVisitas()

        // campo ID siempre readonly visualmente
        if (!txtPacienteId.styleClass.contains("rpe-field-readonly")) txtPacienteId.styleClass.add("rpe-field-readonly")
        txtPacienteId.isEditable = false
        loading = false
    }

    private enum class ChipTipo { ALERGIA, CONDICION }

    private fun agregarChip(tipo: ChipTipo) {
        val rec = current ?: return
        val (tf, list) =
            when (tipo) {
                ChipTipo.ALERGIA -> txtNuevaAlergia to rec.alergias
                ChipTipo.CONDICION -> txtNuevaCondicion to rec.condicionesCronicas
            }

        val value = tf.text?.trim().orEmpty()
        if (value.isBlank()) return
        if (list.any { it.equals(value, ignoreCase = true) }) {
            tf.clear()
            lblEstado.text = "Ese valor ya existe."
            return
        }

        list.add(value)
        tf.clear()
        paintAllChips()
        markDirty()
    }

    private fun paintAllChips() {
        val rec = current ?: return
        renderChips(flowAlergias, rec.alergias) { v ->
            rec.alergias.remove(v)
            paintAllChips()
            markDirty()
        }
        renderChips(flowCondiciones, rec.condicionesCronicas) { v ->
            rec.condicionesCronicas.remove(v)
            paintAllChips()
            markDirty()
        }
    }

    private fun renderChips(container: FlowPane, items: List<String>, onRemove: ((String) -> Unit)?) {
        container.children.clear()
        if (items.isEmpty()) {
            container.children.add(Label("Sin registros.").apply { styleClass.add("muted") })
            return
        }

        items.forEach { value ->
            val chip = HBox(8.0).apply {
                styleClass.add("rpe-chip")
                padding = Insets(6.0, 10.0, 6.0, 10.0)
                children.add(Label(value).apply { styleClass.add("rpe-chip-text") })
                if (onRemove != null) {
                    children.add(
                        Button("x").apply {
                            styleClass.add("rpe-chip-remove")
                            setOnAction { onRemove.invoke(value) }
                        },
                    )
                }
            }
            container.children.add(chip)
        }
    }

    private fun agregarMedicamento() {
        val rec = current ?: return
        val nombre = txtMedNombre.text?.trim().orEmpty()
        val dosis = txtMedDosis.text?.trim().orEmpty()
        val freq = txtMedFrecuencia.text?.trim().orEmpty()

        if (nombre.isBlank() || dosis.isBlank() || freq.isBlank()) {
            lblEstado.text = "Completa nombre, dosis y frecuencia del medicamento."
            return
        }

        rec.medicamentos.add(PacienteRecurrenteStore.Medicamento(nombre = nombre, dosis = dosis, frecuencia = freq))
        txtMedNombre.clear()
        txtMedDosis.clear()
        txtMedFrecuencia.clear()
        renderMedicamentos()
        markDirty()
    }

    private fun renderMedicamentos() {
        val rec = current ?: return
        medsContainer.children.clear()

        if (rec.medicamentos.isEmpty()) {
            medsContainer.children.add(Label("Sin medicamentos activos.").apply { styleClass.add("muted") })
            return
        }

        rec.medicamentos.forEachIndexed { idx, m ->
            val row = HBox(10.0).apply {
                styleClass.add("rpe-table-row")
                padding = Insets(10.0, 10.0, 10.0, 10.0)
                children.addAll(
                    Label(m.nombre).apply { styleClass.add("rpe-table-cell") }.also { HBox.setHgrow(it, Priority.ALWAYS) },
                    Label(m.dosis).apply { styleClass.add("rpe-table-cell") },
                    Label(m.frecuencia).apply { styleClass.add("rpe-table-cell") }.also { HBox.setHgrow(it, Priority.ALWAYS) },
                    Button("Quitar").apply {
                        styleClass.add("rpe-danger-link")
                        setOnAction {
                            if (idx in rec.medicamentos.indices) {
                                rec.medicamentos.removeAt(idx)
                                renderMedicamentos()
                                markDirty()
                            }
                        }
                    },
                )
            }
            medsContainer.children.add(row)
        }
    }

    private fun renderVisitas() {
        val rec = current ?: return
        visitasContainer.children.clear()

        if (rec.visitasNotas.isEmpty()) {
            visitasContainer.children.add(Label("Sin visitas registradas.").apply { styleClass.add("muted") })
            return
        }

        rec.visitasNotas.forEach { v ->
            val box = VBox(6.0).apply {
                styleClass.add("rpe-visit-card")
                padding = Insets(12.0, 12.0, 12.0, 12.0)
                children.addAll(
                    HBox(10.0).apply {
                        children.addAll(
                            Label(v.fecha).apply { styleClass.add("rpe-visit-date") },
                            Label(v.tipo).apply { styleClass.add("muted") },
                            Region().apply { HBox.setHgrow(this, Priority.ALWAYS) },
                            Label(v.estado).apply { styleClass.add("rpe-visit-status") },
                        )
                    },
                    Label(v.titulo).apply { styleClass.add("rp-card-title-text-state-primary") },
                    Label(v.detalle).apply { styleClass.add("rp-note-text-state-primary"); isWrapText = true },
                )
            }
            visitasContainer.children.add(box)
        }
    }

    private fun guardar() {
        val rec = current ?: return
        lblEstado.text = ""

        val nombre = txtNombre.text?.trim().orEmpty()
        val fechaNac = txtFechaNac.text?.trim().orEmpty()
        val tel = txtTelefono.text?.trim().orEmpty()
        val email = txtEmail.text?.trim().orEmpty()
        val dir = txtDireccion.text?.trim().orEmpty()

        txtNombre.styleClass.remove("wizard-field-state-error")
        txtFechaNac.styleClass.remove("wizard-field-state-error")

        var ok = true
        if (nombre.isBlank()) {
            txtNombre.styleClass.add("wizard-field-state-error")
            ok = false
        }
        if (fechaNac.isBlank()) {
            txtFechaNac.styleClass.add("wizard-field-state-error")
            ok = false
        }
        if (!ok) {
            lblEstado.text = "Completa los campos obligatorios."
            return
        }

        rec.nombre = nombre
        rec.fechaNacimiento = fechaNac
        rec.telefono = tel
        rec.email = email
        rec.direccion = dir

        PacienteRecurrenteStore.update(rec)
        dirty = false
        lblEstado.text = "Cambios guardados."
        onSaved?.invoke(rec.pacienteId)
    }

    private fun markDirty() {
        if (loading) return
        dirty = true
        if (lblEstado.text == "Cambios guardados.") lblEstado.text = ""
    }
}

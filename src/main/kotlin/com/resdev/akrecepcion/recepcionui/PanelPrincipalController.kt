package com.resdev.akrecepcion.recepcionui

import javafx.collections.FXCollections
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.geometry.Insets
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import javafx.scene.Parent
import javafx.scene.control.Tooltip
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.javafx.*
import com.resdev.akrecepcion.recepcionui.db.DbHealthcheck
import com.resdev.akrecepcion.recepcionui.dao.jdbc.ActividadPacienteDaoJdbc
import com.resdev.akrecepcion.recepcionui.repository.ActividadRepository
import com.resdev.akrecepcion.recepcionui.repository.impl.ActividadRepositoryImpl

class PanelPrincipalController {
    var onLogout: (() -> Unit)? = null

    data class ActividadPacienteRow(
        val nombre: String,
        val pacienteId: String? = null,
        val tipo: String,
        val estado: String,
        val hora: String,
    )

    @FXML private lateinit var navContainer: VBox

    @FXML private lateinit var centerHost: StackPane
    @FXML private lateinit var dashboardView: VBox
    @FXML private lateinit var navViewHost: StackPane

    @FXML private lateinit var lblSaludo: Label
    @FXML private lateinit var lblResumen: Label

    @FXML private lateinit var actividadRowsContainer: VBox

    @FXML private lateinit var systemHealthCard: VBox
    @FXML private lateinit var lblSystemHealthStatus: Label
    @FXML private lateinit var lblSystemHealthSync: Label

    private val scope = CoroutineScope(Dispatchers.JavaFx + SupervisorJob())

    @FXML
    private fun initialize() {
        val nombre = AppSession.currentUser.nombreCompleto.trim().split(Regex("\\s+")).firstOrNull().orEmpty()
        lblSaludo.text = if (nombre.isBlank()) "Bienvenido de nuevo" else "Bienvenido de nuevo, $nombre"
        lblResumen.text =
            "Tu espacio digital para la atención del paciente. Registra nuevos ingresos y agiliza el flujo de recepción."

        refreshActividadRows()

        // Mark the first nav button as active by default.
        navContainer.children.filterIsInstance<Button>().firstOrNull()?.let { setNavActivo(it) }

        // Salud del sistema: se evalua async para no bloquear UI.
        updateSystemHealthChecking()
        scope.launch {
            val ok = withContext(Dispatchers.IO) { DbHealthcheck.ping().isSuccess }
            if (ok) updateSystemHealthOnline() else updateSystemHealthOffline()
        }
    }

    private val actividadRepo: ActividadRepository = ActividadRepositoryImpl(ActividadPacienteDaoJdbc())

    private fun refreshActividadRows() {
        // Carga async: si no hay DB o falla, deja la tabla vacía sin romper el dashboard.
        scope.launch {
            val rows =
                withContext(Dispatchers.IO) {
                    runCatching { actividadRepo.latestActividad(limit = 3) }.getOrDefault(emptyList())
                }

            renderActividadRows(
                rows.map {
                    ActividadPacienteRow(
                        nombre = it.nombre,
                        pacienteId = it.codigoUsuario.toString(),
                        tipo = it.tipo,
                        estado = it.estado,
                        hora = it.hora,
                    )
                },
            )
        }
    }

    private fun updateSystemHealthChecking() {
        // Estado intermedio (por si tarda la DB o el pool en inicializar).
        lblSystemHealthStatus.text = "Verificando conexión..."
        lblSystemHealthSync.text = "Sincronización en verificación"

        systemHealthCard.styleClass.remove("rose-card")
        if (!systemHealthCard.styleClass.contains("teal-card")) systemHealthCard.styleClass.add("teal-card")

        Tooltip.install(systemHealthCard, Tooltip("Probando conectividad con MariaDB (SELECT 1)."))
    }

    private fun updateSystemHealthOnline() {
        lblSystemHealthStatus.text = "Conexión de Base de datos activa"
        lblSystemHealthSync.text = "Sincronización segura activa"

        systemHealthCard.styleClass.remove("rose-card")
        if (!systemHealthCard.styleClass.contains("teal-card")) systemHealthCard.styleClass.add("teal-card")

        Tooltip.install(systemHealthCard, Tooltip("MariaDB conectada. Los cambios se guardarán normalmente."))
    }

    private fun updateSystemHealthOffline() {
        lblSystemHealthStatus.text = "Servidor sin conexión"
        lblSystemHealthSync.text = "Sincronización no segura, no se guardarán cambios"

        systemHealthCard.styleClass.remove("teal-card")
        if (!systemHealthCard.styleClass.contains("rose-card")) systemHealthCard.styleClass.add("rose-card")

        Tooltip.install(systemHealthCard, Tooltip("No hay conexión con MariaDB. Operación en modo offline (solo visual)."))
    }

    private fun renderActividadRows(items: List<ActividadPacienteRow>) {
        actividadRowsContainer.children.setAll(items.map { createActividadRow(it) })
    }

    private fun createActividadRow(r: ActividadPacienteRow): Node {
        val initials =
            r.nombre
                .trim()
                .split(Regex("\\s+"))
                .filter { it.isNotBlank() }
                .take(2)
                .map { it.first().uppercaseChar() }
                .joinToString("")
                .ifBlank { "P" }

        val avatar = StackPane(Label(initials).apply { styleClass.add("ps-avatar-text") }).apply {
            styleClass.add("ps-avatar")
            minWidth = 36.0
            minHeight = 36.0
            maxWidth = 36.0
            maxHeight = 36.0
        }

        val patientMeta = r.pacienteId?.takeIf { it.isNotBlank() }?.let { "ID: $it" } ?: " "
        val patientBlock = HBox(12.0).apply {
            children.addAll(
                avatar,
                VBox(2.0).apply {
                    children.addAll(
                        Label(r.nombre).apply { styleClass.add("ps-patient-name") },
                        Label(patientMeta).apply { styleClass.add("muted") },
                    )
                },
            )
            HBox.setHgrow(this, Priority.ALWAYS)
        }

        val tipoCell = VBox(2.0).apply {
            prefWidth = 130.0
            children.addAll(
                Label(r.tipo).apply { styleClass.add("ps-cell-strong") },
                Region().apply { minHeight = 1.0 },
            )
        }

        val statusChip = Label(r.estado).apply { styleClass.addAll("ps-status-chip", estadoToneClass(r.estado)) }
        val estadoCell = HBox(statusChip).apply {
            prefWidth = 130.0
            alignmentProperty().set(javafx.geometry.Pos.CENTER_LEFT)
        }

        val horaCell = VBox(2.0).apply {
            prefWidth = 90.0
            children.addAll(
                Label(r.hora).apply { styleClass.add("ps-cell-strong") },
                Region().apply { minHeight = 1.0 },
            )
        }

        val actions = HBox(8.0).apply {
            prefWidth = 120.0
            alignmentProperty().set(javafx.geometry.Pos.CENTER_RIGHT)
            children.add(
                Button("Ver").apply {
                    styleClass.add("ps-action-button")
                    setOnAction {
                        openPacienteFromActividad(r)
                    }
                },
            )
        }

        return HBox(0.0).apply {
            styleClass.add("ps-row")
            children.addAll(
                patientBlock,
                tipoCell,
                estadoCell,
                horaCell,
                actions,
            )
        }
    }

    private fun estadoToneClass(estado: String): String {
        val e = estado.trim().lowercase()
        return when {
            e.contains("verific") || e.contains("complet") || e.contains("list") -> "ps-status-chip-tone-ok"
            e.contains("proces") || e.contains("progres") || e.contains("pend") -> "ps-status-chip-tone-warn"
            else -> "ps-status-chip-tone-muted"
        }
    }

    @FXML
    private fun onNavClick(event: ActionEvent) {
        val boton = event.source as? Button ?: return
        // Solo los botones dentro de navContainer marcan estado "activo" de navegación.
        if (navContainer.children.contains(boton)) setNavActivo(boton)

        when ((boton.userData as? String)?.trim().orEmpty()) {
            "dashboard" -> showDashboard()
            "nuevo-paciente" -> showNuevoPaciente()
            "paciente-recurrente" -> showPacienteRecurrente()
            "busqueda-pacientes" -> showBusquedaPacientes()
            "agendar" -> showAgendarCita()
            "reportes" -> showReportes()
            "configuracion" -> showPlaceholder("Configuración", "Sección en desarrollo.")
            "ayuda" -> showPlaceholder("Ayuda", "Sección en desarrollo.")
            else -> {
                // Si no hay userData (por ejemplo, botones futuros), no cambiar la vista.
            }
        }
    }

    @FXML
    private fun onIniciarRegistro(@Suppress("UNUSED_PARAMETER") event: ActionEvent) {
        // Simula la navegación como si el usuario presionara "Nuevo Paciente" en el nav bar:
        // 1) marca activo
        // 2) cambia la vista
        navContainer.children
            .filterIsInstance<Button>()
            .firstOrNull { (it.userData as? String)?.trim() == "nuevo-paciente" }
            ?.let { setNavActivo(it) }
        showNuevoPaciente()
    }

    private fun setNavActivo(activo: Button) {
        navContainer.children.filterIsInstance<Button>().forEach { it.styleClass.remove("nav-button-active") }
        if (!activo.styleClass.contains("nav-button-active")) activo.styleClass.add("nav-button-active")
    }

    private var nuevoPacienteRoot: Parent? = null
    private var pacienteRecurrenteRoot: Parent? = null
    private var pacienteRecurrenteController: PacienteRecurrenteController? = null
    private var pacienteRecurrenteEditRoot: Parent? = null
    private var pacienteRecurrenteEditController: PacienteRecurrenteEditController? = null
    private var busquedaPacientesRoot: Parent? = null
    private var agendarCitaRoot: Parent? = null
    private var reportesRoot: Parent? = null
    private var reportesController: ReportesController? = null
    private var perfilRoot: Parent? = null
    private var perfilController: PerfilController? = null

    private fun showDashboard() {
        navViewHost.children.clear()
        navViewHost.isVisible = false
        navViewHost.isManaged = false
        dashboardView.isVisible = true
        dashboardView.isManaged = true

        // Al volver al dashboard, refresca “Actividad de hoy”.
        refreshActividadRows()
    }

    private fun showNuevoPaciente() {
        if (nuevoPacienteRoot == null) {
            val url = PanelPrincipalController::class.java.getResource("/com/resdev/akrecepcion/recepcionui/nuevo-paciente-view.fxml")
            val loader = FXMLLoader(url)
            nuevoPacienteRoot = loader.load()
        }

        dashboardView.isVisible = false
        dashboardView.isManaged = false
        navViewHost.children.setAll(nuevoPacienteRoot)
        navViewHost.isVisible = true
        navViewHost.isManaged = true
    }

    private fun showPacienteRecurrente() {
        if (pacienteRecurrenteRoot == null) {
            val url = PanelPrincipalController::class.java.getResource("/com/resdev/akrecepcion/recepcionui/paciente-recurrente-view.fxml")
            val loader = FXMLLoader(url)
            pacienteRecurrenteRoot = loader.load()
            pacienteRecurrenteController = loader.getController()
            pacienteRecurrenteController?.onEditarPaciente = { pacienteId ->
                showPacienteRecurrenteEdit(pacienteId)
            }
        }

        // Cada vez que entramos a la vista, recarga los últimos 3.
        pacienteRecurrenteController?.onViewShown()

        dashboardView.isVisible = false
        dashboardView.isManaged = false
        navViewHost.children.setAll(pacienteRecurrenteRoot)
        navViewHost.isVisible = true
        navViewHost.isManaged = true
    }

    private fun showPacienteRecurrenteEdit(pacienteId: String) {
        if (pacienteRecurrenteEditRoot == null) {
            val url =
                PanelPrincipalController::class.java.getResource(
                    "/com/resdev/akrecepcion/recepcionui/paciente-recurrente-edit-view.fxml",
                )
            val loader = FXMLLoader(url)
            pacienteRecurrenteEditRoot = loader.load()
            pacienteRecurrenteEditController = loader.getController()
            pacienteRecurrenteEditController?.onCancel = {
                showPacienteRecurrente()
                pacienteRecurrenteController?.refresh()
            }
            pacienteRecurrenteEditController?.onSaved = {
                // Guardar no navega: el usuario puede seguir editando o presionar "Cancelar" para volver.
                // El store ya quedó actualizado; cuando se vuelva al listado se re-renderiza con refresh().
                pacienteRecurrenteController?.refresh()
            }
            // Historial aún vive como “demo” en la vista de recurrente; por ahora vuelve y selecciona.
            pacienteRecurrenteEditController?.onOpenHistorial = {
                showPacienteRecurrente()
                pacienteRecurrenteController?.refresh()
                pacienteRecurrenteController?.openPaciente(pacienteId = it, nombre = null)
            }
        }

        dashboardView.isVisible = false
        dashboardView.isManaged = false
        navViewHost.children.setAll(pacienteRecurrenteEditRoot)
        navViewHost.isVisible = true
        navViewHost.isManaged = true

        pacienteRecurrenteEditController?.openPaciente(pacienteId)
    }

    private fun openPacienteFromActividad(r: ActividadPacienteRow) {
        // En la app, el “perfil/historial” más cercano hoy está en la vista de paciente recurrente.
        navContainer.children
            .filterIsInstance<Button>()
            .firstOrNull { (it.userData as? String)?.trim() == "paciente-recurrente" }
            ?.let { setNavActivo(it) }
        showPacienteRecurrente()
        pacienteRecurrenteController?.openPaciente(pacienteId = r.pacienteId, nombre = r.nombre)
    }

    @FXML
    private fun onRecuperarPerfil(@Suppress("UNUSED_PARAMETER") event: ActionEvent) {
        navContainer.children
            .filterIsInstance<Button>()
            .firstOrNull { (it.userData as? String)?.trim() == "paciente-recurrente" }
            ?.let { setNavActivo(it) }
        showPacienteRecurrente()
    }

    private fun showBusquedaPacientes() {
        if (busquedaPacientesRoot == null) {
            val url = PanelPrincipalController::class.java.getResource("/com/resdev/akrecepcion/recepcionui/busqueda-pacientes-view.fxml")
            val loader = FXMLLoader(url)
            busquedaPacientesRoot = loader.load()
        }

        dashboardView.isVisible = false
        dashboardView.isManaged = false
        navViewHost.children.setAll(busquedaPacientesRoot)
        navViewHost.isVisible = true
        navViewHost.isManaged = true
    }

    private fun showAgendarCita() {
        if (agendarCitaRoot == null) {
            val url = PanelPrincipalController::class.java.getResource("/com/resdev/akrecepcion/recepcionui/agendar-cita-view.fxml")
            val loader = FXMLLoader(url)
            agendarCitaRoot = loader.load()
        }

        dashboardView.isVisible = false
        dashboardView.isManaged = false
        navViewHost.children.setAll(agendarCitaRoot)
        navViewHost.isVisible = true
        navViewHost.isManaged = true
    }

    private fun showReportes() {
        if (reportesRoot == null) {
            val url = PanelPrincipalController::class.java.getResource("/com/resdev/akrecepcion/recepcionui/reportes-view.fxml")
            val loader = FXMLLoader(url)
            reportesRoot = loader.load()
            reportesController = loader.getController()
        }

        reportesController?.refresh()
        dashboardView.isVisible = false
        dashboardView.isManaged = false
        navViewHost.children.setAll(reportesRoot)
        navViewHost.isVisible = true
        navViewHost.isManaged = true
    }

    private fun showPlaceholder(titulo: String, descripcion: String) {
        val box = VBox(6.0).apply {
            styleClass.add("content")
            padding = Insets(22.0, 18.0, 22.0, 18.0)
            children.addAll(
                Label(titulo).apply { styleClass.add("h1") },
                Label(descripcion).apply { styleClass.add("muted") },
            )
        }
        dashboardView.isVisible = false
        dashboardView.isManaged = false
        navViewHost.children.setAll(box)
        navViewHost.isVisible = true
        navViewHost.isManaged = true
    }

    @FXML
    private fun onPerfil(@Suppress("UNUSED_PARAMETER") event: ActionEvent) {
        showPerfil()
    }

    private fun showPerfil() {
        if (perfilRoot == null) {
            val url = PanelPrincipalController::class.java.getResource("/com/resdev/akrecepcion/recepcionui/perfil-view.fxml")
            val loader = FXMLLoader(url)
            perfilRoot = loader.load()
            perfilController =
                loader.getController<PerfilController>().also {
                    it.onBackToDashboard = {
                        navContainer.children.filterIsInstance<Button>().firstOrNull()?.let { first -> setNavActivo(first) }
                        showDashboard()
                    }
                }
        }

        perfilController?.refresh()
        dashboardView.isVisible = false
        dashboardView.isManaged = false
        navViewHost.children.setAll(perfilRoot)
        navViewHost.isVisible = true
        navViewHost.isManaged = true
    }

    @FXML
    private fun onCerrarSesion(@Suppress("UNUSED_PARAMETER") event: ActionEvent) {
        onLogout?.invoke()
    }
}

package com.resdev.akrecepcion.recepcionui

import javafx.collections.FXCollections
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.geometry.Insets
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import javafx.scene.Parent

class PanelPrincipalController {
    var onLogout: (() -> Unit)? = null

    data class ActividadPacienteRow(
        val nombre: String,
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

    @FXML private lateinit var tablaActividad: TableView<ActividadPacienteRow>
    @FXML private lateinit var colNombre: TableColumn<ActividadPacienteRow, String>
    @FXML private lateinit var colTipo: TableColumn<ActividadPacienteRow, String>
    @FXML private lateinit var colEstado: TableColumn<ActividadPacienteRow, String>
    @FXML private lateinit var colHora: TableColumn<ActividadPacienteRow, String>

    @FXML
    private fun initialize() {
        val nombre = AppSession.currentUser.nombreCompleto.trim().split(Regex("\\s+")).firstOrNull().orEmpty()
        lblSaludo.text = if (nombre.isBlank()) "Bienvenido de nuevo" else "Bienvenido de nuevo, $nombre"
        lblResumen.text =
            "Tu espacio digital para la atención del paciente. Registra nuevos ingresos y agiliza el flujo de recepción."

        colNombre.cellValueFactory = PropertyValueFactory("nombre")
        colTipo.cellValueFactory = PropertyValueFactory("tipo")
        colEstado.cellValueFactory = PropertyValueFactory("estado")
        colHora.cellValueFactory = PropertyValueFactory("hora")

        tablaActividad.items = FXCollections.observableArrayList(
            ActividadPacienteRow("Elena Rodriguez", "Recurrente", "Verificado", "09:15"),
            ActividadPacienteRow("Marcus Chen", "Nuevo", "Procesando", "10:04"),
            ActividadPacienteRow("Sarah Jenkins", "Recurrente", "Verificado", "10:30"),
        )

        // Mark the first nav button as active by default.
        navContainer.children.filterIsInstance<Button>().firstOrNull()?.let { setNavActivo(it) }
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
            "reportes" -> showPlaceholder("Reportes", "Sección en desarrollo.")
            "configuracion" -> showPlaceholder("Configuración", "Sección en desarrollo.")
            "ayuda" -> showPlaceholder("Ayuda", "Sección en desarrollo.")
            else -> {
                // Si no hay userData (por ejemplo, botones futuros), no cambiar la vista.
            }
        }
    }

    private fun setNavActivo(activo: Button) {
        navContainer.children.filterIsInstance<Button>().forEach { it.styleClass.remove("nav-button-active") }
        if (!activo.styleClass.contains("nav-button-active")) activo.styleClass.add("nav-button-active")
    }

    private var nuevoPacienteRoot: Parent? = null
    private var pacienteRecurrenteRoot: Parent? = null
    private var busquedaPacientesRoot: Parent? = null
    private var agendarCitaRoot: Parent? = null
    private var perfilRoot: Parent? = null
    private var perfilController: PerfilController? = null

    private fun showDashboard() {
        navViewHost.children.clear()
        navViewHost.isVisible = false
        navViewHost.isManaged = false
        dashboardView.isVisible = true
        dashboardView.isManaged = true
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
        }

        dashboardView.isVisible = false
        dashboardView.isManaged = false
        navViewHost.children.setAll(pacienteRecurrenteRoot)
        navViewHost.isVisible = true
        navViewHost.isManaged = true
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

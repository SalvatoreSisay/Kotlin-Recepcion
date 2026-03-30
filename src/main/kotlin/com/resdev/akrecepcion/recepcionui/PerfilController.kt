package com.resdev.akrecepcion.recepcionui

import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.scene.control.CheckBox
import javafx.scene.control.ComboBox
import javafx.scene.control.Label
import javafx.scene.control.PasswordField
import javafx.scene.control.TextField
import javafx.scene.input.Clipboard
import javafx.scene.input.ClipboardContent
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class PerfilController {
    var onBackToDashboard: (() -> Unit)? = null

    @FXML private lateinit var lblAvatarInitials: Label
    @FXML private lateinit var lblNombreResumen: Label
    @FXML private lateinit var lblRolResumen: Label
    @FXML private lateinit var lblEstadoResumen: Label
    @FXML private lateinit var lblUltimaActualizacion: Label

    @FXML private lateinit var txtUsuarioReadOnly: TextField
    @FXML private lateinit var txtCorreoReadOnly: TextField

    @FXML private lateinit var lblPerfilStatus: Label
    @FXML private lateinit var txtNombreCompleto: TextField
    @FXML private lateinit var cmbDepartamento: ComboBox<String>
    @FXML private lateinit var txtCorreo: TextField
    @FXML private lateinit var txtTelefono: TextField
    @FXML private lateinit var txtUsuario: TextField
    @FXML private lateinit var cmbRol: ComboBox<String>
    @FXML private lateinit var chkNotificaciones: CheckBox

    @FXML private lateinit var lblPasswordStatus: Label
    @FXML private lateinit var txtPassActual: PasswordField
    @FXML private lateinit var txtPassNueva: PasswordField
    @FXML private lateinit var txtPassConfirmar: PasswordField

    private val timestampFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")

    @FXML
    private fun initialize() {
        cmbRol.items.setAll(
            "Recepción",
            "Enfermería",
            "Caja",
            "Administración",
            "Soporte",
        )
        cmbDepartamento.items.setAll(
            "Admisiones",
            "Emergencias",
            "Consulta externa",
            "Laboratorio",
            "Radiología",
        )

        renderFromSession()
        clearStatuses()
    }

    fun refresh() {
        renderFromSession()
        clearStatuses()
    }

    private fun renderFromSession() {
        val u = AppSession.currentUser

        lblNombreResumen.text = u.nombreCompleto
        lblRolResumen.text = u.rol
        lblEstadoResumen.text = u.estado

        val initials = initials(u.nombreCompleto)
        lblAvatarInitials.text = initials

        txtUsuarioReadOnly.text = u.usuario
        txtCorreoReadOnly.text = u.correo

        txtNombreCompleto.text = u.nombreCompleto
        cmbDepartamento.value = u.departamento
        txtCorreo.text = u.correo
        txtTelefono.text = u.telefono
        txtUsuario.text = u.usuario
        cmbRol.value = u.rol
        chkNotificaciones.isSelected = u.notificaciones

        lblUltimaActualizacion.text = "Última actualización: ${formatTime(u.lastUpdated)}"
    }

    private fun clearStatuses() {
        lblPerfilStatus.text = ""
        lblPasswordStatus.text = ""
        lblPerfilStatus.styleClass.removeAll("wizard-status-tone-ok", "wizard-status-tone-error")
        lblPasswordStatus.styleClass.removeAll("wizard-status-tone-ok", "wizard-status-tone-error")
    }

    private fun setOk(label: Label, message: String) {
        label.text = message
        label.styleClass.removeAll("wizard-status-tone-error")
        if (!label.styleClass.contains("wizard-status-tone-ok")) label.styleClass.add("wizard-status-tone-ok")
    }

    private fun setError(label: Label, message: String) {
        label.text = message
        label.styleClass.removeAll("wizard-status-tone-ok")
        if (!label.styleClass.contains("wizard-status-tone-error")) label.styleClass.add("wizard-status-tone-error")
    }

    @FXML
    private fun onVolverInicio(@Suppress("UNUSED_PARAMETER") event: ActionEvent) {
        onBackToDashboard?.invoke()
    }

    @FXML
    private fun onCopiarUsuario(@Suppress("UNUSED_PARAMETER") event: ActionEvent) {
        copyToClipboard(AppSession.currentUser.usuario)
        setOk(lblPerfilStatus, "Usuario copiado.")
    }

    @FXML
    private fun onCopiarCorreo(@Suppress("UNUSED_PARAMETER") event: ActionEvent) {
        copyToClipboard(AppSession.currentUser.correo)
        setOk(lblPerfilStatus, "Correo copiado.")
    }

    @FXML
    private fun onRestablecer(@Suppress("UNUSED_PARAMETER") event: ActionEvent) {
        renderFromSession()
        setOk(lblPerfilStatus, "Cambios restablecidos.")
    }

    @FXML
    private fun onGuardarPerfil(@Suppress("UNUSED_PARAMETER") event: ActionEvent) {
        clearStatuses()

        val nombre = txtNombreCompleto.text?.trim().orEmpty()
        val correo = txtCorreo.text?.trim().orEmpty()
        val telefono = txtTelefono.text?.trim().orEmpty()
        val departamento = (cmbDepartamento.value ?: "").trim()
        val rol = (cmbRol.value ?: "").trim()
        val notifs = chkNotificaciones.isSelected

        if (nombre.isBlank()) {
            setError(lblPerfilStatus, "El nombre completo es obligatorio.")
            txtNombreCompleto.requestFocus()
            return
        }
        if (correo.isBlank() || !correo.contains("@")) {
            setError(lblPerfilStatus, "Ingresa un correo válido.")
            txtCorreo.requestFocus()
            return
        }
        if (rol.isBlank()) {
            setError(lblPerfilStatus, "Selecciona un rol.")
            return
        }
        if (departamento.isBlank()) {
            setError(lblPerfilStatus, "Selecciona un departamento.")
            return
        }

        AppSession.currentUser =
            AppSession.currentUser.copy(
                nombreCompleto = nombre,
                correo = correo,
                telefono = telefono,
                rol = rol,
                departamento = departamento,
                notificaciones = notifs,
                lastUpdated = LocalDateTime.now(),
            )

        renderFromSession()
        setOk(lblPerfilStatus, "Perfil actualizado.")
    }

    @FXML
    private fun onActualizarPassword(@Suppress("UNUSED_PARAMETER") event: ActionEvent) {
        clearStatuses()

        val actual = txtPassActual.text.orEmpty()
        val nueva = txtPassNueva.text.orEmpty()
        val confirmar = txtPassConfirmar.text.orEmpty()

        if (actual != AppSession.password) {
            setError(lblPasswordStatus, "La contraseña actual no coincide.")
            txtPassActual.requestFocus()
            return
        }
        if (nueva.length < 4) {
            setError(lblPasswordStatus, "La nueva contraseña debe tener al menos 4 caracteres.")
            txtPassNueva.requestFocus()
            return
        }
        if (nueva != confirmar) {
            setError(lblPasswordStatus, "La confirmación no coincide.")
            txtPassConfirmar.requestFocus()
            return
        }

        AppSession.updatePassword(nueva)
        txtPassActual.clear()
        txtPassNueva.clear()
        txtPassConfirmar.clear()
        setOk(lblPasswordStatus, "Contraseña actualizada (sesión actual).")
    }

    private fun copyToClipboard(text: String) {
        val content = ClipboardContent()
        content.putString(text)
        Clipboard.getSystemClipboard().setContent(content)
    }

    private fun initials(nombre: String): String {
        val parts = nombre.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
        if (parts.isEmpty()) return "U"
        val a = parts.first().firstOrNull()?.uppercaseChar() ?: 'U'
        val b = parts.getOrNull(1)?.firstOrNull()?.uppercaseChar()
        return if (b == null) "$a" else "$a$b"
    }

    private fun formatTime(t: LocalDateTime): String = t.format(timestampFmt)
}

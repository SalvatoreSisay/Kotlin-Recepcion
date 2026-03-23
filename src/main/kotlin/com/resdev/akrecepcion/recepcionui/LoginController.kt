package com.resdev.akrecepcion.recepcionui

import javafx.animation.FadeTransition
import javafx.animation.ParallelTransition
import javafx.animation.ScaleTransition
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.PasswordField
import javafx.scene.control.TextField
import javafx.scene.control.TextInputControl
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.layout.HBox
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import javafx.util.Duration

class LoginController {
    var onLoginSuccess: (() -> Unit)? = null

    @FXML private lateinit var txtUsuario: TextField
    @FXML private lateinit var txtContrasena: PasswordField
    @FXML private lateinit var txtContrasenaVisible: TextField
    @FXML private lateinit var usuarioWrap: HBox
    @FXML private lateinit var contrasenaWrap: HBox

    @FXML private lateinit var popupOverlay: StackPane
    @FXML private lateinit var popupCard: VBox
    @FXML private lateinit var lblPopupMensaje: Label
    @FXML private lateinit var btnPopupAceptar: Button

    @FXML
    private fun initialize() {
        popupOverlay.isVisible = false
        popupOverlay.isManaged = false

        txtContrasenaVisible.isVisible = false
        txtContrasenaVisible.isManaged = false
        txtContrasenaVisible.textProperty().bindBidirectional(txtContrasena.textProperty())

        installFocusClass(usuarioWrap, txtUsuario)
        installFocusClass(contrasenaWrap, txtContrasena, txtContrasenaVisible)

        // Quick UX: Enter triggers login from either field.
        txtUsuario.addEventFilter(KeyEvent.KEY_PRESSED) { if (it.code == KeyCode.ENTER) intentarLogin() }
        txtContrasena.addEventFilter(KeyEvent.KEY_PRESSED) { if (it.code == KeyCode.ENTER) intentarLogin() }
        txtContrasenaVisible.addEventFilter(KeyEvent.KEY_PRESSED) { if (it.code == KeyCode.ENTER) intentarLogin() }
    }

    private fun installFocusClass(container: HBox, vararg fields: TextInputControl) {
        fun update() {
            val focused = fields.any { it.isFocused }
            container.styleClass.remove("field-wrap-focused")
            if (focused) container.styleClass.add("field-wrap-focused")
        }
        fields.forEach { it.focusedProperty().addListener { _, _, _ -> update() } }
        update()
    }

    @FXML
    private fun onLoginClick(@Suppress("UNUSED_PARAMETER") event: ActionEvent) {
        intentarLogin()
    }

    private fun intentarLogin() {
        val usuario = txtUsuario.text?.trim().orEmpty()
        val contrasena = txtContrasena.text.orEmpty()

        if (usuario == "admin" && contrasena == "1234") {
            onLoginSuccess?.invoke()
            return
        }

        mostrarErrorCredenciales()
    }

    private fun mostrarErrorCredenciales() {
        lblPopupMensaje.text = "A ingresado mal el usuario o contraseña, intente de nuevo"
        popupOverlay.isVisible = true
        popupOverlay.isManaged = true

        popupCard.opacity = 0.0
        popupCard.scaleX = 0.92
        popupCard.scaleY = 0.92

        val fade = FadeTransition(Duration.millis(180.0), popupCard).apply {
            fromValue = 0.0
            toValue = 1.0
        }
        val scale = ScaleTransition(Duration.millis(180.0), popupCard).apply {
            fromX = 0.92
            fromY = 0.92
            toX = 1.0
            toY = 1.0
        }
        ParallelTransition(fade, scale).play()
    }

    @FXML
    private fun onTogglePassword(@Suppress("UNUSED_PARAMETER") event: ActionEvent) {
        val mostrando = txtContrasenaVisible.isVisible
        txtContrasenaVisible.isVisible = !mostrando
        txtContrasenaVisible.isManaged = !mostrando
        txtContrasena.isVisible = mostrando
        txtContrasena.isManaged = mostrando

        if (txtContrasenaVisible.isVisible) {
            txtContrasenaVisible.requestFocus()
            txtContrasenaVisible.positionCaret(txtContrasenaVisible.text.length)
        } else {
            txtContrasena.requestFocus()
            txtContrasena.positionCaret(txtContrasena.text.length)
        }
    }

    @FXML
    private fun onPopupAceptar(@Suppress("UNUSED_PARAMETER") event: ActionEvent) {
        val fade = FadeTransition(Duration.millis(140.0), popupCard).apply {
            fromValue = popupCard.opacity
            toValue = 0.0
        }
        val scale = ScaleTransition(Duration.millis(140.0), popupCard).apply {
            fromX = popupCard.scaleX
            fromY = popupCard.scaleY
            toX = 0.96
            toY = 0.96
        }

        ParallelTransition(fade, scale).apply {
            setOnFinished {
                popupOverlay.isVisible = false
                popupOverlay.isManaged = false
                (if (txtContrasenaVisible.isVisible) txtContrasenaVisible else txtContrasena).apply {
                    requestFocus()
                    selectAll()
                }
            }
            play()
        }
    }
}

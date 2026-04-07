package com.resdev.akrecepcion.recepcionui

import javafx.application.Application
import javafx.animation.FadeTransition
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.scene.Parent
import javafx.stage.Stage
import javafx.util.Duration
import com.resdev.akrecepcion.recepcionui.db.Env
import com.resdev.akrecepcion.recepcionui.db.DataSourceProvider

class RecepcionApplication : Application() {
    private companion object {
        private const val RES_BASE = "/com/resdev/akrecepcion/recepcionui/"
        private const val LOGIN_FXML = "${RES_BASE}login-view.fxml"
        private const val PANEL_FXML = "${RES_BASE}panel-principal-view.fxml"
    }

    override fun start(stage: Stage) {
        // Paso 3: carga opcional de ".env" para desarrollo local (no es requerido en prod).
        Env.preload()

        val scene = Scene(loadLogin(stage), 1200.0, 760.0)
        stage.title = "Recepción UI"
        stage.scene = scene
        stage.minWidth = 980.0
        stage.minHeight = 640.0
        stage.show()
    }

    override fun stop() {
        // Cierra el pool para liberar recursos al salir.
        DataSourceProvider.close()
    }

    private fun showPanelPrincipal(stage: Stage) {
        val loader = FXMLLoader(RecepcionApplication::class.java.getResource(PANEL_FXML))
        val root = loader.load<Parent>()
        val controller = loader.getController<PanelPrincipalController>()
        controller.onLogout = { showLogin(stage) }
        root.opacity = 0.0

        stage.scene.root = root
        stage.minWidth = 1024.0
        stage.minHeight = 680.0

        FadeTransition(Duration.millis(320.0), root).apply {
            fromValue = 0.0
            toValue = 1.0
            play()
        }
    }

    private fun showLogin(stage: Stage) {
        val root = loadLogin(stage)
        root.opacity = 0.0

        stage.scene.root = root
        stage.minWidth = 980.0
        stage.minHeight = 640.0

        FadeTransition(Duration.millis(260.0), root).apply {
            fromValue = 0.0
            toValue = 1.0
            play()
        }
    }

    private fun loadLogin(stage: Stage): Parent {
        val loginLoader = FXMLLoader(RecepcionApplication::class.java.getResource(LOGIN_FXML))
        val loginRoot = loginLoader.load<Parent>()
        val loginController = loginLoader.getController<LoginController>()
        loginController.onLoginSuccess = { showPanelPrincipal(stage) }
        return loginRoot
    }
}

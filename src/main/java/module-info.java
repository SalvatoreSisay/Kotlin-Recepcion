module com.resdev.akrecepcion.recepcionui {
    requires javafx.controls;
    requires javafx.fxml;
    requires kotlin.stdlib;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.ikonli.javafx;

    opens com.resdev.akrecepcion.recepcionui to javafx.fxml;
    exports com.resdev.akrecepcion.recepcionui;
}
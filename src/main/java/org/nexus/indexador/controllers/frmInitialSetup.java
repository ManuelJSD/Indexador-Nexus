package org.nexus.indexador.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.nexus.indexador.utils.ConfigManager;

import java.io.File;
import java.io.IOException;

/**
 * Controller para el asistente de configuración inicial.
 */
public class frmInitialSetup {

    @FXML
    private TextField txtGraphicsPath;

    @FXML
    private TextField txtInitPath;

    @FXML
    private TextField txtDatPath;

    @FXML
    private TextField txtExportPath;

    private Stage stage;
    private Runnable onComplete;

    /**
     * Inicializa el controller con el stage y callback.
     */
    public void setStage(Stage stage) {
        this.stage = stage;
    }

    /**
     * Establece el callback a ejecutar cuando se complete la configuración.
     */
    public void setOnComplete(Runnable onComplete) {
        this.onComplete = onComplete;
    }

    /**
     * Examinar carpeta de gráficos.
     */
    @FXML
    private void onBrowseGraphics() {
        String path = browseDirectory("Seleccionar carpeta de Gráficos");
        if (path != null) {
            txtGraphicsPath.setText(path);
        }
    }

    /**
     * Examinar carpeta de Init.
     */
    @FXML
    private void onBrowseInit() {
        String path = browseDirectory("Seleccionar carpeta de Init");
        if (path != null) {
            txtInitPath.setText(path);
        }
    }

    /**
     * Examinar carpeta de Dat.
     */
    @FXML
    private void onBrowseDat() {
        String path = browseDirectory("Seleccionar carpeta de Dat");
        if (path != null) {
            txtDatPath.setText(path);
        }
    }

    /**
     * Examinar carpeta de Exportados.
     */
    @FXML
    private void onBrowseExport() {
        String path = browseDirectory("Seleccionar carpeta de Exportados");
        if (path != null) {
            txtExportPath.setText(path);
        }
    }

    /**
     * Método auxiliar para abrir DirectoryChooser.
     */
    private String browseDirectory(String title) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle(title);

        File selected = chooser.showDialog(stage);
        return selected != null ? selected.getAbsolutePath() : null;
    }

    /**
     * Finalizar configuración y guardar.
     */
    @FXML
    private void onFinish() {
        // Validar que todas las rutas estén configuradas
        if (txtGraphicsPath.getText().isEmpty() ||
                txtInitPath.getText().isEmpty() ||
                txtDatPath.getText().isEmpty() ||
                txtExportPath.getText().isEmpty()) {

            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Configuración Incompleta");
            alert.setHeaderText("Por favor, configure todas las rutas");
            alert.setContentText("Todas las carpetas son necesarias para el correcto funcionamiento de la aplicación.");
            alert.showAndWait();
            return;
        }

        // Guardar configuración
        ConfigManager config = ConfigManager.getInstance();
        config.setGraphicsDir(txtGraphicsPath.getText());
        config.setInitDir(txtInitPath.getText());
        config.setDatDir(txtDatPath.getText());
        config.setExportDir(txtExportPath.getText());

        try {
            config.writeConfig();

            // Cerrar wizard y continuar con la aplicación
            stage.close();

            // Ejecutar callback si existe
            if (onComplete != null) {
                Platform.runLater(onComplete);
            }
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error al Guardar");
            alert.setHeaderText("No se pudo guardar la configuración");
            alert.setContentText("Error: " + e.getMessage());
            alert.showAndWait();
        }
    }
}

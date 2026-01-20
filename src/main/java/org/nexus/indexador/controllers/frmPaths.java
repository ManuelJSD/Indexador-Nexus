package org.nexus.indexador.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.nexus.indexador.utils.ConfigManager;

import java.io.File;
import java.io.IOException;

/**
 * Controller para la ventana de configuración de rutas.
 */
public class frmPaths {

    @FXML
    private TextField txtGraphicsPath;

    @FXML
    private TextField txtInitPath;

    @FXML
    private TextField txtDatPath;

    @FXML
    private TextField txtExportPath;

    private Stage stage;
    private ConfigManager configManager;

    /**
     * Inicializa el controller con el stage.
     */
    public void setStage(Stage stage) {
        this.stage = stage;
        loadCurrentPaths();
    }

    /**
     * Carga las rutas actuales desde el ConfigManager.
     */
    private void loadCurrentPaths() {
        configManager = ConfigManager.getInstance();
        txtGraphicsPath.setText(configManager.getGraphicsDir());
        txtInitPath.setText(configManager.getInitDir());
        txtDatPath.setText(configManager.getDatDir());
        txtExportPath.setText(configManager.getExportDir());
    }

    /**
     * Examinar carpeta de gráficos.
     */
    @FXML
    private void onBrowseGraphics() {
        String path = browseDirectory("Seleccionar carpeta de Gráficos", txtGraphicsPath.getText());
        if (path != null) {
            txtGraphicsPath.setText(path);
        }
    }

    /**
     * Examinar carpeta de Init.
     */
    @FXML
    private void onBrowseInit() {
        String path = browseDirectory("Seleccionar carpeta de Init", txtInitPath.getText());
        if (path != null) {
            txtInitPath.setText(path);
        }
    }

    /**
     * Examinar carpeta de Dat.
     */
    @FXML
    private void onBrowseDat() {
        String path = browseDirectory("Seleccionar carpeta de Dat", txtDatPath.getText());
        if (path != null) {
            txtDatPath.setText(path);
        }
    }

    /**
     * Examinar carpeta de Exportados.
     */
    @FXML
    private void onBrowseExport() {
        String path = browseDirectory("Seleccionar carpeta de Exportados", txtExportPath.getText());
        if (path != null) {
            txtExportPath.setText(path);
        }
    }

    /**
     * Método auxiliar para abrir DirectoryChooser.
     */
    private String browseDirectory(String title, String currentPath) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle(title);

        if (currentPath != null && !currentPath.isEmpty()) {
            File initialDir = new File(currentPath);
            if (initialDir.exists()) {
                chooser.setInitialDirectory(initialDir);
            }
        }

        File selected = chooser.showDialog(stage);
        return selected != null ? selected.getAbsolutePath() : null;
    }

    /**
     * Guardar cambios.
     */
    @FXML
    private void onSave() {
        configManager.setGraphicsDir(txtGraphicsPath.getText());
        configManager.setInitDir(txtInitPath.getText());
        configManager.setDatDir(txtDatPath.getText());
        configManager.setExportDir(txtExportPath.getText());

        try {
            configManager.writeConfig();
            stage.close();
        } catch (IOException e) {
            // TODO: Mostrar error al usuario
            e.printStackTrace();
        }
    }

    /**
     * Cancelar cambios.
     */
    @FXML
    private void onCancel() {
        stage.close();
    }
}

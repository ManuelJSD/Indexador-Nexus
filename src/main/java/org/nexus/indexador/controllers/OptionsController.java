package org.nexus.indexador.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;

import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.nexus.indexador.utils.ConfigManager;
import org.nexus.indexador.utils.WindowManager;

import java.io.File;
import java.io.IOException;

/**
 * Controller para la ventana de Opciones (Rutas y Apariencia).
 */
public class OptionsController {

    @FXML
    private TextField txtGraphicsPath;

    @FXML
    private TextField txtInitPath;

    @FXML
    private TextField txtDatPath;

    @FXML
    private TextField txtExportPath;

    @FXML
    private ComboBox<String> cmbTheme;

    private ConfigManager configManager;

    /**
     * Inicializa el controller.
     */
    @FXML
    public void initialize() {
        loadCurrentSettings();
    }

    /**
     * Obtiene el stage actual.
     */
    private Stage getStage() {
        if (txtGraphicsPath != null && txtGraphicsPath.getScene() != null) {
            return (Stage) txtGraphicsPath.getScene().getWindow();
        }
        return null;
    }

    /**
     * Carga la configuración actual.
     */
    private void loadCurrentSettings() {
        configManager = ConfigManager.getInstance();

        // Rutas
        txtGraphicsPath.setText(configManager.getGraphicsDir());
        txtInitPath.setText(configManager.getInitDir());
        txtDatPath.setText(configManager.getDatDir());
        txtExportPath.setText(configManager.getExportDir());

        // Tema
        cmbTheme.setItems(FXCollections.observableArrayList("Oscuro", "Claro"));
        String currentTheme = configManager.getAppTheme();
        if ("LIGHT".equalsIgnoreCase(currentTheme)) {
            cmbTheme.getSelectionModel().select("Claro");
        } else {
            cmbTheme.getSelectionModel().select("Oscuro");
        }
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

        File selected = chooser.showDialog(getStage());
        return selected != null ? selected.getAbsolutePath() : null;
    }

    /**
     * Guardar cambios.
     */
    @FXML
    private void onSave() {
        // Guardar Rutas
        configManager.setGraphicsDir(txtGraphicsPath.getText());
        configManager.setInitDir(txtInitPath.getText());
        configManager.setDatDir(txtDatPath.getText());
        configManager.setExportDir(txtExportPath.getText());

        // Guardar Tema
        String selectedTheme = cmbTheme.getSelectionModel().getSelectedItem();
        String themeCode = "DARK";
        if ("Claro".equals(selectedTheme)) {
            themeCode = "LIGHT";
        }
        configManager.setAppTheme(themeCode);

        // Aplicar tema inmediatamente a todas las ventanas
        WindowManager.getInstance().updateThemeForAll(themeCode);

        try {
            configManager.writeConfig();
            getStage().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Cancelar cambios.
     */
    @FXML
    private void onCancel() {
        Stage s = getStage();
        if (s != null)
            s.close();
    }
}

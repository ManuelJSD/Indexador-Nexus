package org.nexus.indexador.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.nexus.indexador.utils.ConfigManager;
import org.nexus.indexador.utils.Logger;
import org.nexus.indexador.utils.ProfileManager;

import java.io.File;
import java.io.IOException;

/**
 * Controller para el asistente de configuración inicial.
 */
public class InitialSetupController {

  @FXML
  private TextField txtGraphicsPath;

  @FXML
  private TextField txtInitPath;

  @FXML
  private TextField txtExportPath;

  @FXML
  private TextField txtProfileName;

  @FXML
  private VBox cardMoldSystem;

  @FXML
  private VBox cardTraditionalSystem;

  @FXML
  private VBox cardDarkTheme;

  @FXML
  private VBox cardLightTheme;

  private Stage stage;
  private Runnable onComplete;
  private String selectedIndexingSystem = "CLASSIC"; // Default: Sistema Clásico
  private String selectedTheme = "DARK"; // Default: Tema Oscuro

  /**
   * Ruta del archivo .ini donde se guardará la configuración del nuevo perfil.
   * Inyectada desde {@link org.nexus.indexador.Main}.
   */
  private String targetConfigPath;

  /**
   * Nombre del perfil que se creará al finalizar el wizard.
   * Inyectado desde {@link org.nexus.indexador.Main}.
   */
  private String profileName;

  /**
   * Inicializa el controller.
   */
  @FXML
  public void initialize() {
    // Sistema de Moldes seleccionado por defecto
    updateCardSelection();
    updateThemeSelection();
  }

  /**
   * Maneja el clic en la tarjeta del Sistema de Moldes.
   */
  @FXML
  private void onMoldSystemClick() {
    selectedIndexingSystem = "MOLD";
    updateCardSelection();
  }

  /**
   * Maneja el clic en la tarjeta del Sistema Tradicional.
   */
  @FXML
  private void onTraditionalSystemClick() {
    selectedIndexingSystem = "CLASSIC";
    updateCardSelection();
  }

  /**
   * Maneja el clic en la tarjeta del Tema Oscuro.
   */
  @FXML
  private void onDarkThemeClick() {
    selectedTheme = "DARK";
    updateThemeSelection();
    applyThemePreview();
  }

  /**
   * Maneja el clic en la tarjeta del Tema Claro.
   */
  @FXML
  private void onLightThemeClick() {
    selectedTheme = "LIGHT";
    updateThemeSelection();
    applyThemePreview();
  }

  /**
   * Actualiza el estilo visual de las tarjetas según la selección.
   */
  private void updateCardSelection() {
    // Reset classes
    cardTraditionalSystem.getStyleClass().remove("setup-card-selected-traditional");
    cardMoldSystem.getStyleClass().remove("setup-card-selected");

    if ("CLASSIC".equals(selectedIndexingSystem)) {
      // Clásico seleccionado
      if (!cardTraditionalSystem.getStyleClass().contains("setup-card-selected-traditional")) {
        cardTraditionalSystem.getStyleClass().add("setup-card-selected-traditional");
      }
    } else {
      // Moldes seleccionado
      if (!cardMoldSystem.getStyleClass().contains("setup-card-selected")) {
        cardMoldSystem.getStyleClass().add("setup-card-selected");
      }
    }
  }

  /**
   * Actualiza el estilo visual de las tarjetas de tema.
   */
  private void updateThemeSelection() {
    // Reset classes
    cardDarkTheme.getStyleClass().remove("setup-card-selected");
    cardLightTheme.getStyleClass().remove("setup-card-selected");

    if ("DARK".equals(selectedTheme)) {
      // Oscuro seleccionado
      if (!cardDarkTheme.getStyleClass().contains("setup-card-selected")) {
        cardDarkTheme.getStyleClass().add("setup-card-selected");
      }
    } else {
      // Claro seleccionado
      if (!cardLightTheme.getStyleClass().contains("setup-card-selected")) {
        cardLightTheme.getStyleClass().add("setup-card-selected");
      }
    }
  }

  /**
   * Aplica el tema seleccionado inmediatamente para previsualizar.
   */
  private void applyThemePreview() {
    if (this.stage != null && this.stage.getScene() != null) {
      org.nexus.indexador.utils.WindowManager.getInstance().applyTheme(this.stage.getScene(),
          selectedTheme);
    }
  }

  /**
   * Inicializa el controller con el stage.
   *
   * @param stage Stage del wizard.
   */
  public void setStage(Stage stage) {
    this.stage = stage;
  }

  /**
   * Establece el callback a ejecutar cuando se complete la configuración.
   *
   * @param onComplete Runnable a ejecutar tras guardar.
   */
  public void setOnComplete(Runnable onComplete) {
    this.onComplete = onComplete;
  }

  /**
   * Establece la ruta del archivo .ini donde se guardará la configuración.
   *
   * @param targetConfigPath Ruta relativa al directorio de la aplicación.
   */
  public void setTargetConfigPath(String targetConfigPath) {
    this.targetConfigPath = targetConfigPath;
  }

  /**
   * Indica al controller el nombre sugerido para el perfil.
   * Se pre-rellena en {@link #txtProfileName} cuando la UI ya esté lista.
   *
   * @param profileName Nombre propuesto para el perfil.
   */
  public void setProfileName(String profileName) {
    this.profileName = profileName;
    // Pre-rellenar el campo si el nodo ya está inyectado
    if (txtProfileName != null && (profileName != null)) {
      txtProfileName.setText(profileName);
    }
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
    if (txtGraphicsPath.getText().isEmpty() || txtInitPath.getText().isEmpty()
        || txtExportPath.getText().isEmpty()) {

      Alert alert = new Alert(Alert.AlertType.WARNING);
      alert.setTitle("Configuración Incompleta");
      alert.setHeaderText("Por favor, configure todas las rutas");
      alert.setContentText(
          "Las rutas de Gráficos, Índices y Exportación son necesarias.");
      alert.showAndWait();
      return;
    }

    // Guardar configuración en memoria
    ConfigManager config = ConfigManager.getInstance();
    config.setGraphicsDir(txtGraphicsPath.getText());
    config.setInitDir(txtInitPath.getText());
    config.setDatDir(txtInitPath.getText());
    config.setExportDir(txtExportPath.getText());
    config.setIndexingSystem(selectedIndexingSystem);
    config.setAppTheme(selectedTheme);

    // Si el usuario escribió un nombre en el campo, éste tiene prioridad
    String nombreFinal = (txtProfileName != null && !txtProfileName.getText().trim().isEmpty())
        ? txtProfileName.getText().trim()
        : profileName;

    // Determinar ruta de destino
    File destino = (targetConfigPath != null)
        ? new File(targetConfigPath)
        : new File("config.ini");

    try {
      // Guardar la configuración en el archivo de destino
      config.writeConfig(destino);

      // Registrar el perfil en ProfileManager si tenemos nombre y ruta de perfil
      if (targetConfigPath != null) {
        ProfileManager pm = ProfileManager.getInstance();
        String nombre = (nombreFinal != null && !nombreFinal.isEmpty())
            ? nombreFinal
            : destino.getName().replace(".ini", "");
        org.nexus.indexador.utils.ProfileEntry entry =
            pm.agregarPerfil(nombre, targetConfigPath);
        pm.setPerfilActivo(entry);
      }

      // Cerrar wizard y continuar con la aplicación
      stage.close();

      // Ejecutar callback si existe
      if (onComplete != null) {
        Platform.runLater(onComplete);
      }
    } catch (IOException e) {
      Logger.getInstance().error("Error de E/S al guardar configuración", e);
      Alert alert = new Alert(Alert.AlertType.ERROR);
      alert.setTitle("Error al Guardar");
      alert.setHeaderText("No se pudo guardar la configuración");
      alert.setContentText("Error: " + e.getMessage());
      alert.showAndWait();
    } catch (Exception e) {
      Logger.getInstance().error("Error inesperado al finalizar configuración", e);
      Alert alert = new Alert(Alert.AlertType.ERROR);
      alert.setTitle("Error Inesperado");
      alert.setHeaderText("Ocurrió un error al finalizar la configuración");
      alert.setContentText("Error: " + e.getClass().getSimpleName() + " - " + e.getMessage());
      alert.showAndWait();
    }
  }
}

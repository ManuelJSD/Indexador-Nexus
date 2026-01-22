package org.nexus.indexador.controllers;

import javafx.scene.image.ImageView;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Controller para el asistente visual de Auto-Indexación.
 * Maneja el flujo de selección y configuración previa a la ejecución.
 */
public class AutoIndexWizardController {

  private MainController mainController;
  private Stage stage;
  private Runnable pendingAction; // Action to run after configuration

  // UI Elements
  @FXML
  private Label lblHeaderTitle;
  @FXML
  private Label lblHeaderDesc;
  @FXML
  private VBox step1Selection;
  @FXML
  private VBox step2Config;

  @FXML
  private TextField txtFileNum;
  @FXML
  private Button btnBack;
  @FXML
  private Button btnStart;
  @FXML
  private Button btnCancel;

  // New UI Elements
  @FXML
  private ImageView imgPreview;
  @FXML
  private Label lblNoImage;

  @FXML
  private VBox boxAtlasConfig;
  @FXML
  private TextField txtAtlasCols;
  @FXML
  private TextField txtAtlasRows;

  // State
  private String selectedType = "";

  public void setMainController(MainController controller) {
    this.mainController = controller;
  }

  public void setStage(Stage stage) {
    this.stage = stage;
    if (stage != null) {
      try {
        // Attempt to load icon if available, safely ignored if not
        stage.getIcons().add(new javafx.scene.image.Image(getClass().getResourceAsStream("/img/icon.png")));
      } catch (Exception e) {
      }
    }
  }

  @FXML
  public void initialize() {
    if (txtFileNum != null) {
      txtFileNum.textProperty().addListener((obs, old, val) -> updatePreview(val));
    }
  }

  @FXML
  private void onPersonajeClick() {
    goToConfig("Personaje", "Introduce el número de archivo inicial.",
        () -> mainController.autoIndexPersonaje("", getInputFileNum()));
  }

  @FXML
  private void onObjetosClick() {
    goToConfig("Objetos", "Introduce el número de archivo inicial.",
        () -> mainController.autoIndexObjetos("", getInputFileNum()));
  }

  @FXML
  private void onSuperficiesClick() {
    goToConfig("Superficies", "Introduce el número de archivo inicial.",
        () -> mainController.autoIndexSuperficies("", getInputFileNum(), getAtlasCols(), getAtlasRows()));
  }

  @FXML
  private void onAnimacionClick() {
    goToConfig("Animación", "Introduce el nombre base para la animación.",
        () -> mainController.autoIndexAnimacion("", getInputFileNum()));
  }

  private void goToConfig(String type, String desc, Runnable action) {
    this.selectedType = type;
    this.pendingAction = action;

    // Update Header
    lblHeaderTitle.setText("Configurar: " + type);
    lblHeaderDesc.setText(desc);

    // Switch View
    step1Selection.setVisible(false);
    step2Config.setVisible(true);

    // Atlas Config Visibility
    if ("Superficies".equals(type) && boxAtlasConfig != null) {
      boxAtlasConfig.setVisible(true);
      boxAtlasConfig.setManaged(true);
    } else if (boxAtlasConfig != null) {
      boxAtlasConfig.setVisible(false);
      boxAtlasConfig.setManaged(false);
    }

    // Update Buttons
    btnBack.setVisible(true);
    btnStart.setVisible(true);
    btnCancel.setText("Cancelar");

    // Reset state
    txtFileNum.setText("");
    if (txtAtlasCols != null)
      txtAtlasCols.setText("1");
    if (txtAtlasRows != null)
      txtAtlasRows.setText("1");

    updatePreview("");

    // Focus
    txtFileNum.requestFocus();
  }

  @FXML
  private void onBackClick() {
    // Reset Header
    lblHeaderTitle.setText("Asistente de Auto-Indexación");
    lblHeaderDesc.setText("Selecciona el tipo de recurso que deseas importar.");

    // Switch View Back
    step1Selection.setVisible(true);
    step2Config.setVisible(false);

    // Reset Buttons
    btnBack.setVisible(false);
    btnStart.setVisible(false);
    btnCancel.setText("Cancelar");
  }

  @FXML
  private void onStartClick() {
    if (pendingAction != null) {
      stage.close();
      javafx.application.Platform.runLater(pendingAction);
    }
  }

  private int getAtlasCols() {
    try {
      return Integer.parseInt(txtAtlasCols.getText().trim());
    } catch (Exception e) {
      return 1;
    }
  }

  private int getAtlasRows() {
    try {
      return Integer.parseInt(txtAtlasRows.getText().trim());
    } catch (Exception e) {
      return 1;
    }
  }

  @FXML
  private void onCancelarClick() {
    if (stage != null)
      stage.close();
  }

  @FXML
  private void onOpenGraphicsFolder() {
    if (mainController == null)
      return;
    try {
      String path = mainController.getConfigManager().getGraphicsDir();
      if (path != null) {
        java.awt.Desktop.getDesktop().open(new java.io.File(path));
      }
    } catch (Exception e) {
      // Ignore
    }
  }

  @FXML
  private void onSelectFileClick() {
    if (mainController == null)
      return;

    javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
    fileChooser.setTitle("Seleccionar Gráfico " + selectedType);

    // Locate graphics dir
    try {
      String graphicsDir = mainController.getConfigManager().getGraphicsDir();
      if (graphicsDir != null && !graphicsDir.isEmpty()) {
        java.io.File initialDir = new java.io.File(graphicsDir);
        if (initialDir.exists() && initialDir.isDirectory()) {
          fileChooser.setInitialDirectory(initialDir);
        }
      }
    } catch (Exception ignored) {
    }

    fileChooser.getExtensionFilters().addAll(
        new javafx.stage.FileChooser.ExtensionFilter("Imágenes PNG", "*.png"),
        new javafx.stage.FileChooser.ExtensionFilter("Imágenes BMP", "*.bmp"));

    java.io.File selectedFile = fileChooser.showOpenDialog(stage);
    if (selectedFile != null) {
      String name = selectedFile.getName();
      String numStr = name.replaceAll("[^0-9]", "");
      if (!numStr.isEmpty()) {
        txtFileNum.setText(numStr);
      } else {
        txtFileNum.setText(selectedFile.getAbsolutePath());
      }
      updatePreview(numStr);
    }
  }

  private void updatePreview(String fileNumStr) {
    if (mainController == null || fileNumStr == null || fileNumStr.isEmpty()) {
      if (imgPreview != null)
        imgPreview.setImage(null);
      if (lblNoImage != null)
        lblNoImage.setVisible(true);
      return;
    }

    try {
      int fileNum = Integer.parseInt(fileNumStr);
      String graphicsDir = mainController.getConfigManager().getGraphicsDir();
      String imagePath = graphicsDir + java.io.File.separator + fileNum + ".png";
      java.io.File f = new java.io.File(imagePath);
      if (!f.exists()) {
        imagePath = graphicsDir + java.io.File.separator + fileNum + ".bmp";
        f = new java.io.File(imagePath);
      }

      if (f.exists()) {
        javafx.scene.image.Image img = new javafx.scene.image.Image(f.toURI().toString());
        if (imgPreview != null)
          imgPreview.setImage(img);
        if (lblNoImage != null)
          lblNoImage.setVisible(false);
      } else {
        if (imgPreview != null)
          imgPreview.setImage(null);
        if (lblNoImage != null)
          lblNoImage.setVisible(true);
      }
    } catch (Exception e) {
      if (imgPreview != null)
        imgPreview.setImage(null);
      if (lblNoImage != null)
        lblNoImage.setVisible(true);
    }
  }

  private int getInputFileNum() {
    try {
      return Integer.parseInt(txtFileNum.getText().trim());
    } catch (NumberFormatException e) {
      return -1; // Auto/Default
    }
  }
}

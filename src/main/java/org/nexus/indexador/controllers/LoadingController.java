package org.nexus.indexador.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.nexus.indexador.gamedata.DataManager;
import org.nexus.indexador.utils.ConfigManager;

import java.io.IOException;
import javafx.scene.control.Alert;
import org.nexus.indexador.utils.Logger;

public class LoadingController {

  @FXML
  public Label lblStatus;

  @FXML
  public javafx.scene.control.ProgressBar progressBar;

  private Stage currentStage;
  private final Logger logger = Logger.getInstance();

  private static final int TOTAL_STEPS = 7;
  private int currentStep = 0;

  public void setStage(Stage stage) {
    this.currentStage = stage;
  }

  @FXML
  protected void initialize() {
    // Inicialización básica si es necesario
  }

  public void init() {
    // Ejecutar la lectura de configuración y apertura de nueva ventana en un hilo
    // separado
    new Thread(() -> {
      try {
        // Simular tiempo de carga
        // Thread.sleep(2000);

        // Lectura de la configuración
        ConfigManager configManager = ConfigManager.getInstance();
        configManager.readConfig();

        DataManager dataManager = DataManager.getInstance();

        loadDataSafe(() -> dataManager.loadGrhData(), "cargando índice de gráficos");
        loadDataSafe(() -> dataManager.readHeadFile(), "cargando índice de cabezas");
        loadDataSafe(() -> dataManager.readHelmetFile(), "cargando índice de cascos");
        loadDataSafe(() -> dataManager.readBodyFile(), "cargando índice de cuerpos");
        loadDataSafe(() -> dataManager.readShieldFile(), "cargando índice de escudos");
        loadDataSafe(() -> dataManager.readFXsdFile(), "cargando índice de FXs");
        loadDataSafe(() -> dataManager.readWeaponFile(), "cargando índice de armas");

      } catch (IOException e) {
        logger.error("Error crítico al leer la configuración", e);
        showErrorAndExit("Error al leer la configuración: " + e.getMessage());
        return; // Detener ejecución
      }

      Platform.runLater(() -> {
        // Usar WindowManager para abrir MainController y registrarlo
        org.nexus.indexador.utils.WindowManager winMgr =
            org.nexus.indexador.utils.WindowManager.getInstance();
        boolean success = winMgr.showWindow("MainController", "Indexador Nexus", true); // Resizable
                                                                                        // = true

        if (success) {
          Stage mainStage = winMgr.getWindow("MainController");
          if (mainStage != null) {
            mainStage.centerOnScreen();

            // Configurar cierre de aplicación
            mainStage.setOnCloseRequest(event -> {
              Platform.exit();
              System.exit(0);
            });
          }

          // Cerrar la ventana actual (LoadingController)
          if (currentStage != null) {
            currentStage.close();
          }
        } else {
          String msg = "Error crítico: No se pudo abrir la ventana principal.";
          logger.error(msg);
          showErrorAndExit(msg);
        }
      });
    }).start();
  }

  /**
   * Ejecuta una tarea de carga de datos de forma segura, manejando excepciones y actualizando el
   * estado.
   */
  private void loadDataSafe(DataLoadingTask task, String description) {
    try {
      Platform.runLater(() -> {
        lblStatus.setText(
            Character.toUpperCase(description.charAt(0)) + description.substring(1) + "...");
        double progress = (double) currentStep / TOTAL_STEPS;
        if (progressBar != null)
          progressBar.setProgress(progress);
      });

      task.execute();

      currentStep++;
      Platform.runLater(() -> {
        double progress = (double) currentStep / TOTAL_STEPS;
        if (progressBar != null)
          progressBar.setProgress(progress);
      });

    } catch (Exception e) {
      String errorMsg = "Error al " + description + ": " + e.getMessage();
      logger.error(errorMsg, e);
    }
  }

  /**
   * Interfaz funcional para tareas de carga que pueden lanzar excepciones.
   */
  @FunctionalInterface
  private interface DataLoadingTask {
    void execute() throws Exception;
  }

  /**
   * Muestra un diálogo de error y cierra la aplicación.
   */
  private void showErrorAndExit(String message) {
    Platform.runLater(() -> {
      Alert alert = new Alert(Alert.AlertType.ERROR);
      alert.setTitle("Error Crítico");
      alert.setHeaderText("Ocurrió un error fatal");
      alert.setContentText(message);
      alert.showAndWait();
      Platform.exit();
      System.exit(1);
    });
  }
}

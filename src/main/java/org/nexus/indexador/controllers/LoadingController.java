package org.nexus.indexador.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.nexus.indexador.gamedata.DataManager;
import org.nexus.indexador.utils.ConfigManager;

import java.io.IOException;

public class LoadingController {

    @FXML
    public Label lblStatus;

    private Stage currentStage;

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

                try {
                    Platform.runLater(() -> lblStatus.setText("Cargando indice de graficos..."));
                    dataManager.loadGrhData();
                } catch (Exception e) {
                    System.err.println("Error al cargar graficos: " + e.getMessage());
                }

                try {
                    Platform.runLater(() -> lblStatus.setText("Cargando indice de cabezas..."));
                    dataManager.readHeadFile();
                } catch (Exception e) {
                    System.err.println("Error al cargar cabezas: " + e.getMessage());
                }

                try {
                    Platform.runLater(() -> lblStatus.setText("Cargando indice de cascos..."));
                    dataManager.readHelmetFile();
                } catch (Exception e) {
                    System.err.println("Error al cargar cascos: " + e.getMessage());
                }

                try {
                    Platform.runLater(() -> lblStatus.setText("Cargando indice de cuerpos..."));
                    dataManager.readBodyFile();
                } catch (Exception e) {
                    System.err.println("Error al cargar cuerpos: " + e.getMessage());
                }

                try {
                    Platform.runLater(() -> lblStatus.setText("Cargando indice de escudos..."));
                    dataManager.readShieldFile();
                } catch (Exception e) {
                    System.err.println("Error al cargar escudos: " + e.getMessage());
                }

                try {
                    Platform.runLater(() -> lblStatus.setText("Cargando indice de FXs..."));
                    dataManager.readFXsdFile();
                } catch (Exception e) {
                    System.err.println("Error al cargar FXs: " + e.getMessage());
                }

                try {
                    Platform.runLater(() -> lblStatus.setText("Cargando indice de armas..."));
                    dataManager.readWeaponFile();
                } catch (Exception e) {
                    System.err.println("Error al cargar armas: " + e.getMessage());
                }

            } catch (IOException e) {
                System.err.println("Error al leer la configuración: " + e.getMessage());
            }

            Platform.runLater(() -> {
                // Usar WindowManager para abrir MainController y registrarlo
                org.nexus.indexador.utils.WindowManager winMgr = org.nexus.indexador.utils.WindowManager.getInstance();
                boolean success = winMgr.showWindow("MainController", "Indexador Nexus", false);

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
                    System.err.println("Error critico: No se pudo abrir la ventana principal.");
                }
            });
        }).start();
    }
}
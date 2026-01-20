package org.nexus.indexador.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.nexus.indexador.Main;
import org.nexus.indexador.gamedata.DataManager;
import org.nexus.indexador.utils.ConfigManager;

import java.io.IOException;

public class frmCargando {

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

                Platform.runLater(() -> lblStatus.setText("Cargando indice de graficos..."));
                dataManager.loadGrhData();

                Platform.runLater(() -> lblStatus.setText("Cargando indice de cabezas..."));
                dataManager.readHeadFile();

                Platform.runLater(() -> lblStatus.setText("Cargando indice de cascos..."));
                dataManager.readHelmetFile();

                Platform.runLater(() -> lblStatus.setText("Cargando indice de cuerpos..."));
                dataManager.readBodyFile();

                Platform.runLater(() -> lblStatus.setText("Cargando indice de escudos..."));
                dataManager.readShieldFile();

                Platform.runLater(() -> lblStatus.setText("Cargando indice de FXs..."));
                dataManager.readFXsdFile();

            } catch (IOException e) {
                System.err.println("Error al leer la configuración: " + e.getMessage());
            }

            Platform.runLater(() -> {
                // Crea la nueva ventana
                Stage newStage = new Stage();
                Main.setAppIcon(newStage);
                newStage.setTitle("Indexador Nexus");

                // Lee el archivo FXML para la nueva ventana
                try {
                    Parent consoleRoot = FXMLLoader.load(Main.class.getResource("frmMain.fxml"));
                    Scene mainScene = new Scene(consoleRoot);

                    // Aplicar tema oscuro
                    String darkTheme = Main.class.getResource("styles/dark-theme.css").toExternalForm();
                    mainScene.getStylesheets().add(darkTheme);

                    newStage.setScene(mainScene);
                    newStage.setResizable(false);

                    // Cerrar la ventana actual (frmCargando)
                    if (currentStage != null) {
                        currentStage.close();
                    }

                    newStage.setOnCloseRequest(event -> {
                        Platform.exit();
                        System.exit(0);
                    });

                    newStage.centerOnScreen();
                    newStage.show();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }).start();
    }
}
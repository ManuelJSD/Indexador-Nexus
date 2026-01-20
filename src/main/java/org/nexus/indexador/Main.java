package org.nexus.indexador;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.nexus.indexador.controllers.frmCargando;
import org.nexus.indexador.utils.Logger;

import java.io.IOException;

public class Main extends Application {

    private final Logger logger = Logger.getInstance();

    @Override
    public void start(Stage stage) {
        logger.info("Iniciando aplicación Indexador Nexus");

        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("frmCargando.fxml"));

        try {
            Parent root = fxmlLoader.load();
            Scene scene = new Scene(root);

            // Aplicar tema oscuro
            String darkTheme = Main.class.getResource("styles/dark-theme.css").toExternalForm();
            scene.getStylesheets().add(darkTheme);

            // Obtener el controlador y pasar el Stage
            frmCargando controller = fxmlLoader.getController();
            controller.setStage(stage);
            controller.init();

            stage.initStyle(StageStyle.UNDECORATED);
            stage.setTitle("Indexador Nexus: Iniciando");
            stage.setResizable(false);
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.show();

            logger.info("Pantalla de carga iniciada correctamente");
        } catch (IOException e) {
            logger.error("Error al cargar la interfaz de usuario", e);
        }
    }

    public static void main(String[] args) {
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            Logger logger = Logger.getInstance();
            logger.error("Excepción no capturada en el hilo: " + thread.getName(), throwable);
        });

        launch();
    }
}
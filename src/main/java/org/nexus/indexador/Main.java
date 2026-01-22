package org.nexus.indexador;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.nexus.indexador.controllers.LoadingController;
import org.nexus.indexador.utils.Logger;

import java.io.IOException;
import javafx.scene.image.Image;
import java.io.InputStream;

public class Main extends Application {

    private final Logger logger = Logger.getInstance();
    public static java.util.Map<Integer, org.nexus.indexador.gamedata.models.GrhData> sharedGrhData;

    @Override
    public void start(Stage stage) {
        // Activar redirección de logs a la consola de depuración
        org.nexus.indexador.controllers.ConsoleController.activateGlobalRedirection();

        logger.info("Iniciando aplicación Indexador Nexus");

        // Configurar icono
        setAppIcon(stage);

        // Verificar si es la primera vez que se ejecuta
        org.nexus.indexador.utils.ConfigManager config = org.nexus.indexador.utils.ConfigManager.getInstance();

        if (!config.configExists()) {
            // Primera vez - mostrar wizard de configuración
            showInitialSetup(() -> showLoadingScreen(stage));
        } else {
            // Ya configurado - mostrar pantalla de carga directamente
            showLoadingScreen(stage);
        }
    }

    /**
     * Muestra el wizard de configuración inicial.
     */
    private void showInitialSetup(Runnable onComplete) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(
                    Main.class.getResource("/org/nexus/indexador/InitialSetupController.fxml"));
            Parent root = fxmlLoader.load();
            Scene scene = new Scene(root);

            // Aplicar tema oscuro
            String darkTheme = Main.class.getResource("styles/dark-theme.css").toExternalForm();
            scene.getStylesheets().add(darkTheme);

            // Configurar controller
            org.nexus.indexador.controllers.InitialSetupController controller = fxmlLoader.getController();

            Stage setupStage = new Stage();
            controller.setStage(setupStage);
            controller.setOnComplete(onComplete);

            setupStage.setTitle("Indexador Nexus - Configuración Inicial");
            setAppIcon(setupStage);
            setupStage.setScene(scene);
            setupStage.setResizable(false);
            setupStage.show();

            logger.info("Wizard de configuración inicial mostrado");
        } catch (IOException e) {
            logger.error("Error al cargar wizard de configuración", e);
        }
    }

    /**
     * Muestra la pantalla de carga principal.
     */
    private void showLoadingScreen(Stage stage) {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/org/nexus/indexador/LoadingController.fxml"));

        try {
            Parent root = fxmlLoader.load();
            Scene scene = new Scene(root);

            // Aplicar tema oscuro
            String darkTheme = Main.class.getResource("styles/dark-theme.css").toExternalForm();
            scene.getStylesheets().add(darkTheme);

            // Obtener el controlador y pasar el Stage
            LoadingController controller = fxmlLoader.getController();
            controller.setStage(stage);
            controller.init();

            stage.initStyle(StageStyle.UNDECORATED);
            stage.setTitle("Indexador Nexus: Iniciando");
            setAppIcon(stage);
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

    /**
     * Establece el icono de la aplicación para un Stage dado.
     * 
     * @param stage El escenario al que aplicar el icono.
     */
    public static void setAppIcon(Stage stage) {
        try {
            InputStream iconStream = Main.class.getResourceAsStream("/img/icon.png");
            if (iconStream != null) {
                stage.getIcons().add(new Image(iconStream));
            }
        } catch (Exception e) {
            System.err.println("No se pudo cargar el icono de la aplicación: " + e.getMessage());
        }
    }
}
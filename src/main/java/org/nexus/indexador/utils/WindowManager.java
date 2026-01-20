package org.nexus.indexador.utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.nexus.indexador.Main;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Gestor centralizado de ventanas de la aplicación.
 * Implementa el patrón Singleton para garantizar una única instancia.
 * Evita abrir múltiples instancias de la misma ventana.
 */
public class WindowManager {

    private static volatile WindowManager instance;

    // Mapa de ventanas abiertas: clave = nombre del FXML, valor = Stage
    private final Map<String, Stage> openWindows = new HashMap<>();

    private final Logger logger = Logger.getInstance();

    private WindowManager() {
        logger.info("WindowManager inicializado");
    }

    /**
     * Obtiene la instancia única del WindowManager.
     *
     * @return La instancia del WindowManager.
     */
    public static WindowManager getInstance() {
        if (instance == null) {
            synchronized (WindowManager.class) {
                if (instance == null) {
                    instance = new WindowManager();
                }
            }
        }
        return instance;
    }

    /**
     * Abre una ventana o la enfoca si ya está abierta.
     *
     * @param fxmlName  Nombre del archivo FXML (sin extensión).
     * @param title     Título de la ventana.
     * @param resizable Si la ventana es redimensionable.
     * @return true si se abrió una nueva ventana, false si ya estaba abierta.
     */
    public boolean showWindow(String fxmlName, String title, boolean resizable) {
        // Si la ventana ya está abierta, enfocarla
        if (openWindows.containsKey(fxmlName)) {
            Stage existingStage = openWindows.get(fxmlName);
            if (existingStage.isShowing()) {
                existingStage.requestFocus();
                existingStage.toFront();
                logger.debug("Ventana '" + fxmlName + "' ya abierta, enfocando");
                return false;
            } else {
                // La ventana fue cerrada pero no se limpió del mapa
                openWindows.remove(fxmlName);
            }
        }

        // Crear nueva ventana
        try {
            Stage newStage = new Stage();
            newStage.setTitle(title);

            Parent root = FXMLLoader.load(Main.class.getResource(fxmlName + ".fxml"));
            Scene scene = new Scene(root);

            // Aplicar tema oscuro
            String darkTheme = Main.class.getResource("styles/dark-theme.css").toExternalForm();
            scene.getStylesheets().add(darkTheme);

            newStage.setScene(scene);
            newStage.setResizable(resizable);

            // Registrar en el mapa y configurar limpieza al cerrar
            openWindows.put(fxmlName, newStage);
            newStage.setOnCloseRequest(event -> {
                openWindows.remove(fxmlName);
                logger.debug("Ventana '" + fxmlName + "' cerrada");
            });

            newStage.show();
            logger.info("Ventana '" + fxmlName + "' abierta: " + title);
            return true;

        } catch (IOException e) {
            logger.error("Error al abrir ventana '" + fxmlName + "'", e);
            if (e.getCause() != null) {
                logger.error(
                        "Causa del error: " + e.getCause().getClass().getName() + ": " + e.getCause().getMessage());
            }
            return false;
        }
    }

    /**
     * Cierra una ventana específica si está abierta.
     *
     * @param fxmlName Nombre del archivo FXML.
     */
    public void closeWindow(String fxmlName) {
        Stage stage = openWindows.get(fxmlName);
        if (stage != null) {
            stage.close();
            openWindows.remove(fxmlName);
            logger.info("Ventana '" + fxmlName + "' cerrada programáticamente");
        }
    }

    /**
     * Cierra todas las ventanas abiertas.
     */
    public void closeAllWindows() {
        for (Stage stage : openWindows.values()) {
            stage.close();
        }
        openWindows.clear();
        logger.info("Todas las ventanas cerradas");
    }

    /**
     * Verifica si una ventana está abierta.
     *
     * @param fxmlName Nombre del archivo FXML.
     * @return true si la ventana está abierta.
     */
    public boolean isWindowOpen(String fxmlName) {
        Stage stage = openWindows.get(fxmlName);
        return stage != null && stage.isShowing();
    }

    /**
     * Obtiene el número de ventanas abiertas.
     *
     * @return Número de ventanas abiertas.
     */
    public int getOpenWindowCount() {
        return openWindows.size();
    }
}

package org.nexus.indexador.controllers;

import javafx.fxml.FXML;
import javafx.stage.Stage;

/**
 * Controller para el asistente visual de Auto-Indexación.
 * Permite seleccionar el tipo de gráfico a indexar mediante opciones visuales.
 */
public class AutoIndexWizardController {

    private MainController mainController;
    private Stage stage;

    /** Inyecta referencia al controlador principal */
    public void setMainController(MainController controller) {
        this.mainController = controller;
    }

    /** Inyecta referencia a la ventana para poder cerrarla */
    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private void onPersonajeClick() {
        closeAndExecute(() -> mainController.mnuAutoBody_OnAction());
    }

    @FXML
    private void onObjetosClick() {
        closeAndExecute(() -> mainController.mnuAutoSprites_OnAction());
    }

    @FXML
    private void onSuperficiesClick() {
        closeAndExecute(() -> mainController.mnuAutoSurfaces_OnAction());
    }

    @FXML
    private void onAnimacionClick() {
        closeAndExecute(() -> mainController.mnuAutoAnimation_OnAction());
    }

    @FXML
    private void onCancelarClick() {
        if (stage != null)
            stage.close();
    }

    private void closeAndExecute(Runnable action) {
        if (stage != null)
            stage.close();
        if (mainController != null && action != null) {
            javafx.application.Platform.runLater(action);
        }
    }
}

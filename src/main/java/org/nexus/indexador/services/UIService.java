package org.nexus.indexador.services;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.paint.Color;
import org.nexus.indexador.utils.Logger;

/**
 * Service to handle common UI tasks like alerts and background tasks with
 * feedback.
 */
public class UIService {

    private final Logger logger = Logger.getInstance();
    private Label lblStatus;
    private ProgressBar progressMain;

    public UIService() {
    }

    public void init(Label lblStatus, ProgressBar progressMain) {
        this.lblStatus = lblStatus;
        this.progressMain = progressMain;
    }

    public void showInfo(String title, String content) {
        showAlert(Alert.AlertType.INFORMATION, title, content);
    }

    public void showError(String title, String content) {
        showAlert(Alert.AlertType.ERROR, title, content);
    }

    public void showWarning(String title, String content) {
        showAlert(Alert.AlertType.WARNING, title, content);
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }

    /**
     * Executes a task asynchronously and updates the status bar and progress bar.
     */
    public void runAsyncTask(Runnable task, String startMsg, String endMsg) {
        if (progressMain != null) {
            progressMain.setVisible(true);
            progressMain.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
        }
        if (lblStatus != null) {
            lblStatus.setText(startMsg);
            lblStatus.setTextFill(Color.web("#FFA500"));
        }

        new Thread(() -> {
            try {
                task.run();
                Platform.runLater(() -> {
                    if (lblStatus != null) {
                        lblStatus.setText(endMsg);
                        lblStatus.setTextFill(Color.web("#00FF00"));
                    }
                    if (progressMain != null) {
                        progressMain.setVisible(false);
                    }
                });
            } catch (Exception e) {
                logger.error("Error in async task: " + startMsg, e);
                Platform.runLater(() -> {
                    if (lblStatus != null) {
                        lblStatus.setText("Error en la operación");
                        lblStatus.setTextFill(Color.RED);
                    }
                    if (progressMain != null) {
                        progressMain.setVisible(false);
                    }
                });
            }
        }).start();
    }
}

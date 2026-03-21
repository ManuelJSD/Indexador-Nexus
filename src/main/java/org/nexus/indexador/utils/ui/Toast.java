package org.nexus.indexador.utils.ui;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.control.Label;
import javafx.stage.Popup;
import javafx.stage.Window;
import javafx.util.Duration;

public class Toast {
    /**
     * Muestra una notificación emergente estilizada sobre la ventana que se le asigne.
     * @param message Texto a mostrar (ej: "Guardado exitoso")
     * @param ownerWindow Ventana padre sobre la que centrar u originar las posiciones X/Y
     */
    public static void show(String message, Window ownerWindow) {
        if (ownerWindow == null) return;

        Popup popup = new Popup();
        popup.setAutoFix(true);
        popup.setHideOnEscape(true);

        Label label = new Label(message);
        label.setStyle("-fx-background-color: #4caf50; " +
                "-fx-text-fill: white; " +
                "-fx-padding: 10px 20px; " +
                "-fx-background-radius: 5px; " +
                "-fx-font-size: 14px; " +
                "-fx-font-weight: bold; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 5);");

        popup.getContent().add(label);

        // Centrado adaptativo en la parte inferior de la ventana
        popup.setOnShown(e -> {
            popup.setX(ownerWindow.getX() + ownerWindow.getWidth() / 2 - popup.getWidth() / 2);
            popup.setY(ownerWindow.getY() + ownerWindow.getHeight() - 80);
        });

        // Mostramos el popup base
        popup.show(ownerWindow);

        // Animación de aparición (Fade In) y desvanecimiento (Fade Out)
        Timeline timeline = new Timeline();
        KeyFrame fadeInKey1 = new KeyFrame(Duration.ZERO, new KeyValue(popup.opacityProperty(), 0.0));
        KeyFrame fadeInKey2 = new KeyFrame(Duration.millis(300), new KeyValue(popup.opacityProperty(), 1.0));
        // Se mantiene opaco hasta 2.5 seg
        KeyFrame fadeOutKey1 = new KeyFrame(Duration.millis(2500), new KeyValue(popup.opacityProperty(), 1.0));
        // Se desvanece de 2.5 a 3.0 sec
        KeyFrame fadeOutKey2 = new KeyFrame(Duration.millis(3000), new KeyValue(popup.opacityProperty(), 0.0));

        timeline.getKeyFrames().addAll(fadeInKey1, fadeInKey2, fadeOutKey1, fadeOutKey2);
        timeline.setOnFinished(e -> popup.hide());
        timeline.play();
    }
}

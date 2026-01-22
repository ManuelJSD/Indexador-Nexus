package org.nexus.indexador.utils;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.util.Duration;

/**
 * Utility class for displaying toast notifications.
 */
public final class ToastNotification {

  public static void show(Window owner, String toastMsg) {
    Stage toastStage = new Stage();
    toastStage.initOwner(owner);
    toastStage.setResizable(false);
    toastStage.initStyle(StageStyle.TRANSPARENT);

    Text text = new Text(toastMsg);
    text.setFont(Font.font("Segoe UI", 14)); // Modern font
    text.setFill(Color.WHITE);

    StackPane root = new StackPane(text);
    root.setStyle(
        "-fx-background-radius: 5; -fx-background-color: rgba(30, 30, 30, 0.85); -fx-padding: 15px 20px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.4), 10, 0, 0, 0);");
    root.setOpacity(0);

    Scene scene = new Scene(root);
    scene.setFill(Color.TRANSPARENT);
    toastStage.setScene(scene);

    // Show briefly to calculate bounds, but keep transparent
    toastStage.show();

    // Calculate position: Bottom Center of owner
    if (owner != null) {
      double ownerX = owner.getX();
      double ownerY = owner.getY();
      double ownerW = owner.getWidth();
      double ownerH = owner.getHeight();

      double toastW = toastStage.getWidth();
      double toastH = toastStage.getHeight();

      toastStage.setX(ownerX + (ownerW / 2) - (toastW / 2));
      toastStage.setY(ownerY + ownerH - toastH - 50); // 50px padding from bottom
    } else {
      toastStage.centerOnScreen();
    }

    Timeline fadeInTimeline = new Timeline();
    KeyFrame fadeInKey1 = new KeyFrame(Duration.millis(300),
        new KeyValue(toastStage.getScene().getRoot().opacityProperty(), 1));
    fadeInTimeline.getKeyFrames().add(fadeInKey1);

    fadeInTimeline.setOnFinished((ae) -> {
      PauseTransition delay = new PauseTransition(Duration.seconds(2.5));
      delay.setOnFinished((event) -> {
        Timeline fadeOutTimeline = new Timeline();
        KeyFrame fadeOutKey1 = new KeyFrame(Duration.millis(300),
            new KeyValue(toastStage.getScene().getRoot().opacityProperty(), 0));
        fadeOutTimeline.getKeyFrames().add(fadeOutKey1);
        fadeOutTimeline.setOnFinished((aeb) -> toastStage.close());
        fadeOutTimeline.play();
      });
      delay.play();
    });

    fadeInTimeline.play();
  }
}

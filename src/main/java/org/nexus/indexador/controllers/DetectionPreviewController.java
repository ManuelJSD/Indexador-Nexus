package org.nexus.indexador.controllers;

import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.List;

/**
 * Controller para la ventana de vista previa de detección de sprites.
 * Muestra la imagen con rectángulos sobre los sprites detectados antes de crear
 * GRHs.
 */
public class DetectionPreviewController {

    @FXML
    private Canvas canvas;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private Label lblTitle;
    @FXML
    private Label lblDetected;
    @FXML
    private Label lblMode;
    @FXML
    private Button btnAdjust;
    @FXML
    private Button btnCancel;
    @FXML
    private Button btnConfirm;

    private Stage stage;
    private boolean confirmed = false;
    private Image image;
    private List<Rectangle> detectedRegions;
    private String modeDescription;

    /**
     * Inicializa la vista previa con los datos de detección.
     */
    public void initialize(Image image, List<Rectangle> regions, String title, String modeDesc) {
        this.image = image;
        this.detectedRegions = regions;
        this.modeDescription = modeDesc;

        lblTitle.setText(title);
        lblDetected.setText("Detectados: " + regions.size() + " sprites");
        lblMode.setText(modeDesc);

        drawPreview();
    }

    /**
     * Dibuja la imagen con los rectángulos de detección.
     */
    private void drawPreview() {
        if (image == null || detectedRegions == null)
            return;

        // Redimensionar canvas al tamaño de la imagen
        canvas.setWidth(image.getWidth());
        canvas.setHeight(image.getHeight());

        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Dibujar la imagen de fondo
        gc.drawImage(image, 0, 0);

        // Dibujar rectángulos sobre cada sprite detectado
        Font numberFont = Font.font("Arial", FontWeight.BOLD, 14);
        gc.setFont(numberFont);

        for (int i = 0; i < detectedRegions.size(); i++) {
            Rectangle rect = detectedRegions.get(i);

            // Alternar colores para distinguir sprites adyacentes
            if (i % 2 == 0) {
                gc.setStroke(Color.LIME);
            } else {
                gc.setStroke(Color.CYAN);
            }
            gc.setLineWidth(2);

            // Dibujar rectángulo
            gc.strokeRect(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());

            // Dibujar número del sprite
            String number = String.valueOf(i + 1);
            gc.setFill(Color.YELLOW);
            gc.setStroke(Color.BLACK);
            gc.setLineWidth(3);

            double textX = rect.getX() + 5;
            double textY = rect.getY() + 20;

            // Fondo negro del texto para mejor legibilidad
            gc.strokeText(number, textX, textY);
            gc.fillText(number, textX, textY);
        }
    }

    @FXML
    private void onAdjustClick() {
        // TODO: Implementar ajuste de parámetros de detección
        // Por ahora solo cierra
        confirmed = false;
        if (stage != null)
            stage.close();
    }

    @FXML
    private void onCancelClick() {
        confirmed = false;
        if (stage != null)
            stage.close();
    }

    @FXML
    private void onConfirmClick() {
        confirmed = true;
        if (stage != null)
            stage.close();
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public boolean isConfirmed() {
        return confirmed;
    }
}

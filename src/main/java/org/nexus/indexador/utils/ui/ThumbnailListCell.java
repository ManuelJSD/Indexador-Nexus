package org.nexus.indexador.utils.ui;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

/**
 * Celda re-utilizable para inyectar imágenes personalizadas junto a la ID numérica de la ListView.
 */
public abstract class ThumbnailListCell extends ListCell<String> {

    private final HBox content;
    private final Label idLabel;
    private final ImageView thumbnail;

    public ThumbnailListCell() {
        super();
        idLabel = new Label();
        idLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: white; -fx-min-width: 40px;");

        // Configuración pixel perfect e independiente de asimetrías
        thumbnail = new ImageView();
        thumbnail.setFitWidth(36);
        thumbnail.setFitHeight(36);
        thumbnail.setPreserveRatio(true);
        thumbnail.setSmooth(false);

        content = new HBox(12);
        content.setAlignment(Pos.CENTER_LEFT);
        content.setStyle("-fx-padding: 3 0 3 5;");
        content.getChildren().addAll(thumbnail, idLabel);
    }

    @Override
    protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        if (item != null && !empty) {
            String numericPart = item.replaceAll("[^0-9]", "");
            idLabel.setText("Grh: " + numericPart);  // Main lists are Grh
            try {
                Image img = loadThumbnailForItem(item);
                thumbnail.setImage(img);
            } catch (Exception e) {
                thumbnail.setImage(null);
            }
            setGraphic(content);
        } else {
            setGraphic(null);
            setText(null);
        }
    }

    /**
     * Resuelve el item a una imagen extraida. 
     * Por defecto asume que el input String contiene un ID base-1 tradicional e invoca a loadThumbnailForIndex(int).
     */
    protected Image loadThumbnailForItem(String rawItem) {
        try {
            int zeroBasedIndex = Integer.parseInt(rawItem.replaceAll("[^0-9]", "")) - 1;
            return loadThumbnailForIndex(zeroBasedIndex);
        } catch(Exception e) { return null; }
    }

    /**
     * Implementación estándar delegada. 
     */
    protected Image loadThumbnailForIndex(int zeroBasedIndex) {
        return null;
    }
}

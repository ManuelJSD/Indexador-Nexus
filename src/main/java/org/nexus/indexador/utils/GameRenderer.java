package org.nexus.indexador.utils;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import org.nexus.indexador.gamedata.models.GrhData;

import java.io.File;
import java.util.Map;

/**
 * Encapsulates the rendering logic for GrhData, including static images,
 * animations, and selection indicators.
 */
public class GameRenderer {
    private final ConfigManager configManager = ConfigManager.getInstance();
    private final ImageCache imageCache = ImageCache.getInstance();
    private final Logger logger = Logger.getInstance();

    private final ImageView imgIndice;
    private final ImageView imgGrafico;
    private final Rectangle rectanguloIndice;

    private Timeline animationTimeline;
    private int currentFrameIndex = 1;

    public GameRenderer(ImageView imgIndice, ImageView imgGrafico, Rectangle rectanguloIndice) {
        this.imgIndice = imgIndice;
        this.imgGrafico = imgGrafico;
        this.rectanguloIndice = rectanguloIndice;
    }

    /**
     * Entry point to display a GrhData. Automatically handles static vs animation.
     */
    public void displayGrh(GrhData grh, Map<Integer, GrhData> grhDataMap) {
        if (grh == null) {
            clearViews();
            return;
        }

        if (grh.getNumFrames() > 1) {
            displayAnimation(grh, grh.getNumFrames(), grhDataMap);
        } else {
            stopAnimation();
            displayStaticImage(grh);
        }
    }

    public void stopAnimation() {
        if (animationTimeline != null) {
            animationTimeline.stop();
        }
    }

    private void clearViews() {
        stopAnimation();
        imgIndice.setImage(null);
        imgGrafico.setImage(null);
        rectanguloIndice.setVisible(false);
    }

    private void displayStaticImage(GrhData selectedGrh) {
        String imagePath = getImagePath(selectedGrh.getFileNum());

        Image staticImage = imageCache.getImage(imagePath);
        if (staticImage != null) {
            drawFullImage(staticImage, selectedGrh);
            WritableImage croppedImage = imageCache.getCroppedImage(imagePath, selectedGrh.getsX(),
                    selectedGrh.getsY(), selectedGrh.getTileWidth(), selectedGrh.getTileHeight());

            if (croppedImage != null) {
                imgIndice.setPreserveRatio(true);
                imgIndice.setImage(croppedImage);
            }
        } else {
            logger.warning("No se encontró la imagen: " + imagePath);
        }
    }

    private void displayAnimation(GrhData selectedGrh, int nFrames, Map<Integer, GrhData> grhDataMap) {
        stopAnimation();
        currentFrameIndex = 1;

        animationTimeline = new Timeline(new KeyFrame(Duration.ZERO, event -> {
            updateFrame(selectedGrh, grhDataMap);
            currentFrameIndex = (currentFrameIndex + 1) % nFrames;
            if (currentFrameIndex == 0) {
                currentFrameIndex = 1;
            }
        }), new KeyFrame(Duration.millis(100)));

        animationTimeline.setCycleCount(Animation.INDEFINITE);
        animationTimeline.play();
    }

    private void updateFrame(GrhData selectedGrh, Map<Integer, GrhData> grhDataMap) {
        int[] frames = selectedGrh.getFrames();
        if (currentFrameIndex >= 0 && currentFrameIndex < frames.length) {
            int frameId = frames[currentFrameIndex];
            GrhData currentGrh = grhDataMap.get(frameId);

            if (currentGrh != null) {
                String imagePath = getImagePath(currentGrh.getFileNum());
                Image frameImage = imageCache.getImage(imagePath);

                if (frameImage != null) {
                    drawFullImage(frameImage, currentGrh);
                    WritableImage croppedImage = imageCache.getCroppedImage(imagePath, currentGrh.getsX(),
                            currentGrh.getsY(), currentGrh.getTileWidth(), currentGrh.getTileHeight());

                    if (croppedImage != null) {
                        imgIndice.setImage(croppedImage);
                    }
                } else {
                    logger.warning("No se encontró la imagen: " + imagePath);
                }
            }
        }
    }

    public void drawFullImage(Image image, GrhData grh) {
        try {
            imgGrafico.setImage(image);
            double MAX_WIDTH = 508.0;
            double MAX_HEIGHT = 374.0;

            if (image.getWidth() <= MAX_WIDTH && image.getHeight() <= MAX_HEIGHT) {
                imgGrafico.setFitWidth(image.getWidth());
                imgGrafico.setFitHeight(image.getHeight());
            } else {
                imgGrafico.setFitWidth(MAX_WIDTH);
                imgGrafico.setFitHeight(MAX_HEIGHT);
            }
            drawRectangle(grh);
        } catch (Exception e) {
            logger.error("Error al dibujar la imagen completa", e);
        }
    }

    public void drawRectangle(GrhData selectedGrh) {
        try {
            if (imgGrafico.getImage() == null)
                return;

            double imgViewWidth = imgGrafico.getBoundsInLocal().getWidth();
            double imgViewHeight = imgGrafico.getBoundsInLocal().getHeight();

            if (imgViewWidth <= 0)
                imgViewWidth = imgGrafico.getFitWidth();
            if (imgViewHeight <= 0)
                imgViewHeight = imgGrafico.getFitHeight();

            double originalWidth = imgGrafico.getImage().getWidth();
            double originalHeight = imgGrafico.getImage().getHeight();

            double scaleX = imgViewWidth / originalWidth;
            double scaleY = imgViewHeight / originalHeight;

            if (imgGrafico.isPreserveRatio()) {
                double scale = Math.min(scaleX, scaleY);
                scaleX = scale;
                scaleY = scale;
            }

            double layoutX = 5.0;
            double layoutY = 6.0;

            double rectX = selectedGrh.getsX() * scaleX + layoutX;
            double rectY = selectedGrh.getsY() * scaleY + layoutY;
            double rectWidth = selectedGrh.getTileWidth() * scaleX;
            double rectHeight = selectedGrh.getTileHeight() * scaleY;

            double xOffset = (imgViewWidth - (originalWidth * scaleX)) / 2;
            double yOffset = (imgViewHeight - (originalHeight * scaleY)) / 2;

            if (xOffset > 0)
                rectX += xOffset;
            if (yOffset > 0)
                rectY += yOffset;

            rectanguloIndice.setX(rectX);
            rectanguloIndice.setY(rectY);
            rectanguloIndice.setWidth(rectWidth);
            rectanguloIndice.setHeight(rectHeight);
            rectanguloIndice.setVisible(true);
        } catch (Exception e) {
            logger.error("Error al dibujar el rectángulo", e);
        }
    }

    private String getImagePath(int fileNum) {
        String base = configManager.getGraphicsDir() + fileNum;
        String png = base + ".png";
        if (new File(png).exists())
            return png;
        return base + ".bmp";
    }
}

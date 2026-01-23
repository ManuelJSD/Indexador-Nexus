package org.nexus.indexador.utils;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

public class ImageUtils {

    /**
     * Dibuja un sprite en un lienzo (canvas) y opcionalmente una segunda imagen
     * "fantasma" (overlay)
     * en una posición relativa (offset).
     *
     * @param baseImage    Imagen base (Cuerpo).
     * @param overlayImage Imagen a superponer (Cabeza) - puede ser null.
     * @param canvasWidth  Ancho del lienzo.
     * @param canvasHeight Alto del lienzo.
     * @param offsetX      Offset X de la imagen overlay respecto al origen (0,0) de
     *                     la imagen base.
     * @param offsetY      Offset Y de la imagen overlay respecto al origen (0,0) de
     *                     la imagen base.
     * @param showMarker   Si true, dibuja un marcador rojo en el punto de offset.
     * @return WritableImage con la composición.
     */
    public static WritableImage drawComposite(Image baseImage, Image overlayImage, int canvasWidth, int canvasHeight,
            int offsetX, int offsetY, boolean showMarker) {
        if (baseImage == null)
            return null;

        WritableImage canvas = new WritableImage(canvasWidth, canvasHeight);
        PixelWriter writer = canvas.getPixelWriter();

        int basW = (int) baseImage.getWidth();
        int basH = (int) baseImage.getHeight();

        // 1. Calcular posición para centrar la imagen BASE en el canvas
        int baseX = (canvasWidth - basW) / 2;
        int baseY = (canvasHeight - basH) / 2;

        // 2. Dibujar Base (Cuerpo)
        PixelReader baseReader = baseImage.getPixelReader();
        for (int x = 0; x < basW; x++) {
            for (int y = 0; y < basH; y++) {
                int argb = baseReader.getArgb(x, y);
                // Check alpha AND black color key (0x000000 is transparent)
                if ((argb >> 24) != 0 && (argb & 0x00FFFFFF) != 0) {
                    writePixelSafe(writer, canvasWidth, canvasHeight, baseX + x, baseY + y, argb);
                }
            }
        }

        // 3. Dibujar Overlay (Cabeza) si existe con lógica VB6:
        // Call Draw_Grh(.Head.Head(.Heading), PixelOffsetX + .Body.HeadOffset.X,
        // PixelOffsetY + .Body.HeadOffset.y ...)
        // Implica que la cabeza se dibuja RELATIVA a la posición del cuerpo usando el
        // Offset tal cual.
        if (overlayImage != null) {
            int ovW = (int) overlayImage.getWidth();
            int ovH = (int) overlayImage.getHeight();
            PixelReader ovReader = overlayImage.getPixelReader();

            // Offset directo sin magia
            int absOvX = baseX + offsetX;
            int absOvY = baseY + offsetY;

            for (int x = 0; x < ovW; x++) {
                for (int y = 0; y < ovH; y++) {
                    int argb = ovReader.getArgb(x, y);
                    // Dibuja solo si no es transparente total Y no es negro puro
                    if ((argb >> 24) != 0 && (argb & 0x00FFFFFF) != 0) {
                        writePixelSafe(writer, canvasWidth, canvasHeight, absOvX + x, absOvY + y, argb);
                    }
                }
            }
        }

        // 4. Dibujar Marcador (Si se solicita)
        if (showMarker) {
            // El marcador indica el punto (0,0) del Overlay relativo a la Base
            int markerX = baseX + offsetX;
            int markerY = baseY + offsetY;
            Color color = Color.RED;

            // Cruz grande (5px brazos)
            int size = 5;
            for (int i = -size; i <= size; i++) {
                writePixelSafeColor(writer, canvasWidth, canvasHeight, markerX + i, markerY, color); // H
                writePixelSafeColor(writer, canvasWidth, canvasHeight, markerX, markerY + i, color); // V
            }
        }

        return canvas;
    }

    /* Helper para dibujar pixel seguro (int argb) */
    private static void writePixelSafe(PixelWriter writer, int w, int h, int x, int y, int argb) {
        if (x >= 0 && x < w && y >= 0 && y < h) {
            writer.setArgb(x, y, argb);
        }
    }

    /* Helper para dibujar pixel seguro (Color) */
    private static void writePixelSafeColor(PixelWriter writer, int w, int h, int x, int y, Color color) {
        if (x >= 0 && x < w && y >= 0 && y < h) {
            writer.setColor(x, y, color);
        }
    }

    // Legacy support for FxsController (can be updated later or kept)
    public static WritableImage drawSpriteOnCanvas(Image sprite, int canvasWidth, int canvasHeight, int marginX,
            int marginY, int markerX, int markerY, Color color) {

        WritableImage canvas = new WritableImage(canvasWidth, canvasHeight);
        PixelWriter writer = canvas.getPixelWriter();
        PixelReader reader = sprite.getPixelReader();
        int spriteW = (int) sprite.getWidth();
        int spriteH = (int) sprite.getHeight();
        for (int x = 0; x < spriteW; x++) {
            for (int y = 0; y < spriteH; y++) {
                int destX = marginX + x;
                int destY = marginY + y;
                if (destX >= 0 && destX < canvasWidth && destY >= 0 && destY < canvasHeight) {
                    writer.setArgb(destX, destY, reader.getArgb(x, y));
                }
            }
        }
        int absMarkerX = marginX + markerX;
        int absMarkerY = marginY + markerY;
        for (int i = -5; i <= 5; i++) {
            writePixelSafeColor(writer, canvasWidth, canvasHeight, absMarkerX + i, absMarkerY, color);
            writePixelSafeColor(writer, canvasWidth, canvasHeight, absMarkerX, absMarkerY + i, color);
        }
        return canvas;
    }

    // Legacy
    public static WritableImage drawMarker(Image source, int x, int y, Color color) {
        return drawSpriteOnCanvas(source, (int) source.getWidth(), (int) source.getHeight(), 0, 0, x, y, color);
    }
}

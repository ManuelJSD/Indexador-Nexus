package org.nexus.indexador.utils;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Servicio para la detección automática de regiones de sprites (Auto-Tiling).
 * Implementa el patrón
 * Singleton.
 */
public class AutoTilingService {

  private static volatile AutoTilingService instance;
  private final Logger logger = Logger.getInstance();

  private AutoTilingService() {
  }

  public static AutoTilingService getInstance() {
    if (instance == null) {
      synchronized (AutoTilingService.class) {
        if (instance == null) {
          instance = new AutoTilingService();
        }
      }
    }
    return instance;
  }

  /**
   * Detecta regiones contiguas de píxeles no transparentes en una imagen. Utiliza
   * un algoritmo de
   * Flood Fill (BFS).
   *
   * @param image La imagen a procesar.
   * @return Lista de rectángulos delimitadores (Bounding Boxes) de los sprites
   *         detectados.
   */
  public List<Rectangle> detectSprites(Image image) {
    List<Rectangle> regions = new ArrayList<>();

    if (image == null)
      return regions;

    int width = (int) image.getWidth();
    int height = (int) image.getHeight();
    PixelReader reader = image.getPixelReader();

    // BitSet para marcar píxeles visitados (más eficiente que boolean[][])
    BitSet visited = new BitSet(width * height);

    logger.info("Iniciando detección de sprites en imagen de " + width + "x" + height);

    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        // Si ya fue visitado, saltar
        if (visited.get(y * width + x))
          continue;

        // Verificar transparencia
        Color color = reader.getColor(x, y);
        if (isTransparent(color)) {
          visited.set(y * width + x);
          continue;
        }

        // Encontramos un nuevo pixel no transparente -> Iniciar Flood Fill
        Rectangle region = floodFill(reader, visited, width, height, x, y);

        // Filtrar ruido (opcional: regiones muy pequeñas)
        if (region.getWidth() > 2 && region.getHeight() > 2) {
          regions.add(region);
        }
      }
    }

    // Ordenar regiones geométricamente (Lectura: Arriba->Abajo, Izquierda->Derecha)
    // Esto es crucial para mantener el orden de los frames en animaciones.
    regions.sort((r1, r2) -> {
      // Tolerancia vertical para agrupar en "filas" (50% de la altura más grande)
      // Si la diferencia vertical es pequeña, asumimos que están en la misma fila.
      double h = Math.max(r1.getHeight(), r2.getHeight());
      double tolerance = h * 0.5;

      if (Math.abs(r1.getY() - r2.getY()) < tolerance) {
        // Misma fila -> Ordenar por X (Izquierda a Derecha)
        return Double.compare(r1.getX(), r2.getX());
      } else {
        // Diferente fila -> Ordenar por Y (Arriba a Abajo)
        return Double.compare(r1.getY(), r2.getY());
      }
    });

    logger.info("Se detectaron " + regions.size() + " sprites.");
    return regions;
  }

  private boolean isTransparent(Color color) {
    // Consideramos transparente si alpha es < 10%
    // O si el color es NEGRO PURO (0,0,0) típico de sprites viejos
    return color.getOpacity() < 0.1
        || (color.getRed() == 0 && color.getGreen() == 0 && color.getBlue() == 0);
  }

  private Rectangle floodFill(PixelReader reader, BitSet visited, int width, int height, int startX,
      int startY) {
    int minX = startX;
    int maxX = startX;
    int minY = startY;
    int maxY = startY;

    Queue<Point> queue = new LinkedList<>();
    queue.add(new Point(startX, startY));
    visited.set(startY * width + startX);

    while (!queue.isEmpty()) {
      Point p = queue.poll();

      // Actualizar bounding box
      if (p.x < minX)
        minX = p.x;
      if (p.x > maxX)
        maxX = p.x;
      if (p.y < minY)
        minY = p.y;
      if (p.y > maxY)
        maxY = p.y;

      // Revisar vecinos (4-connectivity)
      int[] dx = { 0, 0, 1, -1 };
      int[] dy = { 1, -1, 0, 0 };

      for (int i = 0; i < 4; i++) {
        int nx = p.x + dx[i];
        int ny = p.y + dy[i];

        if (nx >= 0 && nx < width && ny >= 0 && ny < height) {
          int index = ny * width + nx;
          if (!visited.get(index)) {
            Color color = reader.getColor(nx, ny);
            if (!isTransparent(color)) {
              visited.set(index);
              queue.add(new Point(nx, ny));
            } else {
              // Marcar como visitado aunque sea transparente para no chequearlo de nuevo
              // (Ojo: esto optimiza el escaneo principal)
              // PERO: si lo marcamos aquí, en el loop principal ya aparecerá como visitado.
              // Correcto, porque ya sabemos que es transparente.
              visited.set(index);
            }
          }
        }
      }
    }

    return new Rectangle(minX, minY, maxX - minX + 1, maxY - minY + 1);
  }

  private static class Point {
    int x, y;

    Point(int x, int y) {
      this.x = x;
      this.y = y;
    }
  }

  /**
   * Divide las regiones detectadas en celdas de tamaño fijo (Grid) y
   * opcionalmente por estructura Atlas.
   * Si atlasCols > 1 o atlasRows > 1, primero divide la región en ese numero de
   * bloques.
   */
  public List<Rectangle> splitRegions(List<Rectangle> regions, int tileW, int tileH, int atlasCols, int atlasRows,
      PixelReader reader) {
    List<Rectangle> splitList = new ArrayList<>();

    for (Rectangle r : regions) {
      // Normalizar ("Snap") la región a la grilla global
      double startX = Math.floor(r.getX() / tileW) * tileW;
      double startY = Math.floor(r.getY() / tileH) * tileH;

      double endX = Math.ceil((r.getX() + r.getWidth()) / tileW) * tileW;
      double endY = Math.ceil((r.getY() + r.getHeight()) / tileH) * tileH;

      double totalW = Math.max(tileW, endX - startX);
      double totalH = Math.max(tileH, endY - startY);

      // Si definimos estructura Atlas, dividimos este 'totalW'
      if (atlasCols > 1 || atlasRows > 1) {
        double blockW = totalW / atlasCols;
        double blockH = totalH / atlasRows;

        blockW = Math.max(tileW, Math.round(blockW / tileW) * tileW);
        blockH = Math.max(tileH, Math.round(blockH / tileH) * tileH);

        for (int row = 0; row < atlasRows; row++) {
          for (int col = 0; col < atlasCols; col++) {
            double bX = startX + (col * blockW);
            double bY = startY + (row * blockH);

            Rectangle blockRect = new Rectangle(bX, bY, blockW, blockH);
            addSplitTiles(splitList, blockRect, tileW, tileH, reader);
          }
        }
      } else {
        // Comportamiento normal
        Rectangle snappedR = new Rectangle(startX, startY, totalW, totalH);
        addSplitTiles(splitList, snappedR, tileW, tileH, reader);
      }
    }
    return splitList;
  }

  // Helper simple para dividir un rectangulo en tiles (Overload para
  // compatibilidad)
  public List<Rectangle> splitRegions(List<Rectangle> regions, int tileW, int tileH, int atlasCols, int atlasRows) {
    return splitRegions(regions, tileW, tileH, atlasCols, atlasRows, null);
  }

  public List<Rectangle> splitRegions(List<Rectangle> regions, int tileW, int tileH) {
    return splitRegions(regions, tileW, tileH, 1, 1, null);
  }

  private void addSplitTiles(List<Rectangle> targetList, Rectangle r, int tileW, int tileH, PixelReader reader) {
    double w = r.getWidth();
    double h = r.getHeight();

    int cols = (int) Math.round(w / tileW);
    int rows = (int) Math.round(h / tileH);
    if (cols < 1)
      cols = 1;
    if (rows < 1)
      rows = 1;

    double startX = r.getX();
    double startY = r.getY();

    for (int row = 0; row < rows; row++) {
      for (int col = 0; col < cols; col++) {
        Rectangle tile = new Rectangle(startX + (col * tileW), startY + (row * tileH), tileW, tileH);

        // Si tenemos reader, verificamos si el tile contiene algo visible
        if (reader != null) {
          if (containsPixels(tile, reader)) {
            targetList.add(tile);
          }
        } else {
          targetList.add(tile);
        }
      }
    }
  }

  private boolean containsPixels(Rectangle r, PixelReader reader) {
    int x1 = (int) r.getX();
    int y1 = (int) r.getY();
    int w = (int) r.getWidth();
    int h = (int) r.getHeight();

    // Muestrear píxeles (no hace falta chequear todos, quizás un step de 2 o 4)
    for (int y = y1 + 2; y < y1 + h - 2; y += 2) {
      for (int x = x1 + 2; x < x1 + w - 2; x += 2) {
        try {
          Color c = reader.getColor(x, y);
          if (!isTransparent(c)) {
            return true;
          }
        } catch (Exception e) {
          // Out of bounds ignore
        }
      }
    }
    return false;
  }
}

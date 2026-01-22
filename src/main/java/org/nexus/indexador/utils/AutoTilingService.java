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
 * Servicio para la detección automática de regiones de sprites (Auto-Tiling). Implementa el patrón
 * Singleton.
 */
public class AutoTilingService {

  private static volatile AutoTilingService instance;
  private final Logger logger = Logger.getInstance();

  private AutoTilingService() {}

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
   * Detecta regiones contiguas de píxeles no transparentes en una imagen. Utiliza un algoritmo de
   * Flood Fill (BFS).
   *
   * @param image La imagen a procesar.
   * @return Lista de rectángulos delimitadores (Bounding Boxes) de los sprites detectados.
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
      int[] dx = {0, 0, 1, -1};
      int[] dy = {1, -1, 0, 0};

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
}

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
 * Implementa el patrón Singleton.
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
   * un algoritmo de Flood Fill (BFS).
   */
  public List<Rectangle> detectSprites(Image image) {
    List<Rectangle> regions = new ArrayList<>();

    if (image == null)
      return regions;

    int width = (int) image.getWidth();
    int height = (int) image.getHeight();
    PixelReader reader = image.getPixelReader();

    BitSet visited = new BitSet(width * height);

    logger.info("Iniciando detección de sprites en imagen de " + width + "x" + height);

    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        if (visited.get(y * width + x))
          continue;

        Color color = reader.getColor(x, y);
        if (isTransparent(color)) {
          visited.set(y * width + x);
          continue;
        }

        Rectangle region = floodFill(reader, visited, width, height, x, y);

        if (region.getWidth() > 2 && region.getHeight() > 2) {
          regions.add(region);
        }
      }
    }

    sortRegions(regions);

    logger.info("Se detectaron " + regions.size() + " sprites.");
    return regions;
  }

  public List<Rectangle> detectSprites(Image image, int mergeTolerance) {
    return detectSprites(image, 2, mergeTolerance);
  }

  public List<Rectangle> detectSprites(Image image, int tolX, int tolY) {
    List<Rectangle> regions = detectSprites(image);

    if (!regions.isEmpty()) {
      if (tolX > 0 || tolY > 0) {
        regions = mergeRegions(regions, tolX, tolY);
      }

      regions = mergeOrphanedSparks(regions);

      // Post-Refinement: Split oversized blobs that likely contain multiple merged
      // frames
      regions = refineBySplittingOversizedBlobs(regions);

      sortRegions(regions);

      logger
          .info("Smart Detección (X:" + tolX + ", Y:" + tolY + ") + Orphans + Split: " + regions.size() + " sprites.");
    }
    return regions;
  }

  private List<Rectangle> refineBySplittingOversizedBlobs(List<Rectangle> blobs) {
    if (blobs.isEmpty())
      return blobs;

    // 1. Calculate Median Height (robust average)
    List<Double> heights = new ArrayList<>();
    for (Rectangle r : blobs)
      heights.add(r.getHeight());
    heights.sort(Double::compare);
    double medianHeight = heights.get(heights.size() / 2);

    // If median is small (garbage), abort
    if (medianHeight < 10)
      return blobs;

    List<Rectangle> refined = new ArrayList<>();
    boolean splitOccurred = false;

    for (Rectangle r : blobs) {
      // Check if this blob is roughly a multiple of the median height (1.8x
      // threshold)
      if (r.getHeight() > medianHeight * 1.8) {
        // It's likely 2 or more frames fused verticaly
        int numFrames = (int) Math.round(r.getHeight() / medianHeight);
        if (numFrames < 2)
          numFrames = 2; // Should be at least 2 if we are here

        double splitHeight = r.getHeight() / numFrames;
        for (int i = 0; i < numFrames; i++) {
          // Create sub-rectangles
          // We keep the same X/Width, just slice Y
          Rectangle sub = new Rectangle(
              r.getX(),
              r.getY() + (i * splitHeight),
              r.getWidth(),
              splitHeight);
          refined.add(sub);
        }
        splitOccurred = true;
      } else {
        refined.add(r);
      }
    }

    return splitOccurred ? refined : blobs;
  }

  private List<Rectangle> mergeRegions(List<Rectangle> regions, int tolX, int tolY) {
    boolean changed = true;
    while (changed) {
      changed = false;
      for (int i = 0; i < regions.size(); i++) {
        Rectangle r1 = regions.get(i);
        for (int j = i + 1; j < regions.size(); j++) {
          Rectangle r2 = regions.get(j);

          if (shouldMerge(r1, r2, tolX, tolY)) {
            double minX = Math.min(r1.getX(), r2.getX());
            double minY = Math.min(r1.getY(), r2.getY());
            double maxX = Math.max(r1.getX() + r1.getWidth(), r2.getX() + r2.getWidth());
            double maxY = Math.max(r1.getY() + r1.getHeight(), r2.getY() + r2.getHeight());

            Rectangle newRect = new Rectangle(minX, minY, maxX - minX, maxY - minY);
            regions.set(i, newRect);
            regions.remove(j);
            changed = true;
            j--;
            r1 = newRect;
          }
        }
      }
    }
    return regions;
  }

  private List<Rectangle> mergeOrphanedSparks(List<Rectangle> blobs) {
    if (blobs.size() < 2)
      return blobs;

    List<Double> heights = new ArrayList<>();
    double maxArea = 0;
    for (Rectangle r : blobs) {
      double area = r.getWidth() * r.getHeight();
      maxArea = Math.max(maxArea, area);
    }

    // Identify likely "Main Bodies" and calculate Median Height
    double sparkThreshold = maxArea * 0.4; // 40% threshold from before

    for (Rectangle r : blobs) {
      if (r.getWidth() * r.getHeight() > sparkThreshold) {
        heights.add(r.getHeight());
      }
    }
    heights.sort(Double::compare);
    double medianHeight = heights.isEmpty() ? Double.MAX_VALUE : heights.get(heights.size() / 2);

    // Constraint: Merged blob shouldn't be excessively taller than median (e.g. >
    // 1.3x)
    // Helps prevent merging two stacked frames via a spark bridge.
    double maxHeightLimit = (medianHeight == Double.MAX_VALUE) ? Double.MAX_VALUE : (medianHeight * 1.4);

    boolean changed = true;
    while (changed) {
      changed = false;
      for (int i = 0; i < blobs.size(); i++) {
        Rectangle spark = blobs.get(i);
        double area = spark.getWidth() * spark.getHeight();

        // Treat everything smaller than threshold as potential spark
        // But if it's already huge, don't move it.
        // Also don't merge if spark itself is taller than limit (unlikely)
        if (area > sparkThreshold)
          continue;

        int bestMatchIdx = -1;
        double minDistance = Double.MAX_VALUE;

        for (int j = 0; j < blobs.size(); j++) {
          if (i == j)
            continue;
          Rectangle body = blobs.get(j);
          // Don't merge into another small spark
          if (body.getWidth() * body.getHeight() <= sparkThreshold / 2)
            continue;

          // Loose Alignment: Check for ANY horizontal overlap
          boolean overlapsX = (spark.getX() < body.getX() + body.getWidth()) &&
              (spark.getX() + spark.getWidth() > body.getX());

          if (overlapsX) {
            double dist = distanceBetween(spark, body);
            // 80px tolerance for gap
            if (dist < 80 && dist < minDistance) {
              // CRITICAL CHECK: Does merging break the height limit?
              double newMinY = Math.min(spark.getY(), body.getY());
              double newMaxY = Math.max(spark.getY() + spark.getHeight(), body.getY() + body.getHeight());
              double newHeight = newMaxY - newMinY;

              // If body is ALREADY super tall (e.g. big animation), ignore limit.
              // But if body is normal-ish, enforce limit.
              boolean bodyIsAlreadyTall = body.getHeight() > maxHeightLimit;

              if (!bodyIsAlreadyTall && newHeight > maxHeightLimit) {
                continue; // Skip this merge, it makes the sprite too tall (likely 2 frames)
              }

              minDistance = dist;
              bestMatchIdx = j;
            }
          }
        }

        if (bestMatchIdx != -1) {
          Rectangle body = blobs.get(bestMatchIdx);
          double minX = Math.min(spark.getX(), body.getX());
          double minY = Math.min(spark.getY(), body.getY());
          double maxX = Math.max(spark.getX() + spark.getWidth(), body.getX() + body.getWidth());
          double maxY = Math.max(spark.getY() + spark.getHeight(), body.getY() + body.getHeight());

          Rectangle newBody = new Rectangle(minX, minY, maxX - minX, maxY - minY);
          blobs.set(bestMatchIdx, newBody);
          blobs.remove(i);
          changed = true;
          i--;
          break;
        }
      }
    }
    return blobs;
  }

  private double distanceBetween(Rectangle r1, Rectangle r2) {
    if (r1.getY() + r1.getHeight() < r2.getY()) {
      return r2.getY() - (r1.getY() + r1.getHeight());
    } else if (r2.getY() + r2.getHeight() < r1.getY()) {
      return r1.getY() - (r2.getY() + r2.getHeight());
    } else {
      return 0;
    }
  }

  private boolean shouldMerge(Rectangle r1, Rectangle r2, int tolX, int tolY) {
    double r1x = r1.getX() - tolX;
    double r1y = r1.getY() - tolY;
    double r1w = r1.getWidth() + (tolX * 2);
    double r1h = r1.getHeight() + (tolY * 2);

    return r1x < r2.getX() + r2.getWidth() &&
        r1x + r1w > r2.getX() &&
        r1y < r2.getY() + r2.getHeight() &&
        r1y + r1h > r2.getY();
  }

  private boolean isTransparent(Color color) {
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

      if (p.x < minX)
        minX = p.x;
      if (p.x > maxX)
        maxX = p.x;
      if (p.y < minY)
        minY = p.y;
      if (p.y > maxY)
        maxY = p.y;

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
              visited.set(index);
            }
          }
        }
      }
    }

    return new Rectangle(minX, minY, maxX - minX + 1, maxY - minY + 1);
  }

  private void sortRegions(List<Rectangle> regions) {
    regions.sort((r1, r2) -> {
      double h = Math.max(r1.getHeight(), r2.getHeight());
      double tolerance = h * 0.5;
      if (Math.abs(r1.getY() - r2.getY()) < tolerance) {
        return Double.compare(r1.getX(), r2.getX());
      } else {
        return Double.compare(r1.getY(), r2.getY());
      }
    });
  }

  private static class Point {
    int x, y;

    Point(int x, int y) {
      this.x = x;
      this.y = y;
    }
  }

  public List<Rectangle> splitRegions(List<Rectangle> regions, int tileW, int tileH, int atlasCols, int atlasRows,
      PixelReader reader) {
    List<Rectangle> splitList = new ArrayList<>();

    for (Rectangle r : regions) {
      double startX = Math.floor(r.getX() / tileW) * tileW;
      double startY = Math.floor(r.getY() / tileH) * tileH;

      double endX = Math.ceil((r.getX() + r.getWidth()) / tileW) * tileW;
      double endY = Math.ceil((r.getY() + r.getHeight()) / tileH) * tileH;

      double totalW = Math.max(tileW, endX - startX);
      double totalH = Math.max(tileH, endY - startY);

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
        Rectangle snappedR = new Rectangle(startX, startY, totalW, totalH);
        addSplitTiles(splitList, snappedR, tileW, tileH, reader);
      }
    }
    return splitList;
  }

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

    for (int y = y1 + 2; y < y1 + h - 2; y += 2) {
      for (int x = x1 + 2; x < x1 + w - 2; x += 2) {
        try {
          Color c = reader.getColor(x, y);
          if (!isTransparent(c)) {
            return true;
          }
        } catch (Exception e) {
        }
      }
    }
    return false;
  }
}

package org.nexus.indexador.utils;

import org.nexus.indexador.gamedata.models.GrhData;

import java.io.*;
import java.util.List;

/**
 * Servicio para exportar datos de GRH a diferentes formatos. Soporta JSON y CSV.
 */
public class ExportService {

  private static volatile ExportService instance;
  private final Logger logger = Logger.getInstance();

  private ExportService() {}

  public static ExportService getInstance() {
    if (instance == null) {
      synchronized (ExportService.class) {
        if (instance == null) {
          instance = new ExportService();
        }
      }
    }
    return instance;
  }

  /**
   * Exporta la lista de GRH a formato JSON.
   *
   * @param grhList Lista de GrhData a exportar.
   * @param file Archivo destino.
   * @return true si la exportación fue exitosa.
   */
  public boolean exportToJson(List<GrhData> grhList, File file) {
    logger.info("Exportando a JSON: " + file.getAbsolutePath());

    try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
      writer.write("{\n");
      writer.write("  \"version\": 1,\n");
      writer.write("  \"totalGrhs\": " + grhList.size() + ",\n");
      writer.write("  \"grhs\": [\n");

      for (int i = 0; i < grhList.size(); i++) {
        GrhData grh = grhList.get(i);
        writer.write(grhToJson(grh, "    "));

        if (i < grhList.size() - 1) {
          writer.write(",");
        }
        writer.write("\n");
      }

      writer.write("  ]\n");
      writer.write("}\n");

      logger.info("Exportación JSON completada: " + grhList.size() + " registros");
      return true;

    } catch (IOException e) {
      logger.error("Error al exportar a JSON", e);
      return false;
    }
  }

  /**
   * Exporta la lista de GRH a formato CSV.
   *
   * @param grhList Lista de GrhData a exportar.
   * @param file Archivo destino.
   * @return true si la exportación fue exitosa.
   */
  public boolean exportToCsv(List<GrhData> grhList, File file) {
    logger.info("Exportando a CSV: " + file.getAbsolutePath());

    try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
      // Header
      writer.write("grh,numFrames,fileNum,sX,sY,tileWidth,tileHeight,speed,isAnimation,frames\n");

      for (GrhData grh : grhList) {
        writer.write(grhToCsv(grh));
        writer.write("\n");
      }

      logger.info("Exportación CSV completada: " + grhList.size() + " registros");
      return true;

    } catch (IOException e) {
      logger.error("Error al exportar a CSV", e);
      return false;
    }
  }

  /**
   * Convierte un GrhData a su representación JSON.
   */
  private String grhToJson(GrhData grh, String indent) {
    StringBuilder sb = new StringBuilder();
    sb.append(indent).append("{\n");
    sb.append(indent).append("  \"grh\": ").append(grh.getGrh()).append(",\n");
    sb.append(indent).append("  \"numFrames\": ").append(grh.getNumFrames()).append(",\n");

    if (grh.getNumFrames() > 1) {
      // Animación
      sb.append(indent).append("  \"isAnimation\": true,\n");
      sb.append(indent).append("  \"speed\": ").append(grh.getSpeed()).append(",\n");
      sb.append(indent).append("  \"frames\": [");

      int[] frames = grh.getFrames();
      if (frames != null) {
        for (int i = 1; i <= grh.getNumFrames() && i < frames.length; i++) {
          sb.append(frames[i]);
          if (i < grh.getNumFrames()) {
            sb.append(", ");
          }
        }
      }
      sb.append("]\n");
    } else {
      // Estático
      sb.append(indent).append("  \"isAnimation\": false,\n");
      sb.append(indent).append("  \"fileNum\": ").append(grh.getFileNum()).append(",\n");
      sb.append(indent).append("  \"sX\": ").append(grh.getsX()).append(",\n");
      sb.append(indent).append("  \"sY\": ").append(grh.getsY()).append(",\n");
      sb.append(indent).append("  \"tileWidth\": ").append(grh.getTileWidth()).append(",\n");
      sb.append(indent).append("  \"tileHeight\": ").append(grh.getTileHeight()).append("\n");
    }

    sb.append(indent).append("}");
    return sb.toString();
  }

  /**
   * Convierte un GrhData a su representación CSV.
   */
  private String grhToCsv(GrhData grh) {
    StringBuilder sb = new StringBuilder();
    sb.append(grh.getGrh()).append(",");
    sb.append(grh.getNumFrames()).append(",");

    if (grh.getNumFrames() > 1) {
      // Animación
      sb.append(",,,,,");
      sb.append(grh.getSpeed()).append(",");
      sb.append("true,");

      // Frames como string separado por punto y coma
      int[] frames = grh.getFrames();
      if (frames != null) {
        sb.append("\"");
        for (int i = 1; i <= grh.getNumFrames() && i < frames.length; i++) {
          sb.append(frames[i]);
          if (i < grh.getNumFrames()) {
            sb.append(";");
          }
        }
        sb.append("\"");
      }
    } else {
      // Estático
      sb.append(grh.getFileNum()).append(",");
      sb.append(grh.getsX()).append(",");
      sb.append(grh.getsY()).append(",");
      sb.append(grh.getTileWidth()).append(",");
      sb.append(grh.getTileHeight()).append(",");
      sb.append(",false,");
    }

    return sb.toString();
  }
}

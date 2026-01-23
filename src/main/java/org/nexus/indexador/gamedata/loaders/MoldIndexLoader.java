package org.nexus.indexador.gamedata.loaders;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.nexus.indexador.gamedata.enums.IndexingSystem;
import org.nexus.indexador.gamedata.models.*;
import org.nexus.indexador.utils.ConfigManager;
import org.nexus.indexador.utils.Logger;
import org.nexus.indexador.utils.byteMigration;
import org.nexus.indexador.utils.ResourceResolver;

import java.io.*;
import java.util.Map;

/**
 * Implementación del cargador para el sistema de moldes.
 */
public class MoldIndexLoader implements IndexLoader {

  private final ConfigManager configManager;
  private final byteMigration byteMigration;
  private final Logger logger;
  private final TraditionalIndexLoader traditionalLoader;
  private final java.util.Map<String, IndFileFormat> detectedFormats = new java.util.HashMap<>();

  public MoldIndexLoader() throws IOException {
    this.configManager = ConfigManager.getInstance();
    this.byteMigration = org.nexus.indexador.utils.byteMigration.getInstance();
    this.logger = Logger.getInstance();
    this.traditionalLoader = new TraditionalIndexLoader();
  }

  @Override
  public ObservableList<HeadData> loadHeads() throws IOException {
    logger.info("Cargando datos de cabezas (Sistema de Moldes)...");
    ObservableList<HeadData> headList = FXCollections.observableArrayList();
    File archivo = ResourceResolver.getHeadsInd(configManager.getInitDir());

    if (!archivo.exists()) {
      logger.error("No se encontró cabezas.ind / Heads.ind en: " + configManager.getInitDir());
      return headList;
    }

    try (RandomAccessFile file = new RandomAccessFile(archivo, "r")) {
      IndFileFormat format = detectFormat(file, 7, 8, 10, 12);
      recordFormat("HEADS", format);

      if (!format.isValid()) {
        logger.error("Formato de cabezas.ind (Moldes) inválido o no reconocido. Tamaño: " + file.length());
        return headList;
      }

      file.seek(format.getDataOffset());
      short numHeads = byteMigration.bigToLittle_Short(file.readShort());
      logger
          .info("DIAGNOSTICO: Detectadas " + numHeads + " cabezas. Offset: " + format.getDataOffset() + ", RecordSize: "
              + format.getRecordSize());

      for (int i = 0; i < numHeads; i++) {
        int std;
        if (format.getRecordSize() >= 10) {
          std = byteMigration.bigToLittle_Int(file.readInt());
        } else if (format.getRecordSize() == 8) {
          std = byteMigration.bigToLittle_Short(file.readShort());
        } else if (format.getRecordSize() == 7) {
          std = file.readUnsignedByte();
        } else {
          std = byteMigration.bigToLittle_Short(file.readShort());
        }
        short texture = byteMigration.bigToLittle_Short(file.readShort());
        short startX = byteMigration.bigToLittle_Short(file.readShort());
        short startY = byteMigration.bigToLittle_Short(file.readShort());
        headList.add(new HeadData(std, texture, startX, startY));
      }
    } catch (Exception e) {
      logger.error("Error al cargar índice de cabezas (Moldes)", e);
    }
    return headList;
  }

  @Override
  public ObservableList<HelmetData> loadHelmets() throws IOException {
    logger.info("Cargando datos de cascos (Sistema de Moldes)...");
    ObservableList<HelmetData> helmetList = FXCollections.observableArrayList();
    File archivo = ResourceResolver.getHelmetsInd(configManager.getInitDir());

    if (!archivo.exists()) {
      logger.error("No se encontró cascos.ind / Helmets.ind en: " + configManager.getInitDir());
      return helmetList;
    }

    try (RandomAccessFile file = new RandomAccessFile(archivo, "r")) {
      IndFileFormat format = detectFormat(file, 7, 8, 10, 12);
      recordFormat("HELMETS", format);

      if (!format.isValid()) {
        logger.error("Formato de cascos.ind (Moldes) inválido o no reconocido. Tamaño: " + file.length());
        return helmetList;
      }

      file.seek(format.getDataOffset());
      short numHelmets = byteMigration.bigToLittle_Short(file.readShort());
      logger.info(
          "DIAGNOSTICO: Detectados " + numHelmets + " cascos. Offset: " + format.getDataOffset() + ", RecordSize: "
              + format.getRecordSize());

      for (int i = 0; i < numHelmets; i++) {
        int std;
        if (format.getRecordSize() >= 10) {
          std = byteMigration.bigToLittle_Int(file.readInt());
        } else if (format.getRecordSize() == 8) {
          std = byteMigration.bigToLittle_Short(file.readShort());
        } else if (format.getRecordSize() == 7) {
          std = file.readUnsignedByte();
        } else {
          std = byteMigration.bigToLittle_Short(file.readShort());
        }
        short texture = byteMigration.bigToLittle_Short(file.readShort());
        short startX = byteMigration.bigToLittle_Short(file.readShort());
        short startY = byteMigration.bigToLittle_Short(file.readShort());
        helmetList.add(new HelmetData(std, texture, startX, startY));
      }
    } catch (Exception e) {
      logger.error("Error al cargar índice de cascos (Moldes)", e);
    }
    return helmetList;
  }

  @Override
  public ObservableList<BodyData> loadBodies() throws IOException {
    return traditionalLoader.loadBodies();
  }

  @Override
  public ObservableList<ShieldData> loadShields() throws IOException {
    return traditionalLoader.loadShields();
  }

  @Override
  public ObservableList<FXData> loadFXs() throws IOException {
    return traditionalLoader.loadFXs();
  }

  @Override
  public ObservableList<GrhData> loadGrhs() throws IOException {
    return traditionalLoader.loadGrhs();
  }

  @Override
  public ObservableList<WeaponData> loadWeapons() throws IOException {
    return traditionalLoader.loadWeapons();
  }

  @Override
  public void saveHeads(ObservableList<HeadData> entries) throws IOException {
    File archivo = ResourceResolver.getHeadsInd(configManager.getInitDir());
    IndFileFormat format = detectedFormats.getOrDefault("HEADS", new IndFileFormat(0, true, true, 10));
    int recSize = format.getRecordSize();

    try (RandomAccessFile file = new RandomAccessFile(archivo, "rw")) {
      // Si hay cabecera (offset > 0), saltar o escribir placeholder
      if (format.getDataOffset() > 0) {
        if (file.length() < format.getDataOffset()) {
          file.write(new byte[(int) format.getDataOffset()]);
        }
        file.seek(format.getDataOffset());
      } else {
        file.seek(0);
      }

      file.writeShort(byteMigration.bigToLittle_Short((short) entries.size()));
      for (HeadData entry : entries) {
        if (recSize >= 10) {
          file.writeInt(byteMigration.bigToLittle_Int(entry.getStd()));
        } else if (recSize == 8) {
          file.writeShort(byteMigration.bigToLittle_Short((short) entry.getStd()));
        } else if (recSize == 7) {
          file.writeByte(entry.getStd());
        } else {
          file.writeShort(byteMigration.bigToLittle_Short((short) entry.getStd()));
        }
        file.writeShort(byteMigration.bigToLittle_Short(entry.getTexture()));
        file.writeShort(byteMigration.bigToLittle_Short(entry.getStartX()));
        file.writeShort(byteMigration.bigToLittle_Short(entry.getStartY()));
      }
      // Truncar para evitar residuos de datos anteriores
      file.setLength(file.getFilePointer());
    }
  }

  @Override
  public void saveHelmets(ObservableList<HelmetData> entries) throws IOException {
    File archivo = ResourceResolver.getHelmetsInd(configManager.getInitDir());
    IndFileFormat format = detectedFormats.getOrDefault("HELMETS", new IndFileFormat(0, true, true, 10));
    int recSize = format.getRecordSize();

    try (RandomAccessFile file = new RandomAccessFile(archivo, "rw")) {
      if (format.getDataOffset() > 0) {
        if (file.length() < format.getDataOffset()) {
          file.write(new byte[(int) format.getDataOffset()]);
        }
        file.seek(format.getDataOffset());
      } else {
        file.seek(0);
      }

      file.writeShort(byteMigration.bigToLittle_Short((short) entries.size()));
      for (HelmetData entry : entries) {
        if (recSize >= 10) {
          file.writeInt(byteMigration.bigToLittle_Int(entry.getStd()));
        } else if (recSize == 8) {
          file.writeShort(byteMigration.bigToLittle_Short((short) entry.getStd()));
        } else if (recSize == 7) {
          file.writeByte(entry.getStd());
        } else {
          file.writeShort(byteMigration.bigToLittle_Short((short) entry.getStd()));
        }
        file.writeShort(byteMigration.bigToLittle_Short(entry.getTexture()));
        file.writeShort(byteMigration.bigToLittle_Short(entry.getStartX()));
        file.writeShort(byteMigration.bigToLittle_Short(entry.getStartY()));
      }
      file.setLength(file.getFilePointer());
    }
  }

  @Override
  public void saveBodies(ObservableList<BodyData> entries) throws IOException {
    traditionalLoader.saveBodies(entries);
  }

  @Override
  public void saveShields(ObservableList<ShieldData> entries) throws IOException {
    traditionalLoader.saveShields(entries);
  }

  @Override
  public void saveFXs(ObservableList<FXData> entries) throws IOException {
    traditionalLoader.saveFXs(entries);
  }

  @Override
  public void saveGrhs(ObservableList<GrhData> entries) throws IOException {
    traditionalLoader.saveGrhs(entries);
  }

  @Override
  public void saveWeapons(ObservableList<WeaponData> entries) throws IOException {
    traditionalLoader.saveWeapons(entries);
  }

  @Override
  public IndexingSystem getSystemType() {
    return IndexingSystem.MOLD;
  }

  @Override
  public Map<String, IndFileFormat> getDetectedFormats() {
    return detectedFormats;
  }

  private void recordFormat(String key, IndFileFormat format) {
    detectedFormats.put(key.toUpperCase(), format);
  }

  private IndFileFormat detectFormat(RandomAccessFile file, int... possibleSizes)
      throws IOException {
    long fileSize = file.length();
    if (fileSize < 2)
      return new IndFileFormat(0, false, false);

    // 1. Sin cabecera (Offset 0)
    file.seek(0);
    short num0 = byteMigration.bigToLittle_Short(file.readShort());
    logger.info("DIAGNOSTICO: Probando Offset 0: numRecords=" + num0 + ", fileSize=" + fileSize);
    if (num0 > 0) {
      for (int size : possibleSizes) {
        // Relaxed check: Simply require file to be large enough to hold the records.
        // The "+ 2" accounts for the 2-byte count header.
        long requiredSize = 2 + (long) num0 * size;
        if (fileSize >= requiredSize) {
          // Check if it fits *exactly* or with reasonable padding.
          // BUT for WinterAO, Heads.ind has ~300 bytes extra.
          // So we accept it if it fits, UNLESS the NEXT size up also fits?
          // Actually, simply checking if it's consistent is enough.
          // Since 8 bytes would require 6234 bytes (for 779 records),
          // and file is 5798, 8 bytes is IMPOSSIBLE.
          // So 7 bytes (requiring 5455) is the ONLY valid candidate.
          logger.info(
              "DIAGNOSTICO: Detectado formato: Offset 0, RecordSize=" + size + " (Required: " + requiredSize + ")");
          return new IndFileFormat(0, size >= 10, true, size);
        }
      }
    }

    // 2. Con cabecera (Offset 263)
    if (fileSize > 263 + 2) {
      file.seek(263);
      short num263 = byteMigration.bigToLittle_Short(file.readShort());
      logger.info("DIAGNOSTICO: Probando Offset 263: numRecords=" + num263 + ", fileSize=" + fileSize);
      if (num263 > 0) {
        for (int size : possibleSizes) {
          long requiredSize = 263 + 2 + (long) num263 * size;
          if (fileSize >= requiredSize) {
            logger.info(
                "DIAGNOSTICO: Detectado formato: Offset 263, RecordSize=" + size + " (Required: " + requiredSize + ")");
            return new IndFileFormat(263, size >= 10, true, size);
          }
        }
      }
    }

    // Fallback: Si no hay coincidencia exacta de tamaño, pero el archivo es grande,
    // asumimos que tiene cabecera y registros largos (formato más común en moldes).
    logger.warning(
        "No se pudo validar el tamaño exacto del .ind (Moldes). Usando fallback (Cabecera=263, RecordSize=10). Tamaño: "
            + fileSize);
    return new IndFileFormat(fileSize > 263 ? 263 : 0, true, true, 10);
  }

  @Override
  public ObservableList<HeadData> loadHeadsText() throws IOException {
    return traditionalLoader.loadHeadsText();
  }

  @Override
  public ObservableList<HelmetData> loadHelmetsText() throws IOException {
    return traditionalLoader.loadHelmetsText();
  }

  @Override
  public ObservableList<BodyData> loadBodiesText() throws IOException {
    return traditionalLoader.loadBodiesText();
  }

  @Override
  public ObservableList<ShieldData> loadShieldsText() throws IOException {
    return traditionalLoader.loadShieldsText();
  }

  @Override
  public ObservableList<FXData> loadFXsText() throws IOException {
    return traditionalLoader.loadFXsText();
  }

  @Override
  public ObservableList<GrhData> loadGrhsText() throws IOException {
    return traditionalLoader.loadGrhsText();
  }

  @Override
  public ObservableList<WeaponData> loadWeaponsText() throws IOException {
    return traditionalLoader.loadWeaponsText();
  }

  @Override
  public void saveHeadsText(ObservableList<HeadData> entries) throws IOException {
    traditionalLoader.saveHeadsText(entries);
  }

  @Override
  public void saveHelmetsText(ObservableList<HelmetData> entries) throws IOException {
    traditionalLoader.saveHelmetsText(entries);
  }

  @Override
  public void saveBodiesText(ObservableList<BodyData> entries) throws IOException {
    traditionalLoader.saveBodiesText(entries);
  }

  @Override
  public void saveShieldsText(ObservableList<ShieldData> entries) throws IOException {
    traditionalLoader.saveShieldsText(entries);
  }

  @Override
  public void saveFXsText(ObservableList<FXData> entries) throws IOException {
    traditionalLoader.saveFXsText(entries);
  }

  @Override
  public void saveGrhsText(ObservableList<GrhData> entries) throws IOException {
    traditionalLoader.saveGrhsText(entries);
  }

  @Override
  public void saveWeaponsText(ObservableList<WeaponData> entries) throws IOException {
    traditionalLoader.saveWeaponsText(entries);
  }
}

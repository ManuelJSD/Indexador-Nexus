package org.nexus.indexador.gamedata.loaders;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.nexus.indexador.gamedata.enums.IndexingSystem;
import org.nexus.indexador.gamedata.models.*;
import org.nexus.indexador.utils.ConfigManager;
import org.nexus.indexador.utils.Logger;
import org.nexus.indexador.utils.byteMigration;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementación del cargador para el sistema tradicional con detección dinámica y guardado fiel.
 */
public class TraditionalIndexLoader implements IndexLoader {

  private final ConfigManager configManager;
  private final byteMigration byteMigration;
  private final Logger logger;
  private final Map<String, IndFileFormat> detectedFormats = new HashMap<>();

  public TraditionalIndexLoader() throws IOException {
    this.configManager = ConfigManager.getInstance();
    this.byteMigration = org.nexus.indexador.utils.byteMigration.getInstance();
    this.logger = Logger.getInstance();
  }

  // --- Loading Methods ---

  @Override
  public ObservableList<HeadData> loadHeads() throws IOException {
    logger.info("Cargando datos de cabezas (Sistema Tradicional)...");
    ObservableList<HeadData> headList = FXCollections.observableArrayList();
    File archivo = new File(configManager.getInitDir() + "cabezas.ind");

    try (RandomAccessFile file = new RandomAccessFile(archivo, "r")) {
      IndFileFormat format = detectFormat(file, 8, 16);
      recordFormat("HEADS", format);

      if (!format.isValid()) {
        logger.error("Formato de cabezas.ind desconocido o corrupto.");
        return headList;
      }

      file.seek(format.getDataOffset());
      short numHeads = byteMigration.bigToLittle_Short(file.readShort());

      for (int i = 0; i < numHeads; i++) {
        int[] grh = new int[4];
        for (int j = 0; j < 4; j++) {
          if (format.isLong()) {
            grh[j] = byteMigration.bigToLittle_Int(file.readInt());
          } else {
            grh[j] = byteMigration.bigToLittle_Short(file.readShort());
          }
        }
        headList.add(new HeadData(grh));
      }
    } catch (FileNotFoundException e) {
      logger.error("Archivo no encontrado: " + archivo.getAbsolutePath(), e);
      throw e;
    }
    return headList;
  }

  @Override
  public ObservableList<HelmetData> loadHelmets() throws IOException {
    logger.info("Cargando datos de cascos (Sistema Tradicional)...");
    ObservableList<HelmetData> helmetList = FXCollections.observableArrayList();
    File archivo = new File(configManager.getInitDir() + "cascos.ind");

    try (RandomAccessFile file = new RandomAccessFile(archivo, "r")) {
      IndFileFormat format = detectFormat(file, 8, 16);
      recordFormat("HELMETS", format);

      if (!format.isValid()) {
        logger.error("Formato de cascos.ind desconocido o corrupto.");
        return helmetList;
      }

      file.seek(format.getDataOffset());
      short numHelmets = byteMigration.bigToLittle_Short(file.readShort());

      for (int i = 0; i < numHelmets; i++) {
        int[] grh = new int[4];
        for (int j = 0; j < 4; j++) {
          if (format.isLong()) {
            grh[j] = byteMigration.bigToLittle_Int(file.readInt());
          } else {
            grh[j] = byteMigration.bigToLittle_Short(file.readShort());
          }
        }
        helmetList.add(new HelmetData(grh));
      }
    } catch (FileNotFoundException e) {
      logger.error("Archivo no encontrado: " + archivo.getAbsolutePath(), e);
      throw e;
    }
    return helmetList;
  }

  @Override
  public ObservableList<BodyData> loadBodies() throws IOException {
    logger.info("Cargando datos de cuerpos (Sistema Tradicional)...");
    ObservableList<BodyData> bodyList = FXCollections.observableArrayList();
    File archivo = new File(configManager.getInitDir() + "personajes.ind");

    try (RandomAccessFile file = new RandomAccessFile(archivo, "r")) {
      IndFileFormat format = detectFormat(file, 12, 20); // 4 indices + 2 shorts
      recordFormat("BODIES", format);

      file.seek(format.getDataOffset());
      short numBodys = byteMigration.bigToLittle_Short(file.readShort());

      for (int i = 0; i < numBodys; i++) {
        int[] body = new int[4];
        for (int j = 0; j < 4; j++) {
          if (format.isLong()) {
            body[j] = byteMigration.bigToLittle_Int(file.readInt());
          } else {
            body[j] = byteMigration.bigToLittle_Short(file.readShort());
          }
        }
        short headOffsetX = byteMigration.bigToLittle_Short(file.readShort());
        short headOffsetY = byteMigration.bigToLittle_Short(file.readShort());
        bodyList.add(new BodyData(body, headOffsetX, headOffsetY));
      }
    } catch (FileNotFoundException e) {
      logger.error("Archivo no encontrado: " + archivo.getAbsolutePath(), e);
      throw e;
    } catch (EOFException e) {
      logger.info("Fin de fichero alcanzado en personajes.ind");
    }
    return bodyList;
  }

  @Override
  public ObservableList<ShieldData> loadShields() throws IOException {
    try {
      return loadShieldsBinary();
    } catch (FileNotFoundException | EOFException e) {
      logger.warning("escudos.ind no encontrado o incompleto. Intentando con Escudos.dat...");
      try {
        return loadShieldsText();
      } catch (Exception ex) {
        logger.error("Error al leer Escudos.dat", ex);
        return FXCollections.observableArrayList();
      }
    }
  }

  private ObservableList<ShieldData> loadShieldsBinary() throws IOException {
    logger.info("Cargando datos de escudos (Binario)...");
    ObservableList<ShieldData> shieldList = FXCollections.observableArrayList();
    File archivo = new File(configManager.getInitDir() + "escudos.ind");

    try (RandomAccessFile file = new RandomAccessFile(archivo, "r")) {
      IndFileFormat format = detectFormat(file, 8, 16);
      recordFormat("SHIELDS", format);

      file.seek(format.getDataOffset());
      short numShields = byteMigration.bigToLittle_Short(file.readShort());

      for (int i = 0; i < numShields; i++) {
        int[] shield = new int[4];
        for (int j = 0; j < 4; j++) {
          if (format.isLong()) {
            shield[j] = byteMigration.bigToLittle_Int(file.readInt());
          } else {
            shield[j] = byteMigration.bigToLittle_Short(file.readShort());
          }
        }
        shieldList.add(new ShieldData(shield));
      }
    } catch (EOFException e) {
      logger.info("Fin de fichero alcanzado en escudos.ind");
    }
    return shieldList;
  }

  @Override
  public ObservableList<ShieldData> loadShieldsText() throws IOException {
    logger.info("Cargando datos de escudos (Texto)...");
    ObservableList<ShieldData> shieldList = FXCollections.observableArrayList();

    // Intentar primero en ExportDir con .ini
    File archivo = new File(configManager.getExportDir() + "Escudos.ini");
    if (!archivo.exists()) {
      // Intentar en InitDir con .dat (legacy)
      archivo = new File(configManager.getInitDir() + "Escudos.dat");
    }

    if (!archivo.exists())
      throw new FileNotFoundException("Archivos de texto de escudos no encontrados (.ini o .dat)");

    try (BufferedReader reader = new BufferedReader(new FileReader(archivo))) {
      String line;
      int[] currentShield = new int[4];
      boolean hasData = false;

      while ((line = reader.readLine()) != null) {
        line = line.trim();
        if (line.isEmpty())
          continue;
        if (line.startsWith("[")) {
          if (hasData) {
            shieldList.add(new ShieldData(currentShield.clone()));
            for (int k = 0; k < 4; k++)
              currentShield[k] = 0;
            hasData = false;
          }
          continue;
        }
        if (line.contains("'"))
          line = line.split("'")[0].trim();
        String[] parts = line.split("=");
        if (parts.length < 2)
          continue;

        String key = parts[0].trim().toUpperCase();
        String value = parts[1].trim();
        try {
          int val = Integer.parseInt(value);
          if (key.startsWith("DIR")) {
            int dirIndex = Integer.parseInt(key.substring(3)) - 1;
            if (dirIndex >= 0 && dirIndex < 4) {
              currentShield[dirIndex] = val;
              hasData = true;
            }
          }
        } catch (NumberFormatException e) {
        }
      }
      if (hasData)
        shieldList.add(new ShieldData(currentShield.clone()));
    }
    return shieldList;
  }

  @Override
  public ObservableList<FXData> loadFXs() throws IOException {
    logger.info("Cargando datos de FXs...");
    ObservableList<FXData> fxList = FXCollections.observableArrayList();
    File archivo = new File(configManager.getInitDir() + "fxs.ind");

    try (RandomAccessFile file = new RandomAccessFile(archivo, "r")) {
      IndFileFormat format = detectFormat(file, 6, 8); // Int=2+2+2, Long=4+2+2
      recordFormat("FXS", format);

      file.seek(format.getDataOffset());
      short numFXs = byteMigration.bigToLittle_Short(file.readShort());

      for (int i = 0; i < numFXs; i++) {
        int fx;
        if (format.isLong()) {
          fx = byteMigration.bigToLittle_Int(file.readInt());
        } else {
          fx = byteMigration.bigToLittle_Short(file.readShort());
        }
        short offsetX = byteMigration.bigToLittle_Short(file.readShort());
        short offsetY = byteMigration.bigToLittle_Short(file.readShort());
        fxList.add(new FXData(fx, offsetX, offsetY));
      }
    } catch (FileNotFoundException e) {
      logger.error("Archivo no encontrado: " + archivo.getAbsolutePath(), e);
      throw e;
    } catch (EOFException e) {
      logger.info("Fin de fichero alcanzado en fxs.ind");
    }
    return fxList;
  }

  @Override
  public ObservableList<GrhData> loadGrhs() throws IOException {
    logger.info("Cargando datos de gráficos (Grh)...");
    ObservableList<GrhData> grhList = FXCollections.observableArrayList();
    File archivo = new File(configManager.getInitDir() + "graficos.ind");

    try (RandomAccessFile file = new RandomAccessFile(archivo, "r")) {
      handleGrhHeader(file);
      recordFormat("GRAFICOS", new IndFileFormat(file.getFilePointer(), true, true));

      file.readInt(); // Skip Version
      byteMigration.bigToLittle_Int(file.readInt()); // Count

      while (file.getFilePointer() < file.length()) {
        int grh = byteMigration.bigToLittle_Int(file.readInt());
        short numFrames = byteMigration.bigToLittle_Short(file.readShort());

        if (numFrames > 1) {
          int[] frames = new int[numFrames + 1];
          for (int i = 1; i <= numFrames; i++) {
            frames[i] = byteMigration.bigToLittle_Int(file.readInt());
          }
          int speed = (int) byteMigration.bigToLittle_Float(file.readFloat());
          grhList.add(new GrhData(grh, numFrames, frames, speed));
        } else {
          int fileNum = byteMigration.bigToLittle_Int(file.readInt());
          short x = byteMigration.bigToLittle_Short(file.readShort());
          short y = byteMigration.bigToLittle_Short(file.readShort());
          short width = byteMigration.bigToLittle_Short(file.readShort());
          short height = byteMigration.bigToLittle_Short(file.readShort());
          grhList.add(new GrhData(grh, numFrames, fileNum, x, y, width, height));
        }
      }
    } catch (FileNotFoundException e) {
      logger.error("Archivo no encontrado: " + archivo.getAbsolutePath(), e);
      throw e;
    } catch (EOFException e) {
      logger.info("Fin de fichero alcanzado en graficos.ind");
    }
    return grhList;
  }

  @Override
  public ObservableList<WeaponData> loadWeapons() throws IOException {
    try {
      return loadWeaponsBinary();
    } catch (FileNotFoundException | EOFException e) {
      logger.warning("armas.ind no encontrado o incompleto. Intentando con Armas.dat...");
      try {
        return loadWeaponsText();
      } catch (Exception ex) {
        logger.error("Error al leer Armas.dat", ex);
        return FXCollections.observableArrayList();
      }
    }
  }

  private ObservableList<WeaponData> loadWeaponsBinary() throws IOException {
    logger.info("Cargando datos de armas (Binario)...");
    ObservableList<WeaponData> weaponList = FXCollections.observableArrayList();
    File archivo = new File(configManager.getInitDir() + "armas.ind");

    try (RandomAccessFile file = new RandomAccessFile(archivo, "r")) {
      IndFileFormat format = detectFormat(file, 8, 16);
      recordFormat("WEAPONS", format);

      file.seek(format.getDataOffset());
      short numWeapons = byteMigration.bigToLittle_Short(file.readShort());

      for (int i = 0; i < numWeapons; i++) {
        int[] weapon = new int[4];
        for (int j = 0; j < 4; j++) {
          if (format.isLong()) {
            weapon[j] = byteMigration.bigToLittle_Int(file.readInt());
          } else {
            weapon[j] = byteMigration.bigToLittle_Short(file.readShort());
          }
        }
        weaponList.add(new WeaponData(weapon));
      }
    } catch (EOFException e) {
      logger.info("Fin de fichero alcanzado en armas.ind");
    }
    return weaponList;
  }

  @Override
  public ObservableList<WeaponData> loadWeaponsText() throws IOException {
    logger.info("Cargando datos de armas (Texto)...");
    ObservableList<WeaponData> weaponList = FXCollections.observableArrayList();

    // Intentar primero en ExportDir con .ini
    File archivo = new File(configManager.getExportDir() + "Armas.ini");
    if (!archivo.exists()) {
      // Intentar en InitDir con .dat (legacy)
      archivo = new File(configManager.getInitDir() + "Armas.dat");
    }

    if (!archivo.exists())
      throw new FileNotFoundException("Archivos de texto de armas no encontrados (.ini o .dat)");

    try (BufferedReader reader = new BufferedReader(new FileReader(archivo))) {
      String line;
      int[] currentWeapon = new int[4];
      boolean hasData = false;

      while ((line = reader.readLine()) != null) {
        line = line.trim();
        if (line.isEmpty())
          continue;
        if (line.startsWith("[")) {
          if (hasData) {
            weaponList.add(new WeaponData(currentWeapon.clone()));
            for (int k = 0; k < 4; k++)
              currentWeapon[k] = 0;
            hasData = false;
          }
          continue;
        }
        if (line.contains("'"))
          line = line.split("'")[0].trim();
        String[] parts = line.split("=");
        if (parts.length < 2)
          continue;

        String key = parts[0].trim().toUpperCase();
        String value = parts[1].trim();
        try {
          int val = Integer.parseInt(value);
          if (key.startsWith("DIR")) {
            int dirIndex = Integer.parseInt(key.substring(3)) - 1;
            if (dirIndex >= 0 && dirIndex < 4) {
              currentWeapon[dirIndex] = val;
              hasData = true;
            }
          }
        } catch (NumberFormatException e) {
        }
      }
      if (hasData)
        weaponList.add(new WeaponData(currentWeapon.clone()));
    }
    return weaponList;
  }

  @Override
  public void saveHeads(ObservableList<HeadData> entries) throws IOException {
    saveIndFile("cabezas.ind", "HEADS", file -> {
      file.writeShort(byteMigration.bigToLittle_Short((short) entries.size()));
      IndFileFormat format = detectedFormats.get("HEADS");
      boolean isLong = (format != null) ? format.isLong() : true;

      for (HeadData entry : entries) {
        for (int grh : entry.getGrhIndex()) {
          if (isLong)
            file.writeInt(byteMigration.bigToLittle_Int(grh));
          else
            file.writeShort(byteMigration.bigToLittle_Short((short) grh));
        }
      }
    });
  }

  @Override
  public void saveHelmets(ObservableList<HelmetData> entries) throws IOException {
    saveIndFile("cascos.ind", "HELMETS", file -> {
      file.writeShort(byteMigration.bigToLittle_Short((short) entries.size()));
      IndFileFormat format = detectedFormats.get("HELMETS");
      boolean isLong = (format != null) ? format.isLong() : true;

      for (HelmetData entry : entries) {
        for (int grh : entry.getGrhIndex()) {
          if (isLong)
            file.writeInt(byteMigration.bigToLittle_Int(grh));
          else
            file.writeShort(byteMigration.bigToLittle_Short((short) grh));
        }
      }
    });
  }

  @Override
  public void saveBodies(ObservableList<BodyData> entries) throws IOException {
    saveIndFile("personajes.ind", "BODIES", file -> {
      file.writeShort(byteMigration.bigToLittle_Short((short) entries.size()));
      IndFileFormat format = detectedFormats.get("BODIES");
      boolean isLong = (format != null) ? format.isLong() : true;

      for (BodyData entry : entries) {
        for (int grh : entry.getWalkG()) {
          if (isLong)
            file.writeInt(byteMigration.bigToLittle_Int(grh));
          else
            file.writeShort(byteMigration.bigToLittle_Short((short) grh));
        }
        file.writeShort(byteMigration.bigToLittle_Short(entry.getHeadOffsetX()));
        file.writeShort(byteMigration.bigToLittle_Short(entry.getHeadOffsetY()));
      }
    });
  }

  @Override
  public void saveShields(ObservableList<ShieldData> entries) throws IOException {
    saveIndFile("escudos.ind", "SHIELDS", file -> {
      file.writeShort(byteMigration.bigToLittle_Short((short) entries.size()));
      IndFileFormat format = detectedFormats.get("SHIELDS");
      boolean isLong = (format != null) ? format.isLong() : true;

      for (ShieldData entry : entries) {
        for (int grh : entry.getGrhIndex()) {
          if (isLong)
            file.writeInt(byteMigration.bigToLittle_Int(grh));
          else
            file.writeShort(byteMigration.bigToLittle_Short((short) grh));
        }
      }
    });
  }

  @Override
  public void saveFXs(ObservableList<FXData> entries) throws IOException {
    saveIndFile("fxs.ind", "FXS", file -> {
      file.writeShort(byteMigration.bigToLittle_Short((short) entries.size()));
      IndFileFormat format = detectedFormats.get("FXS");
      boolean isLong = (format != null) ? format.isLong() : true;

      for (FXData entry : entries) {
        if (isLong)
          file.writeInt(byteMigration.bigToLittle_Int(entry.getAnimInstance()));
        else
          file.writeShort(byteMigration.bigToLittle_Short((short) entry.getAnimInstance()));
        file.writeShort(byteMigration.bigToLittle_Short(entry.getOffsetX()));
        file.writeShort(byteMigration.bigToLittle_Short(entry.getOffsetY()));
      }
    });
  }

  @Override
  public void saveGrhs(ObservableList<GrhData> entries) throws IOException {
    saveIndFile("graficos.ind", "GRAFICOS", file -> {
      file.writeInt(byteMigration.bigToLittle_Int(1)); // Version placeholder
      file.writeInt(byteMigration.bigToLittle_Int(entries.size()));

      for (GrhData entry : entries) {
        file.writeInt(byteMigration.bigToLittle_Int(entry.getGrh()));
        file.writeShort(byteMigration.bigToLittle_Short(entry.getNumFrames()));

        if (entry.getNumFrames() > 1) {
          for (int i = 1; i <= entry.getNumFrames(); i++) {
            file.writeInt(byteMigration.bigToLittle_Int(entry.getFrame(i)));
          }
          file.writeFloat(byteMigration.bigToLittle_Float(entry.getSpeed()));
        } else {
          file.writeInt(byteMigration.bigToLittle_Int(entry.getFileNum()));
          file.writeShort(byteMigration.bigToLittle_Short(entry.getsX()));
          file.writeShort(byteMigration.bigToLittle_Short(entry.getsY()));
          file.writeShort(byteMigration.bigToLittle_Short(entry.getTileWidth()));
          file.writeShort(byteMigration.bigToLittle_Short(entry.getTileHeight()));
        }
      }
    });
  }

  @Override
  public void saveWeapons(ObservableList<WeaponData> entries) throws IOException {
    saveIndFile("armas.ind", "WEAPONS", file -> {
      file.writeShort(byteMigration.bigToLittle_Short((short) entries.size()));
      IndFileFormat format = detectedFormats.get("WEAPONS");
      boolean isLong = (format != null) ? format.isLong() : true;

      for (WeaponData entry : entries) {
        for (int grh : entry.getGrhIndex()) {
          if (isLong)
            file.writeInt(byteMigration.bigToLittle_Int(grh));
          else
            file.writeShort(byteMigration.bigToLittle_Short((short) grh));
        }
      }
    });
  }

  // --- Helper Methods ---

  private interface SaveAction {
    void accept(RandomAccessFile file) throws IOException;
  }

  private void saveIndFile(String filename, String formatKey, SaveAction action)
      throws IOException {
    File archive = new File(configManager.getInitDir() + filename);
    IndFileFormat format = detectedFormats.get(formatKey);
    long offset =
        (format != null) ? format.getDataOffset() : (filename.equals("graficos.ind") ? 263 : 0);

    try (RandomAccessFile file = new RandomAccessFile(archive, "rw")) {
      // Si hay cabecera (offset > 0), saltar o escribir placeholder
      if (offset > 0) {
        if (file.length() < offset) {
          file.write(new byte[(int) offset]);
        }
        file.seek(offset);
      } else {
        file.seek(0);
      }
      action.accept(file);
    }
  }

  private void handleGrhHeader(RandomAccessFile file) throws IOException {
    if (file.length() < 263 + 8) {
      file.seek(0);
      return;
    }
    file.seek(0);
    int vNoHeader = byteMigration.bigToLittle_Int(file.readInt());
    file.seek(263);
    int vHeader = byteMigration.bigToLittle_Int(file.readInt());

    if (vNoHeader < 0 || vNoHeader > 500000) {
      if (vHeader >= 0 && vHeader < 500000) {
        file.seek(263);
        logger.info("Cabecera detectada en Graficos.ind");
        return;
      }
    }
    file.seek(0);
  }

  @Override
  public IndexingSystem getSystemType() {
    return IndexingSystem.TRADITIONAL;
  }

  @Override
  public Map<String, IndFileFormat> getDetectedFormats() {
    return detectedFormats;
  }

  private void recordFormat(String key, IndFileFormat format) {
    detectedFormats.put(key.toUpperCase(), format);
  }

  private IndFileFormat detectFormat(RandomAccessFile file, int recordSizeInt, int recordSizeLong)
      throws IOException {
    long fileSize = file.length();

    // 1. Chequear SIN cabecera (Offset 0)
    file.seek(0);
    short numNoHeader = byteMigration.bigToLittle_Short(file.readShort());
    if (numNoHeader > 0) {
      if (fileSize == 2 + (long) numNoHeader * recordSizeLong)
        return new IndFileFormat(0, true, true);
      if (fileSize == 2 + (long) numNoHeader * recordSizeInt)
        return new IndFileFormat(0, false, true);
    }

    // 2. Chequear CON cabecera (Offset 263)
    if (fileSize > 263) {
      file.seek(263);
      short numHeader = byteMigration.bigToLittle_Short(file.readShort());
      if (numHeader > 0) {
        if (fileSize == 263 + 2 + (long) numHeader * recordSizeLong)
          return new IndFileFormat(263, true, true);
        if (fileSize == 263 + 2 + (long) numHeader * recordSizeInt)
          return new IndFileFormat(263, false, true);
      }
    }

    // Fallback
    return new IndFileFormat(fileSize > 263 ? 263 : 0, true, true);
  }

  @Override
  public ObservableList<HeadData> loadHeadsText() throws IOException {
    logger.info("Cargando cabezas desde texto...");
    ObservableList<HeadData> headList = FXCollections.observableArrayList();
    File archivo = new File(configManager.getExportDir() + "Cabezas.ini");
    if (!archivo.exists()) {
      archivo = new File(configManager.getInitDir() + "Cabezas.dat");
    }

    if (!archivo.exists())
      return headList;

    try (BufferedReader reader = new BufferedReader(new FileReader(archivo))) {
      String line;
      int[] currentGrh = new int[4];
      boolean hasData = false;

      while ((line = reader.readLine()) != null) {
        line = line.trim();
        if (line.isEmpty() || line.startsWith("[")) {
          if (hasData) {
            headList.add(new HeadData(currentGrh.clone()));
            for (int k = 0; k < 4; k++)
              currentGrh[k] = 0;
            hasData = false;
          }
          continue;
        }
        String[] parts = line.split("=");
        if (parts.length < 2)
          continue;
        String key = parts[0].trim().toUpperCase();
        try {
          int val = Integer.parseInt(parts[1].trim());
          if (key.startsWith("HEAD")) {
            int index = Integer.parseInt(key.substring(4)) - 1;
            if (index >= 0 && index < 4) {
              currentGrh[index] = val;
              hasData = true;
            }
          }
        } catch (NumberFormatException e) {
        }
      }
      if (hasData)
        headList.add(new HeadData(currentGrh.clone()));
    }
    return headList;
  }

  @Override
  public ObservableList<HelmetData> loadHelmetsText() throws IOException {
    logger.info("Cargando cascos desde texto...");
    ObservableList<HelmetData> helmetList = FXCollections.observableArrayList();
    File archivo = new File(configManager.getExportDir() + "Cascos.ini");
    if (!archivo.exists()) {
      archivo = new File(configManager.getInitDir() + "Cascos.dat");
    }

    if (!archivo.exists())
      return helmetList;

    try (BufferedReader reader = new BufferedReader(new FileReader(archivo))) {
      String line;
      int[] currentGrh = new int[4];
      boolean hasData = false;

      while ((line = reader.readLine()) != null) {
        line = line.trim();
        if (line.isEmpty() || line.startsWith("[")) {
          if (hasData) {
            helmetList.add(new HelmetData(currentGrh.clone()));
            for (int k = 0; k < 4; k++)
              currentGrh[k] = 0;
            hasData = false;
          }
          continue;
        }
        String[] parts = line.split("=");
        if (parts.length < 2)
          continue;
        String key = parts[0].trim().toUpperCase();
        try {
          int val = Integer.parseInt(parts[1].trim());
          if (key.startsWith("HELMET")) {
            int index = Integer.parseInt(key.substring(6)) - 1;
            if (index >= 0 && index < 4) {
              currentGrh[index] = val;
              hasData = true;
            }
          }
        } catch (NumberFormatException e) {
        }
      }
      if (hasData)
        helmetList.add(new HelmetData(currentGrh.clone()));
    }
    return helmetList;
  }

  @Override
  public ObservableList<BodyData> loadBodiesText() throws IOException {
    logger.info("Cargando cuerpos desde texto...");
    ObservableList<BodyData> bodyList = FXCollections.observableArrayList();
    File archivo = new File(configManager.getExportDir() + "Cuerpos.ini");
    if (!archivo.exists()) {
      archivo = new File(configManager.getInitDir() + "Cuerpos.dat");
    }

    if (!archivo.exists())
      return bodyList;

    try (BufferedReader reader = new BufferedReader(new FileReader(archivo))) {
      String line;
      int[] currentGrh = new int[4];
      short offsetX = 0, offsetY = 0;
      boolean hasData = false;

      while ((line = reader.readLine()) != null) {
        line = line.trim();
        if (line.isEmpty() || line.startsWith("[")) {
          if (hasData) {
            bodyList.add(new BodyData(currentGrh.clone(), offsetX, offsetY));
            for (int k = 0; k < 4; k++)
              currentGrh[k] = 0;
            offsetX = 0;
            offsetY = 0;
            hasData = false;
          }
          continue;
        }
        String[] parts = line.split("=");
        if (parts.length < 2)
          continue;
        String key = parts[0].trim().toUpperCase();
        try {
          int val = Integer.parseInt(parts[1].trim());
          if (key.startsWith("WALK")) {
            int index = Integer.parseInt(key.substring(4)) - 1;
            if (index >= 0 && index < 4) {
              currentGrh[index] = val;
              hasData = true;
            }
          } else if (key.equals("HEADOFFSETX")) {
            offsetX = (short) val;
            hasData = true;
          } else if (key.equals("HEADOFFSETY")) {
            offsetY = (short) val;
            hasData = true;
          }
        } catch (NumberFormatException e) {
        }
      }
      if (hasData)
        bodyList.add(new BodyData(currentGrh.clone(), offsetX, offsetY));
    }
    return bodyList;
  }

  @Override
  public ObservableList<FXData> loadFXsText() throws IOException {
    logger.info("Cargando FXs desde texto...");
    ObservableList<FXData> fxList = FXCollections.observableArrayList();
    File archivo = new File(configManager.getExportDir() + "FXs.ini");
    if (!archivo.exists()) {
      archivo = new File(configManager.getInitDir() + "FXs.dat");
    }

    if (!archivo.exists())
      return fxList;

    try (BufferedReader reader = new BufferedReader(new FileReader(archivo))) {
      String line;
      int anim = 0;
      short offsetX = 0, offsetY = 0;
      boolean hasData = false;

      while ((line = reader.readLine()) != null) {
        line = line.trim();
        if (line.isEmpty() || line.startsWith("[")) {
          if (hasData) {
            fxList.add(new FXData(anim, offsetX, offsetY));
            anim = 0;
            offsetX = 0;
            offsetY = 0;
            hasData = false;
          }
          continue;
        }
        String[] parts = line.split("=");
        if (parts.length < 2)
          continue;
        String key = parts[0].trim().toUpperCase();
        try {
          int val = Integer.parseInt(parts[1].trim());
          if (key.equals("ANIMACION")) {
            anim = val;
            hasData = true;
          } else if (key.equals("OFFSETX")) {
            offsetX = (short) val;
            hasData = true;
          } else if (key.equals("OFFSETY")) {
            offsetY = (short) val;
            hasData = true;
          }
        } catch (NumberFormatException e) {
        }
      }
      if (hasData)
        fxList.add(new FXData(anim, offsetX, offsetY));
    }
    return fxList;
  }

  @Override
  public ObservableList<GrhData> loadGrhsText() throws IOException {
    logger.info("Cargando GRHs desde texto...");
    ObservableList<GrhData> grhList = FXCollections.observableArrayList();
    File archivo = new File(configManager.getExportDir() + "Graficos.ini");
    if (!archivo.exists()) {
      archivo = new File(configManager.getInitDir() + "Graficos.ini");
    }
    if (!archivo.exists()) {
      archivo = new File(configManager.getInitDir() + "Graficos.dat");
    }

    if (!archivo.exists())
      return grhList;

    try (BufferedReader reader = new BufferedReader(new FileReader(archivo))) {
      String line;
      while ((line = reader.readLine()) != null) {
        line = line.trim();
        if (line.isEmpty() || line.startsWith("["))
          continue;

        String[] parts = line.split("=");
        if (parts.length < 2)
          continue;

        String key = parts[0].trim().toUpperCase();
        if (!key.startsWith("GRH"))
          continue;

        try {
          int grhNum = Integer.parseInt(key.substring(3));
          String[] values = parts[1].trim().split("-");
          if (values.length < 2)
            continue;

          short numFrames = Short.parseShort(values[0]);
          if (numFrames > 1) {
            int[] frames = new int[numFrames + 1];
            for (int i = 1; i <= numFrames; i++) {
              frames[i] = Integer.parseInt(values[i]);
            }
            float speed = Float.parseFloat(values[values.length - 1]);
            grhList.add(new GrhData(grhNum, numFrames, frames, speed));
          } else {
            int fileNum = Integer.parseInt(values[1]);
            short sx = Short.parseShort(values[2]);
            short sy = Short.parseShort(values[3]);
            short w = Short.parseShort(values[4]);
            short h = Short.parseShort(values[5]);
            grhList.add(new GrhData(grhNum, numFrames, fileNum, sx, sy, w, h));
          }
        } catch (Exception e) {
          // Ignorar lineas mal formadas
        }
      }
    }
    return grhList;
  }

  @Override
  public void saveHeadsText(ObservableList<HeadData> entries) throws IOException {
    ensureExportDirExists();
    File archive = new File(configManager.getExportDir() + "Cabezas.ini");
    try (PrintWriter writer = new PrintWriter(new FileWriter(archive))) {
      writer.println("[INIT]");
      writer.println("NumHeads=" + entries.size());
      for (int i = 0; i < entries.size(); i++) {
        writer.println();
        HeadData head = entries.get(i);
        writer.println("[HEAD" + (i + 1) + "]");
        for (int j = 0; j < 4; j++) {
          writer.println("Head" + (j + 1) + "=" + head.getGrhIndex()[j]);
        }
      }
    }
  }

  @Override
  public void saveHelmetsText(ObservableList<HelmetData> entries) throws IOException {
    ensureExportDirExists();
    File archive = new File(configManager.getExportDir() + "Cascos.ini");
    try (PrintWriter writer = new PrintWriter(new FileWriter(archive))) {
      writer.println("[INIT]");
      writer.println("NumHelmets=" + entries.size());
      for (int i = 0; i < entries.size(); i++) {
        writer.println();
        HelmetData helmet = entries.get(i);
        writer.println("[HELMET" + (i + 1) + "]");
        for (int j = 0; j < 4; j++) {
          writer.println("Helmet" + (j + 1) + "=" + helmet.getGrhIndex()[j]);
        }
      }
    }
  }

  @Override
  public void saveBodiesText(ObservableList<BodyData> entries) throws IOException {
    ensureExportDirExists();
    File archive = new File(configManager.getExportDir() + "Cuerpos.ini");
    try (PrintWriter writer = new PrintWriter(new FileWriter(archive))) {
      writer.println("[INIT]");
      writer.println("NumBodies=" + entries.size());
      for (int i = 0; i < entries.size(); i++) {
        writer.println();
        BodyData body = entries.get(i);
        writer.println("[BODY" + (i + 1) + "]");
        for (int j = 0; j < 4; j++) {
          writer.println("Walk" + (j + 1) + "=" + body.getBody()[j]);
        }
        writer.println("HeadOffsetX=" + body.getHeadOffsetX());
        writer.println("HeadOffsetY=" + body.getHeadOffsetY());
      }
    }
  }

  @Override
  public void saveShieldsText(ObservableList<ShieldData> entries) throws IOException {
    ensureExportDirExists();
    File archive = new File(configManager.getExportDir() + "Escudos.ini");
    try (PrintWriter writer = new PrintWriter(new FileWriter(archive))) {
      writer.println("[INIT]");
      writer.println("NumEscudos=" + entries.size());
      for (int i = 0; i < entries.size(); i++) {
        writer.println();
        ShieldData shield = entries.get(i);
        writer.println("[ESC" + (i + 1) + "]");
        for (int j = 0; j < 4; j++) {
          writer.println("Dir" + (j + 1) + "=" + shield.getGrhIndex()[j]);
        }
      }
    }
  }

  @Override
  public void saveFXsText(ObservableList<FXData> entries) throws IOException {
    ensureExportDirExists();
    File archive = new File(configManager.getExportDir() + "FXs.ini");
    try (PrintWriter writer = new PrintWriter(new FileWriter(archive))) {
      writer.println("[INIT]");
      writer.println("NumFXs=" + entries.size());
      for (int i = 0; i < entries.size(); i++) {
        writer.println();
        FXData fx = entries.get(i);
        writer.println("[FX" + (i + 1) + "]");
        writer.println("Animacion=" + fx.getAnimInstance());
        writer.println("OffsetX=" + fx.getOffsetX());
        writer.println("OffsetY=" + fx.getOffsetY());
      }
    }
  }

  @Override
  public void saveGrhsText(ObservableList<GrhData> entries) throws IOException {
    ensureExportDirExists();
    File archive = new File(configManager.getExportDir() + "Graficos.ini");
    try (PrintWriter writer = new PrintWriter(new FileWriter(archive))) {
      writer.println("[INIT]");
      writer.println("NumGrh=" + entries.size());
      writer.println();
      writer.println("[Graphics]");
      for (GrhData grh : entries) {
        StringBuilder sb = new StringBuilder();
        sb.append("Grh").append(grh.getGrh()).append("=");
        sb.append(grh.getNumFrames());

        if (grh.getNumFrames() > 1) {
          for (int i = 1; i <= grh.getNumFrames(); i++) {
            sb.append("-").append(grh.getFrame(i));
          }
          sb.append("-").append(grh.getSpeed());
        } else {
          sb.append("-").append(grh.getFileNum()).append("-").append(grh.getsX()).append("-")
              .append(grh.getsY()).append("-").append(grh.getTileWidth()).append("-")
              .append(grh.getTileHeight());
        }
        writer.println(sb.toString());
      }
    }
  }

  @Override
  public void saveWeaponsText(ObservableList<WeaponData> entries) throws IOException {
    ensureExportDirExists();
    File archive = new File(configManager.getExportDir() + "Armas.ini");
    try (PrintWriter writer = new PrintWriter(new FileWriter(archive))) {
      writer.println("[INIT]");
      writer.println("NumArmas=" + entries.size());
      for (int i = 0; i < entries.size(); i++) {
        writer.println();
        WeaponData weapon = entries.get(i);
        writer.println("[ARMA" + (i + 1) + "]");
        for (int j = 0; j < 4; j++) {
          writer.println("Dir" + (j + 1) + "=" + weapon.getGrhIndex()[j]);
        }
      }
    }
  }

  private void ensureExportDirExists() {
    File dir = new File(configManager.getExportDir());
    if (!dir.exists()) {
      dir.mkdirs();
    }
  }
}

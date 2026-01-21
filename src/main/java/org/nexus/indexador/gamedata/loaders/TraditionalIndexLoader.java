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
 * Implementación del cargador para el sistema tradicional con detección
 * dinámica de formatos.
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

    private ObservableList<ShieldData> loadShieldsText() throws IOException {
        logger.info("Cargando datos de escudos (Texto)...");
        ObservableList<ShieldData> shieldList = FXCollections.observableArrayList();
        File archivo = new File(configManager.getInitDir() + "Escudos.dat");

        if (!archivo.exists())
            throw new FileNotFoundException("Escudos.dat no encontrado");

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
            // Record format for Grhs too
            recordFormat("GRAFICOS", new IndFileFormat(file.getFilePointer(), true, true));

            byteMigration.bigToLittle_Int(file.readInt()); // Version
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
}

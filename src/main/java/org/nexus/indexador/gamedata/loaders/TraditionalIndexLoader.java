package org.nexus.indexador.gamedata.loaders;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.nexus.indexador.gamedata.enums.IndexingSystem;
import org.nexus.indexador.gamedata.models.*;
import org.nexus.indexador.utils.ConfigManager;
import org.nexus.indexador.utils.Logger;
import org.nexus.indexador.utils.byteMigration;

import java.io.*;

/**
 * Implementación placeholder del cargador para el sistema tradicional.
 * 
 * Este sistema utiliza índices directos a gráficos individuales para
 * representar cabezas y cascos.
 * 
 * NOTA: Esta implementación está pendiente de desarrollo por el usuario.
 */
public class TraditionalIndexLoader implements IndexLoader {

    private final ConfigManager configManager;
    private final byteMigration byteMigration;
    private final Logger logger;

    public TraditionalIndexLoader() throws IOException {
        this.configManager = ConfigManager.getInstance();
        this.byteMigration = org.nexus.indexador.utils.byteMigration.getInstance();
        this.logger = Logger.getInstance();
    }

    @Override
    public ObservableList<HeadData> loadHeads() throws IOException {
        logger.info("Cargando datos de cabezas (Sistema Tradicional)..." + configManager.getInitDir());

        ObservableList<HeadData> headList = FXCollections.observableArrayList();
        File archivo = new File(configManager.getInitDir() + "cabezas.ind");

        try (RandomAccessFile file = new RandomAccessFile(archivo, "r")) {
            logger.info("Comenzando a leer desde " + archivo.getAbsolutePath());

            IndFormat format = detectFormat(file);
            if (!format.valid) {
                logger.error("Formato de cabezas.ind desconocido o corrupto.");
                return headList;
            }

            file.seek(format.dataOffset);
            short numHeads = byteMigration.bigToLittle_Short(file.readShort());

            for (int i = 0; i < numHeads; i++) {
                int[] grh = new int[4]; // [Norte, Sur, Este, Oeste]

                for (int j = 0; j < 4; j++) {
                    if (format.isLong) {
                        grh[j] = byteMigration.bigToLittle_Int(file.readInt());
                    } else {
                        grh[j] = byteMigration.bigToLittle_Short(file.readShort());
                    }
                }

                HeadData headData = new HeadData(grh);
                headList.add(headData);

            }

        } catch (FileNotFoundException e) {
            logger.error("Archivo no encontrado: " + archivo.getAbsolutePath(), e);
            throw e;
        } catch (EOFException e) {
            logger.info("Fin de fichero alcanzado");
        } catch (IOException e) {
            logger.error("Error de E/S al leer el archivo: " + archivo.getAbsolutePath(), e);
            throw e;
        }

        logger.info("Cargadas " + headList.size() + " cabezas exitosamente (Sistema Tradicional)");
        return headList;

    }

    @Override
    public ObservableList<HelmetData> loadHelmets() throws IOException {
        logger.info("Cargando datos de cascos (Sistema Tradicional)...");

        ObservableList<HelmetData> helmetList = FXCollections.observableArrayList();
        File archivo = new File(configManager.getInitDir() + "cascos.ind");

        try (RandomAccessFile file = new RandomAccessFile(archivo, "r")) {
            logger.info("Comenzando a leer desde " + archivo.getAbsolutePath());

            IndFormat format = detectFormat(file);
            if (!format.valid) {
                logger.error("Formato de cascos.ind desconocido o corrupto.");
                return helmetList;
            }

            file.seek(format.dataOffset);
            short numHelmets = byteMigration.bigToLittle_Short(file.readShort());

            for (int i = 0; i < numHelmets; i++) {
                int[] grh = new int[4]; // [Norte, Sur, Este, Oeste]

                for (int j = 0; j < 4; j++) {
                    if (format.isLong) {
                        grh[j] = byteMigration.bigToLittle_Int(file.readInt());
                    } else {
                        grh[j] = byteMigration.bigToLittle_Short(file.readShort());
                    }
                }

                HelmetData helmetData = new HelmetData(grh);
                helmetList.add(helmetData);

            }

        } catch (FileNotFoundException e) {
            logger.error("Archivo no encontrado: " + archivo.getAbsolutePath(), e);
            throw e;
        } catch (EOFException e) {
            logger.info("Fin de fichero alcanzado");
        } catch (IOException e) {
            logger.error("Error de E/S al leer el archivo: " + archivo.getAbsolutePath(), e);
            throw e;
        }

        logger.info("Cargados " + helmetList.size() + " cascos exitosamente (Sistema Tradicional)");
        return helmetList;
    }

    @Override
    public ObservableList<BodyData> loadBodies() throws IOException {
        logger.info("Cargando datos de cuerpos (Sistema Tradicional)...");
        ObservableList<BodyData> bodyList = FXCollections.observableArrayList();
        File archivo = new File(configManager.getInitDir() + "personajes.ind");

        try (RandomAccessFile file = new RandomAccessFile(archivo, "r")) {
            logger.info("Comenzando a leer desde " + archivo.getAbsolutePath());
            handleFixedHeader(file, 20);
            short numBodys = byteMigration.bigToLittle_Short(file.readShort());

            for (int i = 0; i < numBodys; i++) {
                int[] body = new int[4];
                for (int j = 0; j < 4; j++) {
                    body[j] = byteMigration.bigToLittle_Int(file.readInt());
                }
                short headOffsetX = byteMigration.bigToLittle_Short(file.readShort());
                short headOffsetY = byteMigration.bigToLittle_Short(file.readShort());
                BodyData data = new BodyData(body, headOffsetX, headOffsetY);
                bodyList.add(data);
            }
        } catch (FileNotFoundException e) {
            logger.error("Archivo no encontrado: " + archivo.getAbsolutePath(), e);
            throw e;
        } catch (EOFException e) {
            logger.info("Fin de fichero alcanzado");
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
            } catch (FileNotFoundException ex) {
                logger.error("No se encontró ni escudos.ind ni Escudos.dat");
                return FXCollections.observableArrayList();
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
            handleFixedHeader(file, 16);
            short numShields = byteMigration.bigToLittle_Short(file.readShort());

            for (int i = 0; i < numShields; i++) {
                int[] shield = new int[4];
                for (int j = 0; j < 4; j++) {
                    shield[j] = byteMigration.bigToLittle_Int(file.readInt());
                }
                shieldList.add(new ShieldData(shield));
            }
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
            handleFixedHeader(file, 8);
            short numFXs = byteMigration.bigToLittle_Short(file.readShort());

            for (int i = 0; i < numFXs; i++) {
                int fx = byteMigration.bigToLittle_Int(file.readInt());
                short offsetX = byteMigration.bigToLittle_Short(file.readShort());
                short offsetY = byteMigration.bigToLittle_Short(file.readShort());
                fxList.add(new FXData(fx, offsetX, offsetY));
            }
        } catch (FileNotFoundException e) {
            logger.error("Archivo no encontrado: " + archivo.getAbsolutePath(), e);
            throw e;
        } catch (EOFException e) {
            logger.info("Fin de fichero alcanzado");
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
            byteMigration.bigToLittle_Int(file.readInt()); // Version (discarded here)
            byteMigration.bigToLittle_Int(file.readInt()); // Count (discarded here)

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
            logger.info("Fin de fichero alcanzado");
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

        if (vNoHeader < 0 || vNoHeader > 1000) {
            if (vHeader >= 0 && vHeader < 1000) {
                file.seek(263);
                logger.info("Cabecera detectada en Graficos.ind");
                return;
            }
        }
        file.seek(0);
    }

    private void handleFixedHeader(RandomAccessFile file, int recordSize) throws IOException {
        long fileSize = file.length();
        file.seek(0);
        short numNoHeader = byteMigration.bigToLittle_Short(file.readShort());
        if (fileSize == 2 + (long) numNoHeader * recordSize) {
            file.seek(0);
            return;
        }
        if (fileSize >= 263 + 2) {
            file.seek(263);
            short numHeader = byteMigration.bigToLittle_Short(file.readShort());
            if (fileSize == 263 + 2 + (long) numHeader * recordSize) {
                file.seek(263);
                logger.info("Cabecera detectada en archivo de registros fijos.");
                return;
            }
        }
        file.seek(0);
    }

    @Override
    public IndexingSystem getSystemType() {
        return IndexingSystem.TRADITIONAL;
    }

    private static class IndFormat {
        long dataOffset;
        boolean isLong; // true=16bytes/reg, false=8bytes/reg
        boolean valid;

        IndFormat(long offset, boolean isLong, boolean valid) {
            this.dataOffset = offset;
            this.isLong = isLong;
            this.valid = valid;
        }
    }

    /**
     * Detecta automáticamente el formato del archivo:
     * - Si tiene cabecera (263 bytes) o no.
     * - Si usa Integers (2 bytes) o Longs (4 bytes) para los índices.
     */
    private IndFormat detectFormat(RandomAccessFile file) throws IOException {
        long fileSize = file.length();

        // 1. Chequear SIN cabecera (Offset 0)
        file.seek(0);
        short numNoHeader = byteMigration.bigToLittle_Short(file.readShort());
        if (numNoHeader > 0) {
            long sizeLong = 2 + (long) numNoHeader * 16;
            long sizeInt = 2 + (long) numNoHeader * 8;

            if (fileSize == sizeLong)
                return new IndFormat(0, true, true);
            if (fileSize == sizeInt)
                return new IndFormat(0, false, true);
        }

        // 2. Chequear CON cabecera (Offset 263)
        if (fileSize > 263) {
            file.seek(263);
            short numHeader = byteMigration.bigToLittle_Short(file.readShort());
            if (numHeader > 0) {
                long sizeLong = 263 + 2 + (long) numHeader * 16;
                long sizeInt = 263 + 2 + (long) numHeader * 8;

                if (fileSize == sizeLong) {
                    logger.info("Detectado formato: Con Cabecera + Longs (Moderno)");
                    return new IndFormat(263, true, true);
                }
                if (fileSize == sizeInt) {
                    logger.info("Detectado formato: Con Cabecera + Integers (Antiguo)");
                    return new IndFormat(263, false, true);
                }
            }
        }

        // Fallback: Si no cuadra nada, asumimos estándar moderno con cabecera (si size
        // > 263) o sin cabecera
        logger.warning("No se pudo autodetectar formato exacto. Probando default (Con Cabecera + Longs).");
        return new IndFormat(263, true, true);
    }
}

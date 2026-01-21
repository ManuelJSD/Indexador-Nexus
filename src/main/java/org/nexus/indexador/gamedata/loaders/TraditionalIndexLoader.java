package org.nexus.indexador.gamedata.loaders;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.nexus.indexador.gamedata.enums.IndexingSystem;
import org.nexus.indexador.gamedata.models.HeadData;
import org.nexus.indexador.gamedata.models.HelmetData;
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

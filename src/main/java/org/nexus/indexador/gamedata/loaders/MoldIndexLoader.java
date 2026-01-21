package org.nexus.indexador.gamedata.loaders;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.nexus.indexador.gamedata.enums.IndexingSystem;
import org.nexus.indexador.gamedata.models.*;
import org.nexus.indexador.utils.ConfigManager;
import org.nexus.indexador.utils.Logger;
import org.nexus.indexador.utils.byteMigration;

import java.io.*;
import java.util.Collections;
import java.util.Map;

/**
 * Implementación del cargador para el sistema de moldes.
 * 
 * Este sistema utiliza un identificador de molde (Std) y coordenadas
 * en una textura compartida para representar cabezas y cascos.
 */
public class MoldIndexLoader implements IndexLoader {

    private final ConfigManager configManager;
    private final byteMigration byteMigration;
    private final Logger logger;

    public MoldIndexLoader() throws IOException {
        this.configManager = ConfigManager.getInstance();
        this.byteMigration = org.nexus.indexador.utils.byteMigration.getInstance();
        this.logger = Logger.getInstance();
    }

    @Override
    public ObservableList<HeadData> loadHeads() throws IOException {
        logger.info("Cargando datos de cabezas (Sistema de Moldes)...");

        ObservableList<HeadData> headList = FXCollections.observableArrayList();
        File archivo = new File(configManager.getInitDir() + "cabezas.ind");

        try (RandomAccessFile file = new RandomAccessFile(archivo, "r")) {
            logger.info("Comenzando a leer desde " + archivo.getAbsolutePath());

            file.seek(0);
            short numHeads = byteMigration.bigToLittle_Short(file.readShort());

            for (int i = 0; i < numHeads; i++) {
                int std = byteMigration.bigToLittle_Byte(file.readByte());
                short texture = byteMigration.bigToLittle_Short(file.readShort());
                short startx = byteMigration.bigToLittle_Short(file.readShort());
                short starty = byteMigration.bigToLittle_Short(file.readShort());

                HeadData headData = new HeadData(std, texture, startx, starty);
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

        logger.info("Cargadas " + headList.size() + " cabezas exitosamente (Sistema de Moldes)");
        return headList;
    }

    @Override
    public ObservableList<HelmetData> loadHelmets() throws IOException {
        logger.info("Cargando datos de cascos (Sistema de Moldes)...");

        ObservableList<HelmetData> helmetList = FXCollections.observableArrayList();
        File archivo = new File(configManager.getInitDir() + "cascos.ind");

        try (RandomAccessFile file = new RandomAccessFile(archivo, "r")) {
            logger.info("Comenzando a leer desde " + archivo.getAbsolutePath());

            file.seek(0);
            short numHelmets = byteMigration.bigToLittle_Short(file.readShort());

            for (int i = 0; i < numHelmets; i++) {
                int std = byteMigration.bigToLittle_Byte(file.readByte());
                short texture = byteMigration.bigToLittle_Short(file.readShort());
                short startx = byteMigration.bigToLittle_Short(file.readShort());
                short starty = byteMigration.bigToLittle_Short(file.readShort());

                HelmetData helmetData = new HelmetData(std, texture, startx, starty);
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

        logger.info("Cargados " + helmetList.size() + " cascos exitosamente (Sistema de Moldes)");
        return helmetList;
    }

    @Override
    public ObservableList<BodyData> loadBodies() throws IOException {
        logger.info("Cargando datos de cuerpos (Moldes - Placeholder)...");
        return FXCollections.observableArrayList();
    }

    @Override
    public ObservableList<ShieldData> loadShields() throws IOException {
        logger.info("Cargando datos de escudos (Moldes - Placeholder)...");
        return FXCollections.observableArrayList();
    }

    @Override
    public ObservableList<FXData> loadFXs() throws IOException {
        logger.info("Cargando datos de FXs (Moldes - Placeholder)...");
        return FXCollections.observableArrayList();
    }

    @Override
    public ObservableList<GrhData> loadGrhs() throws IOException {
        logger.info("Cargando datos de gráficos (Moldes - Placeholder)...");
        return FXCollections.observableArrayList();
    }

    @Override
    public IndexingSystem getSystemType() {
        return IndexingSystem.MOLD;
    }

    @Override
    public Map<String, IndFileFormat> getDetectedFormats() {
        return Collections.emptyMap();
    }
}

package org.nexus.indexador.gamedata;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.nexus.indexador.gamedata.loaders.IndexLoader;
import org.nexus.indexador.gamedata.models.*;
import org.nexus.indexador.utils.DatEditor;
import org.nexus.indexador.utils.byteMigration;
import org.nexus.indexador.utils.ConfigManager;
import org.nexus.indexador.utils.Logger;

import java.io.*;

public class DataManager {

    private GrhData grhData;

    private ObservableList<GrhData> grhList = FXCollections.observableArrayList();
    private ObservableList<HeadData> headList = FXCollections.observableArrayList();
    private ObservableList<HelmetData> helmetList = FXCollections.observableArrayList();
    private ObservableList<BodyData> bodyList = FXCollections.observableArrayList();
    private ObservableList<ShieldData> shieldList = FXCollections.observableArrayList();
    private ObservableList<FXData> fxList = FXCollections.observableArrayList();

    private int GrhCount;
    private int GrhVersion;
    private short NumHeads;
    private short NumHelmets;
    private short NumBodys;
    private short NumShields;
    private short NumFXs;
    private short NumObjs;

    private final ConfigManager configManager;
    private final byteMigration byteMigration;
    private final DatEditor datEditor;
    private final Logger logger;

    private IndexLoader indexLoader;

    private static volatile DataManager instance;

    private DataManager() throws IOException {

        // Obtenemos instancias:
        configManager = ConfigManager.getInstance();
        byteMigration = org.nexus.indexador.utils.byteMigration.getInstance();
        datEditor = DatEditor.getInstance();
        logger = Logger.getInstance();

        logger.info("DataManager inicializado");
        initializeIndexLoader();
    }

    /**
     * Inicializa el loader de índices según la configuración.
     */
    private void initializeIndexLoader() throws IOException {
        String systemConfig = configManager.getIndexingSystem();

        if ("TRADITIONAL".equals(systemConfig)) {
            indexLoader = new org.nexus.indexador.gamedata.loaders.TraditionalIndexLoader();
            logger.info("Sistema de indexado: Tradicional");
        } else {
            // Por defecto, usar sistema de moldes
            indexLoader = new org.nexus.indexador.gamedata.loaders.MoldIndexLoader();
            logger.info("Sistema de indexado: Moldes");
        }
    }

    public static DataManager getInstance() throws IOException {
        if (instance == null) {
            synchronized (DataManager.class) {
                if (instance == null) {
                    instance = new DataManager();
                }
            }
        }
        return instance;
    }

    /**
     * Obtiene la lista de gráficos (grh) cargados.
     *
     * @return Una lista observable de objetos GrhData que representan los gráficos
     *         cargados.
     */
    public ObservableList<GrhData> getGrhList() {
        return grhList;
    }

    public ObservableList<HeadData> getHeadList() {
        return headList;
    }

    public ObservableList<HelmetData> getHelmetList() {
        return helmetList;
    }

    public ObservableList<BodyData> getBodyList() {
        return bodyList;
    }

    public ObservableList<ShieldData> getShieldList() {
        return shieldList;
    }

    public ObservableList<FXData> getFXList() {
        return fxList;
    }

    public int getGrhCount() {
        return GrhCount;
    }

    public int getGrhVersion() {
        return GrhVersion;
    }

    public short getNumHeads() {
        return NumHeads;
    }

    public short getNumHelmets() {
        return NumHelmets;
    }

    public short getNumBodys() {
        return NumBodys;
    }

    public short getNumShields() {
        return NumShields;
    }

    public short getNumFXs() {
        return NumFXs;
    }

    public short getNumObjs() {
        return NumObjs;
    }

    public void setGrhCount(int GrhCount) {
        this.GrhCount = GrhCount;
    }

    public void setGrhVersion(int GrhVersion) {
        this.GrhVersion = GrhVersion;
    }

    public void setNumHelmets(short numHelmets) {
        NumHelmets = numHelmets;
    }

    public void setNumHeads(short numHeads) {
        NumHeads = numHeads;
    }

    public void setNumBodys(short numBodys) {
        NumBodys = numBodys;
    }

    public void setNumShields(short numShields) {
        NumShields = numShields;
    }

    public void setNumFXs(short numFXs) {
        NumFXs = numFXs;
    }

    public void setNumObjs(short numObjs) {
        NumObjs = numObjs;
    }

    /**
     * Lee los datos de un archivo binario que contiene información sobre gráficos
     * (grh) y los convierte en objetos grhData.
     * Cada gráfico puede ser una imagen estática o una animación.
     *
     * @return Una lista observable de objetos grhData que representan los gráficos
     *         leídos del archivo.
     * @throws IOException Si ocurre un error de entrada/salida al leer el archivo.
     */
    public ObservableList<GrhData> loadGrhData() throws IOException {
        this.grhList = indexLoader.loadGrhs();
        this.GrhCount = grhList.size();
        return grhList;
    }

    /**
     * Lee los datos de cabeza desde un archivo y los devuelve como una lista
     * observable.
     *
     * @return una {@code ObservableList<headData>} que contiene los datos de cabeza
     *         leídos del archivo.
     * @throws IOException si ocurre un error de entrada/salida.
     */
    public ObservableList<HeadData> readHeadFile() throws IOException {
        headList = indexLoader.loadHeads();
        NumHeads = (short) headList.size();
        return headList;
    }

    /**
     * Lee los datos de los cascos desde un archivo y los carga en una lista
     * observable.
     *
     * @return una lista observable de objetos {@code helmetData} que contiene los
     *         datos de los cascos leídos del archivo.
     * @throws IOException si ocurre un error al leer el archivo.
     */
    public ObservableList<HelmetData> readHelmetFile() throws IOException {
        helmetList = indexLoader.loadHelmets();
        NumHelmets = (short) helmetList.size();
        return helmetList;
    }

    public ObservableList<BodyData> readBodyFile() throws IOException {
        this.bodyList = indexLoader.loadBodies();
        this.NumBodys = (short) bodyList.size();
        return bodyList;
    }

    public ObservableList<ShieldData> readShieldFile() throws IOException {
        this.shieldList = indexLoader.loadShields();
        this.NumShields = (short) shieldList.size();
        return shieldList;
    }

    public ObservableList<FXData> readFXsdFile() throws IOException {
        this.fxList = indexLoader.loadFXs();
        this.NumFXs = (short) fxList.size();
        return fxList;
    }

}

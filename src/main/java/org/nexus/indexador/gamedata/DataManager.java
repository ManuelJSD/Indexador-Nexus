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
import java.util.HashMap;
import java.util.Map;

public class DataManager {

    private ObservableList<GrhData> grhList = FXCollections.observableArrayList();
    private ObservableList<HeadData> headList = FXCollections.observableArrayList();
    private ObservableList<HelmetData> helmetList = FXCollections.observableArrayList();
    private ObservableList<BodyData> bodyList = FXCollections.observableArrayList();
    private ObservableList<ShieldData> shieldList = FXCollections.observableArrayList();
    private ObservableList<FXData> fxList = FXCollections.observableArrayList();
    private ObservableList<WeaponData> weaponList = FXCollections.observableArrayList();

    private int GrhCount;
    private int GrhVersion;
    private short NumHeads;
    private short NumHelmets;
    private short NumBodys;
    private short NumShields;
    private short NumFXs;
    private short NumWeapons;
    private short NumObjs;

    private final ConfigManager configManager;
    private final byteMigration byteMigration;
    private final DatEditor datEditor;
    private final Logger logger;

    private IndexLoader indexLoader;
    private final Map<String, IndFileFormat> fileFormats = new HashMap<>();

    private static volatile DataManager instance;

    private DataManager() throws IOException {
        configManager = ConfigManager.getInstance();
        byteMigration = org.nexus.indexador.utils.byteMigration.getInstance();
        datEditor = DatEditor.getInstance();
        logger = Logger.getInstance();

        logger.info("DataManager inicializado");
        initializeIndexLoader();
    }

    private void initializeIndexLoader() throws IOException {
        String systemConfig = configManager.getIndexingSystem();
        if ("TRADITIONAL".equals(systemConfig)) {
            indexLoader = new org.nexus.indexador.gamedata.loaders.TraditionalIndexLoader();
            logger.info("Sistema de indexado: Tradicional");
        } else {
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

    // --- Loading Methods ---

    public ObservableList<GrhData> loadGrhData() throws IOException {
        this.grhList = indexLoader.loadGrhs();
        this.GrhCount = grhList.size();
        syncFormats();
        return grhList;
    }

    public ObservableList<HeadData> readHeadFile() throws IOException {
        this.headList = indexLoader.loadHeads();
        this.NumHeads = (short) headList.size();
        syncFormats();
        return headList;
    }

    public ObservableList<HelmetData> readHelmetFile() throws IOException {
        this.helmetList = indexLoader.loadHelmets();
        this.NumHelmets = (short) helmetList.size();
        syncFormats();
        return helmetList;
    }

    public ObservableList<BodyData> readBodyFile() throws IOException {
        this.bodyList = indexLoader.loadBodies();
        this.NumBodys = (short) bodyList.size();
        syncFormats();
        return bodyList;
    }

    public ObservableList<ShieldData> readShieldFile() throws IOException {
        this.shieldList = indexLoader.loadShields();
        this.NumShields = (short) shieldList.size();
        syncFormats();
        return shieldList;
    }

    public ObservableList<FXData> readFXsdFile() throws IOException {
        this.fxList = indexLoader.loadFXs();
        this.NumFXs = (short) fxList.size();
        syncFormats();
        return fxList;
    }

    public ObservableList<WeaponData> readWeaponFile() throws IOException {
        this.weaponList = indexLoader.loadWeapons();
        this.NumWeapons = (short) weaponList.size();
        syncFormats();
        return weaponList;
    }

    private void syncFormats() {
        if (indexLoader != null) {
            fileFormats.putAll(indexLoader.getDetectedFormats());
        }
    }

    // --- Getters and Setters ---

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

    public ObservableList<WeaponData> getWeaponList() {
        return weaponList;
    }

    public void setFileFormat(String fileKey, IndFileFormat format) {
        fileFormats.put(fileKey.toUpperCase(), format);
        logger.info("Formato registrado para " + fileKey + ": " + format);
    }

    public IndFileFormat getFileFormat(String fileKey) {
        return fileFormats.get(fileKey.toUpperCase());
    }

    public IndexLoader getIndexLoader() {
        return indexLoader;
    }

    public int getGrhCount() {
        return GrhCount;
    }

    public void setGrhCount(int grhCount) {
        GrhCount = grhCount;
    }

    public int getGrhVersion() {
        return GrhVersion;
    }

    public void setGrhVersion(int grhVersion) {
        GrhVersion = grhVersion;
    }

    public short getNumHeads() {
        return NumHeads;
    }

    public void setNumHeads(short numHeads) {
        NumHeads = numHeads;
    }

    public short getNumHelmets() {
        return NumHelmets;
    }

    public void setNumHelmets(short numHelmets) {
        NumHelmets = numHelmets;
    }

    public short getNumBodys() {
        return NumBodys;
    }

    public void setNumBodys(short numBodys) {
        NumBodys = numBodys;
    }

    public short getNumShields() {
        return NumShields;
    }

    public void setNumShields(short numShields) {
        NumShields = numShields;
    }

    public short getNumFXs() {
        return NumFXs;
    }

    public void setNumFXs(short numFXs) {
        NumFXs = numFXs;
    }

    public short getNumWeapons() {
        return NumWeapons;
    }

    public void setNumWeapons(short numWeapons) {
        NumWeapons = numWeapons;
    }

    public short getNumObjs() {
        return NumObjs;
    }

    public void setNumObjs(short numObjs) {
        NumObjs = numObjs;
    }

    // --- Indexing Orchestration ---

    public void indexFromMemory(String type) throws IOException {
        logger.info("Indexando desde memoria: " + type);
        switch (type.toUpperCase()) {
            case "HEADS":
                indexLoader.saveHeads(headList);
                break;
            case "HELMETS":
                indexLoader.saveHelmets(helmetList);
                break;
            case "BODIES":
                indexLoader.saveBodies(bodyList);
                break;
            case "SHIELDS":
                indexLoader.saveShields(shieldList);
                break;
            case "FXS":
                indexLoader.saveFXs(fxList);
                break;
            case "GRHS":
                indexLoader.saveGrhs(grhList);
                break;
            case "WEAPONS":
                indexLoader.saveWeapons(weaponList);
                break;
        }
    }

    public void indexFromExported(String type) throws IOException {
        logger.info("Indexando desde exportados (texto): " + type);
        switch (type.toUpperCase()) {
            case "HEADS":
                headList = indexLoader.loadHeadsText();
                indexLoader.saveHeads(headList);
                break;
            case "HELMETS":
                helmetList = indexLoader.loadHelmetsText();
                indexLoader.saveHelmets(helmetList);
                break;
            case "BODIES":
                bodyList = indexLoader.loadBodiesText();
                indexLoader.saveBodies(bodyList);
                break;
            case "SHIELDS":
                shieldList = indexLoader.loadShieldsText();
                indexLoader.saveShields(shieldList);
                break;
            case "FXS":
                fxList = indexLoader.loadFXsText();
                indexLoader.saveFXs(fxList);
                break;
            case "GRHS":
                grhList = indexLoader.loadGrhsText();
                indexLoader.saveGrhs(grhList);
                break;
            case "WEAPONS":
                weaponList = indexLoader.loadWeaponsText();
                indexLoader.saveWeapons(weaponList);
                break;
        }
    }

    public void exportToText(String type) throws IOException {
        logger.info("Exportando a texto: " + type);
        switch (type.toUpperCase()) {
            case "HEADS":
                indexLoader.saveHeadsText(headList);
                break;
            case "HELMETS":
                indexLoader.saveHelmetsText(helmetList);
                break;
            case "BODIES":
                indexLoader.saveBodiesText(bodyList);
                break;
            case "SHIELDS":
                indexLoader.saveShieldsText(shieldList);
                break;
            case "FXS":
                indexLoader.saveFXsText(fxList);
                break;
            case "GRHS":
                indexLoader.saveGrhsText(grhList);
                break;
            case "WEAPONS":
                indexLoader.saveWeaponsText(weaponList);
                break;
            case "ALL":
                indexLoader.saveGrhsText(grhList);
                indexLoader.saveHeadsText(headList);
                indexLoader.saveHelmetsText(helmetList);
                indexLoader.saveBodiesText(bodyList);
                indexLoader.saveShieldsText(shieldList);
                indexLoader.saveFXsText(fxList);
                indexLoader.saveWeaponsText(weaponList);
                break;
        }
    }

    public void indexAllFromMemory() throws IOException {
        logger.info("Bulk Indexing from Memory (All)");
        indexFromMemory("GRHS");
        indexFromMemory("HEADS");
        indexFromMemory("HELMETS");
        indexFromMemory("BODIES");
        indexFromMemory("SHIELDS");
        indexFromMemory("FXS");
        indexFromMemory("WEAPONS");
    }

    public void indexAllFromExported() throws IOException {
        logger.info("Bulk Indexing from Exported (All)");
        indexFromExported("GRHS");
        indexFromExported("HEADS");
        indexFromExported("HELMETS");
        indexFromExported("BODIES");
        indexFromExported("SHIELDS");
        indexFromExported("FXS");
        indexFromExported("WEAPONS");
    }
}

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
        return FXCollections.observableArrayList();
    }

    @Override
    public ObservableList<HelmetData> loadHelmets() throws IOException {
        logger.info("Cargando datos de cascos (Sistema de Moldes)...");
        return FXCollections.observableArrayList();
    }

    @Override
    public ObservableList<BodyData> loadBodies() throws IOException {
        return FXCollections.observableArrayList();
    }

    @Override
    public ObservableList<ShieldData> loadShields() throws IOException {
        return FXCollections.observableArrayList();
    }

    @Override
    public ObservableList<FXData> loadFXs() throws IOException {
        return FXCollections.observableArrayList();
    }

    @Override
    public ObservableList<GrhData> loadGrhs() throws IOException {
        return FXCollections.observableArrayList();
    }

    @Override
    public ObservableList<WeaponData> loadWeapons() throws IOException {
        return FXCollections.observableArrayList();
    }

    @Override
    public void saveHeads(ObservableList<HeadData> entries) throws IOException {
    }

    @Override
    public void saveHelmets(ObservableList<HelmetData> entries) throws IOException {
    }

    @Override
    public void saveBodies(ObservableList<BodyData> entries) throws IOException {
    }

    @Override
    public void saveShields(ObservableList<ShieldData> entries) throws IOException {
    }

    @Override
    public void saveFXs(ObservableList<FXData> entries) throws IOException {
    }

    @Override
    public void saveGrhs(ObservableList<GrhData> entries) throws IOException {
    }

    @Override
    public void saveWeapons(ObservableList<WeaponData> entries) throws IOException {
    }

    @Override
    public IndexingSystem getSystemType() {
        return IndexingSystem.MOLD;
    }

    @Override
    public Map<String, IndFileFormat> getDetectedFormats() {
        return Collections.emptyMap();
    }

    @Override
    public ObservableList<HeadData> loadHeadsText() throws IOException {
        return FXCollections.observableArrayList();
    }

    @Override
    public ObservableList<HelmetData> loadHelmetsText() throws IOException {
        return FXCollections.observableArrayList();
    }

    @Override
    public ObservableList<BodyData> loadBodiesText() throws IOException {
        return FXCollections.observableArrayList();
    }

    @Override
    public ObservableList<ShieldData> loadShieldsText() throws IOException {
        return FXCollections.observableArrayList();
    }

    @Override
    public ObservableList<FXData> loadFXsText() throws IOException {
        return FXCollections.observableArrayList();
    }

    @Override
    public ObservableList<GrhData> loadGrhsText() throws IOException {
        return FXCollections.observableArrayList();
    }

    @Override
    public ObservableList<WeaponData> loadWeaponsText() throws IOException {
        return FXCollections.observableArrayList();
    }

    @Override
    public void saveHeadsText(ObservableList<HeadData> entries) throws IOException {
    }

    @Override
    public void saveHelmetsText(ObservableList<HelmetData> entries) throws IOException {
    }

    @Override
    public void saveBodiesText(ObservableList<BodyData> entries) throws IOException {
    }

    @Override
    public void saveShieldsText(ObservableList<ShieldData> entries) throws IOException {
    }

    @Override
    public void saveFXsText(ObservableList<FXData> entries) throws IOException {
    }

    @Override
    public void saveGrhsText(ObservableList<GrhData> entries) throws IOException {
    }

    @Override
    public void saveWeaponsText(ObservableList<WeaponData> entries) throws IOException {
    }
}

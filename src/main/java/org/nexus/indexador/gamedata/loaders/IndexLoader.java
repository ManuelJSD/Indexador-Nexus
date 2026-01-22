package org.nexus.indexador.gamedata.loaders;

import javafx.collections.ObservableList;
import org.nexus.indexador.gamedata.enums.IndexingSystem;
import org.nexus.indexador.gamedata.models.HeadData;
import org.nexus.indexador.gamedata.models.HelmetData;
import org.nexus.indexador.gamedata.models.BodyData;
import org.nexus.indexador.gamedata.models.ShieldData;
import org.nexus.indexador.gamedata.models.FXData;
import org.nexus.indexador.gamedata.models.GrhData;
import org.nexus.indexador.gamedata.models.IndFileFormat;
import org.nexus.indexador.gamedata.models.WeaponData;

import java.io.IOException;
import java.util.Map;

/**
 * Interfaz que define el contrato para los cargadores de índices de cabezas y cascos.
 * 
 * Permite implementar diferentes estrategias de carga según el sistema de indexado utilizado
 * (moldes, tradicional, etc.).
 */
public interface IndexLoader {

  /**
   * Carga los datos de cabezas desde el archivo correspondiente.
   */
  ObservableList<HeadData> loadHeads() throws IOException;

  /**
   * Carga los datos de cascos desde el archivo correspondiente.
   */
  ObservableList<HelmetData> loadHelmets() throws IOException;

  /**
   * Carga los datos de cuerpos desde el archivo correspondiente.
   */
  ObservableList<BodyData> loadBodies() throws IOException;

  /**
   * Carga los datos de escudos (binario o texto).
   */
  ObservableList<ShieldData> loadShields() throws IOException;

  /**
   * Carga los datos de FXs.
   */
  ObservableList<FXData> loadFXs() throws IOException;

  /**
   * Carga los datos de gráficos (grh).
   */
  ObservableList<GrhData> loadGrhs() throws IOException;

  /**
   * Carga los datos de armas.
   */
  ObservableList<WeaponData> loadWeapons() throws IOException;

  // --- Text-based Loading Methods (for Indexing Exported files) ---
  ObservableList<HeadData> loadHeadsText() throws IOException;

  ObservableList<HelmetData> loadHelmetsText() throws IOException;

  ObservableList<BodyData> loadBodiesText() throws IOException;

  ObservableList<ShieldData> loadShieldsText() throws IOException;

  ObservableList<FXData> loadFXsText() throws IOException;

  ObservableList<GrhData> loadGrhsText() throws IOException;

  ObservableList<WeaponData> loadWeaponsText() throws IOException;

  /**
   * Métodos de guardado para cada tipo de asset.
   */
  void saveHeads(ObservableList<HeadData> entries) throws IOException;

  void saveHelmets(ObservableList<HelmetData> entries) throws IOException;

  void saveBodies(ObservableList<BodyData> entries) throws IOException;

  void saveShields(ObservableList<ShieldData> entries) throws IOException;

  void saveFXs(ObservableList<FXData> entries) throws IOException;

  void saveGrhs(ObservableList<GrhData> entries) throws IOException;

  void saveWeapons(ObservableList<WeaponData> entries) throws IOException;

  // --- Text-based Saving Methods (for Exporting) ---
  void saveHeadsText(ObservableList<HeadData> entries) throws IOException;

  void saveHelmetsText(ObservableList<HelmetData> entries) throws IOException;

  void saveBodiesText(ObservableList<BodyData> entries) throws IOException;

  void saveShieldsText(ObservableList<ShieldData> entries) throws IOException;

  void saveFXsText(ObservableList<FXData> entries) throws IOException;

  void saveGrhsText(ObservableList<GrhData> entries) throws IOException;

  void saveWeaponsText(ObservableList<WeaponData> entries) throws IOException;

  /**
   * Obtiene el tipo de sistema de indexado que implementa este loader.
   */
  IndexingSystem getSystemType();

  /**
   * Obtiene los formatos detectados durante la carga. La clave es el identificador del archivo
   * (HEADS, HELMETS, etc).
   */
  Map<String, IndFileFormat> getDetectedFormats();
}

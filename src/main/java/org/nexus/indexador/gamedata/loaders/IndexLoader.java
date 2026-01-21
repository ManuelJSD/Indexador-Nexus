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

import java.io.IOException;
import java.util.Map;

/**
 * Interfaz que define el contrato para los cargadores de índices
 * de cabezas y cascos.
 * 
 * Permite implementar diferentes estrategias de carga según el
 * sistema de indexado utilizado (moldes, tradicional, etc.).
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
     * Obtiene el tipo de sistema de indexado que implementa este loader.
     */
    IndexingSystem getSystemType();

    /**
     * Obtiene los formatos detectados durante la carga.
     * La clave es el identificador del archivo (HEADS, HELMETS, etc).
     */
    Map<String, IndFileFormat> getDetectedFormats();
}

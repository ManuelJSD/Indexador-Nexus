package org.nexus.indexador.gamedata.loaders;

import javafx.collections.ObservableList;
import org.nexus.indexador.gamedata.enums.IndexingSystem;
import org.nexus.indexador.gamedata.models.HeadData;
import org.nexus.indexador.gamedata.models.HelmetData;

import java.io.IOException;

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
     * 
     * @return Lista observable con los datos de cabezas cargados
     * @throws IOException si ocurre un error al leer el archivo
     */
    ObservableList<HeadData> loadHeads() throws IOException;

    /**
     * Carga los datos de cascos desde el archivo correspondiente.
     * 
     * @return Lista observable con los datos de cascos cargados
     * @throws IOException si ocurre un error al leer el archivo
     */
    ObservableList<HelmetData> loadHelmets() throws IOException;

    /**
     * Obtiene el tipo de sistema de indexado que implementa este loader.
     * 
     * @return El tipo de sistema de indexado
     */
    IndexingSystem getSystemType();
}

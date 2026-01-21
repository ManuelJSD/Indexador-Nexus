package org.nexus.indexador.gamedata.enums;

/**
 * Enum que representa los diferentes sistemas de indexado soportados
 * para cabezas y cascos.
 */
public enum IndexingSystem {
    /**
     * Sistema de moldes - utiliza un identificador de molde (Std) y coordenadas
     * en una textura compartida.
     */
    MOLD,

    /**
     * Sistema tradicional - utiliza índices directos a gráficos individuales.
     * (A implementar)
     */
    TRADITIONAL
}

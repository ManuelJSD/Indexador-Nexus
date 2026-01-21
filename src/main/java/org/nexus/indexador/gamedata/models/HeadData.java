/**
 * La clase {@code headData} representa la estructura de datos para elementos gráficos de cabeza.
 * Contiene atributos para estándar, textura y coordenadas de inicio.
 * Esta clase proporciona métodos para establecer y obtener estos atributos, y también para leer datos de cabeza desde un archivo.
 */
package org.nexus.indexador.gamedata.models;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.nexus.indexador.utils.byteMigration;
import org.nexus.indexador.utils.ConfigManager;

import java.io.*;
import org.nexus.indexador.gamedata.enums.IndexingSystem;

public class HeadData {

    private IndexingSystem systemType;

    // Sistema de Moldes
    private int Std;
    private short Texture;
    private short StartX;
    private short StartY;

    // Sistema Tradicional
    private int[] grhs;

    /**
     * Constructor para Moldes
     *
     * @param std     el valor estándar.
     * @param texture el valor de la textura.
     * @param startX  la coordenada X de inicio.
     * @param startY  la coordenada Y de inicio.
     */
    public HeadData(int std, short texture, short startX, short startY) {
        this.systemType = IndexingSystem.MOLD;
        Std = std;
        Texture = texture;
        StartX = startX;
        StartY = startY;
    }

    /**
     * Constructor para Tradicional
     *
     * @param grhs el array de datos de la cabeza.
     */
    public HeadData(int[] grhs) {
        this.systemType = IndexingSystem.TRADITIONAL;
        this.grhs = grhs;
    }

    // Métodos GET

    /**
     * Retorna el valor estándar.
     *
     * @return el valor estándar.
     */
    public int getStd() {
        if (systemType != IndexingSystem.MOLD) {
            throw new IllegalStateException("Std solo disponible en sistema de moldes");
        }
        return Std;
    }

    /**
     * Retorna el valor de la textura.
     *
     * @return el valor de la textura.
     */
    public short getTexture() {
        if (systemType != IndexingSystem.MOLD) {
            throw new IllegalStateException("Texture solo disponible en sistema de moldes");
        }
        return Texture;
    }

    /**
     * Retorna la coordenada X de inicio.
     *
     * @return la coordenada X de inicio.
     */
    public short getStartX() {
        if (systemType != IndexingSystem.MOLD) {
            throw new IllegalStateException("StartX solo disponible en sistema de moldes");
        }
        return StartX;
    }

    /**
     * Retorna la coordenada Y de inicio.
     *
     * @return la coordenada Y de inicio.
     */
    public short getStartY() {
        if (systemType != IndexingSystem.MOLD) {
            throw new IllegalStateException("StartY solo disponible en sistema de moldes");
        }
        return StartY;
    }

    public int[] getGrhs() {
        if (systemType != IndexingSystem.TRADITIONAL) {
            throw new IllegalStateException("Grhs solo disponible en sistema tradicional");
        }
        return grhs;
    }

    public IndexingSystem getSystemType() {
        return systemType;
    }

    // Métodos SET

    /**
     * Establece el valor estándar.
     *
     * @param std el valor estándar.
     */
    public void setStd(int std) {
        if (systemType != IndexingSystem.MOLD) {
            throw new IllegalStateException("Std solo disponible en sistema de moldes");
        }
        Std = std;
    }

    /**
     * Establece el valor de la textura.
     *
     * @param texture el valor de la textura.
     */
    public void setTexture(short texture) {
        if (systemType != IndexingSystem.MOLD) {
            throw new IllegalStateException("Texture solo disponible en sistema de moldes");
        }
        Texture = texture;
    }

    /**
     * Establece la coordenada X de inicio.
     *
     * @param startX la coordenada X de inicio.
     */
    public void setStartX(short startX) {
        if (systemType != IndexingSystem.MOLD) {
            throw new IllegalStateException("StartX solo disponible en sistema de moldes");
        }
        StartX = startX;
    }

    /**
     * Establece la coordenada Y de inicio.
     *
     * @param startY la coordenada Y de inicio.
     */
    public void setStartY(short startY) {
        if (systemType != IndexingSystem.MOLD) {
            throw new IllegalStateException("StartY solo disponible en sistema de moldes");
        }
        StartY = startY;
    }

    public void setGrhs(int[] grhs) {
        if (systemType != IndexingSystem.TRADITIONAL) {
            throw new IllegalStateException("Grhs solo disponible en sistema tradicional");
        }
        this.grhs = grhs;
    }

}
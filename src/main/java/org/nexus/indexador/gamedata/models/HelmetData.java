/**
 * La clase {@code helmetData} maneja los datos de los cascos, incluyendo la carga desde un archivo y la manipulación de dichos datos.
 */
package org.nexus.indexador.gamedata.models;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.nexus.indexador.utils.byteMigration;
import org.nexus.indexador.gamedata.enums.IndexingSystem;
import org.nexus.indexador.utils.ConfigManager;

import java.io.*;

public class HelmetData {

    private IndexingSystem systemType;

    // Tradicional
    private int[] grhs;

    // Moldes
    private int Std;
    private short Texture;
    private short StartX;
    private short StartY;

    /**
     * Constructor para Moldes
     *
     * @param std     el valor estándar del casco.
     * @param texture la textura del casco.
     * @param startX  la coordenada X inicial del casco.
     * @param startY  la coordenada Y inicial del casco.
     */
    public HelmetData(int std, short texture, short startX, short startY) {
        this.systemType = IndexingSystem.MOLD;
        Std = std;
        Texture = texture;
        StartX = startX;
        StartY = startY;
    }

    /**
     * Constructor para Tradicional
     *
     * @param grhs el array de datos del casco.
     */
    public HelmetData(int[] grhs) {
        this.systemType = IndexingSystem.TRADITIONAL;
        this.grhs = grhs;
    }

    // Métodos GET
    public int getStd() {
        if (systemType != IndexingSystem.MOLD) {
            throw new IllegalStateException("Std solo disponible en sistema de moldes");
        }
        return Std;
    }

    public short getTexture() {
        if (systemType != IndexingSystem.MOLD) {
            throw new IllegalStateException("Texture solo disponible en sistema de moldes");
        }
        return Texture;
    }

    public short getStartX() {
        if (systemType != IndexingSystem.MOLD) {
            throw new IllegalStateException("StartX solo disponible en sistema de moldes");
        }
        return StartX;
    }

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
    public void setStd(int std) {
        if (systemType != IndexingSystem.MOLD) {
            throw new IllegalStateException("Std solo disponible en sistema de moldes");
        }
        Std = std;
    }

    public void setTexture(short texture) {
        if (systemType != IndexingSystem.MOLD) {
            throw new IllegalStateException("Texture solo disponible en sistema de moldes");
        }
        Texture = texture;
    }

    public void setStartX(short startX) {
        if (systemType != IndexingSystem.MOLD) {
            throw new IllegalStateException("StartX solo disponible en sistema de moldes");
        }
        StartX = startX;
    }

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
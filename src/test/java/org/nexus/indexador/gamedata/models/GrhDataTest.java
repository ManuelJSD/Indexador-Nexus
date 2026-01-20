package org.nexus.indexador.gamedata.models;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for GrhData model class.
 */
class GrhDataTest {

    @Test
    @DisplayName("Should create static GRH with all properties set correctly")
    void shouldCreateStaticGrh() {
        // Arrange
        int grh = 1;
        short numFrames = 1;
        int fileNum = 100;
        short sX = 0;
        short sY = 0;
        short tileWidth = 32;
        short tileHeight = 32;

        // Act
        GrhData grhData = new GrhData(grh, numFrames, fileNum, sX, sY, tileWidth, tileHeight);

        // Assert
        assertEquals(grh, grhData.getGrh());
        assertEquals(numFrames, grhData.getNumFrames());
        assertEquals(fileNum, grhData.getFileNum());
        assertEquals(sX, grhData.getsX());
        assertEquals(sY, grhData.getsY());
        assertEquals(tileWidth, grhData.getTileWidth());
        assertEquals(tileHeight, grhData.getTileHeight());
    }

    @Test
    @DisplayName("Should create animated GRH with frames and speed")
    void shouldCreateAnimatedGrh() {
        // Arrange
        int grh = 100;
        short numFrames = 4;
        int[] frames = { 0, 1, 2, 3, 4 };
        float speed = 100.0f;

        // Act
        GrhData grhData = new GrhData(grh, numFrames, frames, speed);

        // Assert
        assertEquals(grh, grhData.getGrh());
        assertEquals(numFrames, grhData.getNumFrames());
        assertArrayEquals(frames, grhData.getFrames());
        assertEquals(speed, grhData.getSpeed());
    }

    @Test
    @DisplayName("Should create empty GRH with default constructor")
    void shouldCreateEmptyGrh() {
        // Act
        GrhData grhData = new GrhData();

        // Assert
        assertNotNull(grhData);
        assertEquals(0, grhData.getGrh());
        assertEquals(0, grhData.getNumFrames());
    }

    @Test
    @DisplayName("Should update properties via setters")
    void shouldUpdatePropertiesViaSetters() {
        // Arrange
        GrhData grhData = new GrhData();

        // Act
        grhData.setGrh(500);
        grhData.setFileNum(200);
        grhData.setsX((short) 10);
        grhData.setsY((short) 20);
        grhData.setTileWidth((short) 64);
        grhData.setTileHeight((short) 64);
        grhData.setSpeed(150.0f);
        grhData.setNumFrames((short) 2);

        // Assert
        assertEquals(500, grhData.getGrh());
        assertEquals(200, grhData.getFileNum());
        assertEquals(10, grhData.getsX());
        assertEquals(20, grhData.getsY());
        assertEquals(64, grhData.getTileWidth());
        assertEquals(64, grhData.getTileHeight());
        assertEquals(150.0f, grhData.getSpeed());
        assertEquals(2, grhData.getNumFrames());
    }
}

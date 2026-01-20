package org.nexus.indexador.gamedata.models;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for FXData model class.
 */
class FXDataTest {

    @Test
    @DisplayName("Should create FXData with all properties set correctly")
    void shouldCreateFXDataWithAllProperties() {
        // Arrange
        int fx = 1;
        short offsetX = 10;
        short offsetY = 20;

        // Act
        FXData fxData = new FXData(fx, offsetX, offsetY);

        // Assert
        assertEquals(fx, fxData.getFx());
        assertEquals(offsetX, fxData.getOffsetX());
        assertEquals(offsetY, fxData.getOffsetY());
    }

    @Test
    @DisplayName("Should create empty FXData with default constructor")
    void shouldCreateEmptyFXData() {
        // Act
        FXData fxData = new FXData();

        // Assert
        assertNotNull(fxData);
        assertEquals(0, fxData.getFx());
    }

    @Test
    @DisplayName("Should update properties via setters")
    void shouldUpdatePropertiesViaSetters() {
        // Arrange
        FXData fxData = new FXData();

        // Act
        fxData.setFx(100);
        fxData.setOffsetX((short) 15);
        fxData.setOffsetY((short) 25);

        // Assert
        assertEquals(100, fxData.getFx());
        assertEquals(15, fxData.getOffsetX());
        assertEquals(25, fxData.getOffsetY());
    }
}

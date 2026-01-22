package org.nexus.indexador.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for byteMigration utility class.
 */
class ByteMigrationTest {

  @Test
  @DisplayName("Should convert big-endian int to little-endian")
  void shouldConvertBigToLittleInt() {
    // Arrange
    byteMigration migration = byteMigration.getInstance();
    int bigEndian = 0x01020304;

    // Act
    int littleEndian = migration.bigToLittle_Int(bigEndian);

    // Assert
    assertEquals(0x04030201, littleEndian);
  }

  @Test
  @DisplayName("Should convert big-endian short to little-endian")
  void shouldConvertBigToLittleShort() {
    // Arrange
    byteMigration migration = byteMigration.getInstance();
    short bigEndian = 0x0102;

    // Act
    short littleEndian = migration.bigToLittle_Short(bigEndian);

    // Assert
    assertEquals(0x0201, littleEndian);
  }

  @Test
  @DisplayName("Singleton should return same instance")
  void singletonShouldReturnSameInstance() {
    // Act
    byteMigration instance1 = byteMigration.getInstance();
    byteMigration instance2 = byteMigration.getInstance();

    // Assert
    assertSame(instance1, instance2);
  }
}

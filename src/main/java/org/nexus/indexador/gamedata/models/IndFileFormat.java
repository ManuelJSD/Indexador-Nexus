package org.nexus.indexador.gamedata.models;

/**
 * Representa el formato detectado de un archivo de índices (.ind).
 */
public class IndFileFormat {
  private final long dataOffset;
  private final boolean isLong; // true = 4 bytes (Long), false = 2 bytes (Integer)
  private final boolean valid;

  public IndFileFormat(long dataOffset, boolean isLong, boolean valid) {
    this.dataOffset = dataOffset;
    this.isLong = isLong;
    this.valid = valid;
  }

  public long getDataOffset() {
    return dataOffset;
  }

  public boolean isLong() {
    return isLong;
  }

  public boolean isValid() {
    return valid;
  }

  @Override
  public String toString() {
    return "IndFileFormat{" + "offset=" + dataOffset + ", isLong=" + isLong + ", valid=" + valid
        + '}';
  }
}

package org.nexus.indexador.gamedata.models;

/**
 * Representa el formato detectado de un archivo de índices (.ind).
 */
public class IndFileFormat {
  private final long dataOffset;
  private final boolean isLong; // Traditional: 4 bytes (Long) vs 2 bytes (Integer)
  private final int recordSize; // explicit size for mold system (7, 8, 10, etc.)
  private final boolean valid;

  public IndFileFormat(long dataOffset, boolean isLong, boolean valid) {
    this(dataOffset, isLong, valid, isLong ? 10 : 8);
  }

  public IndFileFormat(long dataOffset, boolean isLong, boolean valid, int recordSize) {
    this.dataOffset = dataOffset;
    this.isLong = isLong;
    this.valid = valid;
    this.recordSize = recordSize;
  }

  public long getDataOffset() {
    return dataOffset;
  }

  public boolean isLong() {
    return isLong;
  }

  public int getRecordSize() {
    return recordSize;
  }

  public boolean isValid() {
    return valid;
  }

  @Override
  public String toString() {
    return "IndFileFormat{" + "offset=" + dataOffset + ", isLong=" + isLong + ", recordSize="
        + recordSize + ", valid=" + valid + '}';
  }
}

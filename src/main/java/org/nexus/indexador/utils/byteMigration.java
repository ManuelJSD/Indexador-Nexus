package org.nexus.indexador.utils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class byteMigration {
  // Instancia única de ByteMigration (volatile para thread safety)
  private static volatile byteMigration instance;

  // Constructor privado para evitar instanciación directa
  private byteMigration() {
    // Constructor privado para evitar instanciación directa
  }

  // Método estático para obtener la única instancia de ByteMigration
  // (thread-safe)
  public static byteMigration getInstance() {
    if (instance == null) {
      synchronized (byteMigration.class) {
        if (instance == null) {
          instance = new byteMigration();
        }
      }
    }
    return instance;
  }

  /**
   *
   * @param bigendian
   * @return
   */
  public int bigToLittle_Int(int bigendian) {
    ByteBuffer buf = ByteBuffer.allocate(4);

    buf.order(ByteOrder.BIG_ENDIAN);
    buf.putInt(bigendian);

    buf.order(ByteOrder.LITTLE_ENDIAN);
    return buf.getInt(0);
  }

  /**
   *
   * @param bigendian
   * @return
   */
  public float bigToLittle_Float(float bigendian) {
    ByteBuffer buf = ByteBuffer.allocate(4);

    buf.order(ByteOrder.BIG_ENDIAN);
    buf.putFloat(bigendian);

    buf.order(ByteOrder.LITTLE_ENDIAN);
    return buf.getFloat(0);
  }

  /**
   *
   * @param bigendian
   * @return
   */
  public short bigToLittle_Short(short bigendian) {
    ByteBuffer buf = ByteBuffer.allocate(2);

    buf.order(ByteOrder.BIG_ENDIAN);
    buf.putShort(bigendian);

    buf.order(ByteOrder.LITTLE_ENDIAN);
    return buf.getShort(0);
  }

  /**
   *
   * @param bigendian
   * @return
   */
  public byte bigToLittle_Byte(byte bigendian) {
    ByteBuffer buf = ByteBuffer.allocate(1);

    buf.order(ByteOrder.BIG_ENDIAN);
    buf.put(bigendian);

    buf.order(ByteOrder.LITTLE_ENDIAN);
    return buf.get(0);
  }
}

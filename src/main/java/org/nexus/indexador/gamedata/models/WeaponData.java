package org.nexus.indexador.gamedata.models;

import java.util.Arrays;

/**
 * Representa los datos de una arma (.ind o .dat).
 */
public class WeaponData {
    private final int[] grhIndex; // [Norte, Sur, Este, Oeste]

    public WeaponData(int[] grhIndex) {
        if (grhIndex == null || grhIndex.length != 4) {
            this.grhIndex = new int[4];
        } else {
            this.grhIndex = grhIndex;
        }
    }

    public int[] getGrhIndex() {
        return grhIndex;
    }

    @Override
    public String toString() {
        return "WeaponData{" +
                "grhIndex=" + Arrays.toString(grhIndex) +
                '}';
    }
}

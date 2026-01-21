package org.nexus.indexador.gamedata.models;

public class ShieldData {

    private int[] Shield;

    private static short NumShields;

    public ShieldData(int[] shield) {
        this.Shield = shield;
    }

    public ShieldData() {
    }

    public int[] getShield() {
        return Shield;
    }

    public int[] getGrhIndex() {
        return Shield;
    }

    public static short getNumShields() {
        return NumShields;
    }

    public void setShield(int[] shield) {
        Shield = shield;
    }

    public static void setNumShields(short numBodys) {
        NumShields = numBodys;
    }

}
package org.nexus.indexador.gamedata.models;

public class FXData {

    private int Fx;
    private short OffsetX;
    private short OffsetY;

    private static short NumFxs;

    public FXData(int fx, short offsetX, short offsetY) {
        this.Fx = fx;
        this.OffsetX = offsetX;
        this.OffsetY = offsetY;
    }

    public FXData() {
    }

    public int getFx() {
        return Fx;
    }

    public short getOffsetX() {
        return OffsetX;
    }

    public short getOffsetY() {
        return OffsetY;
    }

    public static short getNumFxs() {
        return NumFxs;
    }

    public void setFx(int fx) {
        Fx = fx;
    }

    public void setOffsetX(short offsetX) {
        OffsetX = offsetX;
    }

    public void setOffsetY(short offsetY) {
        OffsetY = offsetY;
    }

    public static void setNumFxs(short numBodys) {
        NumFxs = numBodys;
    }

}
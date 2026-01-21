package org.nexus.indexador.gamedata.models;

public class GrhData {
    // Variables
    private int grh;
    private int FileNum;

    private int Frames[];
    private short NumFrames;

    private short sX;
    private short sY;

    private short TileWidth;
    private short TileHeight;

    private float Speed;

    // Constructor para graficos estaticos
    public GrhData(int grh, short NumFrames, int FileNum, short sX, short sY, short TileWidth, short TileHeight) {
        this.grh = grh;
        this.NumFrames = NumFrames;
        this.FileNum = FileNum;
        this.sX = sX;
        this.sY = sY;
        this.TileWidth = TileWidth;
        this.TileHeight = TileHeight;
    }

    // Constructor para animaciones
    public GrhData(int grh, short NumFrames, int[] Frames, float Speed) {
        this.grh = grh;
        this.NumFrames = NumFrames;
        this.Frames = Frames;
        this.Speed = Speed;
    }

    // Constructor vacio
    public GrhData() {
    }

    // Metodos SET
    public void setFileNum(int FileNum) {
        this.FileNum = FileNum;
    }

    public void setNumFrames(short NumFrames) {
        this.NumFrames = NumFrames;
    }

    public void setFrames(int[] Frames) {
        this.Frames = Frames;
    }

    public void setsX(short sX) {
        this.sX = sX;
    }

    public void setsY(short sY) {
        this.sY = sY;
    }

    public void setTileWidth(short TileWidth) {
        this.TileWidth = TileWidth;
    }

    public void setTileHeight(short TileHeight) {
        this.TileHeight = TileHeight;
    }

    public void setSpeed(float Speed) {
        this.Speed = Speed;
    }

    public void setGrh(int grh) {
        this.grh = grh;
    }

    // Metodos GET
    public int getFileNum() {
        return this.FileNum;
    }

    public short getNumFrames() {
        return this.NumFrames;
    }

    public int[] getFrames() {
        return this.Frames;
    }

    public short getsX() {
        return this.sX;
    }

    public short getsY() {
        return this.sY;
    }

    public short getTileWidth() {
        return this.TileWidth;
    }

    public short getTileHeight() {
        return this.TileHeight;
    }

    public float getSpeed() {
        return this.Speed;
    }

    public int getGrh() {
        return grh;
    }

    public int getGrhIndex() {
        return grh;
    }

    public int getFrame(int index) {
        if (Frames != null && index >= 1 && index <= NumFrames) {
            return Frames[index];
        }
        return 0;
    }

    public short getSX() {
        return this.sX;
    }

    public short getSY() {
        return this.sY;
    }

    public short getPixelWidth() {
        return this.TileWidth;
    }

    public short getPixelHeight() {
        return this.TileHeight;
    }
}

package org.nexus.indexador.gamedata.models;

import java.io.*;

public class BodyData {

  private int[] Body;
  private short HeadOffsetX;
  private short HeadOffsetY;

  private static short NumBodys;

  public BodyData(int[] body, short headOffsetX, short headOffsetY) {
    this.Body = body;
    this.HeadOffsetX = headOffsetX;
    this.HeadOffsetY = headOffsetY;
  }

  public BodyData() {}

  public int[] getBody() {
    return Body;
  }

  public int[] getWalkG() {
    return Body;
  }

  public short getHeadOffsetX() {
    return HeadOffsetX;
  }

  public short getHeadOffsetY() {
    return HeadOffsetY;
  }

  public static short getNumBodys() {
    return NumBodys;
  }

  public void setBody(int[] body) {
    Body = body;
  }

  public void setHeadOffsetX(short headOffsetX) {
    HeadOffsetX = headOffsetX;
  }

  public void setHeadOffsetY(short headOffsetY) {
    HeadOffsetY = headOffsetY;
  }

  public static void setNumBodys(short numBodys) {
    NumBodys = numBodys;
  }

}

package org.nexus.indexador.utils.actions;

import org.nexus.indexador.gamedata.models.GrhData;
import org.nexus.indexador.utils.UndoManager.UndoableAction;

/**
 * Acción de modificación de un GRH. Guarda el estado anterior para poder deshacerla.
 */
public class ModifyGrhAction implements UndoableAction {

  private final GrhData grh;

  // Estado anterior
  private final int oldFileNum;
  private final short oldSX;
  private final short oldSY;
  private final short oldTileWidth;
  private final short oldTileHeight;
  private final short oldNumFrames;
  private final float oldSpeed;
  private final int[] oldFrames;

  // Estado nuevo
  private final int newFileNum;
  private final short newSX;
  private final short newSY;
  private final short newTileWidth;
  private final short newTileHeight;
  private final short newNumFrames;
  private final float newSpeed;
  private final int[] newFrames;

  /**
   * Crea una acción de modificación para un GRH estático.
   */
  public ModifyGrhAction(GrhData grh, int newFileNum, short newSX, short newSY, short newTileWidth,
      short newTileHeight) {
    this.grh = grh;

    // Guardar estado anterior
    this.oldFileNum = grh.getFileNum();
    this.oldSX = grh.getsX();
    this.oldSY = grh.getsY();
    this.oldTileWidth = grh.getTileWidth();
    this.oldTileHeight = grh.getTileHeight();
    this.oldNumFrames = grh.getNumFrames();
    this.oldSpeed = grh.getSpeed();
    this.oldFrames = grh.getFrames() != null ? grh.getFrames().clone() : null;

    // Estado nuevo
    this.newFileNum = newFileNum;
    this.newSX = newSX;
    this.newSY = newSY;
    this.newTileWidth = newTileWidth;
    this.newTileHeight = newTileHeight;
    this.newNumFrames = 1;
    this.newSpeed = 0;
    this.newFrames = null;
  }

  /**
   * Crea una acción de modificación para un GRH animado.
   */
  public ModifyGrhAction(GrhData grh, short newNumFrames, int[] newFrames, float newSpeed) {
    this.grh = grh;

    // Guardar estado anterior
    this.oldFileNum = grh.getFileNum();
    this.oldSX = grh.getsX();
    this.oldSY = grh.getsY();
    this.oldTileWidth = grh.getTileWidth();
    this.oldTileHeight = grh.getTileHeight();
    this.oldNumFrames = grh.getNumFrames();
    this.oldSpeed = grh.getSpeed();
    this.oldFrames = grh.getFrames() != null ? grh.getFrames().clone() : null;

    // Estado nuevo
    this.newFileNum = 0;
    this.newSX = 0;
    this.newSY = 0;
    this.newTileWidth = 0;
    this.newTileHeight = 0;
    this.newNumFrames = newNumFrames;
    this.newSpeed = newSpeed;
    this.newFrames = newFrames != null ? newFrames.clone() : null;
  }

  @Override
  public void execute() {
    if (newNumFrames > 1) {
      // Animación
      grh.setNumFrames(newNumFrames);
      grh.setFrames(newFrames);
      grh.setSpeed(newSpeed);
    } else {
      // Estático
      grh.setNumFrames(newNumFrames);
      grh.setFileNum(newFileNum);
      grh.setsX(newSX);
      grh.setsY(newSY);
      grh.setTileWidth(newTileWidth);
      grh.setTileHeight(newTileHeight);
    }
  }

  @Override
  public void undo() {
    grh.setNumFrames(oldNumFrames);
    grh.setFileNum(oldFileNum);
    grh.setsX(oldSX);
    grh.setsY(oldSY);
    grh.setTileWidth(oldTileWidth);
    grh.setTileHeight(oldTileHeight);
    grh.setSpeed(oldSpeed);
    grh.setFrames(oldFrames);
  }

  @Override
  public String getDescription() {
    return "Modificar GRH " + grh.getGrh();
  }
}

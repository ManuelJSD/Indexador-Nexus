package org.nexus.indexador.utils.actions;

import org.nexus.indexador.gamedata.models.GrhData;
import org.nexus.indexador.utils.UndoManager.UndoableAction;
import java.util.Map;
import java.util.HashMap;

/**
 * Acción de intercambio de dos GRH. Guarda el estado para poder deshacerlo.
 */
public class SwapGrhAction implements UndoableAction {

  private final GrhData grh1;
  private final GrhData grh2;
  
  // Guardamos un clon completo para evitar problemas de referencias al hacer undo/redo repetido
  private final GrhData oldGrh1State;
  private final GrhData oldGrh2State;
  
  // Guardar estado de animaciones afectadas
  private final Map<GrhData, int[]> oldAnimationsFrames = new HashMap<>();
  private final Map<GrhData, int[]> newAnimationsFrames = new HashMap<>();

  public SwapGrhAction(GrhData grh1, GrhData grh2, Map<GrhData, int[]> affectedAnimationsNewFrames) {
    this.grh1 = grh1;
    this.grh2 = grh2;

    this.oldGrh1State = cloneGrhState(grh1);
    this.oldGrh2State = cloneGrhState(grh2);
    
    if (affectedAnimationsNewFrames != null) {
        for (Map.Entry<GrhData, int[]> entry : affectedAnimationsNewFrames.entrySet()) {
            GrhData animGrh = entry.getKey();
            // Clonamos el estado original de la animación
            oldAnimationsFrames.put(animGrh, animGrh.getFrames() != null ? animGrh.getFrames().clone() : null);
            newAnimationsFrames.put(animGrh, entry.getValue());
        }
    }
  }

  @Override
  public void execute() {
    // Aplicamos el swap de los estados, NOTA: NO cambiamos "grh" (ID interno), ya que la posición en el mapa/lista depende de eso.
    // Solo intercambiamos su contenido.
    applyGrhState(grh1, oldGrh2State);
    applyGrhState(grh2, oldGrh1State);
    
    // Aplicamos nuevas animaciones si las hay
    for (Map.Entry<GrhData, int[]> entry : newAnimationsFrames.entrySet()) {
        entry.getKey().setFrames(entry.getValue());
    }
  }

  @Override
  public void undo() {
    // Volvemos a su estado original
    applyGrhState(grh1, oldGrh1State);
    applyGrhState(grh2, oldGrh2State);
    
    // Restauramos animaciones
    for (Map.Entry<GrhData, int[]> entry : oldAnimationsFrames.entrySet()) {
        entry.getKey().setFrames(entry.getValue());
    }
  }

  @Override
  public String getDescription() {
    return "Intercambiar GRH " + grh1.getGrh() + " con " + grh2.getGrh();
  }
  
  private GrhData cloneGrhState(GrhData source) {
      if (source == null) return null;
      GrhData clone = new GrhData();
      clone.setGrh(source.getGrh());
      clone.setFileNum(source.getFileNum());
      clone.setsX(source.getsX());
      clone.setsY(source.getsY());
      clone.setTileWidth(source.getTileWidth());
      clone.setTileHeight(source.getTileHeight());
      clone.setNumFrames(source.getNumFrames());
      clone.setSpeed(source.getSpeed());
      if (source.getFrames() != null) {
          clone.setFrames(source.getFrames().clone());
      }
      return clone;
  }
  
  private void applyGrhState(GrhData target, GrhData source) {
      if (target == null || source == null) return;
      target.setFileNum(source.getFileNum());
      target.setsX(source.getsX());
      target.setsY(source.getsY());
      target.setTileWidth(source.getTileWidth());
      target.setTileHeight(source.getTileHeight());
      target.setNumFrames(source.getNumFrames());
      target.setSpeed(source.getSpeed());
      if (source.getFrames() != null) {
          target.setFrames(source.getFrames().clone());
      } else {
          target.setFrames(null);
      }
  }
}

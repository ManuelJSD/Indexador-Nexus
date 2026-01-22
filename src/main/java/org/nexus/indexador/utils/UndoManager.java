package org.nexus.indexador.utils;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Gestor de acciones Undo/Redo para la aplicación. Implementa el patrón Command para soportar
 * deshacer y rehacer cambios.
 */
public class UndoManager {

  private static volatile UndoManager instance;

  private final Deque<UndoableAction> undoStack = new ArrayDeque<>();
  private final Deque<UndoableAction> redoStack = new ArrayDeque<>();

  private static final int MAX_UNDO_HISTORY = 50;

  private final Logger logger = Logger.getInstance();

  // Listeners para notificar cambios de estado
  private Runnable onStateChanged;

  // Flag para indicar si hay cambios sin guardar
  private boolean hasUnsavedChanges = false;

  private UndoManager() {
    logger.info("UndoManager inicializado");
  }

  public static UndoManager getInstance() {
    if (instance == null) {
      synchronized (UndoManager.class) {
        if (instance == null) {
          instance = new UndoManager();
        }
      }
    }
    return instance;
  }

  /**
   * Interfaz para acciones que pueden deshacerse.
   */
  public interface UndoableAction {
    /**
     * Ejecuta la acción.
     */
    void execute();

    /**
     * Deshace la acción.
     */
    void undo();

    /**
     * Descripción de la acción para mostrar al usuario.
     */
    String getDescription();
  }

  /**
   * Ejecuta una acción y la añade al historial de undo.
   *
   * @param action La acción a ejecutar.
   */
  public void executeAction(UndoableAction action) {
    action.execute();
    undoStack.push(action);
    redoStack.clear(); // Limpiar redo al hacer nueva acción

    // Limitar historial
    while (undoStack.size() > MAX_UNDO_HISTORY) {
      undoStack.removeLast();
    }

    hasUnsavedChanges = true;
    logger.debug("Acción ejecutada: " + action.getDescription());
    notifyStateChanged();
  }

  /**
   * Deshace la última acción.
   *
   * @return true si se pudo deshacer.
   */
  public boolean undo() {
    if (undoStack.isEmpty()) {
      return false;
    }

    UndoableAction action = undoStack.pop();
    action.undo();
    redoStack.push(action);

    hasUnsavedChanges = true;
    logger.debug("Undo: " + action.getDescription());
    notifyStateChanged();
    return true;
  }

  /**
   * Rehace la última acción deshecha.
   *
   * @return true si se pudo rehacer.
   */
  public boolean redo() {
    if (redoStack.isEmpty()) {
      return false;
    }

    UndoableAction action = redoStack.pop();
    action.execute();
    undoStack.push(action);

    hasUnsavedChanges = true;
    logger.debug("Redo: " + action.getDescription());
    notifyStateChanged();
    return true;
  }

  /**
   * Verifica si hay acciones para deshacer.
   */
  public boolean canUndo() {
    return !undoStack.isEmpty();
  }

  /**
   * Verifica si hay acciones para rehacer.
   */
  public boolean canRedo() {
    return !redoStack.isEmpty();
  }

  /**
   * Obtiene la descripción de la próxima acción a deshacer.
   */
  public String getUndoDescription() {
    return undoStack.isEmpty() ? "" : undoStack.peek().getDescription();
  }

  /**
   * Obtiene la descripción de la próxima acción a rehacer.
   */
  public String getRedoDescription() {
    return redoStack.isEmpty() ? "" : redoStack.peek().getDescription();
  }

  /**
   * Limpia el historial de undo/redo.
   */
  public void clear() {
    undoStack.clear();
    redoStack.clear();
    notifyStateChanged();
  }

  /**
   * Marca que los cambios fueron guardados.
   */
  public void markSaved() {
    hasUnsavedChanges = false;
    notifyStateChanged();
  }

  /**
   * Verifica si hay cambios sin guardar.
   */
  public boolean hasUnsavedChanges() {
    return hasUnsavedChanges;
  }

  /**
   * Establece el listener para cambios de estado.
   */
  public void setOnStateChanged(Runnable listener) {
    this.onStateChanged = listener;
  }

  private void notifyStateChanged() {
    if (onStateChanged != null) {
      onStateChanged.run();
    }
  }

  /**
   * Obtiene el número de acciones en el stack de undo.
   */
  public int getUndoCount() {
    return undoStack.size();
  }

  /**
   * Obtiene el número de acciones en el stack de redo.
   */
  public int getRedoCount() {
    return redoStack.size();
  }
}

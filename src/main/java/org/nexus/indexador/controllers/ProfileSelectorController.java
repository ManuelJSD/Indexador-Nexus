package org.nexus.indexador.controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.nexus.indexador.utils.ConfigManager;
import org.nexus.indexador.utils.Logger;
import org.nexus.indexador.utils.ProfileEntry;
import org.nexus.indexador.utils.ProfileManager;

import java.util.Optional;

/**
 * Controlador de la pantalla de selección de perfiles.
 *
 * <p>Muestra la lista de perfiles disponibles y permite crear, renombrar,
 * reordenar y eliminar perfiles, además de seleccionar el perfil activo
 * para continuar con el arranque de la aplicación.
 */
public class ProfileSelectorController {

  @FXML
  private ListView<ProfileEntry> listViewPerfiles;

  @FXML
  private Button btnAbrir;

  @FXML
  private Button btnMoverArriba;

  @FXML
  private Button btnMoverAbajo;

  @FXML
  private Button btnRenombrar;

  @FXML
  private Button btnEditar;

  @FXML
  private Button btnEliminar;

  @FXML
  private Label lblверсия;

  private Stage stage;

  /** Callback invocado cuando el usuario selecciona un perfil. Recibe el perfil elegido. */
  private java.util.function.Consumer<ProfileEntry> onPerfilSeleccionado;

  /** Callback invocado cuando el usuario quiere crear un nuevo perfil. */
  private Runnable onNuevoPerfil;

  /** Callback invocado cuando el usuario quiere editar un perfil existente. */
  private java.util.function.BiConsumer<Integer, ProfileEntry> onEditarPerfil;

  private final ProfileManager profileManager = ProfileManager.getInstance();
  private final Logger logger = Logger.getInstance();
  private ObservableList<ProfileEntry> perfilesObservables;

  /**
   * Inicializa el controlador: puebla el ListView y configura listeners.
   */
  @FXML
  public void initialize() {
    perfilesObservables = FXCollections.observableArrayList(profileManager.getPerfiles());
    listViewPerfiles.setItems(perfilesObservables);

    // Custom CellFactory para mejorar el 'touch target' y la tipografía
    listViewPerfiles.setCellFactory(lv -> new ListCell<ProfileEntry>() {
      @Override
      protected void updateItem(ProfileEntry item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
          setText(null);
          setGraphic(null);
        } else {
          setText(item.getNombre());
          setStyle("-fx-pref-height: 40px; -fx-alignment: CENTER_LEFT; -fx-padding: 0 10 0 10; -fx-font-size: 14px; -fx-font-weight: bold;");
        }
      }
    });

    // Seleccionar el primero por defecto si hay perfiles
    if (!perfilesObservables.isEmpty()) {
      listViewPerfiles.getSelectionModel().selectFirst();
    }

    // Actualizar estado de botones según selección
    listViewPerfiles.getSelectionModel().selectedItemProperty().addListener(
        (obs, anterior, actual) -> actualizarEstadoBotones());

    // Doble clic abre el perfil directamente
    listViewPerfiles.setOnMouseClicked(e -> {
      if (e.getClickCount() == 2 && listViewPerfiles.getSelectionModel().getSelectedItem() != null) {
        onAbrir();
      }
    });

    actualizarEstadoBotones();
  }

  // -------------------------------------------------------------------------
  // Inyección de dependencias
  // -------------------------------------------------------------------------

  /** @param stage Stage de esta ventana. */
  public void setStage(Stage stage) {
    this.stage = stage;
  }

  /**
   * Callback invocado tras seleccionar un perfil.
   *
   * @param callback Consumer que recibe el {@link ProfileEntry} seleccionado.
   */
  public void setOnPerfilSeleccionado(java.util.function.Consumer<ProfileEntry> callback) {
    this.onPerfilSeleccionado = callback;
  }

  /**
   * Callback invocado cuando el usuario hace clic en "Nuevo Perfil".
   * El {@link Main} se encarga de abrir el wizard de configuración.
   *
   * @param callback Runnable a ejecutar.
   */
  public void setOnNuevoPerfil(Runnable callback) {
    this.onNuevoPerfil = callback;
  }

  /**
   * Callback invocado cuando el usuario hace clic en "Editar".
   *
   * @param callback BiConsumer que recibe el índice y el {@link ProfileEntry}.
   */
  public void setOnEditarPerfil(java.util.function.BiConsumer<Integer, ProfileEntry> callback) {
    this.onEditarPerfil = callback;
  }

  // -------------------------------------------------------------------------
  // Acciones de botones
  // -------------------------------------------------------------------------

  /**
   * Abre el perfil seleccionado: reset → readConfig → callback.
   */
  @FXML
  private void onAbrir() {
    ProfileEntry seleccionado = listViewPerfiles.getSelectionModel().getSelectedItem();
    if (seleccionado == null) {
      return;
    }

    try {
      // 1. Reset a defaults para no filtrar valores anteriores
      ConfigManager.getInstance().resetToDefaults();
      // 2. Leer la config del perfil seleccionado
      ConfigManager.getInstance().readConfig(seleccionado.getConfigFile());
      // 3. Marcar como activo
      profileManager.setPerfilActivo(seleccionado);

      logger.info("Perfil seleccionado: " + seleccionado.getNombre()
          + " (" + seleccionado.getConfigPath() + ")");

      // 4. Cerrar y notificar
      stage.close();
      if (onPerfilSeleccionado != null) {
        Platform.runLater(() -> onPerfilSeleccionado.accept(seleccionado));
      }
    } catch (java.io.IOException e) {
      logger.error("Error al cargar configuración del perfil: " + seleccionado.getNombre(), e);
      mostrarError("No se pudo cargar la configuración del perfil.\n" + e.getMessage());
    }
  }

  /**
   * Crea un nuevo perfil lanzando el wizard de configuración inicial.
   */
  @FXML
  private void onNuevoPerfil() {
    if (onNuevoPerfil != null) {
      stage.close();
      Platform.runLater(onNuevoPerfil);
    }
  }

  /**
   * Abre el wizard de configuración en modo edición para el perfil seleccionado.
   */
  @FXML
  private void onEditar() {
    int indice = listViewPerfiles.getSelectionModel().getSelectedIndex();
    if (indice < 0) {
      return;
    }
    ProfileEntry perfil = perfilesObservables.get(indice);

    if (onEditarPerfil != null) {
      stage.close();
      Platform.runLater(() -> onEditarPerfil.accept(indice, perfil));
    }
  }

  /**
   * Renombra el perfil seleccionado.
   */
  @FXML
  private void onRenombrar() {
    int indice = listViewPerfiles.getSelectionModel().getSelectedIndex();
    if (indice < 0) {
      return;
    }
    ProfileEntry perfil = perfilesObservables.get(indice);

    TextInputDialog dialog = new TextInputDialog(perfil.getNombre());
    dialog.setTitle("Renombrar Perfil");
    dialog.setHeaderText("Introduce el nuevo nombre para el perfil:");
    dialog.setContentText("Nombre:");
    aplicarIconoDialog(dialog);

    Optional<String> resultado = dialog.showAndWait();
    resultado.ifPresent(nuevoNombre -> {
      nuevoNombre = nuevoNombre.trim();
      if (!nuevoNombre.isEmpty() && !nuevoNombre.equals(perfil.getNombre())) {
        profileManager.renombrarPerfil(indice, nuevoNombre);
        refrescarLista(indice);
        logger.info("Perfil renombrado a: " + nuevoNombre);
      }
    });
  }

  /**
   * Elimina el perfil seleccionado tras confirmación.
   */
  @FXML
  private void onEliminar() {
    int indice = listViewPerfiles.getSelectionModel().getSelectedIndex();
    if (indice < 0) {
      return;
    }
    ProfileEntry perfil = perfilesObservables.get(indice);

    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
    confirm.setTitle("Eliminar Perfil");
    confirm.setHeaderText("¿Eliminar el perfil \"" + perfil.getNombre() + "\"?");
    confirm.setContentText(
        "Se eliminará el perfil de la lista.\nEl archivo .ini no será borrado del disco.");
    aplicarIconoDialog(confirm);

    confirm.showAndWait().ifPresent(tipo -> {
      if (tipo == ButtonType.OK) {
        profileManager.eliminarPerfil(indice);
        refrescarLista(Math.min(indice, perfilesObservables.size() - 1));
        logger.info("Perfil eliminado: " + perfil.getNombre());
      }
    });
  }

  /**
   * Mueve el perfil seleccionado una posición hacia arriba.
   */
  @FXML
  private void onMoverArriba() {
    int indice = listViewPerfiles.getSelectionModel().getSelectedIndex();
    if (indice <= 0) {
      return;
    }
    profileManager.moverArriba(indice);
    refrescarLista(indice - 1);
  }

  /**
   * Mueve el perfil seleccionado una posición hacia abajo.
   */
  @FXML
  private void onMoverAbajo() {
    int indice = listViewPerfiles.getSelectionModel().getSelectedIndex();
    if (indice < 0 || indice >= perfilesObservables.size() - 1) {
      return;
    }
    profileManager.moverAbajo(indice);
    refrescarLista(indice + 1);
  }

  // -------------------------------------------------------------------------
  // Helpers
  // -------------------------------------------------------------------------

  /**
   * Refresca el ListView con el estado actual del ProfileManager.
   *
   * @param seleccionar Índice a seleccionar tras la actualización.
   */
  private void refrescarLista(int seleccionar) {
    perfilesObservables.setAll(profileManager.getPerfiles());
    if (seleccionar >= 0 && seleccionar < perfilesObservables.size()) {
      listViewPerfiles.getSelectionModel().select(seleccionar);
    }
    actualizarEstadoBotones();
  }

  /**
   * Habilita o deshabilita los botones según el estado de la selección.
   */
  private void actualizarEstadoBotones() {
    int indice = listViewPerfiles.getSelectionModel().getSelectedIndex();
    int total = perfilesObservables.size();

    btnAbrir.setDisable(indice < 0);
    btnEditar.setDisable(indice < 0);
    btnRenombrar.setDisable(indice < 0);
    btnEliminar.setDisable(indice < 0);
    btnMoverArriba.setDisable(indice <= 0);
    btnMoverAbajo.setDisable(indice < 0 || indice >= total - 1);
  }

  /**
   * Muestra un diálogo de error.
   *
   * @param mensaje Mensaje a mostrar.
   */
  private void mostrarError(String mensaje) {
    Alert alert = new Alert(Alert.AlertType.ERROR);
    alert.setTitle("Error");
    alert.setHeaderText("Ocurrió un error");
    alert.setContentText(mensaje);
    aplicarIconoDialog(alert);
    alert.showAndWait();
  }

  /**
   * Aplica el icono de la aplicación a un diálogo.
   *
   * @param dialog Diálogo al que aplicar el icono.
   */
  private void aplicarIconoDialog(Dialog<?> dialog) {
    Stage dialogStage = (Stage) dialog.getDialogPane().getScene().getWindow();
    org.nexus.indexador.Main.setAppIcon(dialogStage);
  }
}

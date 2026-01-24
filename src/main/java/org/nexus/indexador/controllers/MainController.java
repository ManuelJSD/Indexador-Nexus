package org.nexus.indexador.controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.image.PixelReader;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Rectangle;
import javafx.scene.control.*;
import org.nexus.indexador.gamedata.DataManager;
import org.nexus.indexador.gamedata.models.GrhData;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.nexus.indexador.utils.ConfigManager;
import org.nexus.indexador.utils.ExportService;
import org.nexus.indexador.utils.ImageCache;
import org.nexus.indexador.utils.Logger;
import org.nexus.indexador.utils.ToastNotification;
import org.nexus.indexador.utils.UndoManager;
import org.nexus.indexador.utils.ValidationService;
import org.nexus.indexador.utils.WindowManager;
import org.nexus.indexador.utils.AutoTilingService;
import org.nexus.indexador.utils.GameRenderer;
import org.nexus.indexador.utils.GrhEditorHelper;
import org.nexus.indexador.services.FilterService;
import org.nexus.indexador.services.UIService;

import java.io.*;
import java.util.*;
import javafx.scene.paint.Color; // Importar Color
import javafx.scene.Group; // Import Group
import org.nexus.indexador.Main;

public class MainController {

  @FXML
  public MenuItem mnuShield;

  @FXML
  public Menu mnuVer;

  @FXML
  public MenuItem mnuHead;

  @FXML
  public MenuItem mnuHelmet;

  @FXML
  public MenuItem mnuBody;

  @FXML
  public MenuItem mnuFXs;

  @FXML
  public MenuItem mnuConsola;

  @FXML
  public MenuItem mnuGrhAdapter;

  @FXML
  public MenuItem mnuBuscarGrhLibres;

  @FXML
  public MenuItem mnuAsistente;

  @FXML
  public MenuItem mnuCode;

  @FXML
  public Menu mnuReload;

  @FXML
  public MenuItem mnuReloadGrhs;

  @FXML
  public MenuItem mnuReloadHeads;

  @FXML
  public MenuItem mnuReloadHelmets;

  @FXML
  public MenuItem mnuReloadBodies;

  @FXML
  public MenuItem mnuReloadShields;

  @FXML
  public MenuItem mnuReloadWeapons;

  @FXML
  public MenuItem mnuThemeDark;

  @FXML
  public MenuItem mnuThemeLight;

  @FXML
  public MenuItem mnuReloadFXs;

  @FXML
  public MenuItem mnuReloadAll;

  @FXML
  public MenuItem mnuIndexExpFXs;

  @FXML
  public Label lblIndice;

  @FXML
  public ScrollPane PaneGrhView;

  @FXML
  private ListView<String> lstIndices;

  @FXML
  private ListView<String> lstFrames;

  @FXML
  private Label lblIndices;

  @FXML
  private Label lblVersion; // Nuevo Label agregado

  @FXML
  private TextField txtImagen;

  @FXML
  private TextField txtPosX;

  @FXML
  private TextField txtPosY;

  @FXML
  private TextField txtAncho;

  @FXML
  private TextField txtAlto;

  @FXML
  private TextField txtIndice;

  @FXML
  private TextField txtSpeed;

  @FXML
  private TextField txtFiltro;

  // Filtros avanzados
  @FXML
  private javafx.scene.control.CheckBox chkAnimations;

  @FXML
  private javafx.scene.control.CheckBox chkStatics;

  @FXML
  private TextField txtFilterFileNum;

  @FXML
  private TextField txtFilterWidth;

  @FXML
  private TextField txtFilterHeight;

  @FXML
  private javafx.scene.layout.Pane paneFilterHeader;

  @FXML
  private javafx.scene.layout.Pane paneFilterContent;

  @FXML
  private javafx.scene.layout.Pane panePreviewBackground;

  @FXML
  private ColorPicker cpBackground;

  @FXML
  private Label lblFilterToggle;

  @FXML
  private ImageView imgIndice;

  @FXML
  private ImageView imgGrafico;

  @FXML
  private Group grpPreview; // Inject Group

  @FXML
  private Rectangle rectanguloIndice;

  @FXML
  private Slider sldZoom;

  // Status bar components
  @FXML
  private Label lblStatus;

  @FXML
  private Label lblGrhCount;

  @FXML
  private Label lblAnimCount;

  @FXML
  private Label lblModified;

  @FXML
  private ProgressBar progressMain;

  // Lista observable que contiene los datos de los gráficos indexados.
  private ObservableList<GrhData> grhList;

  // Clase con los datos de la animación y el mapa para la búsqueda rápida
  private Map<Integer, GrhData> grhDataMap;

  // Objeto encargado de manejar la configuración de la aplicación, incluyendo la
  // lectura y escritura de archivos de configuración.
  private ConfigManager configManager;

  private DataManager dataManager;

  // Estado del panel de filtros (expandido/colapsado)
  private boolean filterExpanded = true;

  // Caché de imágenes para optimizar la carga y uso de recursos
  private ImageCache imageCache;

  // Logger para registro de eventos
  private Logger logger;

  // Gestor de ventanas
  private WindowManager windowManager;

  // Renderizador de gráficos y animaciones
  private GameRenderer gameRenderer;

  // Servicio de filtrado y búsqueda
  private FilterService filterService;

  // Ayudante para el editor de propiedades
  private GrhEditorHelper editorHelper;

  // Servicio de UI (Alertas y Tareas Asíncronas)
  private UIService uiService;

  // Clipboard para copiar/pegar GRH
  private GrhData copiedGrh = null;

  // Coordenadas originales del cursor del mouse en la escena al presionar el
  // botón del mouse.
  private double orgSceneX, orgSceneY;

  // Valores de traducción originales del ImageView al arrastrar el mouse.
  private double orgTranslateX, orgTranslateY;

  // Variables para el panning del visor derecho
  private double previewOrgSceneX, previewOrgSceneY;
  private double previewOrgTranslateX, previewOrgTranslateY;
  private double rectOrgTranslateX, rectOrgTranslateY;

  public ConfigManager getConfigManager() {
    return this.configManager;
  }

  /**
   * Método de inicialización del controlador. Carga los datos de gráficos y
   * configura el ListView.
   */
  @FXML
  protected void initialize() throws IOException {

    // Obtener instancias de configuración y datos
    configManager = ConfigManager.getInstance();
    dataManager = org.nexus.indexador.gamedata.DataManager.getInstance();
    imageCache = ImageCache.getInstance();
    logger = Logger.getInstance();
    windowManager = WindowManager.getInstance();

    logger.info("Inicializando controlador MainController");

    loadGrh();
    setupDataSync();
    setupGrhListListener();
    setupFilterTextFieldListener();
    setupSliderZoom();
    setupColorPicker();

    // Inicializar servicios
    gameRenderer = new GameRenderer(imgIndice, imgGrafico, rectanguloIndice);
    filterService = FilterService.getInstance();
    editorHelper = new GrhEditorHelper(txtImagen, txtPosX, txtPosY, txtAncho, txtAlto, txtSpeed,
        txtIndice, lstFrames);
    uiService = new UIService();
    uiService.init(lblStatus, progressMain);

    // Aplicar color de fondo configurado
    updateBackgroundColor();

    // Configurar clip para evitar desbordamiento visual sobre las pestañas
    if (panePreviewBackground != null) {
      Rectangle clip = new Rectangle();
      clip.widthProperty().bind(panePreviewBackground.widthProperty());
      clip.heightProperty().bind(panePreviewBackground.heightProperty());
      panePreviewBackground.setClip(clip);
    }

    logger.info("Controlador MainController inicializado correctamente");
  }

  /**
   * Carga los datos de gráficos desde archivos binarios y actualiza la interfaz
   * de usuario con la
   * información obtenida. Muestra los índices de gráficos en el ListView y
   * actualiza los textos de
   * los labels con información relevante.
   *
   * @throws IOException Sí ocurre un error durante la lectura de los archivos
   *                     binarios.
   */
  private void loadGrh() {

    // Llamar al método para leer el archivo binario y obtener la lista de grhData
    try {
      grhList = dataManager.loadGrhData();

      // Inicializar el mapa de grhData
      grhDataMap = new HashMap<>();

      // Llenar el mapa con los datos de grhList
      int animationCount = 0;
      for (GrhData grh : grhList) {
        grhDataMap.put(grh.getGrh(), grh);
        if (grh.getNumFrames() > 1) {
          animationCount++;
        }
      }

      // Compartir el mapa de datos globalmente para otras ventanas (Cabezas/Cascos)
      // org.nexus.indexador.Main.sharedGrhData = grhDataMap; // REMOVED: Now managed
      // by DataManager

      // Actualizar el texto de los labels con la información obtenida
      lblIndices.setText("Indices cargados: " + dataManager.getGrhCount());
      lblVersion.setText("Versión de Indices: " + dataManager.getGrhVersion());

      // Agregar los índices de gráficos al ListView
      ObservableList<String> grhIndices = FXCollections.observableArrayList();
      for (GrhData grh : grhList) {
        String indice = String.valueOf(grh.getGrh());
        if (grh.getNumFrames() > 1) {
          indice += " (Animación)"; // Agregar indicación de animación
        }
        grhIndices.add(indice);
      }
      lstIndices.setItems(grhIndices);

      // Actualizar status bar
      updateStatusBar("Listo", grhList.size(), animationCount, false);

      logger.info("Gráficos cargados correctamente: " + grhList.size() + " índices");

    } catch (IOException e) {
      logger.error("Error al cargar los datos de gráficos", e);
      updateStatusBar("Error al cargar datos", 0, 0, false);
    }
  }

  private void setupDataSync() {
    if (grhList != null) {
      grhList.addListener((javafx.collections.ListChangeListener<GrhData>) c -> {
        while (c.next()) {
          if (c.wasAdded()) {
            for (GrhData addedItem : c.getAddedSubList()) {
              String indice = String.valueOf(addedItem.getGrh());
              if (addedItem.getNumFrames() > 1) {
                indice += " (Animación)";
              }
              lstIndices.getItems().add(indice);

              // Update Map and status
              if (grhDataMap != null) {
                grhDataMap.put(addedItem.getGrh(), addedItem);
              }
            }

            Platform.runLater(() -> {
              lblIndices.setText("Indices cargados: " + grhList.size());
              updateStatusBar("Índices añadidos externamente", grhList.size(), 0, false);
            });
          }
        }
      });
    }
  }

  /**
   * Configura un listener para el ListView para capturar los eventos de
   * selección. Cuando se
   * selecciona un índice de gráfico, actualiza el editor y el visor con la
   * información
   * correspondiente.
   */
  private void setupGrhListListener() {
    // Agregar un listener al ListView para capturar los eventos de selección
    lstIndices.getSelectionModel().selectedItemProperty()
        .addListener((observable, oldValue, newValue) -> {

          // Detenemos la animación actual si existe
          gameRenderer.stopAnimation();

          // Obtener el índice seleccionado
          int selectedIndex = lstIndices.getSelectionModel().getSelectedIndex();

          if (selectedIndex >= 0) {
            // Obtener el objeto grhData correspondiente al índice seleccionado
            GrhData selectedGrh = grhList.get(selectedIndex);
            updateEditor(selectedGrh);
            updateViewer(selectedGrh);
          }
        });

    // Manejar atajos de teclado explícitamente porque ListView consume algunos
    // eventos
    lstIndices.setOnKeyPressed(event -> {
      if (event.isControlDown()) {
        switch (event.getCode()) {
          case C:
            mnuCopy_OnAction();
            event.consume();
            break;
          case V:
            mnuPaste_OnAction();
            event.consume();
            break;
          case D:
            mnuDuplicate_OnAction();
            event.consume();
            break;
          default:
            break;
        }
      }
    });
  }

  /**
   * Actualiza el editor con la información del gráfico seleccionado. Muestra los
   * detalles del
   * gráfico seleccionado en los campos de texto correspondientes.
   *
   * @param selectedGrh El gráfico seleccionado.
   */
  private void updateEditor(GrhData selectedGrh) {
    editorHelper.updateEditor(selectedGrh);
  }

  /**
   * Actualiza el visor con el gráfico seleccionado. Si el gráfico es estático,
   * muestra la imagen
   * estática correspondiente. Si es una animación, muestra la animación.
   *
   * @param selectedGrh El gráfico seleccionado.
   */
  private void updateViewer(GrhData selectedGrh) {
    gameRenderer.displayGrh(selectedGrh, grhDataMap);
  }

  /**
   * Maneja el evento de presionar el mouse. Este método se invoca cuando el
   * usuario presiona el
   * botón del mouse. Si se presiona el botón secundario del mouse (generalmente
   * el botón derecho),
   * registra las coordenadas de la escena iniciales y los valores de traducción
   * del ImageView.
   *
   * @param event El MouseEvent que representa el evento de presionar el mouse.
   */
  @FXML
  private void onMousePressed(MouseEvent event) {
    if (event.isSecondaryButtonDown()) {
      orgSceneX = event.getSceneX();
      orgSceneY = event.getSceneY();
      orgTranslateX = ((ImageView) (event.getSource())).getTranslateX();
      orgTranslateY = ((ImageView) (event.getSource())).getTranslateY();
    }
  }

  /**
   * Maneja el evento de arrastrar el mouse. Este método se invoca cuando el
   * usuario arrastra el
   * mouse después de presionarlo. Si se presiona el botón secundario del mouse
   * (generalmente el
   * botón derecho), calcula el desplazamiento desde la posición inicial y
   * actualiza los valores de
   * traducción del ImageView en consecuencia.
   *
   * @param event El MouseEvent que representa el evento de arrastrar el mouse.
   */
  @FXML
  private void onMouseDragged(MouseEvent event) {
    if (event.isSecondaryButtonDown()) {
      double offsetX = event.getSceneX() - orgSceneX;
      double offsetY = event.getSceneY() - orgSceneY;
      double newTranslateX = orgTranslateX + offsetX;
      double newTranslateY = orgTranslateY + offsetY;

      ((ImageView) (event.getSource())).setTranslateX(newTranslateX);
      ((ImageView) (event.getSource())).setTranslateY(newTranslateY);
    }
  }

  /**
   * Maneja el evento de presionar el mouse en el visor de vista previa (derecho).
   */
  @FXML
  private void onMousePressedPreview(MouseEvent event) {
    if (event.isSecondaryButtonDown()) {
      previewOrgSceneX = event.getSceneX();
      previewOrgSceneY = event.getSceneY();

      // Capturar posición del Grupo
      if (grpPreview != null) {
        previewOrgTranslateX = grpPreview.getTranslateX();
        previewOrgTranslateY = grpPreview.getTranslateY();
      }
    }
  }

  /**
   * Maneja el evento de arrastrar el mouse en el visor de vista previa (derecho).
   */
  @FXML
  private void onMouseDraggedPreview(MouseEvent event) {
    if (event.isSecondaryButtonDown() && grpPreview != null) {
      double offsetX = event.getSceneX() - previewOrgSceneX;
      double offsetY = event.getSceneY() - previewOrgSceneY;

      double newTranslateX = previewOrgTranslateX + offsetX;
      double newTranslateY = previewOrgTranslateY + offsetY;

      // Mover todo el Grupo (Imagen + Rectángulo)
      grpPreview.setTranslateX(newTranslateX);
      grpPreview.setTranslateY(newTranslateY);
    }
  }

  /**
   * Maneja el evento de scroll (zoom) en el visor de vista previa (derecho).
   */
  @FXML
  private void onScrollPreview(javafx.scene.input.ScrollEvent event) {
    if (grpPreview == null)
      return;

    double delta = event.getDeltaY();
    double scaleFactor = 1.05;
    double currentScale = grpPreview.getScaleX();

    if (delta < 0) {
      // Zoom out
      currentScale /= scaleFactor;
    } else {
      // Zoom in
      currentScale *= scaleFactor;
    }

    // Limitar zoom
    if (currentScale < 0.1)
      currentScale = 0.1;
    if (currentScale > 10.0)
      currentScale = 10.0;

    // Escalar todo el Grupo
    grpPreview.setScaleX(currentScale);
    grpPreview.setScaleY(currentScale);

    event.consume();
  }

  /**
   * Método para manejar la acción cuando se hace clic en el elemento del menú
   * "Consola"
   */
  @FXML
  private void mnuConsola_OnAction() {
    windowManager.showWindow("ConsoleController", "Consola", false);
  }

  @FXML
  private void mnuOptions_OnAction() {
    // Pasar la instancia del controlador principal a la ventana de opciones si es
    // necesario
    // Pero WindowManager se encarga de abrirla.

    // WindowManager abre y retorna true si es nueva
    boolean isNew = windowManager.showWindow("OptionsController", "Opciones", false);

    if (isNew) {
      // Lógica de inicialización adicional si fuera necesaria
    }
  }

  @FXML
  private void mnuThemeDark_OnAction(ActionEvent event) {
    changeTheme("DARK");
  }

  @FXML
  private void mnuThemeLight_OnAction(ActionEvent event) {
    changeTheme("LIGHT");
  }

  private void changeTheme(String theme) {
    configManager.setAppTheme(theme);
    try {
      configManager.writeConfig();
    } catch (IOException e) {
      logger.error("Error saving theme config", e);
    }
    windowManager.updateThemeForAll(theme);
  }

  /**
   * Método para manejar la acción cuando se hace clic en el elemento del menú
   * "Color de Fondo..."
   */
  @FXML
  private void mnuConfigColor_OnAction() {
    ColorPicker colorPicker = new ColorPicker();
    try {
      colorPicker.setValue(Color.web(configManager.getBackgroundColor()));
    } catch (IllegalArgumentException e) {
      colorPicker.setValue(Color.web("#EA3FF7")); // Default fallback
    }

    Dialog<ButtonType> dialog = new Dialog<>();
    dialog.setTitle("Seleccionar Color de Fondo");
    dialog.setHeaderText("Elija el color de fondo para el visor");

    // Aplicar estilos oscuros si es necesario (simple workaround)
    // dialog.getDialogPane().getStylesheets().add(Main.class.getResource("styles/dark-theme.css").toExternalForm());

    dialog.getDialogPane().setContent(colorPicker);
    dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

    Optional<ButtonType> result = dialog.showAndWait();
    if (result.isPresent() && result.get() == ButtonType.OK) {
      Color c = colorPicker.getValue();
      // Convertir a Hex string
      String hex = String.format("#%02X%02X%02X", (int) (c.getRed() * 255),
          (int) (c.getGreen() * 255), (int) (c.getBlue() * 255));

      configManager.setBackgroundColor(hex);
      try {
        configManager.writeConfig();
      } catch (IOException e) {
        logger.error("Error al guardar configuración de color", e);
        showWarningAlert("Error", "No se pudo guardar la configuración.");
      }
      updateBackgroundColor();
    }
  }

  private void updateBackgroundColor() {
    if (panePreviewBackground != null) {
      String color = configManager.getBackgroundColor();
      // Mantener el borde gris
      panePreviewBackground
          .setStyle("-fx-background-color: " + color + "; -fx-border-color: #CBCBCB;");
    }
  }

  @FXML
  private void mnuCode_OnAction() {
    try {
      java.awt.Desktop.getDesktop()
          .browse(new java.net.URI("https://github.com/ManuelJSD/Indexador-Nexus"));
    } catch (Exception e) {
      logger.error("Error al abrir el enlace del repositorio", e);
      showWarningAlert("Error", "No se pudo abrir el navegador: " + e.getMessage());
    }
  }

  /**
   * Deshace la última acción.
   */
  @FXML
  private void mnuUndo_OnAction() {
    UndoManager undoManager = UndoManager.getInstance();
    if (undoManager.canUndo()) {
      String description = undoManager.getUndoDescription();
      undoManager.undo();
      logger.info("Undo: " + description);

      // Refrescar la vista
      int selectedIndex = lstIndices.getSelectionModel().getSelectedIndex();
      if (selectedIndex >= 0 && selectedIndex < grhList.size()) {
        updateEditor(grhList.get(selectedIndex));
      }
      updateUndoRedoStatus();
    }
  }

  /**
   * Rehace la última acción deshecha.
   */
  @FXML
  private void mnuRedo_OnAction() {
    UndoManager undoManager = UndoManager.getInstance();
    if (undoManager.canRedo()) {
      String description = undoManager.getRedoDescription();
      undoManager.redo();
      logger.info("Redo: " + description);

      // Refrescar la vista
      int selectedIndex = lstIndices.getSelectionModel().getSelectedIndex();
      if (selectedIndex >= 0 && selectedIndex < grhList.size()) {
        updateEditor(grhList.get(selectedIndex));
      }
      updateUndoRedoStatus();
    }
  }

  /**
   * Actualiza el estado de los menús Undo/Redo.
   */
  private void updateUndoRedoStatus() {
    UndoManager undoManager = UndoManager.getInstance();
    // Actualizar status bar para mostrar si hay cambios sin guardar
    if (undoManager.hasUnsavedChanges()) {
      if (lblModified != null) {
        lblModified.setText("● Modificado");
      }
    }
  }

  /**
   * Copia el GRH seleccionado al clipboard interno.
   */
  @FXML
  private void mnuCopy_OnAction() {
    int selectedIndex = lstIndices.getSelectionModel().getSelectedIndex();
    if (selectedIndex >= 0 && selectedIndex < grhList.size()) {
      GrhData original = grhList.get(selectedIndex);
      // Crear copia profunda
      if (original.getNumFrames() > 1) {
        // Animación
        int[] framesCopy = original.getFrames() != null ? original.getFrames().clone() : null;
        copiedGrh = new GrhData(original.getGrh(), original.getNumFrames(), framesCopy,
            original.getSpeed());
      } else {
        // Estático
        copiedGrh = new GrhData(original.getGrh(), original.getNumFrames(), original.getFileNum(),
            original.getsX(), original.getsY(), original.getTileWidth(), original.getTileHeight());
      }
      logger.info("GRH " + original.getGrh() + " copiado al clipboard");
      logger.info("GRH " + original.getGrh() + " copiado al clipboard");
      ToastNotification.show(WindowManager.getInstance().getWindow("MainController"),
          "GRH " + original.getGrh() + " copiado al clipboard");
    } else {
      showWarningAlert("Sin selección", "Seleccione un GRH para copiar.");
    }
  }

  /**
   * Pega las propiedades del GRH copiado sobre el GRH seleccionado.
   */
  @FXML
  private void mnuPaste_OnAction() {
    if (copiedGrh == null) {
      showWarningAlert("Clipboard vacío", "No hay ningún GRH copiado. Use Ctrl+C primero.");
      return;
    }

    int selectedIndex = lstIndices.getSelectionModel().getSelectedIndex();
    if (selectedIndex >= 0 && selectedIndex < grhList.size()) {
      GrhData target = grhList.get(selectedIndex);

      // Aplicar propiedades del copiado al seleccionado (mantener el ID)
      target.setNumFrames(copiedGrh.getNumFrames());
      if (copiedGrh.getNumFrames() > 1) {
        target.setFrames(copiedGrh.getFrames() != null ? copiedGrh.getFrames().clone() : null);
        target.setSpeed(copiedGrh.getSpeed());
      } else {
        target.setFileNum(copiedGrh.getFileNum());
        target.setsX(copiedGrh.getsX());
        target.setsY(copiedGrh.getsY());
        target.setTileWidth(copiedGrh.getTileWidth());
        target.setTileHeight(copiedGrh.getTileHeight());
      }

      // Actualizar vista
      updateEditor(target);
      updateViewer(target);

      UndoManager.getInstance().executeAction(new UndoManager.UndoableAction() {
        @Override
        public void execute() {
        }

        @Override
        public void undo() {
          updateEditor(target);
          updateViewer(target);
        }

        @Override
        public String getDescription() {
          return "Pegar en GRH " + target.getGrh();
        }
      });

      logger.info("Propiedades pegadas en GRH " + target.getGrh());
      logger.info("Propiedades pegadas en GRH " + target.getGrh());
      ToastNotification.show(WindowManager.getInstance().getWindow("MainController"),
          "Propiedades pegadas en GRH " + target.getGrh());
    } else {
      showWarningAlert("Sin selección", "Seleccione un GRH donde pegar.");
    }
  }

  /**
   * Duplica el GRH seleccionado en una nueva entrada.
   */
  @FXML
  private void mnuDuplicate_OnAction() {
    int selectedIndex = lstIndices.getSelectionModel().getSelectedIndex();
    if (selectedIndex >= 0 && selectedIndex < grhList.size()) {
      GrhData original = grhList.get(selectedIndex);

      // Crear nuevo ID
      int newId = dataManager.getGrhCount() + 1;
      dataManager.setGrhCount(newId);

      // Crear copia
      GrhData duplicate;
      if (original.getNumFrames() > 1) {
        int[] framesCopy = original.getFrames() != null ? original.getFrames().clone() : null;
        duplicate = new GrhData(newId, original.getNumFrames(), framesCopy, original.getSpeed());
      } else {
        duplicate = new GrhData(newId, original.getNumFrames(), original.getFileNum(),
            original.getsX(), original.getsY(), original.getTileWidth(), original.getTileHeight());
      }

      grhList.add(duplicate);
      lstIndices.getItems().add(String.valueOf(newId));
      lstIndices.getSelectionModel().select(grhList.size() - 1);
      lstIndices.scrollTo(grhList.size() - 1);

      logger.info("GRH " + original.getGrh() + " duplicado como GRH " + newId);
      showInfoAlert("Duplicado", "GRH " + original.getGrh() + " duplicado como GRH " + newId);
    } else {
      showWarningAlert("Sin selección", "Seleccione un GRH para duplicar.");
    }
  }

  /**
   * Exporta los datos de gráficos al archivo "graficos.ini" en el directorio de
   * exportación
   * configurado. Los datos exportados incluyen el número total de gráficos, la
   * versión de los
   * índices y la información detallada de cada gráfico. Si se produce algún error
   * durante el
   * proceso de exportación, se imprime un mensaje de error.
   */
  @FXML
  private void mnuExportGrh_OnAction() {
    File file = new File(configManager.getExportDir() + "graficos.ini");
    logger.info("Exportando indices, espera...");

    ExportService exportService = ExportService.getInstance();
    if (exportService.exportToIni(grhList, dataManager.getGrhCount(), dataManager.getGrhVersion(),
        file)) {
      uiService.showInfo("Exportación Completa", "Índices exportados a:\n" + file.getAbsolutePath());
    } else {
      uiService.showError("Error de Exportación", "No se pudieron exportar los índices.");
    }
  }

  /**
   * Exporta los índices a formato JSON.
   */
  @FXML
  private void mnuExportJson_OnAction() {
    File file = new File(configManager.getExportDir() + "graficos.json");

    ExportService exportService = ExportService.getInstance();
    if (exportService.exportToJson(grhList, file)) {
      uiService.showInfo("Exportación JSON Completa",
          "Índices exportados a:\n" + file.getAbsolutePath());
    } else {
      uiService.showError("Error de Exportación", "No se pudo exportar a JSON.");
    }
  }

  /**
   * Exporta los índices a formato CSV.
   */
  @FXML
  public void mnuExportCsv_OnAction(ActionEvent actionEvent) {
    File file = new File(configManager.getExportDir() + "graficos.csv");

    ExportService exportService = ExportService.getInstance();
    if (exportService.exportToCsv(grhList, file)) {
      uiService.showInfo("Exportación CSV Completa", "Índices exportados a:\n" + file.getAbsolutePath());
    } else {
      uiService.showError("Error de Exportación", "No se pudo exportar a CSV.");
    }
  }

  /**
   * Valida la integridad de los datos de GRH.
   */
  @FXML
  private void mnuValidate_OnAction(ActionEvent actionEvent) {
    ValidationService validationService = ValidationService.getInstance();
    ValidationService.ValidationResult result = validationService.validate(grhList, configManager.getGraphicsDir());

    try {
      FXMLLoader loader = new FXMLLoader(
          getClass().getResource("/org/nexus/indexador/ValidationResultController.fxml"));
      javafx.scene.Parent root = loader.load();

      ValidationResultController controller = loader.getController();
      controller.setResults(result);
      controller.setMainController(this);

      Stage stage = new Stage();
      stage.setTitle("Reporte de Integridad - " + result.getTotalIssues() + " incidencias");
      Scene scene = new Scene(root);
      WindowManager.getInstance().applyTheme(scene, configManager.getAppTheme());
      stage.setScene(scene);
      stage.initModality(Modality.NONE);
      Main.setAppIcon(stage);
      stage.show();

    } catch (IOException e) {
      logger.error("Error al abrir ventana de validación", e);
      uiService.showError("Error", "No se pudo abrir el reporte de validación: " + e.getMessage());
    }
  }

  /**
   * Navega hasta el GRH especificado en la lista.
   * 
   * @param grhId ID del gráfico a seleccionar.
   */
  public void jumpToGrh(int grhId) {
    if (grhDataMap.containsKey(grhId)) {
      // Buscar el índice en la lista (puede ser lento para listas gigantes, pero ok
      // para esto)
      for (int i = 0; i < grhList.size(); i++) {
        if (grhList.get(i).getGrh() == grhId) {
          lstIndices.getSelectionModel().select(i);
          lstIndices.scrollTo(i);
          break;
        }
      }
    } else {
      logger.warning("Intento de saltar a GRH inexistente: " + grhId);
    }
  }

  /**
   * Muestra un diálogo de información.
   */
  private void showInfoAlert(String title, String content) {
    uiService.showInfo(title, content);
  }

  private void showErrorAlert(String title, String content) {
    uiService.showError(title, content);
  }

  private void showWarningAlert(String title, String content) {
    uiService.showWarning(title, content);
  }

  /**
   * Cierra la aplicación con confirmación si hay cambios sin guardar.
   */
  @FXML
  private void mnuClose_OnAction() {
    UndoManager undoManager = UndoManager.getInstance();

    if (undoManager.hasUnsavedChanges()) {
      Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
      alert.setTitle("Confirmar cierre");
      alert.setHeaderText("Hay cambios sin guardar");
      alert.setContentText("¿Desea salir sin guardar los cambios?");

      Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
      Main.setAppIcon(stage);

      ButtonType btnGuardar = new ButtonType("Guardar y Salir");
      ButtonType btnSalir = new ButtonType("Salir sin Guardar");
      ButtonType btnCancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);

      alert.getButtonTypes().setAll(btnGuardar, btnSalir, btnCancelar);

      alert.showAndWait().ifPresent(response -> {
        if (response == btnGuardar) {
          try {
            mnuIndexbyMemory();
            undoManager.markSaved();
            Platform.exit();
          } catch (IOException e) {
            logger.error("Error al guardar antes de cerrar", e);
            showErrorAlert("Error", "No se pudo guardar. ¿Desea salir de todos modos?");
          }
        } else if (response == btnSalir) {
          Platform.exit();
        }
        // Si es Cancelar, no hacer nada
      });
    } else {
      Platform.exit();
    }
  }

  /**
   * Abre el diálogo de configuración de rutas.
   */
  @FXML
  private void mnuConfigPaths_OnAction() {
    try {
      FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/nexus/indexador/PathsController.fxml"));
      VBox root = loader.load();

      PathsController controller = loader.getController();

      Stage stage = new Stage();
      Main.setAppIcon(stage);
      stage.setTitle("Configuración de Rutas");
      stage.setScene(new Scene(root));
      stage.initModality(Modality.APPLICATION_MODAL);
      stage.setResizable(false);

      controller.setStage(stage);

      stage.showAndWait();
    } catch (IOException e) {
      logger.error("Error al abrir configuración de rutas", e);
    }
  }

  /**
   * Muestra el diálogo "Acerca de" con información de la aplicación.
   */
  @FXML
  private void mnuAbout_OnAction() {
    Alert about = new Alert(Alert.AlertType.INFORMATION);
    about.setTitle("Acerca de Indexador Nexus");
    about.setHeaderText("Indexador Nexus v1.0.0");
    about.setContentText("Editor de índices para Argentum Online\n\n" + "Características:\n"
        + "• Visualización y edición de GRHs\n" + "• Soporte para animaciones\n"
        + "• Exportación a INI, JSON y CSV\n" + "• Validación de integridad\n"
        + "• Sistema Undo/Redo\n\n" + "Autor: Lorwik (github.com/ManuelJSD)\n"
        + "Licencia: Open Source\n\n" + "Java: " + System.getProperty("java.version") + "\n"
        + "JavaFX: 17.0.13");
    Stage stage = (Stage) about.getDialogPane().getScene().getWindow();
    Main.setAppIcon(stage);
    about.showAndWait();
  }

  /**
   * Guarda los cambios realizados en los datos del gráfico seleccionado en la
   * lista. Obtiene el
   * índice seleccionado de la lista y actualiza los atributos del objeto grhData
   * correspondiente
   * con los valores ingresados en los campos de texto. Si no hay ningún índice
   * seleccionado, no se
   * realizan cambios. Se imprime un mensaje indicando que los cambios se han
   * aplicado con éxito.
   */
  @FXML
  private void saveGrhData() {
    // Obtenemos el índice seleccionado en la lista:
    int selectedIndex = lstIndices.getSelectionModel().getSelectedIndex();

    if (selectedIndex >= 0) {
      GrhData selectedGrh = grhList.get(selectedIndex);

      try {
        editorHelper.saveGrhData(selectedGrh);
        logger.info("Cambios aplicados!");
        ToastNotification.show(WindowManager.getInstance().getWindow("MainController"),
            "Cambios aplicados");
      } catch (NumberFormatException e) {
        uiService.showError("Error de Formato", "Asegúrate de ingresar solo números válidos.");
      }
    }
  }

  /**
   * Obtiene los criterios de filtrado avanzado desde la UI.
   */
  private FilterService.FilterCriteria getFilterCriteria() {
    FilterService.FilterCriteria criteria = new FilterService.FilterCriteria();
    criteria.onlyAnimations = chkAnimations.isSelected();
    criteria.onlyStatics = chkStatics.isSelected();

    try {
      if (txtFilterFileNum != null && !txtFilterFileNum.getText().isEmpty()) {
        criteria.fileNum = Integer.parseInt(txtFilterFileNum.getText().trim());
      }
      if (txtFilterWidth != null && !txtFilterWidth.getText().isEmpty()) {
        criteria.width = Integer.parseInt(txtFilterWidth.getText().trim());
      }
      if (txtFilterHeight != null && !txtFilterHeight.getText().isEmpty()) {
        criteria.height = Integer.parseInt(txtFilterHeight.getText().trim());
      }
    } catch (NumberFormatException e) {
      // Ignorar valores numéricos inválidos
    }
    return criteria;
  }

  @FXML
  private void toggleFilters(MouseEvent event) {
    boolean isVisible = paneFilterContent.isVisible();
    paneFilterContent.setVisible(!isVisible);
    paneFilterContent.setManaged(!isVisible);
    lblFilterToggle.setText(!isVisible ? "▼ Filtros" : "▶ Filtros");
  }

  @FXML
  private void onApplyFilters() {
    applyFilters();
  }

  @FXML
  private void onClearFilters() {
    txtFiltro.clear();
    chkAnimations.setSelected(false);
    chkStatics.setSelected(false);
    applyFilters();
  }

  private void applyFilters() {
    if (grhList == null)
      return;

    FilterService.FilterCriteria criteria = getFilterCriteria();
    String query = txtFiltro.getText();

    ObservableList<String> filteredIndices = FXCollections.observableArrayList();
    for (GrhData grh : grhList) {
      if (filterService.matches(grh, query, criteria)) {
        String entry = grh.getGrh() + (grh.getNumFrames() > 1 ? " (Animación)" : "");
        filteredIndices.add(entry);
      }
    }

    lstIndices.setItems(filteredIndices);
    lblIndices.setText("Indices cargados: " + filteredIndices.size());
  }

  /**
   * Configura un listener para el TextField de filtro para detectar cambios en su
   * contenido.
   */
  /**
   * Configura un listener para el TextField de filtro para detectar cambios en su
   * contenido.
   */
  private void setupFilterTextFieldListener() {
    // Listener de texto (LIVE FILTER)
    txtFiltro.textProperty().addListener((observable, oldValue, newValue) -> {
      applyFilters();
    });

    // Listener de ENTER (FIND NEXT) - Opcional, mantengo filterIndices si se quiere
    // buscar
    txtFiltro.setOnAction(event -> {
      filterIndices(txtFiltro.getText(), true);
    });
  }

  /**
   * Filtra o busca índices en el ListView según el texto proporcionado. Soporta
   * sintaxis avanzada:
   * - f:123 -> Buscar FileNum 123 - w:32 -> Buscar ancho 32 - h:32 -> Buscar alto
   * 32 - 123 ->
   * Buscar GRH ID 123
   *
   * @param filterText El texto utilizado para filtrar.
   * @param findNext   Si es true, busca la siguiente coincidencia desde la
   *                   posición actual.
   */
  private void filterIndices(String filterText, boolean findNext) {
    FilterService.FilterCriteria criteria = getFilterCriteria();

    // Si no hay texto ni filtros avanzados, limpiar selección
    if (filterText.isEmpty() && !criteria.onlyAnimations && !criteria.onlyStatics
        && criteria.fileNum == null && criteria.width == null && criteria.height == null) {
      // lstIndices.getSelectionModel().clearSelection();
      return;
    }

    try {
      int startIndex = 0;
      if (findNext) {
        startIndex = lstIndices.getSelectionModel().getSelectedIndex() + 1;
        if (startIndex >= grhList.size()) {
          startIndex = 0; // Wrap around
        }
      }

      // Buscar coincidencia utilizando el FilterService
      for (int i = startIndex; i < grhList.size(); i++) {
        if (filterService.matches(grhList.get(i), filterText, criteria)) {
          lstIndices.getSelectionModel().select(i);
          lstIndices.scrollTo(i);
          return;
        }
      }

      // Si buscamos siguiente y no encontramos, probamos desde el principio (wrap
      // total)
      if (findNext && startIndex > 0) {
        for (int i = 0; i < startIndex; i++) {
          if (filterService.matches(grhList.get(i), filterText, criteria)) {
            lstIndices.getSelectionModel().select(i);
            lstIndices.scrollTo(i);
            return;
          }
        }
      }

    } catch (Exception e) {
      logger.error("Error al filtrar índices", e);
    }
  }

  /**
   * Configura el deslizador de zoom. Este método configura un listener para el
   * deslizador de zoom,
   * que ajusta la escala del ImageView según el valor del deslizador.
   */
  private void setupSliderZoom() {
    // Listener para el slider
    sldZoom.valueProperty().addListener((observable, oldValue, newValue) -> {
      double zoomValue = newValue.doubleValue();
      // Aplica la escala al ImageView
      imgIndice.setScaleX(zoomValue);
      imgIndice.setScaleY(zoomValue);
    });

    // Zoom con rueda del mouse
    imgIndice.setOnScroll(event -> {
      double delta = event.getDeltaY();
      double currentZoom = sldZoom.getValue();
      double zoomStep = 0.1;

      if (delta > 0) {
        // Zoom in
        currentZoom = Math.min(sldZoom.getMax(), currentZoom + zoomStep);
      } else {
        // Zoom out
        currentZoom = Math.max(sldZoom.getMin(), currentZoom - zoomStep);
      }

      sldZoom.setValue(currentZoom);
      event.consume();
    });
  }

  /**
   * Elimina el elemento seleccionado de la lista de índices. Muestra un mensaje
   * de confirmación
   * antes de eliminar el elemento.
   */
  @FXML
  private void btnDelete_OnAction() {
    int selectedIndex = lstIndices.getSelectionModel().getSelectedIndex();

    if (selectedIndex != -1) {
      Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
      alert.setTitle("Confirmación");
      alert.setHeaderText("¿Estás seguro de que quieres eliminar este elemento?");
      alert.setContentText("Esta acción no se puede deshacer.");

      Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
      Main.setAppIcon(stage);

      Optional<ButtonType> result = alert.showAndWait();
      if (result.isPresent() && result.get() == ButtonType.OK) {
        lstIndices.getItems().remove(selectedIndex);
        grhList.remove(selectedIndex);
      }
    }
  }

  /**
   * Método que se activa al hacer clic en el botón "Añadir". Incrementa el
   * contador de gráficos
   * (grhCount) en el grhDataManager, crea un nuevo objeto grhData con valores
   * predeterminados y lo
   * agrega tanto al ListView como al grhList.
   *
   * @throws IllegalArgumentException Si ocurre algún error al obtener el contador
   *                                  de gráficos del
   *                                  grhDataManager.
   */
  @FXML
  private void btnAdd_OnAction() {
    int grhCount = dataManager.getGrhCount() + 1;

    // Incrementar el contador de grhDataManager
    dataManager.setGrhCount(grhCount);

    // Crear un nuevo objeto grhData con los valores adecuados
    GrhData newGrhData = new GrhData(grhCount, (short) 1, 0, (short) 0, (short) 0, (short) 0, (short) 0);

    // Agregar el nuevo elemento al ListView
    lstIndices.getItems().add(String.valueOf(grhCount));

    // Agregar el nuevo elemento al grhList
    grhList.add(newGrhData);
  }

  @FXML
  public void mnuIndexMemIndices_OnAction(ActionEvent actionEvent) {
    runAsyncTask(() -> {
      try {
        dataManager.indexFromMemory("GRHS");
      } catch (IOException e) {
        logger.error("Error al indexar desde memoria", e);
      }
    }, "Indexando desde Memoria...", "Indexado completado");
  }

  @FXML
  public void mnuIndexMemHeads_OnAction(ActionEvent actionEvent) {
    runAsyncTask(() -> {
      try {
        dataManager.indexFromMemory("HEADS");
      } catch (IOException e) {
        logger.error("Error al indexar cabezas desde memoria", e);
      }
    }, "Indexando Cabezas...", "Cabezas indexadas correctamente");
  }

  @FXML
  public void mnuIndexMemHelmets_OnAction(ActionEvent actionEvent) {
    runAsyncTask(() -> {
      try {
        dataManager.indexFromMemory("HELMETS");
      } catch (IOException e) {
        logger.error("Error al indexar cascos desde memoria", e);
      }
    }, "Indexando Cascos...", "Cascos indexados correctamente");
  }

  @FXML
  public void mnuIndexMemBodies_OnAction(ActionEvent actionEvent) {
    runAsyncTask(() -> {
      try {
        dataManager.indexFromMemory("BODIES");
      } catch (IOException e) {
        logger.error("Error al indexar cuerpos desde memoria", e);
      }
    }, "Indexando Cuerpos...", "Cuerpos indexados correctamente");
  }

  @FXML
  public void mnuIndexMemShields_OnAction(ActionEvent actionEvent) {
    runAsyncTask(() -> {
      try {
        dataManager.indexFromMemory("SHIELDS");
      } catch (IOException e) {
        logger.error("Error al indexar escudos desde memoria", e);
      }
    }, "Indexando Escudos...", "Escudos indexados correctamente");
  }

  @FXML
  public void mnuIndexMemWeapons_OnAction(ActionEvent actionEvent) {
    runAsyncTask(() -> {
      try {
        dataManager.indexFromMemory("WEAPONS");
      } catch (IOException e) {
        logger.error("Error al indexar armas desde memoria", e);
      }
    }, "Indexando Armas...", "Armas indexadas correctamente");
  }

  @FXML
  public void mnuIndexMemFXs_OnAction(ActionEvent actionEvent) {
    runAsyncTask(() -> {
      try {
        dataManager.indexFromMemory("FXS");
      } catch (IOException e) {
        logger.error("Error al indexar FXs desde memoria", e);
      }
    }, "Indexando FXs...", "FXs indexados correctamente");
  }

  @FXML
  public void mnuIndexMemAll_OnAction(ActionEvent actionEvent) {
    runAsyncTask(() -> {
      try {
        dataManager.indexAllFromMemory();
      } catch (Exception e) {
        logger.error("Error al indexar todo desde memoria", e);
      }
    }, "Indexando TODO desde Memoria...", "Todo indexado correctamente");
  }

  @FXML
  public void mnuIndexExpIndices_OnAction(ActionEvent actionEvent) {
    runAsyncTask(() -> {
      try {
        dataManager.indexFromExported("GRHS");
        Platform.runLater(this::loadGrh);
      } catch (IOException e) {
        logger.error("Error al indexar desde exportados", e);
      }
    }, "Indexando desde Exportados...", "Indexado completado");
  }

  @FXML
  public void mnuIndexExpHeads_OnAction(ActionEvent actionEvent) {
    runAsyncTask(() -> {
      try {
        dataManager.indexFromExported("HEADS");
      } catch (IOException e) {
        logger.error("Error al indexar cabezas desde exportados", e);
      }
    }, "Indexando Cabezas...", "Cabezas indexadas correctamente");
  }

  @FXML
  public void mnuIndexExpHelmets_OnAction(ActionEvent actionEvent) {
    runAsyncTask(() -> {
      try {
        dataManager.indexFromExported("HELMETS");
      } catch (IOException e) {
        logger.error("Error al indexar cascos desde exportados", e);
      }
    }, "Indexando Cascos...", "Cascos indexados correctamente");
  }

  @FXML
  public void mnuIndexExpBodies_OnAction(ActionEvent actionEvent) {
    runAsyncTask(() -> {
      try {
        dataManager.indexFromExported("BODIES");
      } catch (IOException e) {
        logger.error("Error al indexar cuerpos desde exportados", e);
      }
    }, "Indexando Cuerpos...", "Cuerpos indexados correctamente");
  }

  @FXML
  public void mnuIndexExpShields_OnAction(ActionEvent actionEvent) {
    runAsyncTask(() -> {
      try {
        dataManager.indexFromExported("SHIELDS");
      } catch (IOException e) {
        logger.error("Error al indexar escudos desde exportados", e);
      }
    }, "Indexando Escudos...", "Escudos indexados correctamente");
  }

  @FXML
  public void mnuIndexExpWeapons_OnAction(ActionEvent actionEvent) {
    runAsyncTask(() -> {
      try {
        dataManager.indexFromExported("WEAPONS");
      } catch (IOException e) {
        logger.error("Error al indexar armas desde exportados", e);
      }
    }, "Indexando Armas...", "Armas indexadas correctamente");
  }

  @FXML
  public void mnuIndexExpFXs_OnAction(ActionEvent actionEvent) {
    runAsyncTask(() -> {
      try {
        dataManager.indexFromExported("FXS");
      } catch (IOException e) {
        logger.error("Error al indexar FXs desde exportados", e);
      }
    }, "Indexando FXs...", "FXs indexados correctamente");
  }

  @FXML
  public void mnuIndexExpAll_OnAction(ActionEvent actionEvent) {
    runAsyncTask(() -> {
      try {
        dataManager.indexAllFromExported();
        Platform.runLater(this::loadGrh);
      } catch (Exception e) {
        logger.error("Error al indexar todo desde exportados", e);
      }
    }, "Indexando TODO desde Exportados...", "Todo indexado correctamente");
  }

  private void handleReload(String type, String startMsg, String endMsg, boolean isGrh) {
    uiService.runAsyncTask(() -> {
      try {
        dataManager.reloadResources(type);
        if (isGrh) {
          Platform.runLater(this::loadGrh);
        }
      } catch (Exception e) {
        logger.error("Error al recargar " + type, e);
      }
    }, startMsg, endMsg);
  }

  @FXML
  public void mnuReloadGrhs_OnAction(ActionEvent actionEvent) {
    handleReload("GRHS", "Recargando Índices...", "Índices recargados correctamente", true);
  }

  @FXML
  public void mnuReloadHeads_OnAction(ActionEvent actionEvent) {
    handleReload("HEADS", "Recargando Cabezas...", "Cabezas recargadas correctamente", false);
  }

  @FXML
  public void mnuReloadHelmets_OnAction(ActionEvent actionEvent) {
    handleReload("HELMETS", "Recargando Cascos...", "Cascos recargados correctamente", false);
  }

  @FXML
  public void mnuReloadBodies_OnAction(ActionEvent actionEvent) {
    handleReload("BODIES", "Recargando Cuerpos...", "Cuerpos recargados correctamente", false);
  }

  @FXML
  public void mnuReloadShields_OnAction(ActionEvent actionEvent) {
    handleReload("SHIELDS", "Recargando Escudos...", "Escudos recargados correctamente", false);
  }

  @FXML
  public void mnuReloadWeapons_OnAction(ActionEvent actionEvent) {
    handleReload("WEAPONS", "Recargando Armas...", "Armas recargadas correctamente", false);
  }

  @FXML
  public void mnuReloadFXs_OnAction(ActionEvent actionEvent) {
    handleReload("FXS", "Recargando FXs...", "FXs recargados correctamente", false);
  }

  @FXML
  public void mnuReloadAll_OnAction(ActionEvent actionEvent) {
    handleReload("ALL", "Recargando TODO...", "Todos los recursos recargados correctamente", true);
  }

  private void handleExport(String type, String startMsg, String endMsg) {
    uiService.runAsyncTask(() -> {
      try {
        dataManager.exportToText(type);
      } catch (IOException e) {
        logger.error("Error al exportar " + type, e);
      }
    }, startMsg, endMsg);
  }

  @FXML
  public void mnuExportGrh_OnAction(ActionEvent actionEvent) {
    handleExport("GRHS", "Exportando Indices...", "Indices exportados correctamente");
  }

  @FXML
  public void mnuExportHead_OnAction(ActionEvent actionEvent) {
    handleExport("HEADS", "Exportando Cabezas...", "Cabezas exportadas correctamente");
  }

  @FXML
  public void mnuExportHelmet_OnAction(ActionEvent actionEvent) {
    handleExport("HELMETS", "Exportando Cascos...", "Cascos exportados correctamente");
  }

  @FXML
  public void mnuExportBody_OnAction(ActionEvent actionEvent) {
    handleExport("BODIES", "Exportando Cuerpos...", "Cuerpos exportadas correctamente");
  }

  @FXML
  public void mnuExportShield_OnAction(ActionEvent actionEvent) {
    handleExport("SHIELDS", "Exportando Escudos...", "Escudos exportados correctamente");
  }

  @FXML
  public void mnuExportFX_OnAction(ActionEvent actionEvent) {
    handleExport("FXS", "Exportando FXs...", "FXs exportados correctamente");
  }

  @FXML
  public void mnuExportWeapon_OnAction(ActionEvent actionEvent) {
    handleExport("WEAPONS", "Exportando Armas...", "Armas exportadas correctamente");
  }

  @FXML
  public void mnuExportAll_OnAction(ActionEvent actionEvent) {
    handleExport("ALL", "Exportando TODO...", "Todo exportado correctamente");
  }

  @FXML
  public void mnuExportJson_OnAction(ActionEvent actionEvent) {
    logger.info("Exportación persistente a JSON no implementada aún.");
  }

  @FXML
  public void mnuUndo_OnAction(ActionEvent actionEvent) {
    UndoManager.getInstance().undo();
    updateEditorFromCurrentSelection();
    updateStatusBar("Deshacer completado", grhList.size(), getAnimCount(), true);
  }

  @FXML
  public void mnuRedo_OnAction(ActionEvent actionEvent) {
    UndoManager.getInstance().redo();
    updateEditorFromCurrentSelection();
    updateStatusBar("Rehacer completado", grhList.size(), getAnimCount(), true);
  }

  @FXML
  public void mnuCopy_OnAction(ActionEvent actionEvent) {
    int index = lstIndices.getSelectionModel().getSelectedIndex();
    if (index >= 0) {
      copiedGrh = grhList.get(index);
      logger.info("Grh " + copiedGrh.getGrh() + " copiado al portapapeles.");
    }
  }

  @FXML
  public void mnuPaste_OnAction(ActionEvent actionEvent) {
    if (copiedGrh == null)
      return;
    int index = lstIndices.getSelectionModel().getSelectedIndex();
    if (index >= 0) {
      GrhData target = grhList.get(index);
      target.setFileNum(copiedGrh.getFileNum());
      target.setsX(copiedGrh.getsX());
      target.setsY(copiedGrh.getsY());
      target.setTileWidth(copiedGrh.getTileWidth());
      target.setTileHeight(copiedGrh.getTileHeight());
      updateEditor(target);
      logger.info("Propiedades pegadas en Grh " + target.getGrh());
    }
  }

  @FXML
  public void mnuDuplicate_OnAction(ActionEvent actionEvent) {
    int index = lstIndices.getSelectionModel().getSelectedIndex();
    if (index >= 0) {
      GrhData source = grhList.get(index);
      int newId = dataManager.getGrhCount() + 1;
      GrhData newGrh = new GrhData(newId, source.getNumFrames(), source.getFileNum(),
          source.getsX(), source.getsY(), source.getTileWidth(), source.getTileHeight());
      if (source.getNumFrames() > 1) {
        newGrh.setFrames(source.getFrames().clone());
        newGrh.setSpeed(source.getSpeed());
      }
      grhList.add(newGrh);
      grhDataMap.put(newId, newGrh);
      dataManager.setGrhCount(newId);
      lstIndices.getItems()
          .add(String.valueOf(newId) + (newGrh.getNumFrames() > 1 ? " (Animación)" : ""));
      lstIndices.getSelectionModel().selectLast();
      lstIndices.scrollTo(lstIndices.getItems().size() - 1);
      logger.info("Grh duplicado con ID: " + newId);
    }
  }

  @FXML
  public void mnuHead_OnAction(ActionEvent actionEvent) {
    windowManager.showWindow("HeadsController", "Cabezas", false);
  }

  @FXML
  public void mnuHelmet_OnAction(ActionEvent actionEvent) {
    windowManager.showWindow("HelmetsController", "Cascos", false);
  }

  @FXML
  public void mnuBody_OnAction(ActionEvent actionEvent) {
    windowManager.showWindow("BodiesController", "Cuerpos", false);
  }

  @FXML
  public void mnuShield_OnAction(ActionEvent actionEvent) {
    windowManager.showWindow("ShieldsController", "Escudos", false);
  }

  @FXML
  public void mnuConsola_OnAction(ActionEvent actionEvent) {
    windowManager.showWindow("ConsoleController", "Consola de Depuración", false);
  }

  @FXML
  public void mnuIndexbyMemory() throws IOException {
    dataManager.indexFromMemory("GRHS");
    logger.info("Indices guardados desde memoria correctamente.");
    UndoManager.getInstance().markSaved();
    updateStatusBar("Guardado en memoria", grhList.size(), getAnimCount(), false);
  }

  @FXML
  public void btnAdd_OnAction(ActionEvent actionEvent) {
    int newId = dataManager.getGrhCount() + 1;
    GrhData newGrh = new GrhData();
    newGrh.setGrh(newId);
    newGrh.setFileNum(0);
    newGrh.setNumFrames((short) 1);

    grhList.add(newGrh);
    grhDataMap.put(newId, newGrh);
    dataManager.setGrhCount(newId);

    lstIndices.getItems().add(String.valueOf(newId));
    lstIndices.getSelectionModel().selectLast();
    lstIndices.scrollTo(lstIndices.getItems().size() - 1);

    logger.info("Nuevo Grh creado con ID: " + newId);
    updateStatusBar("Nuevo Grh añadido", grhList.size(), getAnimCount(), true);
  }

  private void runAsyncTask(Runnable task, String startMsg, String endMsg) {
    uiService.runAsyncTask(task, startMsg, endMsg);
  }

  private void updateEditorFromCurrentSelection() {
    int index = lstIndices.getSelectionModel().getSelectedIndex();
    if (index >= 0) {
      updateEditor(grhList.get(index));
    }
  }

  private int getAnimCount() {
    int count = 0;
    if (grhList == null)
      return 0;
    for (GrhData grh : grhList) {
      if (grh.getNumFrames() > 1)
        count++;
    }
    return count;
  }

  @FXML
  public void mnuFXs_OnAction(ActionEvent actionEvent) {
    windowManager.showWindow("FxsController", "FXs", false);
  }

  @FXML
  public void mnuWeapon_OnAction(ActionEvent actionEvent) {
    windowManager.showWindow("WeaponsController", "Armas", false);
  }

  @FXML
  private void btnAddFrame_OnAction() {

    TextInputDialog dialog = new TextInputDialog();
    dialog.setTitle("Introduce el numero del indice");
    dialog.setHeaderText("Por favor, introduce un Grh:");
    dialog.setContentText("Grh:");

    Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
    Main.setAppIcon(stage);

    Optional<String> result = dialog.showAndWait();
    if (result.isPresent()) {
      try {
        int numero = Integer.parseInt(result.get());

        // Obtenemos el índice seleccionado en la lista de indices:
        int selectedIndex = lstIndices.getSelectionModel().getSelectedIndex();

        if (selectedIndex >= 0) {

          // Solo podemos añadir los indices estáticos
          if (grhList.get(numero).getNumFrames() == 1) {

            grhList.get(selectedIndex)
                .setNumFrames((short) (grhList.get(selectedIndex).getNumFrames() + 1));

            int[] frames = grhList.get(selectedIndex).getFrames();

            int[] newFrames = Arrays.copyOf(frames, frames.length + 1);
            newFrames[frames.length] = numero;

            // Establecer el nuevo array utilizando el método setFrames(), si está
            // disponible
            grhList.get(selectedIndex).setFrames(newFrames);

            updateEditor(grhList.get(selectedIndex));

          } else {
            logger.warning("El indice seleccionado no es valido.");
          }

        } else {
          logger.warning("Indice invalido. Solo se aceptan indices desde el 1 hasta el "
              + dataManager.getGrhCount());
        }

      } catch (NumberFormatException e) {
        logger.warning("Error: Entrada inválida. Introduce un número válido.");
      }

    } else {
      logger.info("Operación cancelada.");
    }

  }

  @FXML
  private void btnRemoveFrame_OnAction() {
    // Obtenemos el índice del frame seleccionado en la lista lstFrames
    int selectedFrameIndex = lstFrames.getSelectionModel().getSelectedIndex() + 1;

    // Verificamos si se ha seleccionado un frame
    if (selectedFrameIndex != -1) {
      // Creamos un diálogo de confirmación
      Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
      alert.setTitle("Confirmación");
      alert.setHeaderText("¿Estás seguro de que quieres eliminar este elemento?");
      alert.setContentText("Esta acción no se puede deshacer.");

      Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
      Main.setAppIcon(stage);

      // Mostramos el diálogo y esperamos la respuesta del usuario
      Optional<ButtonType> result = alert.showAndWait();

      // Verificamos si el usuario ha confirmado la eliminación
      if (result.isPresent() && result.get() == ButtonType.OK) {
        // Obtenemos el índice seleccionado en la lista de índices
        int selectedIndex = lstIndices.getSelectionModel().getSelectedIndex();

        // Verificamos si se ha seleccionado un índice
        if (selectedIndex >= 0) {
          // Obtenemos el objeto grhData seleccionado en la lista de índices
          GrhData selectedGrh = grhList.get(selectedIndex);

          // Obtenemos los frames actuales del objeto grhData
          int[] frames = selectedGrh.getFrames();

          // Creamos un nuevo array para almacenar los frames sin el frame seleccionado
          int[] newFrames = new int[frames.length - 1];
          int newIndex = 0;

          // Copiamos los frames al nuevo array, omitiendo el frame seleccionado
          for (int i = 0; i < frames.length; i++) {
            if (i != selectedFrameIndex) {
              newFrames[newIndex] = frames[i];
              newIndex++;
            }
          }

          // Actualizamos el array de frames del objeto grhData
          selectedGrh.setFrames(newFrames);

          // Disminuimos el número de frames en el objeto grhData
          selectedGrh.setNumFrames((short) (selectedGrh.getNumFrames() - 1));

          // Actualizamos el editor con el objeto grhData modificado
          updateEditor(selectedGrh);
        } else {
          logger.warning("No se ha seleccionado ningún grhData.");
        }
      }
    } else {
      logger.warning("No se ha seleccionado ningún frame.");
    }
  }

  @FXML
  public void mnuBuscarGrhLibres_OnAction(ActionEvent actionEvent) {
    TextInputDialog dialog = new TextInputDialog();
    dialog.setTitle("Buscar Grh libres");
    dialog.setHeaderText("Por favor, introduce cuantos Grh libres necesitas:");

    Optional<String> result = dialog.showAndWait();

    result.ifPresent(value -> {
      try {
        int numGrhLibres = Integer.parseInt(value);

        if (numGrhLibres < 1) {
          Alert alert = new Alert(Alert.AlertType.ERROR);
          alert.setTitle("Error");
          alert.setHeaderText("Número inválido");
          alert.setContentText("Por favor, introduce un número mayor o igual a 1.");
          alert.showAndWait();
          return;
        }

        int grhLibres = buscarGrhLibres(numGrhLibres);

        if (grhLibres == 0) {
          Alert alert = new Alert(Alert.AlertType.INFORMATION);
          alert.setTitle("No se encontraron Grh libres");
          alert.setHeaderText(null);
          alert.setContentText("No se encontraron secuencias de " + grhLibres + " Grh libres.");
          alert.showAndWait();
        } else {
          StringBuilder mensaje = new StringBuilder("Se encontraron secuencias de Grh libres desde Grh"
              + (grhLibres - (numGrhLibres - 1)) + " hasta Grh" + grhLibres);
          Alert alert = new Alert(Alert.AlertType.INFORMATION);
          alert.setTitle("Grh libres encontrados");
          alert.setHeaderText(null);
          alert.setContentText(mensaje.toString());
          alert.showAndWait();
        }
      } catch (NumberFormatException e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Entrada inválida");
        alert.setContentText("Por favor, introduce un número válido.");
        alert.showAndWait();
      }
    });
  }

  private int buscarGrhLibres(int numGrhLibres) {
    int contador = 0;

    // Buscar secuencias de Grh libres en grhList
    for (int i = 1; i < dataManager.getGrhCount(); i++) {
      GrhData currentGrh = grhDataMap.get(i);

      if (currentGrh == null) { // Determina si el Grh está libre
        contador++;

        if (contador == numGrhLibres) {

          return i;

        }
      } else {
        contador = 0;
      }
    }

    return 0;

  }

  /**
   * Maneja el evento de clic en el menú "Adaptador de Grh". Abre una nueva
   * ventana que permite
   * adaptar gráficos.
   */
  @FXML
  public void mnuGrhAdapter_OnAction(ActionEvent actionEvent) {
    windowManager.showWindow("AdapterController", "Adaptador de Grh", false);
  }

  /**
   * Abre el Indexador Visual Manual (Slicer).
   */
  @FXML
  public void mnuManualIndexer_OnAction(ActionEvent actionEvent) {
    // Usamos path relativo/absoluto según funcione el WindowManager
    // Si WindowManager usa Main.class.getResource(name + ".fxml"), y name empieza
    // con /, busca en root.
    // Probemos con el path completo por si acaso, o solo el nombre si están en el
    // mismo paquete.
    // Dado que AdapterController usa solo el nombre, asumimos que están en el mismo
    // paquete.
    windowManager.showWindow("ManualIndexerController", "Indexador Visual", true);
  }

  /**
   * Abre el asistente visual de Auto-Indexación.
   */
  @FXML
  private void mnuAutoIndexWizard_OnAction() {
    try {
      javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
          getClass().getResource("/org/nexus/indexador/AutoIndexWizardController.fxml"));
      javafx.scene.Parent root = loader.load();

      AutoIndexWizardController wizardController = loader.getController();
      wizardController.setMainController(this);

      javafx.stage.Stage wizardStage = new javafx.stage.Stage();
      wizardStage.setTitle("Auto-Indexar");
      wizardStage.setScene(new javafx.scene.Scene(root));
      wizardStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
      wizardStage.setResizable(false);

      wizardController.setStage(wizardStage);

      // Aplicar estilos según tema
      if ("DARK".equalsIgnoreCase(configManager.getAppTheme())) {
        wizardStage.getScene().getStylesheets().add(
            getClass().getResource("/org/nexus/indexador/styles/dark-theme.css").toExternalForm());
      } else {
        wizardStage.getScene().getStylesheets().add(
            getClass().getResource("/org/nexus/indexador/styles/light-theme.css").toExternalForm());
      }

      wizardStage.showAndWait();
    } catch (Exception e) {
      logger.error("Error al abrir asistente de indexación", e);
      showErrorAlert("Error", "No se pudo abrir el asistente: " + e.getMessage());
    }
  }

  // ========== AUTO-INDEXAR: MODOS ==========

  // Public wrapper methods for Wizard
  public void autoIndexPersonaje(String namePrefix, int startFileNum) {
    // TODO: Refactor underlying logic to accept parameters.
    // For now, we will just call the existing method, but we need to pass these
    // params down.
    // Since existing methods use TextInputDialog inside 'loadAndDetectSprites', we
    // need to refactor that first.
    runAutoIndexLogic("Personaje", namePrefix, startFileNum, 1, 1);
  }

  public void autoIndexObjetos(String namePrefix, int startFileNum) {
    runAutoIndexLogic("Objeto", namePrefix, startFileNum, 1, 1);
  }

  public void autoIndexSuperficies(String namePrefix, int startFileNum) {
    autoIndexSuperficies(namePrefix, startFileNum, 1, 1);
  }

  public void autoIndexSuperficies(String namePrefix, int startFileNum, int cols, int rows) {
    runAutoIndexLogic("Superficie", namePrefix, startFileNum, cols, rows);
  }

  public void autoIndexAnimacion(String namePrefix, int startFileNum) {
    runAutoIndexLogic("Animacion", namePrefix, startFileNum, 1, 1);
  }

  private void runAutoIndexLogic(String type, String namePrefix, int fileNum, int atlasCols, int atlasRows) {
    // 1. If fileNum is -1, ask user? Or did wizard handle it?
    // Wizard handles it being optional (-1).

    if (fileNum <= 0) {
      // Fallback to dialog if not provided? Or just error?
      // For UX, wizard should have encouraged input.
      // Let's assume user wants to pick file via FileChooser or just type a number.
      TextInputDialog dialog = new TextInputDialog();
      dialog.setTitle("Número de Archivo");
      dialog.setContentText("Introduce el número de imagen inicial:");
      Optional<String> res = dialog.showAndWait();
      if (res.isPresent()) {
        try {
          fileNum = Integer.parseInt(res.get());
        } catch (Exception e) {
          return;
        }
      }
    }

    // Smart Auto-Index
    // Default tolerances
    int tolX = 2;
    int tolY = 16;

    // Disable Grid Split for Bodies (Personajes) to prevent cutting tall frames
    boolean enableGridSplit = type != null
        && (type.toLowerCase().startsWith("anim") || type.toLowerCase().startsWith("fx"));

    // Specific Tolerances and Settings based on Type
    if ("Personaje".equalsIgnoreCase(type)) {
      // Strict vertical tolerance to prevent row merging
      tolX = 0;
      tolY = 0;
      enableGridSplit = false;
    } else if (type != null && type.toLowerCase().startsWith("anim")) {
      tolX = 0;
      tolY = 10;
    }

    ImageDetectionResult result = loadAndDetectSpritesDirect(fileNum, tolX, tolY, enableGridSplit);
    if (result != null) {
      // Si es superficies, aplicar split ANTES de la preview para que el usuario vea
      // la rejilla
      if (type != null && type.toLowerCase().startsWith("superficie")) {
        // Obtenemos el PixelReader de la imagen actual cargada para filtrar tiles
        // vacíos
        PixelReader reader = null;
        try {
          String imagePath = configManager.getGraphicsDir() + fileNum + ".png";
          if (!new java.io.File(imagePath).exists()) {
            imagePath = configManager.getGraphicsDir() + fileNum + ".bmp";
          }
          Image img = imageCache.getImage(imagePath);
          if (img != null) {
            reader = img.getPixelReader();
          }
        } catch (Exception e) {
          logger.error("Error loading image for tiling check", e);
        }

        result.regions = org.nexus.indexador.utils.AutoTilingService.getInstance()
            .splitRegions(result.regions, 32, 32, atlasCols, atlasRows, reader);
      }

      // Normalization check for Animations and Bodies
      if ("Personaje".equalsIgnoreCase(type) || (type != null && type.toLowerCase().startsWith("anim"))) {
        int imgW = (int) result.image.getWidth();
        int imgH = (int) result.image.getHeight();
        result.regions = org.nexus.indexador.utils.AutoTilingService.getInstance().normalizeRegions(result.regions,
            imgW, imgH);
      }

      boolean confirmed = showDetectionPreview(result, "Auto-Indexar " + type,
          "Se han detectado " + result.regions.size() + " sprites/cuadros.\n¿Deseas importarlos?");

      if (confirmed) {
        if ("Personaje".equalsIgnoreCase(type)) {
          // Lógica para personajes: Agrupar por filas (direcciones) y crear animación por
          // fila
          List<List<Rectangle>> rows = groupSpritesByRows(result.regions);
          List<Rectangle> sortedRegions = new ArrayList<>();
          for (List<Rectangle> r : rows)
            sortedRegions.addAll(r);

          List<Integer> staticIds = createStaticGrhs(sortedRegions, fileNum);
          int animCount = 0;
          int offset = 0;
          for (List<Rectangle> row : rows) {
            if (!row.isEmpty()) {
              createSingleAnimation(staticIds.subList(offset, offset + row.size()), fileNum);
              animCount++;
            }
            offset += row.size();
          }
          org.nexus.indexador.utils.ToastNotification.show(txtIndice.getScene().getWindow(),
              "¡Éxito! " + staticIds.size() + " frames y " + animCount + " animaciones.");

        } else if (type != null && type.toLowerCase().startsWith("anim")) {
          // Lógica para animación simple: Todo es una secuencia
          List<Integer> staticIds = createStaticGrhs(result.regions, fileNum);
          createSingleAnimation(staticIds, fileNum);
          org.nexus.indexador.utils.ToastNotification.show(txtIndice.getScene().getWindow(),
              "¡Éxito! " + staticIds.size() + " frames y 1 animación.");

        } else {
          // Objetos / Superficies (Solo estáticos)
          List<Integer> createdIds;

          if (type != null && type.toLowerCase().startsWith("superficie")) {
            // El split ya se realizó antes de la preview (línea anrriba)
            // por lo tanto result.regions ya tiene los tiles correctos.
            // No volver a splitear o causaremos recursión de división (32px -> 10px).
            createdIds = createStaticGrhs(result.regions, fileNum);

            org.nexus.indexador.utils.ToastNotification.show(txtIndice.getScene().getWindow(),
                "¡Éxito! Se crearon (Grid 32x32) " + createdIds.size() + " índices.");
          } else {
            createdIds = createStaticGrhs(result.regions, fileNum);
            org.nexus.indexador.utils.ToastNotification.show(txtIndice.getScene().getWindow(),
                "¡Éxito! Se crearon " + createdIds.size() + " índices.");
          }
        }
      }
    }
  }

  private ImageDetectionResult loadAndDetectSpritesDirect(int fileNum, int tolX, int tolY, boolean enableGridSplit) {
    String imagePath = configManager.getGraphicsDir() + File.separator + fileNum + ".png";

    File f = new File(imagePath);
    if (!f.exists()) {
      imagePath = configManager.getGraphicsDir() + File.separator + fileNum + ".bmp";
      f = new File(imagePath);
      if (!f.exists()) {
        showErrorAlert("Error", "No se encontró la imagen: " + fileNum);
        return null;
      }
    }

    Image image = imageCache.getImage(imagePath);
    if (image == null) {
      showErrorAlert("Error", "No se pudo cargar la imagen.");
      return null;
    }

    List<Rectangle> regions = AutoTilingService.getInstance().detectSprites(image, tolX, tolY, enableGridSplit);
    // Log for debug but also return
    logger.info("Se detectaron " + regions.size() + " sprites.");
    return new ImageDetectionResult(fileNum, image, regions);
  }

  /**
   * Carga una imagen y detecta sprites usando FloodFill. Devuelve null si hay
   * error.
   */
  private ImageDetectionResult loadAndDetectSprites(String title) {
    TextInputDialog dialog = new TextInputDialog();
    dialog.setTitle(title);
    dialog.setHeaderText("Introduce el número de archivo de imagen (FileNum):");
    dialog.setContentText("Imagen:");

    Optional<String> result = dialog.showAndWait();
    if (!result.isPresent())
      return null;

    try {
      int fileNum = Integer.parseInt(result.get());
      String imagePath = configManager.getGraphicsDir() + File.separator + fileNum + ".png";

      File f = new File(imagePath);
      if (!f.exists()) {
        imagePath = configManager.getGraphicsDir() + File.separator + fileNum + ".bmp";
        f = new File(imagePath);
        if (!f.exists()) {
          showErrorAlert("Error", "No se encontró la imagen: " + fileNum);
          return null;
        }
      }

      Image image = imageCache.getImage(imagePath);
      if (image == null) {
        showErrorAlert("Error", "No se pudo cargar la imagen.");
        return null;
      }

      List<Rectangle> regions = AutoTilingService.getInstance().detectSprites(image);
      return new ImageDetectionResult(fileNum, image, regions);

    } catch (NumberFormatException e) {
      showErrorAlert("Error", "Número de archivo inválido.");
      return null;
    }
  }

  /** Resultado de detección de imagen */
  private static class ImageDetectionResult {
    int fileNum;
    Image image;
    List<Rectangle> regions;

    ImageDetectionResult(int fileNum, Image image, List<Rectangle> regions) {
      this.fileNum = fileNum;
      this.image = image;
      this.regions = regions;
    }
  }

  /**
   * Muestra la ventana de previsualización de detección. Retorna true si el
   * usuario confirma, false
   * si cancela.
   */
  private boolean showDetectionPreview(ImageDetectionResult result, String title,
      String modeDescription) {
    try {
      javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
          getClass().getResource("/org/nexus/indexador/DetectionPreviewController.fxml"));
      javafx.scene.Parent root = loader.load();

      DetectionPreviewController previewController = loader.getController();
      previewController.initialize(result.image, result.regions, title, modeDescription);

      javafx.stage.Stage previewStage = new javafx.stage.Stage();
      previewStage.setTitle("Vista Previa de Detección");
      previewStage.setScene(new javafx.scene.Scene(root));
      previewStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);

      previewController.setStage(previewStage);

      // Aplicar estilos
      previewStage.getScene().getStylesheets().add(
          getClass().getResource("/org/nexus/indexador/styles/dark-theme.css").toExternalForm());

      previewStage.showAndWait();

      return previewController.isConfirmed();
    } catch (Exception e) {
      logger.error("Error al mostrar previsualización", e);
      showErrorAlert("Error", "No se pudo mostrar la previsualización: " + e.getMessage());
      return false;
    }
  }

  /**
   * Crea GRHs estáticos a partir de rectángulos detectados. Devuelve lista de IDs
   * creados.
   */
  private List<Integer> createStaticGrhs(List<Rectangle> regions, int fileNum) {
    int startId = dataManager.getGrhCount() + 1;
    List<Integer> createdIds = new ArrayList<>();

    for (int i = 0; i < regions.size(); i++) {
      Rectangle r = regions.get(i);
      int grhId = startId + i;

      GrhData newGrh = new GrhData();
      newGrh.setGrh(grhId);
      newGrh.setFileNum(fileNum);
      newGrh.setsX((short) r.getX());
      newGrh.setsY((short) r.getY());
      newGrh.setTileWidth((short) r.getWidth());
      newGrh.setTileHeight((short) r.getHeight());
      newGrh.setNumFrames((short) 1);

      grhList.add(newGrh);
      if (grhDataMap != null)
        grhDataMap.put(grhId, newGrh);
      createdIds.add(grhId);
    }

    dataManager.setGrhCount(dataManager.getGrhCount() + createdIds.size());
    return createdIds;
  }

  /**
   * Crea animaciones GRH a partir de IDs estáticos.
   */
  private int createAnimationGrhs(List<Integer> staticIds, int framesPerAnim, int fileNum) {
    int animCount = 0;
    int currentId = dataManager.getGrhCount();

    for (int i = 0; i < staticIds.size(); i += framesPerAnim) {
      if (i + framesPerAnim > staticIds.size())
        break;

      currentId++;
      GrhData animGrh = new GrhData();
      animGrh.setGrh(currentId);
      animGrh.setFileNum(fileNum);
      animGrh.setNumFrames((short) framesPerAnim);

      int[] frames = new int[framesPerAnim + 1];
      frames[0] = 0;
      for (int f = 0; f < framesPerAnim; f++) {
        frames[f + 1] = staticIds.get(i + f);
      }

      animGrh.setFrames(frames);
      animGrh.setSpeed(120.0f);

      grhList.add(animGrh);
      if (grhDataMap != null)
        grhDataMap.put(currentId, animGrh);
      animCount++;
    }

    dataManager.setGrhCount(currentId);
    return animCount;
  }

  /**
   * MODO: Cuerpo Animado (4 direcciones x N frames)
   */
  @FXML
  public void mnuAutoBody_OnAction() {
    ImageDetectionResult result = loadAndDetectSprites("Cuerpo Animado");
    if (result == null || result.regions.isEmpty()) {
      if (result != null)
        showInfoAlert("Info", "No se detectaron sprites.");
      return;
    }

    // Agrupar sprites por filas (basado en Y)
    List<List<Rectangle>> rows = groupSpritesByRows(result.regions);

    if (rows.isEmpty()) {
      showInfoAlert("Info", "No se pudieron detectar filas.");
      return;
    }

    // Mostrar resumen de lo detectado
    StringBuilder summary = new StringBuilder();
    summary.append("Se detectaron ").append(rows.size()).append(" filas:\n");
    for (int i = 0; i < rows.size(); i++) {
      summary.append("  Fila ").append(i + 1).append(": ").append(rows.get(i).size())
          .append(" frames\n");
    }

    // Mostrar vista previa
    String modeDesc = summary.toString();
    if (!showDetectionPreview(result, "Cuerpo Animado", modeDesc)) {
      return; // Usuario canceló
    }

    // Crear estáticos para todas las filas
    List<Rectangle> allRects = new ArrayList<>();
    for (List<Rectangle> row : rows) {
      allRects.addAll(row);
    }
    List<Integer> allStaticIds = createStaticGrhs(allRects, result.fileNum);

    // Crear animaciones por fila
    int animCount = 0;
    int offset = 0;
    for (List<Rectangle> row : rows) {
      int framesInRow = row.size();
      if (framesInRow > 0) {
        List<Integer> rowIds = allStaticIds.subList(offset, offset + framesInRow);
        createSingleAnimation(rowIds, result.fileNum);
        animCount++;
      }
      offset += framesInRow;
    }

    showInfoAlert("Éxito",
        "Se crearon " + allStaticIds.size() + " estáticos y " + animCount + " animaciones.");
    scrollToEnd();
  }

  /** Agrupa sprites en filas basándose en coordenada Y */
  private List<List<Rectangle>> groupSpritesByRows(List<Rectangle> regions) {
    if (regions.isEmpty())
      return new ArrayList<>();

    // Calcular altura promedio para tolerancia
    double avgHeight = regions.stream().mapToDouble(Rectangle::getHeight).average().orElse(32);
    double tolerance = avgHeight * 0.5;

    List<List<Rectangle>> rows = new ArrayList<>();
    List<Rectangle> currentRow = new ArrayList<>();
    double currentRowY = regions.get(0).getY();

    for (Rectangle r : regions) {
      if (Math.abs(r.getY() - currentRowY) < tolerance) {
        currentRow.add(r);
      } else {
        if (!currentRow.isEmpty())
          rows.add(currentRow);
        currentRow = new ArrayList<>();
        currentRow.add(r);
        currentRowY = r.getY();
      }
    }
    if (!currentRow.isEmpty())
      rows.add(currentRow);

    return rows;
  }

  /** Crea una sola animación GRH a partir de IDs */
  private void createSingleAnimation(List<Integer> frameIds, int fileNum) {
    int currentId = dataManager.getGrhCount() + 1;

    GrhData animGrh = new GrhData();
    animGrh.setGrh(currentId);
    animGrh.setFileNum(fileNum);
    animGrh.setNumFrames((short) frameIds.size());

    int[] frames = new int[frameIds.size() + 1];
    frames[0] = 0;
    for (int i = 0; i < frameIds.size(); i++) {
      frames[i + 1] = frameIds.get(i);
    }

    animGrh.setFrames(frames);
    animGrh.setSpeed(120.0f);

    grhList.add(animGrh);
    if (grhDataMap != null)
      grhDataMap.put(currentId, animGrh);
    dataManager.setGrhCount(currentId);
  }

  /**
   * MODO: Sprites Individuales (solo estáticos, sin animación)
   */
  @FXML
  public void mnuAutoSprites_OnAction() {
    ImageDetectionResult result = loadAndDetectSprites("Sprites Individuales");
    if (result == null || result.regions.isEmpty()) {
      if (result != null)
        showInfoAlert("Info", "No se detectaron sprites.");
      return;
    }

    // Mostrar vista previa
    String modeDesc = "Modo: Sprites Individuales (estáticos)";
    if (!showDetectionPreview(result, "Sprites Individuales", modeDesc)) {
      return; // Usuario canceló
    }

    List<Integer> createdIds = createStaticGrhs(result.regions, result.fileNum);
    showInfoAlert("Éxito", "Se crearon " + createdIds.size() + " GRHs estáticos.");
    scrollToEnd();
  }

  /**
   * MODO: Grid de Tiles (cuadrícula uniforme)
   */
  @FXML
  private void mnuAutoGrid_OnAction() {
    TextInputDialog fileDialog = new TextInputDialog();
    fileDialog.setTitle("Grid de Tiles");
    fileDialog.setHeaderText("Introduce el número de archivo de imagen:");
    fileDialog.setContentText("FileNum:");

    Optional<String> fileResult = fileDialog.showAndWait();
    if (!fileResult.isPresent())
      return;

    try {
      int fileNum = Integer.parseInt(fileResult.get());
      String imagePath = configManager.getGraphicsDir() + File.separator + fileNum + ".png";

      File f = new File(imagePath);
      if (!f.exists()) {
        imagePath = configManager.getGraphicsDir() + File.separator + fileNum + ".bmp";
        f = new File(imagePath);
        if (!f.exists()) {
          showErrorAlert("Error", "No se encontró la imagen.");
          return;
        }
      }

      Image image = imageCache.getImage(imagePath);
      if (image == null) {
        showErrorAlert("Error", "No se pudo cargar la imagen.");
        return;
      }

      int imgWidth = (int) image.getWidth();
      int imgHeight = (int) image.getHeight();

      // Elegir modo: por tamaño o por cantidad
      Alert modeChoice = new Alert(Alert.AlertType.CONFIRMATION);
      modeChoice.setTitle("Modo de Grid");
      modeChoice.setHeaderText("Imagen: " + imgWidth + "x" + imgHeight);
      modeChoice.setContentText("¿Cómo desea dividir la imagen?");

      ButtonType btnBySize = new ButtonType("Por Tamaño (32x32, 64x64...)", ButtonBar.ButtonData.LEFT);
      ButtonType btnByCount = new ButtonType("Por Cantidad (cols x filas)", ButtonBar.ButtonData.RIGHT);
      ButtonType btnCancel = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
      modeChoice.getButtonTypes().setAll(btnBySize, btnByCount, btnCancel);

      Optional<ButtonType> modeResult = modeChoice.showAndWait();
      if (!modeResult.isPresent() || modeResult.get() == btnCancel)
        return;

      int tileWidth, tileHeight, cols, rows;

      if (modeResult.get() == btnBySize) {
        // Modo por tamaño fijo
        ChoiceDialog<String> sizeDialog = new ChoiceDialog<>("32x32", "16x16", "32x32", "64x64", "128x128");
        sizeDialog.setTitle("Tamaño de Tile");
        sizeDialog.setHeaderText("Imagen: " + imgWidth + "x" + imgHeight);
        sizeDialog.setContentText("Tamaño de cada tile:");

        Optional<String> sizeResult = sizeDialog.showAndWait();
        if (!sizeResult.isPresent())
          return;

        tileWidth = tileHeight = Integer.parseInt(sizeResult.get().split("x")[0]);
        cols = imgWidth / tileWidth;
        rows = imgHeight / tileHeight;

      } else {
        // Modo por cantidad de tiles
        TextInputDialog colsDialog = new TextInputDialog("1");
        colsDialog.setTitle("Número de Columnas");
        colsDialog.setHeaderText("Imagen: " + imgWidth + "x" + imgHeight);
        colsDialog.setContentText("¿Cuántas columnas de tiles?");

        Optional<String> colsResult = colsDialog.showAndWait();
        if (!colsResult.isPresent())
          return;
        cols = Integer.parseInt(colsResult.get());

        TextInputDialog rowsDialog = new TextInputDialog("1");
        rowsDialog.setTitle("Número de Filas");
        rowsDialog.setHeaderText("Columnas: " + cols);
        rowsDialog.setContentText("¿Cuántas filas de tiles?");

        Optional<String> rowsResult = rowsDialog.showAndWait();
        if (!rowsResult.isPresent())
          return;
        rows = Integer.parseInt(rowsResult.get());

        tileWidth = imgWidth / cols;
        tileHeight = imgHeight / rows;
      }

      int total = cols * rows;

      Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
      confirm.setTitle("Confirmar Grid");
      confirm.setHeaderText("Se generarán " + total + " tiles.");
      confirm.setContentText(
          cols + " cols x " + rows + " filas\nTamaño: " + tileWidth + "x" + tileHeight + " px");

      if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK)
        return;

      // Crear rectángulos de grid
      List<Rectangle> gridRects = new ArrayList<>();
      for (int row = 0; row < rows; row++) {
        for (int col = 0; col < cols; col++) {
          gridRects.add(new Rectangle(col * tileWidth, row * tileHeight, tileWidth, tileHeight));
        }
      }

      List<Integer> createdIds = createStaticGrhs(gridRects, fileNum);
      showInfoAlert("Éxito",
          "Se crearon " + createdIds.size() + " tiles de " + tileWidth + "x" + tileHeight + ".");
      scrollToEnd();

    } catch (NumberFormatException e) {
      showErrorAlert("Error", "Valor inválido.");
    }
  }

  /**
   * MODO: Superficies (múltiples superficies en una imagen, cada una dividida en
   * tiles)
   */
  @FXML
  public void mnuAutoSurfaces_OnAction() {
    TextInputDialog fileDialog = new TextInputDialog();
    fileDialog.setTitle("Superficies");
    fileDialog.setHeaderText("Introduce el número de archivo de imagen:");
    fileDialog.setContentText("FileNum:");

    Optional<String> fileResult = fileDialog.showAndWait();
    if (!fileResult.isPresent())
      return;

    try {
      int fileNum = Integer.parseInt(fileResult.get());
      String imagePath = configManager.getGraphicsDir() + File.separator + fileNum + ".png";

      File f = new File(imagePath);
      if (!f.exists()) {
        imagePath = configManager.getGraphicsDir() + File.separator + fileNum + ".bmp";
        f = new File(imagePath);
        if (!f.exists()) {
          showErrorAlert("Error", "No se encontró la imagen.");
          return;
        }
      }

      Image image = imageCache.getImage(imagePath);
      if (image == null) {
        showErrorAlert("Error", "No se pudo cargar la imagen.");
        return;
      }

      int imgWidth = (int) image.getWidth();
      int imgHeight = (int) image.getHeight();

      // Pedir tamaño de cada superficie
      TextInputDialog surfSizeDialog = new TextInputDialog("128");
      surfSizeDialog.setTitle("Tamaño de Superficie");
      surfSizeDialog.setHeaderText("Imagen: " + imgWidth + "x" + imgHeight);
      surfSizeDialog.setContentText("Tamaño de cada superficie (px):");

      Optional<String> surfSizeResult = surfSizeDialog.showAndWait();
      if (!surfSizeResult.isPresent())
        return;
      int surfaceSize = Integer.parseInt(surfSizeResult.get());

      // Pedir tamaño de cada tile
      TextInputDialog tileSizeDialog = new TextInputDialog("32");
      tileSizeDialog.setTitle("Tamaño de Tile");
      tileSizeDialog.setHeaderText("Superficie: " + surfaceSize + "x" + surfaceSize);
      tileSizeDialog.setContentText("Tamaño de cada tile (px):");

      Optional<String> tileSizeResult = tileSizeDialog.showAndWait();
      if (!tileSizeResult.isPresent())
        return;
      int tileSize = Integer.parseInt(tileSizeResult.get());

      // Calcular cuántas superficies caben
      int surfaceCols = imgWidth / surfaceSize;
      int surfaceRows = imgHeight / surfaceSize;
      int totalSurfaces = surfaceCols * surfaceRows;

      // Calcular tiles por superficie
      int tilesPerSurfaceRow = surfaceSize / tileSize;
      int tilesPerSurface = tilesPerSurfaceRow * tilesPerSurfaceRow;
      int totalTiles = totalSurfaces * tilesPerSurface;

      Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
      confirm.setTitle("Confirmar Superficies");
      confirm.setHeaderText(totalSurfaces + " superficies de " + surfaceSize + "x" + surfaceSize);
      confirm.setContentText(tilesPerSurface + " tiles por superficie (" + tileSize + "x" + tileSize
          + ")\nTotal: " + totalTiles + " tiles");

      if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK)
        return;

      // Crear rectángulos: recorrer superficie por superficie
      List<Rectangle> allTiles = new ArrayList<>();

      for (int surfRow = 0; surfRow < surfaceRows; surfRow++) {
        for (int surfCol = 0; surfCol < surfaceCols; surfCol++) {
          // Origen de esta superficie
          int surfX = surfCol * surfaceSize;
          int surfY = surfRow * surfaceSize;

          // Recorrer tiles dentro de esta superficie
          for (int tileRow = 0; tileRow < tilesPerSurfaceRow; tileRow++) {
            for (int tileCol = 0; tileCol < tilesPerSurfaceRow; tileCol++) {
              int x = surfX + (tileCol * tileSize);
              int y = surfY + (tileRow * tileSize);
              allTiles.add(new Rectangle(x, y, tileSize, tileSize));
            }
          }
        }
      }

      List<Integer> createdIds = createStaticGrhs(allTiles, fileNum);
      showInfoAlert("Éxito",
          "Se crearon " + createdIds.size() + " tiles de " + totalSurfaces + " superficies.");
      scrollToEnd();

    } catch (NumberFormatException e) {
      showErrorAlert("Error", "Valor inválido.");
    }
  }

  /**
   * MODO: Animación Simple (1 fila = 1 animación)
   */
  @FXML
  public void mnuAutoAnimation_OnAction() {
    ImageDetectionResult result = loadAndDetectSprites("Animación Simple");
    if (result == null || result.regions.isEmpty()) {
      if (result != null)
        showInfoAlert("Info", "No se detectaron sprites.");
      return;
    }

    // Mostrar vista previa
    String modeDesc = "Modo: Animación Simple (" + result.regions.size() + " frames)";
    if (!showDetectionPreview(result, "Animación Simple", modeDesc)) {
      return; // Usuario canceló
    }

    // Crear estáticos
    List<Integer> staticIds = createStaticGrhs(result.regions, result.fileNum);

    // Crear 1 animación con todos los frames
    int animCount = createAnimationGrhs(staticIds, staticIds.size(), result.fileNum);

    showInfoAlert("Éxito",
        "Se crearon " + staticIds.size() + " estáticos y " + animCount + " animación.");
    scrollToEnd();
  }

  /** Scroll al final de la lista */
  private void scrollToEnd() {
    if (!lstIndices.getItems().isEmpty()) {
      lstIndices.scrollTo(lstIndices.getItems().size() - 1);
      lstIndices.getSelectionModel().select(lstIndices.getItems().size() - 1);
    }
  }

  /**
   * Muestra un diálogo con los atajos de teclado y tips de uso.
   */
  @FXML
  private void mnuShortcuts_OnAction() {
    String shortcuts = "Edición:\n" + "• Ctrl+C: Copiar GRH\n"
        + "• Ctrl+V: Pegar GRH (propiedades)\n" + "• Ctrl+D: Duplicar GRH\n"
        + "• Ctrl+Z: Deshacer\n" + "• Ctrl+Y: Rehacer\n" + "• Del: Eliminar GRH\n\n"
        + "Búsqueda (Filtro):\n" + "• f:Num : Buscar por FileNum\n" + "• w:Num : Buscar por Ancho\n"
        + "• h:Num : Buscar por Alto\n" + "• Enter: Buscar siguiente (cíclico)\n\n" + "Vista:\n"
        + "• Scroll Mouse: Zoom In/Out\n" + "• Ctrl+1-5: Abrir editores de índices";

    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle("Atajos y Tips");
    alert.setHeaderText("Guía Rápida de Uso");
    alert.setContentText(shortcuts);
    alert.getDialogPane().setMinWidth(400);
    alert.showAndWait();
  }

  /**
   * Actualiza la barra de estado con información relevante.
   *
   * @param status     Mensaje de estado a mostrar.
   * @param grhCount   Número total de GRHs.
   * @param animCount  Número de animaciones.
   * @param isModified Indica si hay cambios sin guardar.
   */
  private void updateStatusBar(String status, int grhCount, int animCount, boolean isModified) {
    if (lblStatus != null) {
      lblStatus.setText(status);
      lblStatus.setStyle(
          status.contains("Error") ? "-fx-text-fill: #FF6B6B;" : "-fx-text-fill: #00FF00;");
    }
    if (lblGrhCount != null) {
      lblGrhCount.setText("GRHs: " + grhCount);
    }
    if (lblAnimCount != null) {
      lblAnimCount.setText("Animaciones: " + animCount);
    }
    if (lblModified != null) {
      lblModified.setText(isModified ? "● Modificado" : "");
    }
  }

  // ========== FINAL DEL CONTROLADOR ==========

  private void setupColorPicker() {
    if (cpBackground != null) {
      String currentColor = configManager.getBackgroundColor();
      try {
        cpBackground.setValue(Color.web(currentColor));
      } catch (Exception e) {
        cpBackground.setValue(Color.web("#EA3FF7"));
      }

      cpBackground.setOnAction(event -> {
        Color c = cpBackground.getValue();
        String hex = String.format("#%02X%02X%02X", (int) (c.getRed() * 255),
            (int) (c.getGreen() * 255), (int) (c.getBlue() * 255));

        configManager.setBackgroundColor(hex);
        try {
          configManager.writeConfig();
        } catch (IOException e) {
          logger.error("Error al guardar configuración de color", e);
        }
        updateBackgroundColor();
      });
    }
  }

}

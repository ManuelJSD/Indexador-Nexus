package org.nexus.indexador.controllers;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

import javafx.collections.transformation.FilteredList;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import org.nexus.indexador.utils.ImageUtils;
import javafx.scene.paint.Color;
import javafx.scene.layout.StackPane; // Import StackPane

import javafx.util.Duration;
import org.nexus.indexador.gamedata.DataManager;
import org.nexus.indexador.gamedata.models.FXData;
import org.nexus.indexador.gamedata.models.GrhData;
import org.nexus.indexador.utils.AnimationState;
import org.nexus.indexador.utils.ConfigManager;
import org.nexus.indexador.utils.ImageCache;
import org.nexus.indexador.utils.Logger;

import java.io.File;

import java.util.HashMap;
import java.util.Map;

public class FxsController {

  @FXML
  public ListView lstFxs;
  @FXML
  public ImageView imgFX;
  @FXML
  public TextField txtFX;
  @FXML
  public Label lblNFXs;
  @FXML
  public TextField txtOffsetX;
  @FXML
  public Label lblOffsetX;
  @FXML
  public TextField txtOffsetY;
  @FXML
  public Label lblOffsetY;
  @FXML
  public Button btnSave;
  @FXML
  public Button btnAdd;
  @FXML
  public Button btnDelete;
  @FXML
  public TextField txtSearch;

  @FXML
  public ScrollPane scrollPreview;
  @FXML
  public StackPane stackPreview;
  @FXML
  public Slider sliderZoom;
  @FXML
  public CheckBox chkShowMarker;

  // Estado de Arrastre (Drag)
  private double dragStartX, dragStartY;
  private int originalOffX, originalOffY;

  private FXData fxDataManager;
  private ObservableList<FXData> fxList;
  private ObservableList<GrhData> grhList;

  private ConfigManager configManager;
  private DataManager dataManager;
  private ImageCache imageCache;
  private Logger logger;

  // Mapa de estados de animación
  private Map<Integer, AnimationState> animationStates = new HashMap<>();

  // Mapa para búsqueda rápida de datos Grh
  private Map<Integer, GrhData> grhDataMap;

  // Índice del frame actual en la animación.
  private int currentFrameIndex = 1;
  // Línea de tiempo que controla la animación de los frames en el visor.
  private Timeline animationTimeline;

  /**
   * Inicializa el controlador, cargando la configuración y los datos de los FXs.
   */
  @FXML
  protected void initialize() {
    configManager = ConfigManager.getInstance();
    try {
      dataManager = DataManager.getInstance();
      imageCache = ImageCache.getInstance();
      logger = Logger.getInstance();

      fxDataManager = new FXData();

      // Inicializar estados (si fueran necesarios por índice)
      animationStates.put(0, new AnimationState());
      animationStates.put(1, new AnimationState());
      animationStates.put(2, new AnimationState());
      animationStates.put(3, new AnimationState());

      loadFxData();
      setupFXListListener();
      setupInteraction(); // Nuevo setup para zoom y arrastre

      // Asegurar que el scroll empiece centrado
      javafx.application.Platform.runLater(() -> {
        scrollPreview.setHvalue(0.5);
        scrollPreview.setVvalue(0.5);
      });
    } catch (Exception e) {
      System.err.println("Error al inicializar FxsController:");
      e.printStackTrace();
    }
  }

  private void setupInteraction() {
    // Configurar listeners de texto para actualización en tiempo real
    txtOffsetX.textProperty().addListener((obs, ov, nv) -> requestFrameUpdate());
    txtOffsetY.textProperty().addListener((obs, ov, nv) -> requestFrameUpdate());

    if (chkShowMarker != null) {
      chkShowMarker.selectedProperty().addListener((obs, ov, nv) -> requestFrameUpdate());
    }

    // Listener de Zoom
    if (sliderZoom != null) {
      sliderZoom.valueProperty().addListener((obs, oldV, newV) -> {
        double scale = newV.doubleValue();
        imgFX.setFitWidth(400 * scale);
        imgFX.setFitHeight(400 * scale);
      });
    }

    // Lógica de Arrastre (Pan)
    imgFX.setPickOnBounds(true);

    imgFX.setOnMousePressed(e -> {
      dragStartX = e.getX();
      dragStartY = e.getY();
      try {
        originalOffX = Integer.parseInt(txtOffsetX.getText());
        originalOffY = Integer.parseInt(txtOffsetY.getText());
      } catch (Exception ex) {
        originalOffX = 0;
        originalOffY = 0;
      }
    });

    imgFX.setOnMouseDragged(e -> {
      double currentScale = imgFX.getBoundsInLocal().getWidth() / 400.0;
      double deltaX = (e.getX() - dragStartX);
      double deltaY = (e.getY() - dragStartY);

      // Ajustar delta según escala del zoom
      if (currentScale != 0) {
        deltaX /= currentScale;
        deltaY /= currentScale;
      }

      // Los offsets de FX funcionan como desplazamiento desde el centro.
      // Mover ratón a la derecha -> Incrementar X

      int newOffX = originalOffX + (int) Math.round(deltaX);
      int newOffY = originalOffY + (int) Math.round(deltaY);

      txtOffsetX.setText(String.valueOf(newOffX));
      txtOffsetY.setText(String.valueOf(newOffY));
    });

    // Zoom con Rueda
    imgFX.setOnScroll(e -> {
      if (e.getDeltaY() == 0)
        return;
      double zoomFactor = 0.1;
      double val = sliderZoom.getValue();
      if (e.getDeltaY() > 0)
        val += zoomFactor;
      else
        val -= zoomFactor;
      sliderZoom.setValue(val);

      // Auto-centrar tras (posible) re-layout
      final double targetVal = 0.5;
      javafx.application.Platform.runLater(() -> {
        scrollPreview.setHvalue(targetVal);
        scrollPreview.setVvalue(targetVal);
      });

      e.consume();
    });

    scrollPreview.addEventFilter(javafx.scene.input.ScrollEvent.SCROLL, e -> {
      if (e.getDeltaY() != 0) {
        double zoomFactor = 0.1;
        double val = sliderZoom.getValue();
        if (e.getDeltaY() > 0)
          val += zoomFactor;
        else
          val -= zoomFactor;
        sliderZoom.setValue(val);

        javafx.application.Platform.runLater(() -> {
          scrollPreview.setHvalue(0.5);
          scrollPreview.setVvalue(0.5);
        });

        e.consume();
      }
    });
  }

  private void requestFrameUpdate() {
    // Redibujar frame actual
    if (lstFxs.getSelectionModel().getSelectedIndex() < 0)
      return;
    FXData selectedFx = fxList.get(lstFxs.getSelectionModel().getSelectedIndex());
    if (selectedFx != null) {
      GrhData selectedGrh = grhDataMap.get(selectedFx.getFx());
      if (selectedGrh != null)
        updateFrame(selectedGrh);
    }
  }

  /**
   * Carga los datos de los FXs desde la memoria/disco y los muestra en la lista.
   */
  private void loadFxData() {
    // Obtener lista de FXs desde DataManager
    fxList = dataManager.getFXList();

    // Inicializar el mapa de grhData
    grhDataMap = new HashMap<>();

    grhList = dataManager.getGrhList();

    // Llenar el mapa con los datos de grhList
    for (GrhData grh : grhList) {
      grhDataMap.put(grh.getGrh(), grh);
    }

    // Actualizar el texto de los labels con la información obtenida
    lblNFXs.setText("FXs cargados: " + dataManager.getNumFXs());

    // Agregar los índices de gráficos al ListView
    ObservableList<String> fxIndices = FXCollections.observableArrayList();
    for (int i = 1; i < fxList.size() + 1; i++) {
      fxIndices.add(String.valueOf(i));
    }

    lstFxs.setItems(fxIndices);

    // Configurar FilteredList
    FilteredList<String> filteredData = new FilteredList<>(fxIndices, p -> true);

    // Binding del buscador
    if (txtSearch != null) {
      txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
        filteredData.setPredicate(index -> {
          if (newValue == null || newValue.isEmpty()) {
            return true;
          }
          return index.contains(newValue); // Filtrado simple por ID
        });
      });
    }

    lstFxs.setItems(filteredData);

  }

  /**
   * Configura un listener para el ListView, manejando los eventos de selección de
   * ítems.
   */
  private void setupFXListListener() {
    // Agregar un listener al ListView para capturar los eventos de selección
    lstFxs.getSelectionModel().selectedItemProperty()
        .addListener((observable, oldValue, newValue) -> {

          // Obtener el índice seleccionado
          int selectedIndex = lstFxs.getSelectionModel().getSelectedIndex();

          if (selectedIndex >= 0) {
            // Obtener el objeto headData correspondiente al índice seleccionado
            FXData selectedFx = fxList.get(selectedIndex);
            updateEditor(selectedFx);
            displayAnimation(selectedFx);

          }
        });
  }

  /**
   * Actualiza los campos de texto del editor con los datos del FX seleccionado.
   *
   * @param selectedFx el objeto FXData seleccionado.
   */
  private void updateEditor(FXData selectedFx) {
    // Obtenemos todos los datos
    int grhFxs = selectedFx.getFx();
    int offsetX = selectedFx.getOffsetX();
    int offsetY = selectedFx.getOffsetY();

    txtFX.setText(String.valueOf(grhFxs));
    txtOffsetX.setText(String.valueOf(offsetX));
    txtOffsetY.setText(String.valueOf(offsetY));
  }

  /**
   * Muestra y reproduce la animación del FX seleccionado en el visor.
   *
   * @param selectedFX El FX seleccionado.
   */
  private void displayAnimation(FXData selectedFX) {

    // Obtenemos el Grh de animación desde el indice del FX
    GrhData selectedGrh = grhDataMap.get(selectedFX.getFx());

    int nFrames = selectedGrh.getNumFrames();

    // Configurar la animación
    if (animationTimeline != null) {
      animationTimeline.stop();
    }

    currentFrameIndex = 1; // Reiniciar el índice del frame al iniciar la animación

    animationTimeline = new Timeline(new KeyFrame(Duration.ZERO, event -> {
      // Actualizar la imagen en el ImageView con el frame actual
      updateFrame(selectedGrh);
      currentFrameIndex = (currentFrameIndex + 1) % nFrames; // Avanzar al siguiente frame
                                                             // circularmente
      if (currentFrameIndex == 0) {
        currentFrameIndex = 1; // Omitir la posición 0
      }
    }), new KeyFrame(Duration.millis(100)) // Ajustar la duración según sea necesario
    );
    animationTimeline.setCycleCount(Animation.INDEFINITE); // Repetir la animación indefinidamente
    animationTimeline.play(); // Iniciar la animación
  }

  /**
   * Renderiza el frame actual de la animación en el lienzo.
   *
   * @param selectedGrh El gráfico (GRH) del frame actual.
   */
  private void updateFrame(GrhData selectedGrh) {
    int[] frames = selectedGrh.getFrames(); // Obtener índices de frames de animación

    // Verificar que el índice actual esté dentro del rango adecuado
    if (currentFrameIndex >= 0 && currentFrameIndex < frames.length) {
      int frameId = frames[currentFrameIndex];

      // Buscar el GrhData correspondiente al frameId utilizando el mapa
      GrhData currentGrh = grhDataMap.get(frameId);

      if (currentGrh != null) {
        String imagePath = configManager.getGraphicsDir() + currentGrh.getFileNum() + ".png";

        if (!new File(imagePath).exists()) {
          imagePath = configManager.getGraphicsDir() + currentGrh.getFileNum() + ".bmp";
        }

        File imageFile = new File(imagePath);

        // Verificar si el archivo de imagen existe
        if (imageFile.exists()) {
          Image frameImage = imageCache.getImage(imagePath);

          if (frameImage != null) {
            WritableImage croppedImage = imageCache.getCroppedImage(imagePath, currentGrh.getsX(),
                currentGrh.getsY(), currentGrh.getTileWidth(), currentGrh.getTileHeight());
            WritableImage finalImage = croppedImage;

            int offX = 0;
            int offY = 0;
            try {
              offX = Integer.parseInt(txtOffsetX.getText());
              offY = Integer.parseInt(txtOffsetY.getText());
            } catch (NumberFormatException e) {
            }

            // Usar método centrado con marcador
            boolean showMarker = chkShowMarker != null && chkShowMarker.isSelected();
            // Canvas width/height fijo 400x400 para permitir movimiento amplio
            finalImage = ImageUtils.drawCenteredImageWithOffset(croppedImage, 400, 400, offX, offY,
                showMarker);

            imgFX.setImage(finalImage);
          }
        }
      }
    }
  }

  public void btnSave_OnAction(ActionEvent actionEvent) {
    int selectedIndex = lstFxs.getSelectionModel().getSelectedIndex();
    if (selectedIndex >= 0) {
      try {
        FXData data = fxList.get(selectedIndex);

        data.setFx(Integer.parseInt(txtFX.getText()));
        data.setOffsetX(Short.parseShort(txtOffsetX.getText()));
        data.setOffsetY(Short.parseShort(txtOffsetY.getText()));

        // Guardado real en disco
        dataManager.getIndexLoader().saveFXs(fxList);
        logger.info("FXs guardados en disco correctamente.");

        // Recargar animación visual
        displayAnimation(data);
      } catch (Exception e) {
        logger.error("Error al guardar FXs", e);
      }
    }
  }

  public void btnAdd_OnAction(ActionEvent actionEvent) {
    // Crear nuevo FX inicializado
    FXData newFx = new FXData(0, (short) 0, (short) 0);
    fxList.add(newFx);

    lstFxs.getItems().add(String.valueOf(fxList.size()));
    lstFxs.getSelectionModel().selectLast();
    logger.info("Nuevo FX añadido.");
  }

  public void btnDelete_OnAction(ActionEvent actionEvent) {
    int selectedIndex = lstFxs.getSelectionModel().getSelectedIndex();
    if (selectedIndex >= 0) {
      fxList.remove(selectedIndex);
      loadFxData();
      logger.info("FX eliminado de memoria. Recuerde guardar.");
    }
  }

}

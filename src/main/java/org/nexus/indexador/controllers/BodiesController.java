package org.nexus.indexador.controllers;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;

import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.Group;
import javafx.util.Duration;
import org.nexus.indexador.gamedata.DataManager;
import org.nexus.indexador.gamedata.models.BodyData;
import org.nexus.indexador.gamedata.models.GrhData;
import org.nexus.indexador.gamedata.models.HeadData;
import org.nexus.indexador.utils.AnimationState;
import org.nexus.indexador.utils.ConfigManager;
import org.nexus.indexador.utils.ImageCache;
import org.nexus.indexador.utils.ImageUtils;
import org.nexus.indexador.utils.Logger;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class BodiesController {

  @FXML
  public ListView<String> lstBodys;
  @FXML
  public TextField txtSearch;

  // Preview Controls
  @FXML
  public ScrollPane scrollPreview;
  @FXML
  public StackPane stackPreview;
  @FXML
  public ImageView imgPreview;
  @FXML
  public Slider sliderZoom;
  @FXML
  public ToggleGroup grpHeading;
  @FXML
  public RadioButton rbSur;
  @FXML
  public RadioButton rbNorte;
  @FXML
  public RadioButton rbEste;
  @FXML
  public RadioButton rbOeste;
  @FXML
  public CheckBox chkShowHead;
  @FXML
  public CheckBox chkShowMarker;

  // Properties
  @FXML
  public TextField txtNorte;
  @FXML
  public TextField txtEste;
  @FXML
  public TextField txtSur;
  @FXML
  public TextField txtOeste;
  @FXML
  public TextField txtHeadOffsetX;
  @FXML
  public TextField txtHeadOffsetY;

  @FXML
  public Button btnSave;
  @FXML
  public Button btnAdd;
  @FXML
  public Button btnDelete;

  private ObservableList<BodyData> bodyList;
  private ObservableList<GrhData> grhList;

  // Default Ghost Head
  private HeadData ghostHeadData;

  private ConfigManager configManager;
  private DataManager dataManager;
  private ImageCache imageCache;
  private Logger logger;

  private AnimationState animationState;
  private Map<Integer, GrhData> grhDataMap;

  private BodyData selectedBody;
  private int currentHeading = 0; // 0: Sur, 1: Norte, 2: Oeste, 3: Este

  // Estado de arrastre (Drag)
  private double dragStartX, dragStartY;
  private int originalOffX, originalOffY;

  @FXML
  protected void initialize() {
    configManager = ConfigManager.getInstance();
    try {
      dataManager = DataManager.getInstance();
      imageCache = ImageCache.getInstance();
      logger = Logger.getInstance();

      // Initialize single animation state
      animationState = new AnimationState();

      // Load Default Head (Try to find head 1 or first available)
      loadDefaultHead();

      loadBodyData();
      setupListeners();
      setupInteraction();
    } catch (Exception e) {
      System.err.println("Error al inicializar BodiesController:");
      e.printStackTrace();
    }
  }

  private void loadDefaultHead() {
    ObservableList<HeadData> heads = dataManager.getHeadList();
    if (heads != null && !heads.isEmpty()) {
      // Intentar encontrar una cabeza válida, p.ej. índice 1 (generalmente Cabeza
      // Humana)
      if (heads.size() > 1)
        ghostHeadData = heads.get(1); // Índice 1
      else
        ghostHeadData = heads.get(0);
    }
  }

  private void loadBodyData() {
    bodyList = dataManager.getBodyList();
    grhList = dataManager.getGrhList();
    grhDataMap = new HashMap<>();
    for (GrhData grh : grhList) {
      grhDataMap.put(grh.getGrh(), grh);
    }

    ObservableList<String> bodyIndices = FXCollections.observableArrayList();
    for (int i = 1; i <= bodyList.size(); i++) {
      bodyIndices.add(String.valueOf(i));
    }

    FilteredList<String> filteredData = new FilteredList<>(bodyIndices, p -> true);
    if (txtSearch != null) {
      txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
        filteredData.setPredicate(index -> {
          if (newValue == null || newValue.isEmpty())
            return true;
          return index.contains(newValue);
        });
      });
    }
    lstBodys.setItems(filteredData);
  }

  private void setupListeners() {
    lstBodys.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
      int idx = lstBodys.getSelectionModel().getSelectedIndex();
      if (idx >= 0) {
        // Advertencia: Esto depende de que la lista filtrada mantenga el orden o mapee
        // de vuelta.
        // Por seguridad con listas filtradas, parseamos el entero desde el string
        // seleccionado.
        if (newV != null) {
          try {
            int realIndex = Integer.parseInt(newV) - 1;
            if (realIndex >= 0 && realIndex < bodyList.size()) {
              selectedBody = bodyList.get(realIndex);
              updateEditor(selectedBody);
            }
          } catch (NumberFormatException e) {
          }
        }
      }
    });

    // Orientation Change
    grpHeading.selectedToggleProperty().addListener((obs, oldV, newV) -> {
      if (newV == rbSur)
        currentHeading = 0; // Sur usually index 2 in ARRAY, but visual standard Sur
      else if (newV == rbNorte)
        currentHeading = 1;
      else if (newV == rbOeste)
        currentHeading = 2;
      else if (newV == rbEste)
        currentHeading = 3;

      // Re-mapear encabezado visual al índice del array
      // Modelo de Datos: [Norte, Este, Sur, Oeste] -> [0, 1, 2, 3]
      // Estándar Visual (AO): Sur, Norte, Oeste, Este

      // Mapeo de RadioButtons a índices:
      // rbSur -> Índice 2
      // rbNorte -> Índice 0
      // rbOeste -> Índice 3
      // rbEste -> Índice 1

      startAnimation();
    });

    // Real-time text listeners for Offset
    txtHeadOffsetX.textProperty().addListener((obs, ov, nv) -> requestFrameUpdate());
    txtHeadOffsetY.textProperty().addListener((obs, ov, nv) -> requestFrameUpdate());

    chkShowHead.selectedProperty().addListener((obs, ov, nv) -> requestFrameUpdate());
  }

  private void setupInteraction() {
    // Zoom
    if (sliderZoom != null) {
      sliderZoom.valueProperty().addListener((obs, oldV, newV) -> {
        double scale = newV.doubleValue();
        imgPreview.setFitWidth(400 * scale);
        imgPreview.setFitHeight(400 * scale);
      });
    }

    // Lógica de Arrastre (Dragging)
    // Usamos imgPreview para capturar eventos de ratón.
    imgPreview.setPickOnBounds(true);

    // Zoom con Rueda del Ratón
    imgPreview.setOnScroll(e -> {
      if (e.getDeltaY() == 0)
        return;

      double delta = e.getDeltaY();
      double zoomFactor = 0.1;

      double val = sliderZoom.getValue();
      if (delta > 0) {
        val += zoomFactor;
      } else {
        val -= zoomFactor;
      }

      // Los límites del slider manejan el clamp, pero establecemos el valor seguro
      sliderZoom.setValue(val);
      scrollPreview.setHvalue(0.5);
      scrollPreview.setVvalue(0.5);
      e.consume(); // Prevenir scroll del scrollpane
    });

    // Filtro de evento para el ScrollPane para interceptar el scroll y usarlo como
    // zoom
    scrollPreview.addEventFilter(javafx.scene.input.ScrollEvent.SCROLL, e -> {
      if (e.getDeltaY() != 0) {
        double delta = e.getDeltaY();
        double zoomFactor = 0.1;
        double val = sliderZoom.getValue();
        if (delta > 0)
          val += zoomFactor;
        else
          val -= zoomFactor;
        sliderZoom.setValue(val);
        // Auto-centrar tras zoom
        scrollPreview.setHvalue(0.5);
        scrollPreview.setVvalue(0.5);
        e.consume();
      }
    });

    imgPreview.setOnMousePressed(e -> {
      if (!chkShowHead.isSelected())
        return;
      // Guardar estado inicial del arrastre
      dragStartX = e.getX();
      dragStartY = e.getY();
      try {
        originalOffX = Integer.parseInt(txtHeadOffsetX.getText());
        originalOffY = Integer.parseInt(txtHeadOffsetY.getText());
      } catch (Exception ex) {
        originalOffX = 0;
        originalOffY = 0;
      }
    });

    imgPreview.setOnMouseDragged(e -> {
      if (!chkShowHead.isSelected())
        return;

      // Calcular delta
      // Nota: e.getX() es relativo al nodo (imgPreview).

      double currentScale = imgPreview.getBoundsInLocal().getWidth() / 400.0;

      double deltaX = (e.getX() - dragStartX);
      double deltaY = (e.getY() - dragStartY);

      if (currentScale != 0) {
        deltaX /= currentScale;
        deltaY /= currentScale;
      }

      // Ajustar sensibilidad con redondeo para mejor respuesta
      int changeX = (int) Math.round(deltaX);
      int changeY = (int) Math.round(deltaY);

      int newOffX = originalOffX + changeX;
      int newOffY = originalOffY + changeY;

      // Actualizar UI - esto dispara el listener de textProperty ->
      // requestFrameUpdate
      // Optimización: Podríamos solo actualizar texto y diferir el render si fuera
      // lento.

      txtHeadOffsetX.setText(String.valueOf(newOffX));
      txtHeadOffsetY.setText(String.valueOf(newOffY));
    });

    if (chkShowMarker != null) {
      chkShowMarker.selectedProperty().addListener((obs, ov, nv) -> requestFrameUpdate());
    }
  }

  private int getHeadingIndex() {
    if (rbSur.isSelected())
      return 2;
    if (rbNorte.isSelected())
      return 0;
    if (rbEste.isSelected())
      return 1;
    if (rbOeste.isSelected())
      return 3;
    return 2;
  }

  private void updateEditor(BodyData body) {
    if (body == null)
      return;
    int[] grhs = body.getBody();
    txtNorte.setText(String.valueOf(grhs[0]));
    txtEste.setText(String.valueOf(grhs[1]));
    txtSur.setText(String.valueOf(grhs[2]));
    txtOeste.setText(String.valueOf(grhs[3]));

    txtHeadOffsetX.setText(String.valueOf(body.getHeadOffsetX()));
    txtHeadOffsetY.setText(String.valueOf(body.getHeadOffsetY()));

    startAnimation();
  }

  private void startAnimation() {
    if (selectedBody == null)
      return;

    int headingIdx = getHeadingIndex();
    int grhIndex = selectedBody.getBody()[headingIdx];

    if (grhIndex <= 0) {
      imgPreview.setImage(null);
      if (animationState.getTimeline() != null)
        animationState.getTimeline().stop();
      return;
    }

    GrhData grh = grhDataMap.get(grhIndex);
    if (grh == null)
      return;

    Timeline timeline = animationState.getTimeline();
    if (timeline != null)
      timeline.stop();
    timeline.getKeyFrames().clear();

    animationState.setCurrentFrameIndex(1); // 1-based frames usually
    int nFrames = grh.getNumFrames();

    if (nFrames <= 1) {
      // Static image
      updateFrame(grh);
    } else {
      // Animated
      timeline.getKeyFrames().add(new KeyFrame(Duration.millis(250), e -> {
        updateFrame(grh);
        int next = animationState.getCurrentFrameIndex() + 1;
        if (next > nFrames)
          next = 1;
        animationState.setCurrentFrameIndex(next);
      }));
      timeline.setCycleCount(Animation.INDEFINITE);
      timeline.play();
    }
  }

  private void requestFrameUpdate() {
    // Redraw current frame with new offset settings without resetting animation
    if (selectedBody == null)
      return;
    int headingIdx = getHeadingIndex();
    int grhIndex = selectedBody.getBody()[headingIdx];
    GrhData grh = grhDataMap.get(grhIndex);
    if (grh != null)
      updateFrame(grh); // Uses current frame index stored in state
  }

  private void updateFrame(GrhData rootGrh) {
    // Resolve current frame GRH
    int frameIdx = animationState.getCurrentFrameIndex();
    int[] frames = rootGrh.getFrames();

    // Safety check
    if (frames == null || frames.length == 0) {
      // If no frames, try to render the root grh itself if it's a valid image
      // reference
      // But usually rootGrh of an animation just points to frames.
      // If it has fileNum > 0, maybe it's a single static image treated as GRH?
      if (rootGrh.getFileNum() > 0) {
        renderComposite(rootGrh);
      }
      return;
    }

    if (frameIdx < 1 || frameIdx > frames.length)
      frameIdx = 1;

    // Double check valid index
    if (frameIdx - 1 >= 0 && frameIdx - 1 < frames.length) {
      int frameGrhIndex = frames[frameIdx - 1]; // Array is 0-indexed
      GrhData currentFrame = grhDataMap.get(frameGrhIndex);

      if (currentFrame != null) {
        renderComposite(currentFrame);
      }
    }
  }

  private void renderComposite(GrhData bodyGrh) {
    Image bodyImg = cropGrh(bodyGrh);
    if (bodyImg == null)
      return;

    Image headImg = null;

    if (chkShowHead.isSelected() && ghostHeadData != null) {
      // Necesitamos encontrar el GRH de la cabeza para la orientación actual
      int heading = getHeadingIndex();
      // Asumimos sistema tradicional para visualizar
      headImg = resolveHeadImage(ghostHeadData, heading);
    }

    int offX = 0, offY = 0;
    try {
      offX = Integer.parseInt(txtHeadOffsetX.getText());
      offY = Integer.parseInt(txtHeadOffsetY.getText());
    } catch (Exception e) {
    }

    // Calcular recorte para el cuerpo desde Grh
    // ImageCache.getCroppedImage maneja esto si pasamos la región.

    Image finalBody = cropGrh(bodyGrh);
    if (finalBody == null)
      finalBody = bodyImg; // Redundant but safe

    // Dibujar Composición
    // Offset aditivo puro coincidiendo con lógica VB6 Draw_Grh.
    // HeadXY = BodyXY + BodyHeadOffsetXY
    // Corrección visual (+4 X, -5 Y) basada en ajuste fino manual del usuario.
    boolean showMarker = chkShowMarker != null && chkShowMarker.isSelected();
    WritableImage composite = ImageUtils.drawComposite(finalBody, headImg, 400, 400, offX + 4, offY - 5, showMarker);
    imgPreview.setImage(composite);
  }

  private Image cropGrh(GrhData grh) {
    if (grh == null)
      return null;
    String path = configManager.getGraphicsDir() + grh.getFileNum() + ".png";
    if (!new File(path).exists())
      path = configManager.getGraphicsDir() + grh.getFileNum() + ".bmp";
    if (!new File(path).exists())
      return null;

    return imageCache.getCroppedImage(path, grh.getsX(), grh.getsY(), grh.getTileWidth(), grh.getTileHeight());
  }

  private Image resolveHeadImage(HeadData head, int headingIdx) {
    // headingIdx: 2=Sur, 0=Norte, 1=Este, 3=Oeste
    // Mapeo Estándar de Cabezas: [Norte, Este, Sur, Oeste] -> Coincide con el
    // Cuerpo.

    if (head.getSystemType() == org.nexus.indexador.gamedata.enums.IndexingSystem.TRADITIONAL) {
      int[] grhs = head.getGrhIndex();
      if (grhs == null || headingIdx >= grhs.length)
        return null;
      int hGrhIndex = grhs[headingIdx];
      GrhData hGrh = grhDataMap.get(hGrhIndex);
      if (hGrh == null)
        return null;
      // Asumimos que la cabeza no está animada para la vista previa, usamos frame 1
      if (hGrh.getNumFrames() > 0) {
        int[] frames = hGrh.getFrames();
        if (frames != null && frames.length > 0) {
          int frame1 = frames[0];
          hGrh = grhDataMap.get(frame1);
        }
      }
      return cropGrh(hGrh);
    } else {
      // Sistema de Moldes (No implementado en preview)
      return null;
    }
  }

  public void btnSave_OnAction(ActionEvent actionEvent) {
    if (selectedBody == null)
      return;
    try {
      int[] bodies = selectedBody.getBody();
      bodies[0] = Integer.parseInt(txtNorte.getText());
      bodies[1] = Integer.parseInt(txtEste.getText());
      bodies[2] = Integer.parseInt(txtSur.getText());
      bodies[3] = Integer.parseInt(txtOeste.getText());
      selectedBody.setBody(bodies);

      selectedBody.setHeadOffsetX(Short.parseShort(txtHeadOffsetX.getText()));
      selectedBody.setHeadOffsetY(Short.parseShort(txtHeadOffsetY.getText()));

      dataManager.getIndexLoader().saveBodies(bodyList);
      logger.info("Guardado exitoso.");
    } catch (Exception e) {
      logger.error("Error al guardar", e);
    }
  }

  public void btnAdd_OnAction(ActionEvent actionEvent) {
    BodyData newBody = new BodyData(new int[4], (short) 0, (short) 0);
    bodyList.add(newBody);
    lstBodys.getItems().add(String.valueOf(bodyList.size()));
    lstBodys.getSelectionModel().selectLast();
  }

  public void btnDelete_OnAction(ActionEvent actionEvent) {
    int idx = lstBodys.getSelectionModel().getSelectedIndex();
    if (idx >= 0) {
      bodyList.remove(idx);
      loadBodyData();
    }
  }
}

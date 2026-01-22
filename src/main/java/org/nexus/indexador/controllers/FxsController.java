package org.nexus.indexador.controllers;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.util.Duration;
import org.nexus.indexador.gamedata.DataManager;
import org.nexus.indexador.gamedata.models.FXData;
import org.nexus.indexador.gamedata.models.GrhData;
import org.nexus.indexador.utils.AnimationState;
import org.nexus.indexador.utils.ConfigManager;

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

  private FXData fxDataManager; // Objeto que gestiona los datos de los FXs, incluyendo la carga y
                                // manipulación
                                // de los mismos
  private ObservableList<FXData> fxList;
  private ObservableList<GrhData> grhList;

  private ConfigManager configManager;
  private DataManager dataManager;

  private Map<Integer, AnimationState> animationStates = new HashMap<>();

  // Clase con los datos de la animación y el mapa para la búsqueda rápida
  private Map<Integer, GrhData> grhDataMap;

  // Índice del frame actual en la animación.
  private int currentFrameIndex = 1;
  // Línea de tiempo que controla la animación de los frames en el visor.
  private Timeline animationTimeline;

  /**
   * Inicializa el controlador, cargando la configuración y los datos de los cuerpos.
   */
  @FXML
  protected void initialize() {
    configManager = ConfigManager.getInstance();
    try {
      dataManager = DataManager.getInstance();

      fxDataManager = new FXData(); // Crear una instancia de headData

      animationStates.put(0, new AnimationState());
      animationStates.put(1, new AnimationState());
      animationStates.put(2, new AnimationState());
      animationStates.put(3, new AnimationState());

      loadFxData();
      setupFXListListener();
    } catch (Exception e) {
      System.err.println("Error al inicializar FxsController:");
      e.printStackTrace();
    }
  }

  /**
   * Carga los datos de los cuerpos desde un archivo y los muestra en la interfaz.
   */
  private void loadFxData() {
    // Llamar al método para leer el archivo binario y obtener la lista de headData
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

  }

  /**
   * Configura un listener para el ListView, manejando los eventos de selección de ítems.
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
   * Actualiza el editor de la interfaz con los datos de la cabeza seleccionada.
   *
   * @param selectedFx el objeto headData seleccionado.
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
   * Muestra una animación en el ImageView correspondiente al gráfico seleccionado. Configura y
   * ejecuta una animación de fotogramas clave para mostrar la animación. La animación se ejecuta en
   * un bucle infinito hasta que se detenga explícitamente.
   *
   * @param selectedFX El gráfico seleccionado.
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
   * Actualiza el fotograma actual en el ImageView durante la reproducción de una animación. Obtiene
   * el siguiente fotograma de la animación y actualiza el ImageView con la imagen correspondiente.
   *
   * @param selectedGrh El gráfico seleccionado.
   */
  private void updateFrame(GrhData selectedGrh) {
    int[] frames = selectedGrh.getFrames(); // Obtener el arreglo de índices de los frames de la
                                            // animación

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
          Image frameImage = new Image(imageFile.toURI().toString());

          PixelReader pixelReader = frameImage.getPixelReader();
          WritableImage croppedImage = new WritableImage(pixelReader, currentGrh.getsX(),
              currentGrh.getsY(), currentGrh.getTileWidth(), currentGrh.getTileHeight());
          imgFX.setImage(croppedImage);
        } else {
          // El archivo no existe, mostrar un mensaje de error o registrar un mensaje de
          // advertencia
          System.out.println("updateFrame: El archivo de imagen no existe: " + imagePath);
        }
      } else {
        // No se encontró el GrhData correspondiente
        System.out.println(
            "updateFrame: No se encontró el GrhData correspondiente para frameId: " + frameId);
      }
    } else {
      // El índice actual está fuera del rango adecuado
      System.out.println(
          "updateFrame: El índice actual está fuera del rango adecuado: " + currentFrameIndex);
    }
  }

  public void btnSave_OnAction(ActionEvent actionEvent) {}

  public void btnAdd_OnAction(ActionEvent actionEvent) {}

  public void btnDelete_OnAction(ActionEvent actionEvent) {}
}

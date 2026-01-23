package org.nexus.indexador.controllers;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.beans.value.ObservableValue;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;

import javafx.util.Duration;
import org.nexus.indexador.gamedata.DataManager;
import org.nexus.indexador.gamedata.models.GrhData;
import org.nexus.indexador.gamedata.models.ShieldData;
import org.nexus.indexador.utils.AnimationState;
import org.nexus.indexador.utils.ConfigManager;
import org.nexus.indexador.utils.ImageCache;
import org.nexus.indexador.utils.Logger;

import java.io.File;
import java.io.IOException;

import java.util.HashMap;
import java.util.Map;

public class ShieldsController {

  @FXML
  public ListView lstShields;
  @FXML
  public ImageView imgOeste;
  @FXML
  public ImageView imgNorte;
  @FXML
  public ImageView imgEste;
  @FXML
  public ImageView imgSur;
  @FXML
  public TextField txtNorte;
  @FXML
  public TextField txtEste;
  @FXML
  public TextField txtSur;
  @FXML
  public TextField txtOeste;
  @FXML
  public Label lblNEscudos;
  @FXML
  public Button btnSave;
  @FXML
  public Button btnAdd;
  @FXML
  public Button btnDelete;
  @FXML
  public TextField txtSearch;

  private ShieldData shieldDataManager; // Objeto que gestiona los datos de los escudos, incluyendo
                                        // la carga y
                                        // manipulación de los mismos
  private ObservableList<ShieldData> shieldList;
  private ObservableList<GrhData> grhList;

  private ConfigManager configManager;
  private DataManager dataManager;
  private ImageCache imageCache;
  private Logger logger;

  private Map<Integer, AnimationState> animationStates = new HashMap<>();

  // Clase con los datos de la animación y el mapa para la búsqueda rápida
  private Map<Integer, GrhData> grhDataMap;

  /**
   * Inicializa el controlador, cargando la configuración y los datos de los
   * cuerpos.
   */
  @FXML
  protected void initialize() {
    configManager = ConfigManager.getInstance();
    try {
      dataManager = DataManager.getInstance();
      imageCache = ImageCache.getInstance();
      logger = Logger.getInstance();

      logger.info("Inicializando controlador ShieldsController");

      shieldDataManager = new ShieldData(); // Crear una instancia de headData

      animationStates.put(0, new AnimationState());
      animationStates.put(1, new AnimationState());
      animationStates.put(2, new AnimationState());
      animationStates.put(3, new AnimationState());

      loadShieldData();
      setupHeadListListener();

      logger.info("Controlador ShieldsController inicializado correctamente");
    } catch (Exception e) {
      System.err.println("Error al inicializar ShieldsController:");
      e.printStackTrace();
    }
  }

  /**
   * Carga los datos de los cuerpos desde un archivo y los muestra en la interfaz.
   */
  private void loadShieldData() {
    try {
      // Llamar al método para leer el archivo binario y obtener la lista de headData
      shieldList = dataManager.readShieldFile();

      // Inicializar el mapa de grhData
      grhDataMap = new HashMap<>();

      grhList = dataManager.getGrhList();

      // Llenar el mapa con los datos de grhList
      for (GrhData grh : grhList) {
        grhDataMap.put(grh.getGrh(), grh);
      }

      // Actualizar el texto de los labels con la información obtenida
      lblNEscudos.setText("Escudos cargados: " + dataManager.getNumShields());

      // Agregar los índices de gráficos al ListView
      ObservableList<String> shieldIndices = FXCollections.observableArrayList();
      for (int i = 1; i < shieldList.size() + 1; i++) {
        shieldIndices.add(String.valueOf(i));
      }

      lstShields.setItems(shieldIndices);

      // Configurar FilteredList
      FilteredList<String> filteredData = new FilteredList<>(shieldIndices, p -> true);

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

      lstShields.setItems(filteredData);

      logger.info("Datos de escudos cargados: " + shieldList.size() + " escudos");
    } catch (IOException e) {
      logger.error("Error al cargar datos de escudos", e);
    }

  }

  /**
   * Configura un listener para el ListView, manejando los eventos de selección de
   * ítems.
   */
  private void setupHeadListListener() {
    // Agregar un listener al ListView para capturar los eventos de selección
    lstShields.getSelectionModel().selectedItemProperty()
        .addListener((observable, oldValue, newValue) -> {

          // Obtener el índice seleccionado
          int selectedIndex = lstShields.getSelectionModel().getSelectedIndex();

          if (selectedIndex >= 0) {
            // Obtener el objeto headData correspondiente al índice seleccionado
            ShieldData selectedShield = shieldList.get(selectedIndex);
            updateEditor(selectedShield);

            for (int i = 0; i <= 3; i++) {
              drawShields(selectedShield, i);
            }

            logger.debug("Escudo seleccionado: índice " + (selectedIndex + 1));
          }
        });
  }

  /**
   * Actualiza el editor de la interfaz con los datos de la cabeza seleccionada.
   *
   * @param selectedShield el objeto headData seleccionado.
   */
  private void updateEditor(ShieldData selectedShield) {
    // Obtenemos todos los datos
    int grhShields[] = selectedShield.getShield();

    txtNorte.setText(String.valueOf(grhShields[0]));
    txtEste.setText(String.valueOf(grhShields[1]));
    txtSur.setText(String.valueOf(grhShields[2]));
    txtOeste.setText(String.valueOf(grhShields[3]));
  }

  /**
   * Dibuja las imágenes de los cuerpos en las diferentes vistas (Norte, Sur,
   * Este, Oeste).
   *
   * @param selectedShield el objeto headData seleccionado.
   * @param heading        la dirección en la que se debe dibujar la cabeza (0:
   *                       Sur, 1: Norte, 2: Oeste, 3:
   *                       Este).
   */
  private void drawShields(ShieldData selectedShield, int heading) {
    int[] bodies = selectedShield.getShield();

    // Obtenemos el Grh de animación desde el indice del shield + el heading
    GrhData selectedGrh = grhDataMap.get(bodies[heading]);

    int nFrames = selectedGrh.getNumFrames();

    AnimationState animationState = animationStates.get(heading);
    Timeline animationTimeline = animationState.getTimeline();

    if (animationTimeline != null) {
      animationTimeline.stop();
    }

    animationTimeline.getKeyFrames().clear();
    animationState.setCurrentFrameIndex(1); // Reiniciar el índice del frame a 1

    animationTimeline.getKeyFrames().add(new KeyFrame(Duration.ZERO, event -> {
      // Actualizar la imagen en el ImageView con el frame actual
      updateFrame(selectedGrh, heading);
      animationState.setCurrentFrameIndex((animationState.getCurrentFrameIndex() + 1) % nFrames); // Avanzar
                                                                                                  // al
                                                                                                  // siguiente
                                                                                                  // frame
                                                                                                  // circularmente
      if (animationState.getCurrentFrameIndex() == 0) {
        animationState.setCurrentFrameIndex(1); // Omitir la posición 0
      }
    }));

    animationTimeline.getKeyFrames().add(new KeyFrame(Duration.millis(100)));
    animationTimeline.setCycleCount(Animation.INDEFINITE);
    animationTimeline.play();

    logger.debug("Animación iniciada para dirección " + heading);
  }

  /**
   * Actualiza el fotograma actual en el ImageView durante la reproducción de una
   * animación. Obtiene
   * el siguiente fotograma de la animación y actualiza el ImageView con la imagen
   * correspondiente.
   *
   * @param selectedGrh El gráfico seleccionado.
   */
  private void updateFrame(GrhData selectedGrh, int heading) {
    int[] frames = selectedGrh.getFrames();

    AnimationState animationState = animationStates.get(heading);
    int currentFrameIndex = animationState.getCurrentFrameIndex();

    if (currentFrameIndex >= 0 && currentFrameIndex < frames.length) {
      int frameId = frames[currentFrameIndex];

      // Buscar el GrhData correspondiente al frameId utilizando el mapa
      GrhData currentGrh = grhDataMap.get(frameId);

      if (currentGrh != null) {
        String imagePath = configManager.getGraphicsDir() + currentGrh.getFileNum() + ".png";

        if (!new File(imagePath).exists()) {
          imagePath = configManager.getGraphicsDir() + currentGrh.getFileNum() + ".bmp";
        }

        // Obtener imagen desde el caché
        Image frameImage = imageCache.getImage(imagePath);

        if (frameImage != null) {
          // Obtener la imagen recortada del caché
          WritableImage croppedImage = imageCache.getCroppedImage(imagePath, currentGrh.getsX(),
              currentGrh.getsY(), currentGrh.getTileWidth(), currentGrh.getTileHeight());

          if (croppedImage != null) {
            switch (heading) {
              case 0:
                imgSur.setImage(croppedImage);
                break;
              case 1:
                imgNorte.setImage(croppedImage);
                break;
              case 2:
                imgOeste.setImage(croppedImage);
                break;
              case 3:
                imgEste.setImage(croppedImage);
                break;
            }
          }
        } else {
          logger.warning("Error al cargar la imagen: " + imagePath);
        }
      } else {
        logger.warning("No se encontró el GrhData correspondiente para frameId: " + frameId);
      }
    } else {
      logger.warning("El índice actual está fuera del rango adecuado: " + currentFrameIndex);
    }
  }

  @FXML
  public void btnSave_OnAction(ActionEvent actionEvent) {
    int selectedIndex = lstShields.getSelectionModel().getSelectedIndex();
    if (selectedIndex >= 0) {
      try {
        ShieldData data = shieldList.get(selectedIndex);
        int[] grhs = data.getShield();
        grhs[0] = Integer.parseInt(txtNorte.getText());
        grhs[1] = Integer.parseInt(txtEste.getText());
        grhs[2] = Integer.parseInt(txtSur.getText());
        grhs[3] = Integer.parseInt(txtOeste.getText());
        data.setShield(grhs);

        logger.info("Cambios aplicados en memoria para el escudo " + (selectedIndex + 1));

        // Guardado real en disco
        dataManager.getIndexLoader().saveShields(shieldList);
        logger.info("Escudos guardados en disco correctamente.");
      } catch (Exception e) {
        logger.error("Error al guardar escudos", e);
      }
    }
  }

  @FXML
  public void btnAdd_OnAction(ActionEvent actionEvent) {
    shieldList.add(new ShieldData(new int[4]));
    lstShields.getItems().add(String.valueOf(shieldList.size()));
    lstShields.getSelectionModel().selectLast();
    logger.info("Nuevo escudo añadido.");
  }

  @FXML
  public void btnDelete_OnAction(ActionEvent actionEvent) {
    int selectedIndex = lstShields.getSelectionModel().getSelectedIndex();
    if (selectedIndex >= 0) {
      shieldList.remove(selectedIndex);
      loadShieldData(); // Recargar lista visual
      logger.info("Escudo eliminado.");
    }
  }

}

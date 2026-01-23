/**
 * La clase {@code HeadsController} es un controlador para la interfaz gráfica de usuario (GUI)
 * relacionada con los datos de cabezas. Esta clase maneja la interacción del usuario con la
 * interfaz y gestiona la carga, visualización y manipulación de los datos de las cabezas.
 */
package org.nexus.indexador.controllers;

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

import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import org.nexus.indexador.gamedata.DataManager;
import org.nexus.indexador.gamedata.enums.IndexingSystem;
import org.nexus.indexador.gamedata.models.HeadData;
import org.nexus.indexador.utils.ConfigManager;

import java.io.File;

import java.util.Optional;

import org.nexus.indexador.gamedata.models.GrhData;
import org.nexus.indexador.utils.ImageCache;
import org.nexus.indexador.utils.Logger;

public class HeadsController {

  @FXML
  public ListView lstHeads;
  @FXML
  public ImageView imgOeste;
  @FXML
  public ImageView imgNorte;
  @FXML
  public ImageView imgEste;
  @FXML
  public ImageView imgSur;
  @FXML
  public Label lblNGrafico;
  @FXML
  public Label lblStartX;
  @FXML
  public Label lblStartY;

  @FXML
  public TextField txtNGrafico;
  @FXML
  public TextField txtStartX;
  @FXML
  public TextField txtStartY;

  // Tradicional
  @FXML
  public Label lblHeadUp;
  @FXML
  public TextField txtHeadUp;
  @FXML
  public Label lblHeadRight;
  @FXML
  public TextField txtHeadRight;
  @FXML
  public Label lblHeadDown;
  @FXML
  public TextField txtHeadDown;
  @FXML
  public Label lblHeadLeft;
  @FXML
  public TextField txtHeadLeft;
  @FXML
  public Label lblNCabezas;
  @FXML
  public TextField txtSearch; // New search field
  @FXML
  public Button btnSave;
  @FXML
  public Button btnAdd;
  @FXML
  public Button btnDelete;

  private HeadData headDataManager;
  private ObservableList<HeadData> headList;

  private ConfigManager configManager;
  private DataManager dataManager;
  private ImageCache imageCache;
  private Logger logger;

  /**
   * Inicializa el controlador, cargando la configuración y los datos de las
   * cabezas.
   */
  @FXML
  protected void initialize() {
    configManager = ConfigManager.getInstance();
    try {
      dataManager = DataManager.getInstance();
      imageCache = ImageCache.getInstance();
      logger = Logger.getInstance();

      // headDataManager = new HeadData(); // No se usa, comentado
      loadHeadData();
      setupHeadListListener();
    } catch (Exception e) {
      System.err.println("Error al inicializar HeadsController:");
      e.printStackTrace();
    }
  }

  /**
   * Carga los datos de las cabezas desde la memoria/disco y los muestra en la
   * interfaz.
   */
  private void loadHeadData() {
    // Llamar al método para leer el archivo binario y obtener la lista de headData
    headList = dataManager.getHeadList();

    // Actualizar el texto de los labels con la información obtenida
    lblNCabezas.setText("Cabezas cargadas: " + dataManager.getNumHeads());

    // Crear lista de índices
    ObservableList<String> headIndices = FXCollections.observableArrayList();
    for (int i = 1; i <= headList.size(); i++) {
      headIndices.add(String.valueOf(i));
    }

    // Configurar FilteredList
    FilteredList<String> filteredData = new FilteredList<>(headIndices, p -> true);

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

    lstHeads.setItems(filteredData);

    lstHeads.setItems(filteredData);
  }

  /**
   * Configura un listener para el ListView, manejando los eventos de selección de
   * ítems.
   */
  private void setupHeadListListener() {
    // Agregar un listener al ListView para capturar los eventos de selección
    lstHeads.getSelectionModel().selectedItemProperty()
        .addListener((observable, oldValue, newValue) -> {

          // Obtener el índice seleccionado
          int selectedIndex = lstHeads.getSelectionModel().getSelectedIndex();

          if (selectedIndex >= 0) {
            // Obtener el objeto headData correspondiente al índice seleccionado
            HeadData selectedHead = headList.get(selectedIndex);
            updateEditor(selectedHead);

            for (int i = 0; i <= 3; i++) {
              drawHeads(selectedHead, i);
            }
          }
        });
  }

  /**
   * Actualiza el editor de la interfaz con los datos de la cabeza seleccionada.
   *
   * @param selectedHead el objeto headData seleccionado.
   */
  /**
   * Actualiza el editor de la interfaz con los datos de la cabeza seleccionada.
   *
   * @param selectedHead el objeto HeadData seleccionado.
   */
  private void updateEditor(HeadData selectedHead) {
    // Verificar el tipo de sistema
    if (selectedHead.getSystemType() == IndexingSystem.MOLD) {
      // Sistema de Moldes - mostrar campos normalmente
      setVisibleMold(true);
      setVisibleTraditional(false);

      short Texture = selectedHead.getTexture();
      short StartX = selectedHead.getStartX();
      short StartY = selectedHead.getStartY();

      txtNGrafico.setText(String.valueOf(Texture));
      txtStartX.setText(String.valueOf(StartX));
      txtStartY.setText(String.valueOf(StartY));
    } else {
      // Sistema Tradicional
      setVisibleMold(false);
      setVisibleTraditional(true);

      int[] grhs = selectedHead.getGrhIndex();
      // [Norte, Sur, Este, Oeste]
      if (grhs != null && grhs.length >= 4) {
        txtHeadUp.setText(String.valueOf(grhs[0])); // Norte
        txtHeadDown.setText(String.valueOf(grhs[2])); // Sur (Standard AO: Index 2)
        txtHeadRight.setText(String.valueOf(grhs[1])); // Este (Standard AO: Index 1)
        txtHeadLeft.setText(String.valueOf(grhs[3])); // Oeste
      }
    }
  }

  private void setVisibleMold(boolean visible) {
    txtNGrafico.setVisible(visible);
    txtStartX.setVisible(visible);
    txtStartY.setVisible(visible);
    lblNGrafico.setVisible(visible);
    lblStartX.setVisible(visible);
    lblStartY.setVisible(visible);
  }

  private void setVisibleTraditional(boolean visible) {
    txtHeadUp.setVisible(visible);
    txtHeadDown.setVisible(visible);
    txtHeadRight.setVisible(visible);
    txtHeadLeft.setVisible(visible);
    lblHeadUp.setVisible(visible);
    lblHeadDown.setVisible(visible);
    lblHeadRight.setVisible(visible);
    lblHeadLeft.setVisible(visible);
  }

  /**
   * Dibuja las imágenes de las cabezas en las diferentes vistas (Norte, Sur,
   * Este, Oeste).
   *
   * @param selectedHead el objeto headData seleccionado.
   * @param heading      la dirección en la que se debe dibujar la cabeza (0: Sur,
   *                     1: Norte, 2: Oeste, 3:
   *                     Este).
   */
  private void drawHeads(HeadData selectedHead, int heading) {
    // Verificar el tipo de sistema
    if (selectedHead.getSystemType() == IndexingSystem.TRADITIONAL) {
      // Sistema Tradicional - usar grhs directamente
      drawTraditionalHead(selectedHead, heading);
      return;
    }

    // Sistema de Moldes - código original
    // Construir la ruta completa de la imagen para imagePath
    String imagePath = configManager.getGraphicsDir() + selectedHead.getTexture() + ".png";

    if (!new File(imagePath).exists()) {
      imagePath = configManager.getGraphicsDir() + selectedHead.getTexture() + ".bmp";
    }

    File imageFile = new File(imagePath);

    // ¿La imagen existe?
    if (imageFile.exists()) {
      Image staticImage = imageCache.getImage(imagePath);

      if (staticImage != null) {
        int textureX2 = 27;
        int textureY2 = 32;
        int textureX1 = selectedHead.getStartX();
        int textureY1 = (heading * textureY2) + selectedHead.getStartY();

        // Verificar que las coordenadas de recorte estén dentro de los límites de la
        // imagen
        if (textureX1 + textureX2 > staticImage.getWidth()) {
          textureX1 = (int) staticImage.getWidth() - textureX2;
        }
        if (textureY1 + textureY2 > staticImage.getHeight()) {
          textureY1 = (int) staticImage.getHeight() - textureY2;
        }

        // Recortar la región adecuada de la imagen completa
        WritableImage croppedImage = imageCache.getCroppedImage(imagePath, textureX1, textureY1, textureX2, textureY2);

        // Desactivar la preservación de la relación de aspecto
        imgNorte.setPreserveRatio(false);

        // Mostrar la región recortada en el ImageView correspondiente
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
          default:
            // Dirección desconocida
            System.out.println("Dirección desconocida: " + heading);
            break;
        }
      }

    } else {
      // El archivo no existe, mostrar un mensaje de error o registrar un mensaje de
      // advertencia
      System.out.println("displayStaticImage: El archivo de imagen no existe: " + imagePath);

      // Limpiar todos los ImageViews
      imgSur.setImage(null);
      imgNorte.setImage(null);
      imgOeste.setImage(null);
      imgEste.setImage(null);
    }
  }

  /**
   * Dibuja cabezas usando el sistema tradicional (grhs directos).
   */
  private void drawTraditionalHead(HeadData selectedHead, int heading) {
    int[] grhs = selectedHead.getGrhIndex();
    // heading: 0: Sur, 1: Norte, 2: Oeste, 3: Este
    // grhs indices: 0: Norte, 1: Sur, 2: Este, 3: Oeste (según loader)

    // Mapear heading a índice del array grhs
    int grhIndex = -1;
    switch (heading) {
      case 0:
        grhIndex = 2; // Sur -> Index 2
        break;
      case 1:
        grhIndex = 0; // Norte -> Index 0
        break;
      case 2:
        grhIndex = 3; // Oeste -> Index 3
        break;
      case 3:
        grhIndex = 1; // Este -> Index 1
        break;
    }

    if (grhIndex >= 0 && grhIndex < grhs.length) {
      int grhId = grhs[grhIndex];
      if (grhId > 0) {
        GrhData grhData = dataManager.getGrh(grhId);
        if (grhData != null) {
          Image img = loadImageFromGrh(grhData);
          ImageView targetView = null;
          switch (heading) {
            case 0:
              targetView = imgSur;
              break;
            case 1:
              targetView = imgNorte;
              break;
            case 2:
              targetView = imgOeste;
              break;
            case 3:
              targetView = imgEste;
              break;
          }
          if (targetView != null) {
            targetView.setImage(img);
            // Ajustar si queremos pixel art nítido
            targetView.setSmooth(false);
            // Ajuste visual para centrar cabezas (que suelen tener espacio vacío abajo)
            targetView.setTranslateY(15);
          }
        }
      }
    }
  }

  private Image loadImageFromGrh(GrhData grh) {
    if (grh == null)
      return null;
    try {
      String fileName = String.valueOf(grh.getFileNum());
      String imagePath = configManager.getGraphicsDir() + fileName + ".png";
      if (!new File(imagePath).exists()) {
        imagePath = configManager.getGraphicsDir() + fileName + ".bmp";
      }
      File file = new File(imagePath);
      if (!file.exists())
        return null;

      Image fullImage = imageCache.getImage(imagePath);
      if (fullImage == null)
        return null;

      int x = grh.getsX();
      int y = grh.getsY();
      int w = grh.getTileWidth();
      int h = grh.getTileHeight();

      if (x + w > fullImage.getWidth())
        w = (int) fullImage.getWidth() - x;
      if (y + h > fullImage.getHeight())
        h = (int) fullImage.getHeight() - y;
      if (w <= 0 || h <= 0)
        return null;

      return imageCache.getCroppedImage(imagePath, x, y, w, h);
    } catch (Exception e) {
      // e.printStackTrace();
      return null;
    }
  }

  public void btnSave_OnAction(ActionEvent actionEvent) {
    // Obtenemos el índice seleccionado en la lista:
    int selectedHeadIndex = lstHeads.getSelectionModel().getSelectedIndex();

    // Nos aseguramos de que el índice es válido
    if (selectedHeadIndex >= 0) {
      // Obtenemos el objeto headData correspondiente al índice seleccionado
      HeadData selectedHead = headList.get(selectedHeadIndex);

      // Comenzamos aplicar los cambios:
      try {
        if (selectedHead.getSystemType() == IndexingSystem.MOLD) {
          selectedHead.setTexture(Short.parseShort(txtNGrafico.getText()));
          selectedHead.setStartX(Short.parseShort(txtStartX.getText()));
          selectedHead.setStartY(Short.parseShort(txtStartY.getText()));
        } else {
          // Tradicional
          int[] grhs = new int[4];
          selectedHead.getGrhIndex()[0] = Integer.parseInt(txtHeadUp.getText());
          selectedHead.getGrhIndex()[1] = Integer.parseInt(txtHeadDown.getText());
          selectedHead.getGrhIndex()[2] = Integer.parseInt(txtHeadRight.getText());
          selectedHead.getGrhIndex()[3] = Integer.parseInt(txtHeadLeft.getText());
          selectedHead.setGrhs(grhs);
        }

        // Recargar visualizacion
        drawHeads(selectedHead, 0);
        drawHeads(selectedHead, 1);
        drawHeads(selectedHead, 2);
        drawHeads(selectedHead, 3);

        System.out.println(("¡Cambios aplicados!"));
      } catch (NumberFormatException e) {
        System.out.println("Error de formato numérico: " + e.getMessage());
      }
    }
  }

  /**
   * Maneja el evento de acción del botón "Agregar". Crea una nueva cabeza vacía.
   */
  @FXML
  private void btnAdd_OnAction() {
    int headCount = dataManager.getNumHeads() + 1;

    // Incrementar el contador de headDataManager
    dataManager.setNumHeads((short) headCount);

    // Crear un nuevo objeto headData con los valores adecuados
    HeadData newHeadData = new HeadData(1, (short) 0, (short) 0, (short) 0);

    // Agregar el nuevo elemento al ListView
    lstHeads.getItems().add(String.valueOf(headCount));

    // Agregar el nuevo elemento a headList
    headList.add(newHeadData);
  }

  /**
   * Maneja el evento de acción del botón "Eliminar". Elimina el objeto headData
   * seleccionado de la
   * lista.
   *
   * @param actionEvent el evento de acción del botón.
   */
  public void btnDelete_OnAction(ActionEvent actionEvent) {
    int selectedIndex = lstHeads.getSelectionModel().getSelectedIndex();

    if (selectedIndex != -1) {
      Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
      alert.setTitle("Confirmación");
      alert.setHeaderText("¿Estás seguro de que quieres eliminar este elemento?");
      alert.setContentText("Esta acción no se puede deshacer.");

      Optional<ButtonType> result = alert.showAndWait();
      if (result.isPresent() && result.get() == ButtonType.OK) {
        headList.remove(selectedIndex);
        loadHeadData(); // Recargar para actualizar índices y filtros
      }
    }
  }

}

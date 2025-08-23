/**
 * La clase {@code frmCabezas} es un controlador para la interfaz gráfica de usuario (GUI) relacionada con los datos de cabezas.
 * Esta clase maneja la interacción del usuario con la interfaz y gestiona la carga, visualización y manipulación de los datos de las cabezas.
 */
package org.nexus.indexador.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import org.nexus.indexador.gamedata.DataManager;
import org.nexus.indexador.gamedata.models.HeadData;
import org.nexus.indexador.utils.ConfigManager;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

public class frmCabezas {

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
    public TextField txtNGrafico;
    @FXML
    public TextField txtStartX;
    @FXML
    public TextField txtStartY;
    @FXML
    public Label lblNCabezas;
    @FXML
    public Button btnSave;
    @FXML
    public Button btnAdd;
    @FXML
    public Button btnDelete;

    private HeadData headDataManager; // Objeto que gestiona los datos de las cabezas, incluyendo la carga y manipulación de los mismos
    private ObservableList<HeadData> headList; // Lista observable que contiene los datos de los gráficos indexados.

    private ConfigManager configManager; // Objeto encargado de manejar la configuración de la aplicación, incluyendo la lectura y escritura de archivos de configuración.
    private DataManager dataManager;

    /**
     * Inicializa el controlador, cargando la configuración y los datos de las cabezas.
     */
    @FXML
    protected void initialize() throws IOException {
        configManager = ConfigManager.getInstance();
        dataManager = DataManager.getInstance();

        headDataManager = new HeadData(); // Crear una instancia de headData
        loadHeadData();
        setupHeadListListener();
    }

    /**
     * Carga los datos de las cabezas desde un archivo y los muestra en la interfaz.
     */
    private void loadHeadData() {
        // Llamar al método para leer el archivo binario y obtener la lista de headData
        headList = dataManager.getHeadList();

        // Actualizar el texto de los labels con la información obtenida
        lblNCabezas.setText("Cabezas cargadas: " + dataManager.getNumHeads());

        // Agregar los índices de gráficos al ListView
        ObservableList<String> headIndices = FXCollections.observableArrayList();
        for (int i = 1; i < headList.size() + 1; i++) {
            headIndices.add(String.valueOf(i));
        }

        lstHeads.setItems(headIndices);

    }

    /**
     * Configura un listener para el ListView, manejando los eventos de selección de ítems.
     */
    private void setupHeadListListener() {
        // Agregar un listener al ListView para capturar los eventos de selección
        lstHeads.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {

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
    private void updateEditor(HeadData selectedHead) {
        // Obtenemos todos los datos
        short Texture = selectedHead.getTexture();
        short StartX = selectedHead.getStartX();
        short StartY = selectedHead.getStartY();

        txtNGrafico.setText(String.valueOf(Texture));
        txtStartX.setText(String.valueOf(StartX));
        txtStartY.setText(String.valueOf(StartY));
    }

    /**
     * Dibuja las imágenes de las cabezas en las diferentes vistas (Norte, Sur, Este, Oeste).
     *
     * @param selectedHead el objeto headData seleccionado.
     * @param heading la dirección en la que se debe dibujar la cabeza (0: Sur, 1: Norte, 2: Oeste, 3: Este).
     */
    private void drawHeads(HeadData selectedHead, int heading) {
        // Construir la ruta completa de la imagen para imagePath
        String imagePath = configManager.getGraphicsDir() + selectedHead.getTexture() + ".png";

        if (!new File(imagePath).exists()) {
            imagePath = configManager.getGraphicsDir() + selectedHead.getTexture() + ".bmp";
        }

        File imageFile = new File(imagePath);

        // ¿La imagen existe?
        if (imageFile.exists()) {
            Image staticImage = new Image(imageFile.toURI().toString());

            int textureX2 = 27;
            int textureY2 = 32;
            int textureX1 = selectedHead.getStartX();
            int textureY1 = (heading * textureY2) + selectedHead.getStartY();

            // Verificar que las coordenadas de recorte estén dentro de los límites de la imagen
            if (textureX1 + textureX2 > staticImage.getWidth()) {
                textureX1 = (int) staticImage.getWidth() - textureX2;
            }
            if (textureY1 + textureY2 > staticImage.getHeight()) {
                textureY1 = (int) staticImage.getHeight() - textureY2;
            }

            // Recortar la región adecuada de la imagen completa
            PixelReader pixelReader = staticImage.getPixelReader();
            WritableImage croppedImage = new WritableImage(pixelReader, textureX1, textureY1, textureX2, textureY2);

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

        } else {
            // El archivo no existe, mostrar un mensaje de error o registrar un mensaje de advertencia
            System.out.println("displayStaticImage: El archivo de imagen no existe: " + imagePath);

            // Limpiar todos los ImageViews
            imgSur.setImage(null);
            imgNorte.setImage(null);
            imgOeste.setImage(null);
            imgEste.setImage(null);
        }
    }

    /**
     * Maneja el evento de acción del botón "Guardar". Aplica los cambios al objeto headData seleccionado.
     *
     * @param actionEvent el evento de acción del botón.
     */
    public void btnSave_OnAction(ActionEvent actionEvent) {
        // Obtenemos el índice seleccionado en la lista:
        int selectedHeadIndex = lstHeads.getSelectionModel().getSelectedIndex();

        // Nos aseguramos de que el índice es válido
        if (selectedHeadIndex >= 0) {
            // Obtenemos el objeto headData correspondiente al índice seleccionado
            HeadData selectedHead = headList.get(selectedHeadIndex);

            // Comenzamos aplicar los cambios:
            selectedHead.setTexture(Short.parseShort(txtNGrafico.getText()));
            selectedHead.setStartX(Short.parseShort(txtStartX.getText()));
            selectedHead.setStartY(Short.parseShort(txtStartY.getText()));

            System.out.println(("¡Cambios aplicados!"));
        }
    }

    /**
     * Maneja el evento de acción del botón "Agregar". Agrega un nuevo objeto headData a la lista.
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
     * Maneja el evento de acción del botón "Eliminar". Elimina el objeto headData seleccionado de la lista.
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
                lstHeads.getItems().remove(selectedIndex);
                headList.remove(selectedIndex);
            }
        }
    }
}
/**
 * La clase {@code frmCascos} es un controlador para la interfaz gráfica de usuario (GUI) relacionada con los datos de cascos.
 * Esta clase maneja la interacción del usuario con la interfaz y gestiona la carga, visualización y manipulación de los datos de los cascos.
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
import org.nexus.indexador.gamedata.enums.IndexingSystem;
import org.nexus.indexador.gamedata.models.HelmetData;
import org.nexus.indexador.utils.ConfigManager;

import java.io.File;

import java.util.Optional;

import org.nexus.indexador.Main;
import org.nexus.indexador.gamedata.models.GrhData;

public class frmCascos {

    @FXML
    public ListView lstHelmets;
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
    public Label lblHelmUp;
    @FXML
    public TextField txtHelmUp;
    @FXML
    public Label lblHelmRight;
    @FXML
    public TextField txtHelmRight;
    @FXML
    public Label lblHelmDown;
    @FXML
    public TextField txtHelmDown;
    @FXML
    public Label lblHelmLeft;
    @FXML
    public TextField txtHelmLeft;
    @FXML
    public Label lblNCascos;
    @FXML
    public Button btnSave;
    @FXML
    public Button btnAdd;
    @FXML
    public Button btnDelete;

    private HelmetData helmetDataManager; // Objeto que gestiona los datos de los cascos, incluyendo la carga y
                                          // manipulación de los mismos
    private ObservableList<HelmetData> helmetList; // Lista observable que contiene los datos de los gráficos indexados.

    private ConfigManager configManager; // Objeto encargado de manejar la configuración de la aplicación, incluyendo la
                                         // lectura y escritura de archivos de configuración.
    private DataManager dataManager;

    /**
     * Inicializa el controlador, cargando la configuración y los datos de los
     * cascos.
     */
    @FXML
    protected void initialize() {
        configManager = ConfigManager.getInstance();
        try {
            dataManager = DataManager.getInstance();
            // helmetDataManager = new HelmetData(); // No se usa, comentado
            loadHelmetData();
            setupHelmetListListener();
        } catch (Exception e) {
            System.err.println("Error al inicializar frmCascos:");
            e.printStackTrace();
        }
    }

    /**
     * Carga los datos de los cascos desde un archivo y los muestra en la interfaz.
     */
    private void loadHelmetData() {
        // Llamar al método para leer el archivo binario y obtener la lista de
        // helmetData
        helmetList = dataManager.getHelmetList();

        // Actualizar el texto de los labels con la información obtenida
        lblNCascos.setText("Cascos cargados: " + dataManager.getNumHelmets());

        // Agregar los índices de gráficos al ListView
        ObservableList<String> helmetIndices = FXCollections.observableArrayList();
        for (int i = 1; i < helmetList.size() + 1; i++) {
            helmetIndices.add(String.valueOf(i));
        }

        lstHelmets.setItems(helmetIndices);

    }

    /**
     * Configura un listener para el ListView, manejando los eventos de selección de
     * ítems.
     */
    private void setupHelmetListListener() {
        // Agregar un listener al ListView para capturar los eventos de selección
        lstHelmets.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {

            // Obtener el índice seleccionado
            int selectedIndex = lstHelmets.getSelectionModel().getSelectedIndex();

            if (selectedIndex >= 0) {
                // Obtener el objeto helmetData correspondiente al índice seleccionado
                HelmetData selectedHelmet = helmetList.get(selectedIndex);
                updateEditor(selectedHelmet);

                for (int i = 0; i <= 3; i++) {
                    drawHelmets(selectedHelmet, i);
                }
            }
        });
    }

    /**
     * Actualiza el editor de la interfaz con los datos del casco seleccionado.
     *
     * @param selectedHelmet el objeto helmetData seleccionado.
     */
    /**
     * Actualiza el editor de la interfaz con los datos del casco seleccionado.
     *
     * @param selectedHelmet el objeto helmetData seleccionado.
     */
    private void updateEditor(HelmetData selectedHelmet) {
        // Verificar el tipo de sistema
        if (selectedHelmet.getSystemType() == IndexingSystem.MOLD) {
            // Sistema de Moldes - mostrar campos normalmente
            setVisibleMold(true);
            setVisibleTraditional(false);

            short Texture = selectedHelmet.getTexture();
            short StartX = selectedHelmet.getStartX();
            short StartY = selectedHelmet.getStartY();

            txtNGrafico.setText(String.valueOf(Texture));
            txtStartX.setText(String.valueOf(StartX));
            txtStartY.setText(String.valueOf(StartY));
        } else {
            // Sistema Tradicional
            setVisibleMold(false);
            setVisibleTraditional(true);

            int[] grhs = selectedHelmet.getGrhIndex();
            // [Norte, Sur, Este, Oeste]
            if (grhs != null && grhs.length >= 4) {
                txtHelmUp.setText(String.valueOf(grhs[0])); // Norte
                txtHelmDown.setText(String.valueOf(grhs[2])); // Sur (Standard AO: Index 2)
                txtHelmRight.setText(String.valueOf(grhs[1])); // Este (Standard AO: Index 1)
                txtHelmLeft.setText(String.valueOf(grhs[3])); // Oeste
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
        txtHelmUp.setVisible(visible);
        txtHelmDown.setVisible(visible);
        txtHelmRight.setVisible(visible);
        txtHelmLeft.setVisible(visible);
        lblHelmUp.setVisible(visible);
        lblHelmDown.setVisible(visible);
        lblHelmRight.setVisible(visible);
        lblHelmLeft.setVisible(visible);
    }

    /**
     * Dibuja las imágenes de los cascos en las diferentes vistas (Norte, Sur, Este,
     * Oeste).
     *
     * @param selectedHelmet el objeto helmetData seleccionado.
     * @param helmeting      la dirección en la que se debe dibujar el casco (0:
     *                       Sur, 1: Norte, 2: Oeste, 3: Este).
     */
    private void drawHelmets(HelmetData selectedHelmet, int helmeting) {
        // Verificar el tipo de sistema
        if (selectedHelmet.getSystemType() == IndexingSystem.TRADITIONAL) {
            // Sistema Tradicional - usar grhs directamente
            drawTraditionalHelmet(selectedHelmet, helmeting);
            return;
        }

        // Sistema de Moldes - código original
        // Construir la ruta completa de la imagen para imagePath
        String imagePath = configManager.getGraphicsDir() + selectedHelmet.getTexture() + ".png";

        if (!new File(imagePath).exists()) {
            imagePath = configManager.getGraphicsDir() + selectedHelmet.getTexture() + ".bmp";
        }

        File imageFile = new File(imagePath);

        // ¿La imagen existe?
        if (imageFile.exists()) {
            Image staticImage = new Image(imageFile.toURI().toString());

            int textureX2 = 27;
            int textureY2 = 32;
            int textureX1 = selectedHelmet.getStartX();
            int textureY1 = (helmeting * textureY2) + selectedHelmet.getStartY();

            // Verificar que las coordenadas de recorte estén dentro de los límites de la
            // imagen
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
            switch (helmeting) {
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
                    System.out.println("Dirección desconocida: " + helmeting);
                    break;
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
     * Dibuja cascos usando el sistema tradicional (grhs directos).
     */
    private void drawTraditionalHelmet(HelmetData selectedHelmet, int helmeting) {
        int[] grhs = selectedHelmet.getGrhIndex();

        int grhIndex = -1;
        switch (helmeting) {
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
            if (grhId > 0 && Main.sharedGrhData != null) {
                GrhData grhData = Main.sharedGrhData.get(grhId);
                if (grhData != null) {
                    Image img = loadImageFromGrh(grhData);
                    ImageView targetView = null;
                    switch (helmeting) {
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
                        targetView.setSmooth(false);
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

            Image fullImage = new Image(file.toURI().toString());
            PixelReader reader = fullImage.getPixelReader();

            // Usar getsX/getsY
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

            return new WritableImage(reader, x, y, w, h);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Maneja el evento de acción del botón "Guardar". Aplica los cambios al objeto
     * helmetData seleccionado.
     *
     * @param actionEvent el evento de acción del botón.
     */
    public void btnSave_OnAction(ActionEvent actionEvent) {
        // Obtenemos el índice seleccionado en la lista:
        int selectedHelmetIndex = lstHelmets.getSelectionModel().getSelectedIndex();

        // Nos aseguramos de que el índice es válido
        if (selectedHelmetIndex >= 0) {
            // Obtenemos el objeto helmetData correspondiente al índice seleccionado
            HelmetData selectedHelmet = helmetList.get(selectedHelmetIndex);

            // Comenzamos aplicar los cambios:
            try {
                if (selectedHelmet.getSystemType() == IndexingSystem.MOLD) {
                    selectedHelmet.setTexture(Short.parseShort(txtNGrafico.getText()));
                    selectedHelmet.setStartX(Short.parseShort(txtStartX.getText()));
                    selectedHelmet.setStartY(Short.parseShort(txtStartY.getText()));
                } else {
                    // Tradicional
                    int[] grhs = new int[4];
                    grhs[0] = Integer.parseInt(txtHelmUp.getText());
                    grhs[1] = Integer.parseInt(txtHelmDown.getText());
                    grhs[2] = Integer.parseInt(txtHelmRight.getText());
                    grhs[3] = Integer.parseInt(txtHelmLeft.getText());
                    selectedHelmet.setGrhs(grhs);
                }

                // Recargar visualizacion
                drawHelmets(selectedHelmet, 0);
                drawHelmets(selectedHelmet, 1);
                drawHelmets(selectedHelmet, 2);
                drawHelmets(selectedHelmet, 3);

                System.out.println(("¡Cambios aplicados!"));
            } catch (NumberFormatException e) {
                System.out.println("Error de formato numérico: " + e.getMessage());
            }
        }
    }

    /**
     * Maneja el evento de acción del botón "Agregar". Agrega un nuevo objeto
     * helmetData a la lista.
     */
    @FXML
    private void btnAdd_OnAction() {
        int helmetCount = dataManager.getNumHelmets() + 1;

        // Incrementar el contador de helmetDataManager
        dataManager.setNumHelmets((short) helmetCount);

        // Crear un nuevo objeto helmetData con los valores adecuados
        HelmetData newHelmetData = new HelmetData(2, (short) 0, (short) 0, (short) 0);

        // Agregar el nuevo elemento al ListView
        lstHelmets.getItems().add(String.valueOf(helmetCount));

        // Agregar el nuevo elemento a helmetList
        helmetList.add(newHelmetData);
    }

    /**
     * Maneja el evento de acción del botón "Eliminar". Elimina el objeto helmetData
     * seleccionado de la lista.
     *
     * @param actionEvent el evento de acción del botón.
     */
    public void btnDelete_OnAction(ActionEvent actionEvent) {
        int selectedIndex = lstHelmets.getSelectionModel().getSelectedIndex();

        if (selectedIndex != -1) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmación");
            alert.setHeaderText("¿Estás seguro de que quieres eliminar este elemento?");
            alert.setContentText("Esta acción no se puede deshacer.");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                lstHelmets.getItems().remove(selectedIndex);
                helmetList.remove(selectedIndex);
            }
        }
    }
}
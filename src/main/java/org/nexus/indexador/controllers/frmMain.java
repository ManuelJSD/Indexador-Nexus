package org.nexus.indexador.controllers;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.util.Duration;
import javafx.fxml.FXML;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Rectangle;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.nexus.indexador.gamedata.DataManager;
import org.nexus.indexador.gamedata.models.GrhData;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.nexus.indexador.utils.byteMigration;
import org.nexus.indexador.utils.ConfigManager;
import org.nexus.indexador.utils.ExportService;
import org.nexus.indexador.utils.ImageCache;
import org.nexus.indexador.utils.Logger;
import org.nexus.indexador.utils.UndoManager;
import org.nexus.indexador.utils.ValidationService;
import org.nexus.indexador.utils.WindowManager;

import java.io.*;
import java.util.*;

public class frmMain {

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

    @FXML
    private ImageView imgIndice;

    @FXML
    private ImageView imgGrafico;

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

    // Lista observable que contiene los datos de los gráficos indexados.
    private ObservableList<GrhData> grhList;

    // Clase con los datos de la animación y el mapa para la búsqueda rápida
    private Map<Integer, GrhData> grhDataMap;

    // Objeto encargado de manejar la configuración de la aplicación, incluyendo la
    // lectura y escritura de archivos de configuración.
    private ConfigManager configManager;

    private byteMigration byteMigration;

    private DataManager dataManager;

    // Caché de imágenes para optimizar la carga y uso de recursos
    private ImageCache imageCache;

    // Logger para registro de eventos
    private Logger logger;

    // Gestor de ventanas
    private WindowManager windowManager;

    // Índice del frame actual en la animación.
    private int currentFrameIndex = 1;
    // Línea de tiempo que controla la animación de los frames en el visor.
    private Timeline animationTimeline;

    // Coordenadas originales del cursor del mouse en la escena al presionar el
    // botón del mouse.
    private double orgSceneX, orgSceneY;

    // Valores de traducción originales del ImageView al arrastrar el mouse.
    private double orgTranslateX, orgTranslateY;

    /**
     * Método de inicialización del controlador. Carga los datos de gráficos y
     * configura el ListView.
     */
    @FXML
    protected void initialize() throws IOException {

        // Obtener instancias de configManager y byteMigration
        configManager = ConfigManager.getInstance();
        byteMigration = org.nexus.indexador.utils.byteMigration.getInstance();
        dataManager = org.nexus.indexador.gamedata.DataManager.getInstance();
        imageCache = ImageCache.getInstance();
        logger = Logger.getInstance();
        windowManager = WindowManager.getInstance();

        logger.info("Inicializando controlador frmMain");

        loadGrh();
        setupGrhListListener();
        setupFilterTextFieldListener();
        setupSliderZoom();

        logger.info("Controlador frmMain inicializado correctamente");
    }

    /**
     * Carga los datos de gráficos desde archivos binarios y actualiza la interfaz
     * de usuario con la información obtenida.
     * Muestra los índices de gráficos en el ListView y actualiza los textos de los
     * labels con información relevante.
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

    /**
     * Configura un listener para el ListView para capturar los eventos de
     * selección.
     * Cuando se selecciona un índice de gráfico, actualiza el editor y el visor con
     * la información correspondiente.
     */
    private void setupGrhListListener() {
        // Agregar un listener al ListView para capturar los eventos de selección
        lstIndices.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {

            // Detenemos la animación actual si existe
            if (animationTimeline != null) {
                animationTimeline.stop();
            }

            // Obtener el índice seleccionado
            int selectedIndex = lstIndices.getSelectionModel().getSelectedIndex();

            if (selectedIndex >= 0) {
                // Obtener el objeto grhData correspondiente al índice seleccionado
                GrhData selectedGrh = grhList.get(selectedIndex);
                updateEditor(selectedGrh);
                updateViewer(selectedGrh);
            }
        });
    }

    /**
     * Actualiza el editor con la información del gráfico seleccionado.
     * Muestra los detalles del gráfico seleccionado en los campos de texto
     * correspondientes.
     *
     * @param selectedGrh El gráfico seleccionado.
     */
    private void updateEditor(GrhData selectedGrh) {
        // Obtenemos todos los datos
        int fileGrh = selectedGrh.getFileNum();
        int nFrames = selectedGrh.getNumFrames();
        int x = selectedGrh.getsX();
        int y = selectedGrh.getsY();
        int width = selectedGrh.getTileWidth();
        int height = selectedGrh.getTileHeight();
        float speed = selectedGrh.getSpeed();

        txtImagen.setText(String.valueOf(fileGrh));
        txtPosX.setText(String.valueOf(x));
        txtPosY.setText(String.valueOf(y));
        txtAncho.setText(String.valueOf(width));
        txtAlto.setText(String.valueOf(height));
        txtSpeed.setText(String.valueOf(speed));

        if (nFrames == 1) { // ¿Es estatico?

            txtIndice.setText("Grh" + selectedGrh.getGrh() + "=" + nFrames + "-" + fileGrh + "-" + x + "-" + y + "-"
                    + width + "-" + height);

            lstFrames.getItems().clear();

        } else { // Entonces es animación...

            StringBuilder frameText = new StringBuilder();

            // Agregar los índices de gráficos al ListView
            ObservableList<String> grhIndices = FXCollections.observableArrayList();

            int[] frames = selectedGrh.getFrames();

            for (int i = 1; i < selectedGrh.getNumFrames() + 1; i++) {
                String frame = String.valueOf(frames[i]);
                grhIndices.add(frame);

                frameText.append("-").append(frame);
            }

            lstFrames.setItems(grhIndices);

            txtIndice.setText("Grh" + selectedGrh.getGrh() + "=" + nFrames + frameText + "-" + speed);
        }
    }

    /**
     * Actualiza el visor con el gráfico seleccionado.
     * Si el gráfico es estático, muestra la imagen estática correspondiente. Si es
     * una animación, muestra la animación.
     *
     * @param selectedGrh El gráfico seleccionado.
     */
    private void updateViewer(GrhData selectedGrh) {
        int nFrames = selectedGrh.getNumFrames();
        if (nFrames == 1) {
            displayStaticImage(selectedGrh);
        } else {
            displayAnimation(selectedGrh, nFrames);
        }

    }

    /**
     * Muestra una imagen estática en el ImageView correspondiente al gráfico
     * seleccionado.
     * Si el archivo de imagen existe, carga la imagen y la muestra en el ImageView.
     * Además, recorta la región adecuada de la imagen completa para mostrar solo la
     * parte relevante del gráfico.
     * Si el archivo de imagen no existe, imprime un mensaje de advertencia.
     *
     * @param selectedGrh El gráfico seleccionado.
     */
    private void displayStaticImage(GrhData selectedGrh) {
        // Construir la ruta completa de la imagen para imagePath
        String imagePath = configManager.getGraphicsDir() + selectedGrh.getFileNum() + ".png";

        if (!new File(imagePath).exists()) {
            imagePath = configManager.getGraphicsDir() + selectedGrh.getFileNum() + ".bmp";
        }

        // Usar el caché de imágenes para obtener la imagen
        Image staticImage = imageCache.getImage(imagePath);

        if (staticImage != null) {
            // Mandamos a dibujar el grafico completo en otro ImageView
            drawFullImage(staticImage, selectedGrh);

            // Obtener la imagen recortada del caché
            WritableImage croppedImage = imageCache.getCroppedImage(
                    imagePath,
                    selectedGrh.getsX(),
                    selectedGrh.getsY(),
                    selectedGrh.getTileWidth(),
                    selectedGrh.getTileHeight());

            if (croppedImage != null) {
                // Establecer el tamaño preferido del ImageView para que coincida con el tamaño
                // de la imagen
                imgIndice.setFitWidth(selectedGrh.getTileWidth()); // Ancho de la imagen
                imgIndice.setFitHeight(selectedGrh.getTileHeight()); // Alto de la imagen

                // Desactivar la preservación de la relación de aspecto
                imgIndice.setPreserveRatio(false);

                // Mostrar la región recortada en el ImageView
                imgIndice.setImage(croppedImage);
            }
        } else {
            logger.warning("No se encontró la imagen: " + imagePath);
        }
    }

    /**
     * Muestra una animación en el ImageView correspondiente al gráfico
     * seleccionado.
     * Configura y ejecuta una animación de fotogramas clave para mostrar la
     * animación.
     * La animación se ejecuta en un bucle infinito hasta que se detenga
     * explícitamente.
     *
     * @param selectedGrh El gráfico seleccionado.
     * @param nFrames     El número total de fotogramas en la animación.
     */
    private void displayAnimation(GrhData selectedGrh, int nFrames) {
        // Configurar la animación
        if (animationTimeline != null) {
            animationTimeline.stop();
        }

        currentFrameIndex = 1; // Reiniciar el índice del frame al iniciar la animación

        animationTimeline = new Timeline(
                new KeyFrame(Duration.ZERO, event -> {
                    // Actualizar la imagen en el ImageView con el frame actual
                    updateFrame(selectedGrh);
                    currentFrameIndex = (currentFrameIndex + 1) % nFrames; // Avanzar al siguiente frame circularmente
                    if (currentFrameIndex == 0) {
                        currentFrameIndex = 1; // Omitir la posición 0
                    }
                }),
                new KeyFrame(Duration.millis(100)) // Ajustar la duración según sea necesario
        );
        animationTimeline.setCycleCount(Animation.INDEFINITE); // Repetir la animación indefinidamente
        animationTimeline.play(); // Iniciar la animación
    }

    /**
     * Actualiza el fotograma actual en el ImageView durante la reproducción de una
     * animación.
     * Obtiene el siguiente fotograma de la animación y actualiza el ImageView con
     * la imagen correspondiente.
     *
     * @param selectedGrh El gráfico seleccionado.
     */
    private void updateFrame(GrhData selectedGrh) {
        int[] frames = selectedGrh.getFrames(); // Obtener el arreglo de índices de los frames de la animación

        // Verificar que el índice actual esté dentro del rango adecuado
        if (currentFrameIndex >= 0 && currentFrameIndex < frames.length) {
            int frameId = frames[currentFrameIndex];

            // Buscar el GrhData correspondiente al frameId utilizando el mapa
            GrhData currentGrh = grhDataMap.get(frameId);

            if (currentGrh != null) {
                String imagePath = configManager.getGraphicsDir() + currentGrh.getFileNum() + ".png";

                if (!new File(imagePath).exists()) {
                    imagePath = configManager.getGraphicsDir() + selectedGrh.getFileNum() + ".bmp";
                }

                // Obtener imagen desde el caché
                Image frameImage = imageCache.getImage(imagePath);

                if (frameImage != null) {
                    // Mandar a dibujar el gráfico completo en otro ImageView
                    drawFullImage(frameImage, currentGrh);

                    // Obtener subimagen recortada desde el caché
                    WritableImage croppedImage = imageCache.getCroppedImage(
                            imagePath,
                            currentGrh.getsX(),
                            currentGrh.getsY(),
                            currentGrh.getTileWidth(),
                            currentGrh.getTileHeight());

                    if (croppedImage != null) {
                        // Mostrar la región recortada en el ImageView
                        imgIndice.setImage(croppedImage);
                    }
                } else {
                    logger.warning("No se encontró la imagen: " + imagePath);
                }
            } else {
                logger.warning("No se encontró el GrhData correspondiente para frameId: " + frameId);
            }
        } else {
            logger.warning("El índice actual está fuera del rango adecuado: " + currentFrameIndex);
        }
    }

    /**
     * Dibuja un rectángulo alrededor de la región del índice seleccionado en la
     * imagen completa del gráfico.
     *
     * @param selectedGrh El gráfico seleccionado que contiene la información de la
     *                    región del índice.
     */
    private void drawRectangle(GrhData selectedGrh) {
        try {
            // Verificar que la imagen esté cargada
            if (imgGrafico.getImage() == null) {
                return;
            }

            // Obtener las dimensiones del ImageView imgGrafico
            double imgViewWidth = imgGrafico.getFitWidth();
            double imgViewHeight = imgGrafico.getFitHeight();

            if (imgViewWidth == 0)
                imgViewWidth = imgGrafico.getBoundsInLocal().getWidth();
            if (imgViewHeight == 0)
                imgViewHeight = imgGrafico.getBoundsInLocal().getHeight();

            // Obtener las dimensiones de la imagen original
            double originalWidth = imgGrafico.getImage().getWidth();
            double originalHeight = imgGrafico.getImage().getHeight();

            // Calcular la escala entre el ImageView y la imagen original
            double scaleX = imgViewWidth / originalWidth;
            double scaleY = imgViewHeight / originalHeight;

            // Si la imagen se está ajustando para preservar la relación, usar la escala más
            // pequeña
            if (imgGrafico.isPreserveRatio()) {
                double scale = Math.min(scaleX, scaleY);
                scaleX = scale;
                scaleY = scale;
            }

            // Obtener las coordenadas del rectángulo en relación con las coordenadas del
            // ImageView
            double rectX = selectedGrh.getsX() * scaleX + 5;
            double rectY = selectedGrh.getsY() * scaleY + 5;
            double rectWidth = selectedGrh.getTileWidth() * scaleX;
            double rectHeight = selectedGrh.getTileHeight() * scaleY;

            // Si la imagen está centrada en el ImageView, ajustar las coordenadas
            double xOffset = (imgViewWidth - (originalWidth * scaleX)) / 2;
            double yOffset = (imgViewHeight - (originalHeight * scaleY)) / 2;

            if (xOffset > 0)
                rectX += xOffset;
            if (yOffset > 0)
                rectY += yOffset;

            // Configurar las propiedades del rectángulo
            rectanguloIndice.setX(rectX);
            rectanguloIndice.setY(rectY);
            rectanguloIndice.setWidth(rectWidth);
            rectanguloIndice.setHeight(rectHeight);
            rectanguloIndice.setVisible(true);

            logger.debug("Rectángulo dibujado en: x=" + rectX + ", y=" + rectY +
                    ", ancho=" + rectWidth + ", alto=" + rectHeight +
                    ", escala: " + scaleX + "x" + scaleY);
        } catch (Exception e) {
            logger.error("Error al dibujar el rectángulo", e);
        }
    }

    /**
     * Dibuja la imagen completa en un ImageView para visualización y coloca un
     * rectángulo
     * alrededor de la región específica que representa el gráfico.
     *
     * @param image La imagen a dibujar.
     * @param grh   El objeto GrhData que contiene la información sobre la imagen.
     */
    private void drawFullImage(Image image, GrhData grh) {
        try {
            // Establecer la imagen completa en el ImageView
            imgGrafico.setImage(image);

            // Dibujar el rectángulo que marca la región del gráfico
            drawRectangle(grh);
        } catch (Exception e) {
            logger.error("Error al dibujar la imagen completa", e);
        }
    }

    /**
     * Maneja el evento de presionar el mouse.
     * Este método se invoca cuando el usuario presiona el botón del mouse. Si se
     * presiona el botón
     * secundario del mouse (generalmente el botón derecho), registra las
     * coordenadas de la escena
     * iniciales y los valores de traducción del ImageView.
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
     * Maneja el evento de arrastrar el mouse.
     * Este método se invoca cuando el usuario arrastra el mouse después de
     * presionarlo. Si se presiona
     * el botón secundario del mouse (generalmente el botón derecho), calcula el
     * desplazamiento desde
     * la posición inicial y actualiza los valores de traducción del ImageView en
     * consecuencia.
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
     * Método para manejar la acción cuando se hace clic en el elemento del menú
     * "Consola"
     */
    @FXML
    private void mnuConsola_OnAction() {
        windowManager.showWindow("frmConsola", "Consola", false);
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
     * Exporta los datos de gráficos al archivo "graficos.ini" en el directorio de
     * exportación configurado.
     * Los datos exportados incluyen el número total de gráficos, la versión de los
     * índices y la información detallada de cada gráfico.
     * Si se produce algún error durante el proceso de exportación, se imprime un
     * mensaje de error.
     */
    @FXML
    private void mnuExportGrh_OnAction() {

        File file = new File(configManager.getExportDir() + "graficos.ini");

        logger.info("Exportando indices, espera...");

        try (BufferedWriter bufferWriter = new BufferedWriter(new FileWriter(file))) {
            bufferWriter.write("[INIT]");
            bufferWriter.newLine();
            bufferWriter.write("NumGrh=" + dataManager.getGrhCount());
            bufferWriter.newLine();
            bufferWriter.write("Version=" + dataManager.getGrhVersion());
            bufferWriter.newLine();
            bufferWriter.write("[GRAPHICS]");
            bufferWriter.newLine();

            for (GrhData grh : grhList) {
                if (grh.getNumFrames() > 1) {
                    bufferWriter.write("Grh" + grh.getGrh() + "=" + grh.getNumFrames() + "-");

                    int[] frames = grh.getFrames();

                    for (int i = 1; i < grh.getNumFrames() + 1; i++) {
                        bufferWriter.write(frames[i] + "-");
                    }

                    bufferWriter.write(String.valueOf(grh.getSpeed()));

                } else {
                    bufferWriter.write("Grh" + grh.getGrh() + "=" + grh.getNumFrames() + "-" +
                            grh.getFileNum() + "-" + grh.getsX() + "-" + grh.getsY() + "-" +
                            grh.getTileWidth() + "-" + grh.getTileHeight());
                }
                bufferWriter.newLine();

            }

            logger.info("Indices exportados!");
            showInfoAlert("Exportación Completa", "Índices exportados a:\n" + file.getAbsolutePath());

        } catch (IOException e) {
            logger.error("Error al exportar los datos de gráficos", e);
            showErrorAlert("Error de Exportación", "No se pudieron exportar los índices.");
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
            showInfoAlert("Exportación JSON Completa",
                    "Índices exportados a:\n" + file.getAbsolutePath());
        } else {
            showErrorAlert("Error de Exportación", "No se pudo exportar a JSON.");
        }
    }

    /**
     * Exporta los índices a formato CSV.
     */
    @FXML
    private void mnuExportCsv_OnAction() {
        File file = new File(configManager.getExportDir() + "graficos.csv");

        ExportService exportService = ExportService.getInstance();
        if (exportService.exportToCsv(grhList, file)) {
            showInfoAlert("Exportación CSV Completa",
                    "Índices exportados a:\n" + file.getAbsolutePath());
        } else {
            showErrorAlert("Error de Exportación", "No se pudo exportar a CSV.");
        }
    }

    /**
     * Valida la integridad de los datos de GRH.
     */
    @FXML
    private void mnuValidate_OnAction() {
        ValidationService validationService = ValidationService.getInstance();
        ValidationService.ValidationResult result = validationService.validate(
                grhList, configManager.getGraphicsDir());

        StringBuilder message = new StringBuilder();
        message.append("Resultado de la validación:\n\n");

        if (result.hasErrors()) {
            message.append("❌ ERRORES (").append(result.getErrors().size()).append("):\n");
            for (int i = 0; i < Math.min(5, result.getErrors().size()); i++) {
                message.append("  • ").append(result.getErrors().get(i)).append("\n");
            }
            if (result.getErrors().size() > 5) {
                message.append("  ... y ").append(result.getErrors().size() - 5).append(" más\n");
            }
            message.append("\n");
        }

        if (result.hasWarnings()) {
            message.append("⚠️ ADVERTENCIAS (").append(result.getWarnings().size()).append("):\n");
            for (int i = 0; i < Math.min(5, result.getWarnings().size()); i++) {
                message.append("  • ").append(result.getWarnings().get(i)).append("\n");
            }
            if (result.getWarnings().size() > 5) {
                message.append("  ... y ").append(result.getWarnings().size() - 5).append(" más\n");
            }
            message.append("\n");
        }

        // Estadísticas
        message.append("📊 ESTADÍSTICAS:\n");
        for (ValidationService.ValidationIssue info : result.getInfos()) {
            message.append("  • ").append(info.getMessage()).append("\n");
        }

        if (result.hasErrors()) {
            showErrorAlert("Validación con Errores", message.toString());
        } else if (result.hasWarnings()) {
            showWarningAlert("Validación con Advertencias", message.toString());
        } else {
            showInfoAlert("Validación Exitosa", "✅ No se encontraron problemas.\n\n" + message);
        }
    }

    /**
     * Muestra un diálogo de información.
     */
    private void showInfoAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Muestra un diálogo de error.
     */
    private void showErrorAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Muestra un diálogo de advertencia.
     */
    private void showWarningAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
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

    @FXML
    private void mnuCode_OnAction() {
        /**
         * if (Desktop.isDesktopSupported()) {
         * Desktop desktop = Desktop.getDesktop();
         * if (desktop.isSupported(Desktop.Action.BROWSE)) {
         * try {
         * desktop.browse(new URI("https://github.com/Lorwik/Indexador-Nexus"));
         * } catch (IOException | URISyntaxException e) {
         * logger.error("Error al abrir el enlace", e);
         * }
         * } else {
         * logger.warning("El navegador web no es compatible.");
         * }
         * } else {
         * logger.warning("La funcionalidad de escritorio no es compatible.");
         * }
         **/
    }

    /**
     * Muestra el diálogo "Acerca de" con información de la aplicación.
     */
    @FXML
    private void mnuAbout_OnAction() {
        Alert about = new Alert(Alert.AlertType.INFORMATION);
        about.setTitle("Acerca de Indexador Nexus");
        about.setHeaderText("Indexador Nexus v1.0.0");
        about.setContentText(
                "Editor de índices para Argentum Online\n\n" +
                        "Características:\n" +
                        "• Visualización y edición de GRHs\n" +
                        "• Soporte para animaciones\n" +
                        "• Exportación a INI, JSON y CSV\n" +
                        "• Validación de integridad\n" +
                        "• Sistema Undo/Redo\n\n" +
                        "Autor: Lorwik (github.com/ManuelJSD)\n" +
                        "Licencia: Open Source\n\n" +
                        "Java: " + System.getProperty("java.version") + "\n" +
                        "JavaFX: 17.0.13");
        about.showAndWait();
    }

    /**
     * Guarda los cambios realizados en los datos del gráfico seleccionado en la
     * lista.
     * Obtiene el índice seleccionado de la lista y actualiza los atributos del
     * objeto grhData correspondiente con los valores ingresados en los campos de
     * texto.
     * Si no hay ningún índice seleccionado, no se realizan cambios.
     * Se imprime un mensaje indicando que los cambios se han aplicado con éxito.
     */
    @FXML
    private void saveGrhData() {
        // Obtenemos el índice seleccionado en la lista:
        int selectedIndex = lstIndices.getSelectionModel().getSelectedIndex();

        // Nos aseguramos de que el índice es válido
        if (selectedIndex >= 0) {
            // Obtenemos el objeto grhData correspondiente al índice seleccionado
            GrhData selectedGrh = grhList.get(selectedIndex);

            // Comenzamos aplicar los cambios:
            selectedGrh.setFileNum(Integer.parseInt(txtImagen.getText()));
            selectedGrh.setsX(Short.parseShort(txtPosX.getText()));
            selectedGrh.setsY(Short.parseShort(txtPosY.getText()));
            selectedGrh.setTileWidth(Short.parseShort(txtAncho.getText()));
            selectedGrh.setTileHeight(Short.parseShort(txtAlto.getText()));

            logger.info("Cambios aplicados!");
        }
    }

    /**
     * Configura un listener para el TextField de filtro para detectar cambios en su
     * contenido.
     */
    private void setupFilterTextFieldListener() {
        // Agregar un listener al TextField de filtro para detectar cambios en su
        // contenido
        txtFiltro.textProperty().addListener((observable, oldValue, newValue) -> {
            filterIndices(newValue); // Llamar al método para filtrar los índices
        });
    }

    /**
     * Filtra los índices en el ListView según el texto proporcionado.
     *
     * @param filterText El texto utilizado para filtrar los índices.
     */
    private void filterIndices(String filterText) {
        if (!filterText.isEmpty()) {
            // El texto de filtro no está vacío
            try {
                int filterIndex = Integer.parseInt(filterText);

                // Buscar el índice en la lista de índices
                for (int i = 0; i < grhList.size(); i++) {
                    if (grhList.get(i).getGrh() == filterIndex) {
                        // Seleccionar el índice correspondiente en el ListView
                        lstIndices.getSelectionModel().select(i);
                        lstIndices.scrollTo(i); // Desplazar el ListView para mostrar el índice seleccionado
                        return; // Salir del bucle una vez que se encuentre el índice
                    }
                }

                // Si no se encuentra el índice, limpiar la selección en el ListView
                lstIndices.getSelectionModel().clearSelection();

            } catch (NumberFormatException e) {
                logger.warning("Entrada inválida. Introduce un número válido.");
            }
        } else {
            // Si el texto de filtro está vacío, limpiar la selección en el ListView
            lstIndices.getSelectionModel().clearSelection();
        }
    }

    /**
     * Configura el deslizador de zoom.
     * Este método configura un listener para el deslizador de zoom, que ajusta la
     * escala del ImageView
     * según el valor del deslizador.
     */
    private void setupSliderZoom() {
        sldZoom.valueProperty().addListener((observable, oldValue, newValue) -> {
            double zoomValue = newValue.doubleValue();
            // Aplica la escala al ImageView
            imgIndice.setScaleX(zoomValue);
            imgIndice.setScaleY(zoomValue);
        });
    }

    /**
     * Elimina el elemento seleccionado de la lista de índices.
     * Muestra un mensaje de confirmación antes de eliminar el elemento.
     */
    @FXML
    private void btnDelete_OnAction() {
        int selectedIndex = lstIndices.getSelectionModel().getSelectedIndex();

        if (selectedIndex != -1) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmación");
            alert.setHeaderText("¿Estás seguro de que quieres eliminar este elemento?");
            alert.setContentText("Esta acción no se puede deshacer.");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                lstIndices.getItems().remove(selectedIndex);
                grhList.remove(selectedIndex);
            }
        }
    }

    /**
     * Método que se activa al hacer clic en el botón "Añadir". Incrementa el
     * contador de gráficos (grhCount) en el grhDataManager,
     * crea un nuevo objeto grhData con valores predeterminados y lo agrega tanto al
     * ListView como al grhList.
     *
     * @throws IllegalArgumentException Si ocurre algún error al obtener el contador
     *                                  de gráficos del grhDataManager.
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

    /**
     * Guarda los datos de los gráficos en memoria en un archivo binario.
     * Los datos incluyen la versión del archivo, la cantidad de gráficos indexados
     * y la información de cada gráfico.
     * Si el archivo no existe, se crea. Si existe, se sobrescribe.
     * Se utilizan las instancias de `configManager` y `byteMigration` para manejar
     * la configuración y la conversión de bytes.
     *
     * @throws IOException Si ocurre un error de entrada/salida al intentar escribir
     *                     en el archivo.
     */
    @FXML
    private void mnuIndexbyMemory() throws IOException {

        // Crear un objeto File para el archivo donde se guardarán los datos de los
        // gráficos
        File archivo = new File(configManager.getInitDir() + "Graficos.ind");

        logger.info("Iniciando el guardado de índices desde memoria.");

        try (RandomAccessFile file = new RandomAccessFile(archivo, "rw")) {
            // Posicionarse al inicio del archivo
            file.seek(0);

            // Escribir la versión del archivo
            file.writeInt(byteMigration.bigToLittle_Int(dataManager.getGrhVersion()));

            // Escribir la cantidad de gráficos indexados
            file.writeInt(byteMigration.bigToLittle_Int(dataManager.getGrhCount()));

            // Escribir cada gráfico en el archivo
            for (GrhData grh : grhList) {
                // Escribir el número de gráfico y el número de frames
                file.writeInt(byteMigration.bigToLittle_Int(grh.getGrh()));
                file.writeShort(byteMigration.bigToLittle_Short(grh.getNumFrames()));

                // Si es una animación, escribir los frames y la velocidad
                if (grh.getNumFrames() > 1) {
                    int[] frames = grh.getFrames();
                    for (int i = 1; i <= grh.getNumFrames(); i++) {
                        file.writeInt(byteMigration.bigToLittle_Int(frames[i]));
                    }
                    file.writeFloat(byteMigration.bigToLittle_Float(grh.getSpeed()));
                } else { // Si es una imagen estática, escribir el resto de los datos
                    file.writeInt(byteMigration.bigToLittle_Int(grh.getFileNum()));
                    file.writeShort(byteMigration.bigToLittle_Short(grh.getsX()));
                    file.writeShort(byteMigration.bigToLittle_Short(grh.getsY()));
                    file.writeShort(byteMigration.bigToLittle_Short(grh.getTileWidth()));
                    file.writeShort(byteMigration.bigToLittle_Short(grh.getTileHeight()));
                }
            }

            logger.info("Índices guardados!");
        } catch (IOException e) {
            logger.error("Error al guardar los datos de gráficos", e);
            throw e; // Relanzar la excepción para manejarla fuera del método
        }
    }

    public void mnuIndexbyExported(ActionEvent actionEvent) {
    }

    public void mnuHead_OnAction(ActionEvent actionEvent) {
        windowManager.showWindow("frmCabezas", "Cabezas", false);
    }

    public void mnuHelmet_OnAction(ActionEvent actionEvent) {
        windowManager.showWindow("frmCascos", "Cascos", false);
    }

    public void mnuBody_OnAction(ActionEvent actionEvent) {
        windowManager.showWindow("frmCuerpos", "Cuerpos", false);
    }

    public void mnuShield_OnAction(ActionEvent actionEvent) {
        windowManager.showWindow("frmEscudos", "Escudos", false);
    }

    public void mnuFXs_OnAction(ActionEvent actionEvent) {
        windowManager.showWindow("frmFXs", "FXs", false);
    }

    public void mnuAsistente_OnAction(ActionEvent actionEvent) {
    }

    @FXML
    private void btnAddFrame_OnAction() {

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Introduce el numero del indice");
        dialog.setHeaderText("Por favor, introduce un Grh:");
        dialog.setContentText("Grh:");

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

    public void mnuExportHead_OnAction(ActionEvent actionEvent) {
    }

    public void mnuExportHelmet_OnAction(ActionEvent actionEvent) {
    }

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
     * Maneja el evento de clic en el menú "Adaptador de Grh".
     * Abre una nueva ventana que permite adaptar gráficos.
     */
    @FXML
    private void mnuGrhAdapter_OnAction() {
        windowManager.showWindow("frmAdaptador", "Adaptador de Grh", false);
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
            lblStatus.setStyle(status.contains("Error") ? "-fx-text-fill: #FF6B6B;" : "-fx-text-fill: #00FF00;");
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
}
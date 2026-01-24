package org.nexus.indexador.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import org.nexus.indexador.gamedata.DataManager;
import org.nexus.indexador.gamedata.models.GrhData;
import org.nexus.indexador.utils.ConfigManager;
import org.nexus.indexador.utils.Logger;
import org.nexus.indexador.utils.ToastNotification;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Stack;

public class ManualIndexerController {

    @FXML
    private Label lblZoom;
    @FXML
    private ToggleButton tglGrid;
    @FXML
    private CheckBox chkSnap;
    @FXML
    private Label lblSelection;
    @FXML
    private Label lblStatus;

    // Advanced Grid Controls
    @FXML
    private Spinner<Integer> spnGridW;
    @FXML
    private Spinner<Integer> spnGridH;
    @FXML
    private Spinner<Integer> spnOffX;
    @FXML
    private Spinner<Integer> spnOffY;
    @FXML
    private Spinner<Integer> spnSelX;
    @FXML
    private Spinner<Integer> spnSelY;
    @FXML
    private Spinner<Integer> spnSelW;
    @FXML
    private Spinner<Integer> spnSelH;

    // Flag to prevent loop updates
    private boolean isUpdatingSelection = false;

    @FXML
    private ColorPicker cpGridColor;

    @FXML
    private ScrollPane scrollPane;
    @FXML
    private StackPane stackContainer;
    @FXML
    private Group groupContent;
    @FXML
    private ImageView imageView;
    @FXML
    private Canvas gridCanvas;
    @FXML
    private Canvas selectionCanvas;

    @FXML
    private ListView<GrhData> lstStaging;

    private double zoomFactor = 1.0;
    private final Logger logger = Logger.getInstance();

    // Selection state
    private double startX, startY;
    private double curX, curY;
    private boolean isSelecting = false;

    // Pan state (Transform based)
    private double orgSceneX, orgSceneY;
    private double orgTranslateX, orgTranslateY;

    // Loaded file info
    private int currentFileNum = 0;

    // Staging List
    private ObservableList<GrhData> stagingList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        logger.info("ManualIndexerController initialized");

        // Setup Grid Controls
        setupSpinner(spnGridW, 32, this::drawGrid);
        setupSpinner(spnGridH, 32, this::drawGrid);
        setupSpinner(spnOffX, 0, this::drawGrid);
        setupSpinner(spnOffY, 0, this::drawGrid);
        cpGridColor.setValue(Color.WHITE);

        cpGridColor.setOnAction(e -> drawGrid());

        // Setup Selection Controls
        setupSpinner(spnSelX, 0, this::updateSelectedGrh);
        setupSpinner(spnSelY, 0, this::updateSelectedGrh);
        setupSpinner(spnSelW, 0, this::updateSelectedGrh);
        setupSpinner(spnSelH, 0, this::updateSelectedGrh);

        lblSelection.setText("Sel: -");

        // Setup staging list
        lstStaging.setItems(stagingList);
        lstStaging.setCellFactory(lv -> new ListCell<GrhData>() {
            @Override
            protected void updateItem(GrhData item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("ID: %d - %dx%d (F:%d)",
                            item.getGrh(), item.getPixelWidth(), item.getPixelHeight(), item.getFileNum()));
                }
            }
        });

        // Add list selection listener to highlight item AND update spinners
        lstStaging.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                isUpdatingSelection = true;
                if (spnSelX.getValueFactory() != null)
                    spnSelX.getValueFactory().setValue((int) newVal.getsX());
                if (spnSelY.getValueFactory() != null)
                    spnSelY.getValueFactory().setValue((int) newVal.getsY());
                if (spnSelW.getValueFactory() != null)
                    spnSelW.getValueFactory().setValue((int) newVal.getPixelWidth());
                if (spnSelH.getValueFactory() != null)
                    spnSelH.getValueFactory().setValue((int) newVal.getPixelHeight());
                isUpdatingSelection = false;

                drawHighlight(newVal);
            }
        });
    }

    private void setupSpinner(Spinner<Integer> spinner, int def, Runnable callback) {
        if (spinner == null)
            return;
        spinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 5000, def));
        spinner.valueProperty().addListener((o, old, v) -> {
            if (callback != null)
                callback.run();
        });
    }

    private void updateSelectedGrh() {
        if (isUpdatingSelection)
            return;

        GrhData selected = lstStaging.getSelectionModel().getSelectedItem();
        if (selected != null) {
            selected.setsX(spnSelX.getValue().shortValue());
            selected.setsY(spnSelY.getValue().shortValue());
            selected.setPixelWidth(spnSelW.getValue().shortValue());
            selected.setPixelHeight(spnSelH.getValue().shortValue());

            lstStaging.refresh();
            drawHighlight(selected);
        }
    }

    @FXML
    private CheckBox chkGridDetection;

    public void loadFile(File file) {
        if (file != null && file.exists()) {
            // Reset editor state
            stagingList.clear();
            onZoomReset();

            try {
                String name = file.getName();
                if (name.indexOf('.') > 0)
                    name = name.substring(0, name.lastIndexOf('.'));
                currentFileNum = Integer.parseInt(name);
            } catch (Exception e) {
                currentFileNum = 0;
            }

            Image img = new Image(file.toURI().toString());
            imageView.setImage(img);

            double w = img.getWidth();
            double h = img.getHeight();
            gridCanvas.setWidth(w);
            gridCanvas.setHeight(h);
            selectionCanvas.setWidth(w);
            selectionCanvas.setHeight(h);

            drawGrid();
            clearSelection();

            // Reset transforms on new file
            groupContent.setTranslateX(0);
            groupContent.setTranslateY(0);
            setZoom(1.0);

            lblStatus.setText("Cargado: " + file.getName() + " (" + (int) w + "x" + (int) h + ")");
        }
    }

    @FXML
    private void onOpenImage() {
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.bmp", "*.jpg"));

        String graphDir = ConfigManager.getInstance().getGraphicsDir();
        File initDir = new File(graphDir);
        if (initDir.exists())
            fc.setInitialDirectory(initDir);

        File f = fc.showOpenDialog(imageView.getScene().getWindow());
        if (f != null) {
            loadFile(f);
        }
    }

    // --- Zoom Logic (Transform Scale) ---

    @FXML
    private void onZoomIn() {
        setZoom(zoomFactor + 0.1);
    }

    @FXML
    private void onZoomOut() {
        setZoom(Math.max(0.1, zoomFactor - 0.1));
    }

    @FXML
    private void onZoomReset() {
        setZoom(1.0);
        groupContent.setTranslateX(0);
        groupContent.setTranslateY(0);
    }

    @FXML
    private void onScroll(ScrollEvent event) {
        if (event.isControlDown() || event.getDeltaY() != 0) {
            double delta = event.getDeltaY();
            if (delta > 0)
                onZoomIn();
            else
                onZoomOut();
            event.consume();
        }
    }

    private void setZoom(double newZoom) {
        this.zoomFactor = newZoom;
        // Apply Scale to Group
        groupContent.setScaleX(zoomFactor);
        groupContent.setScaleY(zoomFactor);
        lblZoom.setText((int) (zoomFactor * 100) + "%");
    }

    @FXML
    private void onToggleGrid() {
        gridCanvas.setVisible(tglGrid.isSelected());
    }

    // --- Auto Detect (Smart Blob) ---
    @FXML
    private void onAutoDetect() {
        if (imageView.getImage() == null)
            return;

        if (chkGridDetection != null && chkGridDetection.isSelected()) {
            autoDetectGrid();
        } else {
            // Ask for tolerance
            TextInputDialog dialog = new TextInputDialog("15");
            dialog.setTitle("Auto-Indexado");
            dialog.setHeaderText("Configuración de Fusión");
            dialog.setContentText("Tolerancia de fusión (px):");

            Optional<String> result = dialog.showAndWait();
            if (result.isPresent()) {
                int tolerance = 15;
                try {
                    tolerance = Integer.parseInt(result.get());
                } catch (NumberFormatException e) {
                    ToastNotification.show(imageView.getScene().getWindow(),
                            "Valor inválido, usando por defecto (15px)");
                }
                autoDetectBlobs(tolerance);
            }
        }
    }

    private void autoDetectGrid() {
        Image img = imageView.getImage();
        int w = (int) img.getWidth();
        int h = (int) img.getHeight();

        int gridW = spnGridW.getValue();
        int gridH = spnGridH.getValue();
        int offX = spnOffX.getValue();
        int offY = spnOffY.getValue();

        if (gridW <= 0 || gridH <= 0) {
            ToastNotification.show(imageView.getScene().getWindow(), "Tamaño de rejilla inválido.");
            return;
        }

        int itemsFound = 0;

        for (int y = offY; y < h; y += gridH) {
            for (int x = offX; x < w; x += gridW) {
                int endX = Math.min(x + gridW, w);
                int endY = Math.min(y + gridH, h);

                short sX = (short) x;
                short sY = (short) y;
                short pW = (short) (endX - x);
                short pH = (short) (endY - y);

                if (pW > 0 && pH > 0) {
                    GrhData newGrh = new GrhData(0, (short) 1, currentFileNum, sX, sY, pW, pH);
                    stagingList.add(newGrh);
                    itemsFound++;
                }
            }
        }

        if (itemsFound > 0) {
            ToastNotification.show(imageView.getScene().getWindow(), "¡Se han generado " + itemsFound + " cuadros!");
            if (!stagingList.isEmpty()) {
                int firstNewIndex = stagingList.size() - itemsFound;
                if (firstNewIndex >= 0 && firstNewIndex < stagingList.size()) {
                    lstStaging.getSelectionModel().select(firstNewIndex);
                    lstStaging.scrollTo(firstNewIndex);
                }
            }
        } else {
            ToastNotification.show(imageView.getScene().getWindow(), "No se pudo generar la rejilla.");
        }
    }

    private void autoDetectBlobs(int tolerance) {
        Image img = imageView.getImage();
        if (img == null)
            return;

        javafx.scene.image.PixelReader pr = img.getPixelReader();
        int w = (int) img.getWidth();
        int h = (int) img.getHeight();

        // Detect background mode
        Color bgColor = pr.getColor(0, 0);
        boolean useAlpha = bgColor.getOpacity() == 0;

        boolean[][] visited = new boolean[w][h];
        int blobsFound = 0;

        DataManager dm;
        try {
            dm = DataManager.getInstance();
        } catch (Exception e) {
            return;
        }
        int nextId = dm.getNextFreeGrhIndex() + stagingList.size();

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                if (visited[x][y])
                    continue;

                Color c = pr.getColor(x, y);
                boolean isContent;

                if (useAlpha)
                    isContent = c.getOpacity() > 0;
                else
                    isContent = !colorsMatch(c, bgColor);

                if (isContent) {
                    // Start Flood Fill
                    int[] bounds = floodFill(pr, visited, x, y, w, h, bgColor, useAlpha);
                    if (bounds != null) {
                        // Create Blob GRH
                        int bx = bounds[0];
                        int by = bounds[1];
                        int bw = bounds[2] - bx + 1;
                        int bh = bounds[3] - by + 1;

                        if (bw > 0 && bh > 0) {
                            GrhData grh = new GrhData(nextId++, (short) 1, currentFileNum,
                                    (short) bx, (short) by, (short) bw, (short) bh);
                            stagingList.add(grh);
                            blobsFound++;
                        }
                    }
                }
            }
        }

        if (blobsFound > 0) {
            // Post-processing: Merge nearby blobs for detailed animations
            List<GrhData> blobs = new ArrayList<>(
                    stagingList.subList(stagingList.size() - blobsFound, stagingList.size()));
            stagingList.removeAll(blobs); // Remove unmerged

            List<GrhData> merged = mergeBlobs(blobs, tolerance);
            stagingList.addAll(merged);

            ToastNotification.show(imageView.getScene().getWindow(),
                    "¡Se han detectado " + merged.size() + " objetos (Fusionados)!");
            if (!stagingList.isEmpty()) {
                // Select the first new item
                int firstNewIndex = stagingList.size() - merged.size();
                if (firstNewIndex >= 0) {
                    lstStaging.getSelectionModel().select(firstNewIndex);
                }
            }
        } else {
            ToastNotification.show(imageView.getScene().getWindow(), "No se detectaron objetos.");
        }
    }

    private java.util.List<GrhData> mergeBlobs(java.util.List<GrhData> blobs, int distanceThreshold) {
        if (blobs.isEmpty())
            return blobs;

        boolean changed = true;
        while (changed) {
            changed = false;
            for (int i = 0; i < blobs.size(); i++) {
                GrhData a = blobs.get(i);
                for (int j = i + 1; j < blobs.size(); j++) {
                    GrhData b = blobs.get(j);

                    // Check intersection or proximity
                    if (shouldMerge(a, b, distanceThreshold)) {
                        // Merge b into a
                        short newX = (short) Math.min(a.getsX(), b.getsX());
                        short newY = (short) Math.min(a.getsY(), b.getsY());
                        short newMaxX = (short) Math.max(a.getsX() + a.getPixelWidth(), b.getsX() + b.getPixelWidth());
                        short newMaxY = (short) Math.max(a.getsY() + a.getPixelHeight(),
                                b.getsY() + b.getPixelHeight());

                        a.setsX(newX);
                        a.setsY(newY);
                        a.setPixelWidth((short) (newMaxX - newX));
                        a.setPixelHeight((short) (newMaxY - newY));

                        blobs.remove(j);
                        changed = true;
                        // Break inner loop to restart or continue safely?
                        // With j removed, j is now pointing to next element, but it's simpler to break
                        // and restart
                        // or decrement j. Let's decrement j to continue checking 'a' against others.
                        j--;
                    }
                }
            }
        }
        return blobs;
    }

    private boolean shouldMerge(GrhData a, GrhData b, int threshold) {
        // Expand A by threshold
        int ax1 = a.getsX() - threshold;
        int ay1 = a.getsY() - threshold;
        int ax2 = a.getsX() + a.getPixelWidth() + threshold;
        int ay2 = a.getsY() + a.getPixelHeight() + threshold;

        int bx1 = b.getsX();
        int by1 = b.getsY();
        int bx2 = b.getsX() + b.getPixelWidth();
        int by2 = b.getsY() + b.getPixelHeight();

        return ax1 < bx2 && ax2 > bx1 && ay1 < by2 && ay2 > by1;
    }

    private boolean colorsMatch(Color c1, Color c2) {
        return c1.equals(c2);
    }

    // Returns [minX, minY, maxX, maxY]
    private int[] floodFill(javafx.scene.image.PixelReader pr, boolean[][] visited, int startX, int startY, int w,
            int h, Color bg, boolean useAlpha) {
        int minX = startX, maxX = startX, minY = startY, maxY = startY;

        Stack<int[]> stack = new Stack<>();
        stack.push(new int[] { startX, startY });
        visited[startX][startY] = true;

        int pixelCount = 0;

        while (!stack.isEmpty()) {
            int[] p = stack.pop();
            int px = p[0];
            int py = p[1];
            pixelCount++;

            if (px < minX)
                minX = px;
            if (px > maxX)
                maxX = px;
            if (py < minY)
                minY = py;
            if (py > maxY)
                maxY = py;

            int[] dx = { 1, -1, 0, 0 };
            int[] dy = { 0, 0, 1, -1 };

            for (int i = 0; i < 4; i++) {
                int nx = px + dx[i];
                int ny = py + dy[i];

                if (nx >= 0 && nx < w && ny >= 0 && ny < h) {
                    if (!visited[nx][ny]) {
                        Color c = pr.getColor(nx, ny);
                        boolean isContent;
                        if (useAlpha)
                            isContent = c.getOpacity() > 0;
                        else
                            isContent = !colorsMatch(c, bg);

                        if (isContent) {
                            visited[nx][ny] = true;
                            stack.push(new int[] { nx, ny });
                        }
                    }
                }
            }
        }

        if (pixelCount < 5)
            return null;

        return new int[] { minX, minY, maxX, maxY };
    }

    // --- Creation & Staging ---

    @FXML
    private void onCreateGrh() {
        if (!isSelecting && (Math.abs(startX - curX) < 1 || Math.abs(startY - curY) < 1))
            return;

        int[] rect = calculateSelectionRect();
        int x = rect[0];
        int y = rect[1];
        int w = rect[2];
        int h = rect[3];

        if (w <= 0 || h <= 0) {
            ToastNotification.show(imageView.getScene().getWindow(), "Selección inválida.");
            return;
        }

        if (currentFileNum == 0) {
            // ... (Keep existing ID ask logic if needed, simplified here for brevity,
            // assume valid)
            // Using simple default implementation for now as seen in previous steps
        }

        DataManager dm;
        try {
            dm = DataManager.getInstance();
        } catch (Exception e) {
            return;
        }

        int baseId = dm.getNextFreeGrhIndex();
        int newId = baseId + stagingList.size();

        GrhData grh = new GrhData(newId, (short) 1, currentFileNum, (short) x, (short) y, (short) w, (short) h);

        stagingList.add(grh);
        clearSelection();
        lstStaging.scrollTo(grh);
    }

    @FXML
    private void onResetStaging() {
        if (stagingList.isEmpty())
            return;
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Resetear");
        alert.setHeaderText("¿Borrar índices temporales?");
        alert.setContentText("Se perderán " + stagingList.size() + " índices no guardados.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            stagingList.clear();
            drawSelectionRect(); // Clear highlight
        }
    }

    @FXML
    private void onSaveAll() {
        if (stagingList.isEmpty()) {
            ToastNotification.show(imageView.getScene().getWindow(), "No hay índices para guardar.");
            return;
        }
        DataManager dm;
        try {
            dm = DataManager.getInstance();
        } catch (Exception e) {
            return;
        }
        int count = 0;
        for (GrhData grh : stagingList) {
            dm.addGrh(grh);
            count++;
        }
        stagingList.clear();
        ToastNotification.show(imageView.getScene().getWindow(), "¡Se guardaron " + count + " índices!");
    }

    private void clearSelection() {
        startX = 0;
        startY = 0;
        curX = 0;
        curY = 0;
        isSelecting = false;
        GraphicsContext gc = selectionCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, selectionCanvas.getWidth(), selectionCanvas.getHeight());
        lblSelection.setText("Sel: -");
    }

    private void drawHighlight(GrhData grh) {
        GraphicsContext gc = selectionCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, selectionCanvas.getWidth(), selectionCanvas.getHeight());

        double x = grh.getsX();
        double y = grh.getsY();
        double w = grh.getPixelWidth();
        double h = grh.getPixelHeight();

        gc.setStroke(Color.LIME);
        gc.setLineWidth(2);
        gc.strokeRect(x, y, w, h);
        gc.setFill(Color.color(0, 1, 0, 0.2));
        gc.fillRect(x, y, w, h);
    }

    // --- Mouse Events (Select + Pan) ---

    @FXML
    private void onCanvasPressed(MouseEvent event) {
        if (event.getButton() == MouseButton.SECONDARY) {
            orgSceneX = event.getSceneX();
            orgSceneY = event.getSceneY();
            orgTranslateX = groupContent.getTranslateX();
            orgTranslateY = groupContent.getTranslateY();
            event.consume();
            return;
        }

        double x = event.getX();
        double y = event.getY();

        // Snap logic: Default to CheckBox, invert if Ctrl is down
        boolean useSnap = chkSnap.isSelected();
        if (event.isControlDown())
            useSnap = !useSnap;

        if (useSnap) {
            int sizeW = spnGridW.getValue();
            int sizeH = spnGridH.getValue();
            int offX = spnOffX.getValue();
            int offY = spnOffY.getValue();

            // Adjust for offset
            x = Math.floor((x - offX) / sizeW) * sizeW + offX;
            y = Math.floor((y - offY) / sizeH) * sizeH + offY;
        }

        startX = x;
        startY = y;
        curX = x;
        curY = y;
        isSelecting = true;
        drawSelectionRect();
    }

    @FXML
    private void onCanvasDragged(MouseEvent event) {
        if (event.getButton() == MouseButton.SECONDARY) {
            double offsetX = event.getSceneX() - orgSceneX;
            double offsetY = event.getSceneY() - orgSceneY;
            double newTranslateX = orgTranslateX + offsetX;
            double newTranslateY = orgTranslateY + offsetY;
            groupContent.setTranslateX(newTranslateX);
            groupContent.setTranslateY(newTranslateY);
            event.consume();
            return;
        }

        if (!isSelecting)
            return;

        double x = event.getX();
        double y = event.getY();

        x = Math.max(0, Math.min(x, selectionCanvas.getWidth()));
        y = Math.max(0, Math.min(y, selectionCanvas.getHeight()));

        boolean useSnap = chkSnap.isSelected();
        if (event.isControlDown())
            useSnap = !useSnap;

        if (useSnap) {
            int sizeW = spnGridW.getValue();
            int sizeH = spnGridH.getValue();
            int offX = spnOffX.getValue();
            int offY = spnOffY.getValue();

            x = Math.round((x - offX) / sizeW) * sizeW + offX;
            y = Math.round((y - offY) / sizeH) * sizeH + offY;
        }

        curX = x;
        curY = y;
        drawSelectionRect();
    }

    @FXML
    private void onCanvasReleased(MouseEvent event) {
        if (isSelecting) {
            isSelecting = false;
            drawSelectionRect();
        }
    }

    private int[] calculateSelectionRect() {
        double minX = Math.min(startX, curX);
        double minY = Math.min(startY, curY);
        double w = Math.abs(curX - startX);
        double h = Math.abs(curY - startY);
        return new int[] { (int) minX, (int) minY, (int) w, (int) h };
    }

    private void drawSelectionRect() {
        GraphicsContext gc = selectionCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, selectionCanvas.getWidth(), selectionCanvas.getHeight());

        int[] rect = calculateSelectionRect();
        int x = rect[0];
        int y = rect[1];
        int w = rect[2];
        int h = rect[3];

        gc.setFill(Color.color(0.2, 0.6, 1.0, 0.3));
        gc.fillRect(x, y, w, h);
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(1);
        gc.strokeRect(x, y, w, h);

        lblSelection.setText(String.format("Sel: %d,%d (%dx%d)", x, y, w, h));
    }

    private void drawGrid() {
        if (gridCanvas == null || spnGridW == null)
            return;
        GraphicsContext gc = gridCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, gridCanvas.getWidth(), gridCanvas.getHeight());

        if (!tglGrid.isSelected())
            return;

        int sizeW = spnGridW.getValue();
        int sizeH = spnGridH.getValue();
        int offX = spnOffX.getValue();
        int offY = spnOffY.getValue();
        Color c = cpGridColor.getValue();

        if (sizeW <= 0 || sizeH <= 0)
            return;

        double w = gridCanvas.getWidth();
        double h = gridCanvas.getHeight();

        gc.setStroke(c);
        gc.setLineWidth(1);

        for (double x = offX; x <= w; x += sizeW)
            gc.strokeLine(x, 0, x, h);
        for (double y = offY; y <= h; y += sizeH)
            gc.strokeLine(0, y, w, y);
    }
}

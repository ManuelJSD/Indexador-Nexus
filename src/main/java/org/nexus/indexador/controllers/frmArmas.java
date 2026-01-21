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
import javafx.scene.image.WritableImage;
import javafx.util.Duration;
import org.nexus.indexador.gamedata.DataManager;
import org.nexus.indexador.gamedata.models.GrhData;
import org.nexus.indexador.gamedata.models.WeaponData;
import org.nexus.indexador.utils.AnimationState;
import org.nexus.indexador.utils.ConfigManager;
import org.nexus.indexador.utils.ImageCache;
import org.nexus.indexador.utils.Logger;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class frmArmas {

    @FXML
    public ListView<String> lstWeapons;
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
    public Label lblNArmas;
    @FXML
    public Button btnSave;
    @FXML
    public Button btnAdd;
    @FXML
    public Button btnDelete;

    private ObservableList<WeaponData> weaponList;
    private ObservableList<GrhData> grhList;

    private ConfigManager configManager;
    private DataManager dataManager;
    private ImageCache imageCache;
    private Logger logger;

    private Map<Integer, AnimationState> animationStates = new HashMap<>();
    private Map<Integer, GrhData> grhDataMap;

    @FXML
    protected void initialize() {
        configManager = ConfigManager.getInstance();
        try {
            dataManager = DataManager.getInstance();
            imageCache = ImageCache.getInstance();
            logger = Logger.getInstance();

            logger.info("Inicializando controlador frmArmas");

            animationStates.put(0, new AnimationState());
            animationStates.put(1, new AnimationState());
            animationStates.put(2, new AnimationState());
            animationStates.put(3, new AnimationState());

            loadWeaponData();
            setupWeaponListListener();

            logger.info("Controlador frmArmas inicializado correctamente");
        } catch (Exception e) {
            System.err.println("Error al inicializar frmArmas:");
            e.printStackTrace();
        }
    }

    private void loadWeaponData() {
        try {
            weaponList = dataManager.readWeaponFile();
            grhDataMap = new HashMap<>();
            grhList = dataManager.getGrhList();

            for (GrhData grh : grhList) {
                grhDataMap.put(grh.getGrh(), grh);
            }

            lblNArmas.setText("Armas cargadas: " + dataManager.getNumWeapons());

            ObservableList<String> weaponIndices = FXCollections.observableArrayList();
            for (int i = 1; i <= weaponList.size(); i++) {
                weaponIndices.add(String.valueOf(i));
            }

            lstWeapons.setItems(weaponIndices);
            logger.info("Datos de armas cargados: " + weaponList.size() + " armas");
        } catch (IOException e) {
            logger.error("Error al cargar datos de armas", e);
        }
    }

    private void setupWeaponListListener() {
        lstWeapons.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            int selectedIndex = lstWeapons.getSelectionModel().getSelectedIndex();
            if (selectedIndex >= 0) {
                WeaponData selectedWeapon = weaponList.get(selectedIndex);
                updateEditor(selectedWeapon);
                for (int i = 0; i <= 3; i++) {
                    drawWeapons(selectedWeapon, i);
                }
                logger.debug("Arma seleccionada: índice " + (selectedIndex + 1));
            }
        });
    }

    private void updateEditor(WeaponData selectedWeapon) {
        int[] grhs = selectedWeapon.getGrhIndex();
        txtNorte.setText(String.valueOf(grhs[0]));
        txtEste.setText(String.valueOf(grhs[1]));
        txtSur.setText(String.valueOf(grhs[2]));
        txtOeste.setText(String.valueOf(grhs[3]));
    }

    private void drawWeapons(WeaponData selectedWeapon, int heading) {
        int[] indices = selectedWeapon.getGrhIndex();
        int grhId = indices[heading];

        if (grhId <= 0 || !grhDataMap.containsKey(grhId)) {
            clearImage(heading);
            return;
        }

        GrhData selectedGrh = grhDataMap.get(grhId);
        int nFrames = selectedGrh.getNumFrames();

        AnimationState animationState = animationStates.get(heading);
        Timeline animationTimeline = animationState.getTimeline();

        if (animationTimeline != null) {
            animationTimeline.stop();
        }

        if (nFrames > 1) {
            animationTimeline.getKeyFrames().clear();
            animationState.setCurrentFrameIndex(1);
            animationTimeline.getKeyFrames().add(
                    new KeyFrame(Duration.ZERO, event -> {
                        updateFrame(selectedGrh, heading);
                        animationState
                                .setCurrentFrameIndex((animationState.getCurrentFrameIndex() + 1) % (nFrames + 1));
                        if (animationState.getCurrentFrameIndex() == 0)
                            animationState.setCurrentFrameIndex(1);
                    }));
            animationTimeline.getKeyFrames().add(new KeyFrame(Duration.millis(100)));
            animationTimeline.setCycleCount(Animation.INDEFINITE);
            animationTimeline.play();
        } else {
            updateFrame(selectedGrh, heading);
        }
    }

    private void updateFrame(GrhData selectedGrh, int heading) {
        int frameId;
        if (selectedGrh.getNumFrames() > 1) {
            AnimationState animationState = animationStates.get(heading);
            frameId = selectedGrh.getFrame(animationState.getCurrentFrameIndex());
        } else {
            frameId = selectedGrh.getGrh();
        }

        GrhData currentGrh = grhDataMap.get(frameId);
        if (currentGrh != null) {
            String imagePath = configManager.getGraphicsDir() + currentGrh.getFileNum() + ".png";
            if (!new File(imagePath).exists()) {
                imagePath = configManager.getGraphicsDir() + currentGrh.getFileNum() + ".bmp";
            }

            Image frameImage = imageCache.getImage(imagePath);
            if (frameImage != null) {
                WritableImage croppedImage = imageCache.getCroppedImage(
                        imagePath,
                        currentGrh.getsX(),
                        currentGrh.getsY(),
                        currentGrh.getTileWidth(),
                        currentGrh.getTileHeight());

                if (croppedImage != null) {
                    setImageView(heading, croppedImage);
                }
            }
        }
    }

    private void setImageView(int heading, Image img) {
        switch (heading) {
            case 0:
                imgSur.setImage(img);
                break;
            case 1:
                imgNorte.setImage(img);
                break;
            case 2:
                imgOeste.setImage(img);
                break;
            case 3:
                imgEste.setImage(img);
                break;
        }
    }

    private void clearImage(int heading) {
        setImageView(heading, null);
    }

    @FXML
    public void btnSave_OnAction(ActionEvent actionEvent) {
        int selectedIndex = lstWeapons.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0) {
            try {
                WeaponData data = weaponList.get(selectedIndex);
                data.getGrhIndex()[0] = Integer.parseInt(txtNorte.getText());
                data.getGrhIndex()[1] = Integer.parseInt(txtEste.getText());
                data.getGrhIndex()[2] = Integer.parseInt(txtSur.getText());
                data.getGrhIndex()[3] = Integer.parseInt(txtOeste.getText());

                logger.info("Cambios aplicados en memoria para el arma " + (selectedIndex + 1));

                // Guardado real en disco
                dataManager.getIndexLoader().saveWeapons(weaponList);
                logger.info("Armas guardadas en disco correctamente.");
            } catch (Exception e) {
                logger.error("Error al guardar armas", e);
            }
        }
    }

    @FXML
    public void btnAdd_OnAction(ActionEvent actionEvent) {
        weaponList.add(new WeaponData(new int[4]));
        lstWeapons.getItems().add(String.valueOf(weaponList.size()));
        lstWeapons.getSelectionModel().selectLast();
        logger.info("Nueva arma añadida.");
    }

    @FXML
    public void btnDelete_OnAction(ActionEvent actionEvent) {
        int selectedIndex = lstWeapons.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0) {
            weaponList.remove(selectedIndex);
            loadWeaponData(); // Recargar lista visual
            logger.info("Arma eliminada.");
        }
    }
}

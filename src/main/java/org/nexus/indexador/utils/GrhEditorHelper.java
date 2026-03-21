package org.nexus.indexador.utils;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import org.nexus.indexador.gamedata.models.GrhData;

import javafx.scene.control.Spinner;

/**
 * Helper class to manage the synchronization between GRH property fields and
 * the GrhData model.
 */
public class GrhEditorHelper {

    private final TextField txtImagen;
    private final Spinner<Integer> txtPosX;
    private final Spinner<Integer> txtPosY;
    private final Spinner<Integer> txtAncho;
    private final Spinner<Integer> txtAlto;
    private final Spinner<Double> txtSpeed;
    private final TextField txtIndice;
    private final ListView<String> lstFrames;

    public GrhEditorHelper(TextField txtImagen, Spinner<Integer> txtPosX, Spinner<Integer> txtPosY,
            Spinner<Integer> txtAncho, Spinner<Integer> txtAlto, Spinner<Double> txtSpeed,
            TextField txtIndice, ListView<String> lstFrames) {
        this.txtImagen = txtImagen;
        this.txtPosX = txtPosX;
        this.txtPosY = txtPosY;
        this.txtAncho = txtAncho;
        this.txtAlto = txtAlto;
        this.txtSpeed = txtSpeed;
        this.txtIndice = txtIndice;
        this.lstFrames = lstFrames;
    }

    /**
     * Updates the UI fields from the provided GrhData object.
     */
    public void updateEditor(GrhData selectedGrh) {
        if (selectedGrh == null)
            return;

        int fileGrh = selectedGrh.getFileNum();
        int nFrames = selectedGrh.getNumFrames();
        int x = selectedGrh.getsX();
        int y = selectedGrh.getsY();
        int width = selectedGrh.getTileWidth();
        int height = selectedGrh.getTileHeight();
        float speed = selectedGrh.getSpeed();

        txtImagen.setText(String.valueOf(fileGrh));
        txtPosX.getValueFactory().setValue(x);
        txtPosY.getValueFactory().setValue(y);
        txtAncho.getValueFactory().setValue(width);
        txtAlto.getValueFactory().setValue(height);
        txtSpeed.getValueFactory().setValue((double) speed);

        if (nFrames == 1) { // Static
            txtIndice.setText("Grh" + selectedGrh.getGrh() + "=" + nFrames + "-" + fileGrh + "-" + x + "-"
                    + y + "-" + width + "-" + height);
            lstFrames.getItems().clear();
        } else { // Animation
            StringBuilder frameText = new StringBuilder();
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
     * Saves the values from the UI fields back to the provided GrhData object.
     */
    public void saveGrhData(GrhData selectedGrh) {
        if (selectedGrh == null)
            return;

        try {
            selectedGrh.setFileNum(Integer.parseInt(txtImagen.getText()));
            selectedGrh.setsX(txtPosX.getValue().shortValue());
            selectedGrh.setsY(txtPosY.getValue().shortValue());
            selectedGrh.setTileWidth(txtAncho.getValue().shortValue());
            selectedGrh.setTileHeight(txtAlto.getValue().shortValue());

            // Re-generate the index string summary
            updateEditor(selectedGrh);
        } catch (NumberFormatException e) {
            // Error handling should be managed by the caller (Controller) using
            // Toast/Alerts
            throw e;
        }
    }
}

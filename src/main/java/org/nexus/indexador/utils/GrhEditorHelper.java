package org.nexus.indexador.utils;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import org.nexus.indexador.gamedata.models.GrhData;

/**
 * Helper class to manage the synchronization between GRH property fields and
 * the GrhData model.
 */
public class GrhEditorHelper {

    private final TextField txtImagen;
    private final TextField txtPosX;
    private final TextField txtPosY;
    private final TextField txtAncho;
    private final TextField txtAlto;
    private final TextField txtSpeed;
    private final TextField txtIndice;
    private final ListView<String> lstFrames;

    public GrhEditorHelper(TextField txtImagen, TextField txtPosX, TextField txtPosY,
            TextField txtAncho, TextField txtAlto, TextField txtSpeed,
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
        txtPosX.setText(String.valueOf(x));
        txtPosY.setText(String.valueOf(y));
        txtAncho.setText(String.valueOf(width));
        txtAlto.setText(String.valueOf(height));
        txtSpeed.setText(String.valueOf(speed));

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
            selectedGrh.setsX(Short.parseShort(txtPosX.getText()));
            selectedGrh.setsY(Short.parseShort(txtPosY.getText()));
            selectedGrh.setTileWidth(Short.parseShort(txtAncho.getText()));
            selectedGrh.setTileHeight(Short.parseShort(txtAlto.getText()));

            // Re-generate the index string summary
            updateEditor(selectedGrh);
        } catch (NumberFormatException e) {
            // Error handling should be managed by the caller (Controller) using
            // Toast/Alerts
            throw e;
        }
    }
}

package org.nexus.indexador.controllers;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import org.nexus.indexador.utils.ValidationService.ValidationIssue;
import org.nexus.indexador.utils.ValidationService.ValidationResult;
import org.nexus.indexador.utils.Logger;

public class ValidationResultController {

    @FXML
    private TableView<ValidationIssue> tblResults;

    @FXML
    private TableColumn<ValidationIssue, String> colType;

    @FXML
    private TableColumn<ValidationIssue, Integer> colId;

    @FXML
    private TableColumn<ValidationIssue, String> colMessage;

    @FXML
    private Button btnClose;

    @FXML
    private Button btnCopy;

    private MainController mainController;

    @FXML
    public void initialize() {
        colType.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getSeverity().toString()));
        colId.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getGrhId()).asObject());
        colMessage.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getMessage()));

        // Handle double click to jump to GRH
        tblResults.setRowFactory(tv -> {
            TableRow<ValidationIssue> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    ValidationIssue rowData = row.getItem();
                    if (mainController != null && rowData.getGrhId() > 0) {
                        mainController.jumpToGrh(rowData.getGrhId());
                    }
                }
            });
            return row;
        });
    }

    public void setResults(ValidationResult result) {
        if (result != null) {
            tblResults.setItems(FXCollections.observableArrayList(result.getErrors())); // Add errors
            tblResults.getItems().addAll(result.getWarnings()); // Add warnings
            tblResults.getItems().addAll(result.getInfos()); // Add infos
        }
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    public void btnClose_OnAction() {
        Stage stage = (Stage) btnClose.getScene().getWindow();
        stage.close();
    }

    @FXML
    public void btnCopy_OnAction() {
        StringBuilder sb = new StringBuilder();
        for (ValidationIssue issue : tblResults.getItems()) {
            sb.append(issue.toString()).append("\n");
        }

        ClipboardContent content = new ClipboardContent();
        content.putString(sb.toString());
        Clipboard.getSystemClipboard().setContent(content);

        // Could show a toast here if we had access to one, but button feedback is
        // enough for now
    }
}

package org.nexus.indexador.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

import java.io.OutputStream;
import java.io.PrintStream;

public class frmConsola {

    @FXML
    private TextArea txtConsole;

    @FXML
    private CheckBox chkAutoScroll;

    private static frmConsola instance;

    // Buffer global de logs para capturar todo desde el inicio
    private static final StringBuilder logBuffer = new StringBuilder();
    private static boolean captureActive = false;

    // Streams originales
    private static final PrintStream originalOut = System.out;
    private static final PrintStream originalErr = System.err;

    public static frmConsola getInstance() {
        return instance;
    }

    /**
     * Activa la captura de logs globalmente.
     * Debe llamarse al inicio de la aplicación (Main.java).
     */
    public static void activateGlobalRedirection() {
        if (captureActive)
            return;

        // Registrar listener en el Logger principal
        org.nexus.indexador.utils.Logger.getInstance().addLogListener(message -> {
            try {
                // Escribir en buffer
                synchronized (logBuffer) {
                    logBuffer.append(message);
                }

                // Actualizar UI si existe
                if (instance != null) {
                    instance.appendText(message);
                }
            } catch (Exception e) {
                e.printStackTrace(); // Imprimir en stderr por si acaso
                if (instance != null) {
                    // Intentar mostrar error en consola si es posible
                    try {
                        instance.appendText(" [ERROR LISTENER] " + e.getMessage() + "\n");
                    } catch (Exception ignored) {
                    }
                }
            }
        });

        // Log de confirmación
        synchronized (logBuffer) {
            logBuffer.append("--- LISTENER REGISTERED ---\n");
        }
        org.nexus.indexador.utils.Logger.getInstance().info("SISTEMA DE LOGS VINCULADO CORRECTAMENTE");

        captureActive = true;
    }

    @FXML
    public void initialize() {
        instance = this;

        txtConsole.appendText("--- Console Ready ---\n");

        // Cargar logs históricos sin borrar lo anterior
        synchronized (logBuffer) {
            String historicLogs = logBuffer.toString();
            if (!historicLogs.isEmpty()) {
                txtConsole.appendText(historicLogs);
            }
        }

        // Scroll al final
        if (chkAutoScroll.isSelected()) {
            Platform.runLater(this::scrollToBottom);
        }
    }

    // Helper para append de un solo caracter
    private void appendChar(char c) {
        appendText(String.valueOf(c));
    }

    private void appendText(String str) {
        if (txtConsole != null) {
            Platform.runLater(() -> {
                txtConsole.appendText(str);
                if (chkAutoScroll != null && chkAutoScroll.isSelected()) {
                    scrollToBottom();
                }
            });
        }
    }

    private void scrollToBottom() {
        if (txtConsole != null) {
            txtConsole.setScrollTop(Double.MAX_VALUE);
            txtConsole.selectPositionCaret(txtConsole.getLength());
            txtConsole.deselect();
        }
    }

    public void log(String message) {
        System.out.println("[LOG] " + message);
    }

    @FXML
    void btnClear_OnAction(ActionEvent event) {
        if (txtConsole != null) {
            txtConsole.clear();
        }
        synchronized (logBuffer) {
            logBuffer.setLength(0);
        }
    }

    @FXML
    void btnCopy_OnAction(ActionEvent event) {
        if (txtConsole != null && !txtConsole.getText().isEmpty()) {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putString(txtConsole.getText());
            clipboard.setContent(content);
        }
    }
}

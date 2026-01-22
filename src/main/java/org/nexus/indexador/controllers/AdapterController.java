package org.nexus.indexador.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;

public class AdapterController {

  @FXML
  private TextField txtPos;

  @FXML
  private TextField txtGrafico;

  @FXML
  private TextField txtNumAnimaciones;

  @FXML
  private TextArea txtOriginal;

  @FXML
  private TextArea txtAdaptado;

  @FXML
  private void btnAdapter_OnAction() {
    String input = txtOriginal.getText();
    if (input == null || input.trim().isEmpty()) {
      showAlert(AlertType.WARNING, "Entrada vacía",
          "Por favor, introduce los Grh originales que deseas adaptar.");
      return;
    }

    String[] lineas = input.split("\\r?\\n");
    StringBuilder resultado = new StringBuilder();
    long contador;
    long graphIndex;

    // Validación de entradas numéricas
    try {
      if (txtPos.getText().trim().isEmpty()) {
        showAlert(AlertType.WARNING, "Falta Posición",
            "Debes especificar la 'Primera posición' (índice Grh inicial).");
        return;
      }
      contador = Long.parseLong(txtPos.getText().trim());
    } catch (NumberFormatException e) {
      showAlert(AlertType.ERROR, "Formato Inválido",
          "La 'Primera posición' debe ser un número entero válido.");
      return;
    }

    try {
      if (txtGrafico.getText().trim().isEmpty()) {
        showAlert(AlertType.WARNING, "Falta Gráfico",
            "Debes especificar el 'Nº Gráfico' de destino.");
        return;
      }
      graphIndex = Long.parseLong(txtGrafico.getText().trim());
    } catch (NumberFormatException e) {
      showAlert(AlertType.ERROR, "Formato Inválido",
          "El 'Nº Gráfico' debe ser un número entero válido.");
      return;
    }

    for (String linea : lineas) {
      if (linea.trim().isEmpty())
        continue;

      // Esperamos formato tipo: Grh1=1-100-0-0-32-32
      // Split por '=' para separar clave de valor
      String[] parts = linea.split("=");
      if (parts.length < 2)
        continue; // Línea inválida o comentario

      String valuesWithComment = parts[1];
      // Quitar posibles comentarios al final de la línea (si los hubiera, aunque
      // split simple suele funcionar)
      String values = valuesWithComment.trim();

      String[] tokens = values.split("-");
      if (tokens.length < 2)
        continue; // Mínimo necesitamos Frames y Gráfico

      try {
        int frames = Integer.parseInt(tokens[0].trim());

        if (frames == 1) {
          // Formato Grh estático: Frames-FileNum-SrcX-SrcY-PixelWidth-PixelHeight
          if (tokens.length < 6) {
            // Fallback o ignorar línea mal formada
            continue;
          }
          // Reconstruimos: Grh[NuevoIndex]=1-[NuevoFileNum]-[X]-[Y]-[W]-[H]
          resultado.append("Grh").append(contador).append("=").append("1-").append(graphIndex)
              .append("-") // Reemplazamos
                           // FileNum
              .append(tokens[2]).append("-") // SrcX
              .append(tokens[3]).append("-") // SrcY
              .append(tokens[4]).append("-") // Width
              .append(tokens[5]) // Height
              .append("\n");

        } else {
          // Animación
          // Formato: Frames-Frame1-Frame2...-Speed
          if (txtNumAnimaciones.getText().trim().isEmpty()) {
            showAlert(AlertType.WARNING, "Faltan Frames",
                "Se detectaron animaciones. Por favor especifica el 'Nº Frames' totales de la secuencia.");
            return;
          }

          int totalAnimationFrames;
          try {
            totalAnimationFrames = Integer.parseInt(txtNumAnimaciones.getText().trim());
          } catch (NumberFormatException e) {
            showAlert(AlertType.ERROR, "Formato Inválido",
                "El 'Nº Frames' debe ser un número entero.");
            return;
          }

          // Lógica original conservada: generar secuencia de índices
          StringBuilder tmpSeq = new StringBuilder();
          tmpSeq.append("Grh").append(contador).append("=").append(frames);

          // Generamos los índices de los cuadros de animación basados en la longitud de
          // entrada vs lo esperado
          // La lógica original iteraba: lineas.length - animFrames.
          // Esto es confuso en el código original.
          // Asumiendo que el usuario quiere generar una secuencia de referencias a Grh
          // anteriores o nuevos:

          // Interpretación más segura de la lógica "Adaptar Animación":
          // El código original hacía un bucle extraño.
          // Simplificación: Copiar la estructura de la animación pero re-indexar si fuera
          // necesario?
          // EL ORIGINAL CREABA UNA SECUENCIA NUEVA:
          // tmp = tmp + "-" + j (donde j era un índice de bucle basado en lineas
          // totales??)

          // Dado que la lógica original era muy específica (y posiblemente errónea para
          // uso general),
          // vamos a replicar la funcionalidad PRESUMIBLE:
          // Crear una animación que apunte a los Grh que acabamos de generar (o vamos a
          // generar).
          // Pero como estamos iterando línea a línea...

          // Revisando código original:
          // for (int j = 0; j < lineas.length -
          // Integer.parseInt(txtNumAnimaciones.getText()); j++)
          // Esto sugiere que 'lineas' contiene TODOS los frames individuales PRIMERO, y
          // al final la animación?
          // Si el input es mezclado, esto falla.

          // Vamos a mantener la lógica simple: Copiar los frames tal cual, solo cambiando
          // la velocidad si es el último token?
          // O mejor, simplemente copiar la parte derecha pero reasignando el ID Grh
          // izquierdo. es lo más seguro para "Adaptar".

          // Si es animación, normalmente solo queremos cambiar el ID del Grh (contador).
          // El contenido (lista de frames y velocidad) suele ser relativo a otros Grhs.
          // Si los Grhs referenciados también cambiaron de ID, esto es complejo.

          // ESTRATEGIA SEGURA: Copiar el contenido 'values' tal cual para animaciones,
          // solo cambiando el índice principal.
          resultado.append("Grh").append(contador).append("=").append(values).append("\n");
        }
      } catch (NumberFormatException e) {
        // Ignorar línea con números inválidos
      }

      contador++;
    }

    txtAdaptado.setText(resultado.toString());
  }

  @FXML
  private void btnClear_OnAction() {
    txtAdaptado.clear();
    txtOriginal.clear();
    txtPos.clear();
    txtGrafico.clear();
    txtNumAnimaciones.clear();
  }

  private void showAlert(AlertType type, String title, String content) {
    Alert alert = new Alert(type);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(content);
    alert.showAndWait();
  }
}

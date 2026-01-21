package org.nexus.indexador.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Clase de utilidad para el registro de mensajes (logging) de la aplicación.
 * Permite registrar mensajes de información, advertencia y error, con la
 * posibilidad
 * de guardarlos en un archivo de registro y/o mostrarlos en la consola.
 */
public class Logger {
    private static Logger instance;
    private boolean consoleOutput = true;
    private boolean fileOutput = true;
    private String logFilePath;
    private File logFile;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * Nivel de log para categorizar los mensajes.
     */
    public enum Level {
        INFO,
        WARNING,
        ERROR,
        DEBUG
    }

    private Logger() {
        // Constructor privado para singleton
        // Usar directorio actual de la aplicación
        this.logFilePath = "logs";

        // Crear directorio de logs si no existe
        File logDir = new File(logFilePath);
        if (!logDir.exists()) {
            logDir.mkdirs();
        }

        // Crear un archivo de log con la fecha actual
        String date = new SimpleDateFormat("yyyyMMdd").format(new Date());
        this.logFile = new File(logFilePath + File.separator + "nexus_log_" + date + ".log");

        // Mostrar mensaje de inicio
        log(Level.INFO, "Logger iniciado. Ruta de archivos de log: " + new File(logFilePath).getAbsolutePath());
    }

    /**
     * Obtiene la instancia única del Logger.
     *
     * @return La instancia del Logger.
     */
    public static synchronized Logger getInstance() {
        if (instance == null) {
            instance = new Logger();
        }
        return instance;
    }

    /**
     * Activa o desactiva la salida por consola de los mensajes de log.
     *
     * @param enabled true para activar, false para desactivar.
     */
    public void setConsoleOutput(boolean enabled) {
        this.consoleOutput = enabled;
    }

    /**
     * Activa o desactiva la escritura de los mensajes de log en el archivo.
     *
     * @param enabled true para activar, false para desactivar.
     */
    public void setFileOutput(boolean enabled) {
        this.fileOutput = enabled;
    }

    /**
     * Establece la ruta del archivo de log.
     *
     * @param path Ruta del archivo de log.
     */
    public void setLogFilePath(String path) {
        this.logFilePath = path;
    }

    /**
     * Registra un mensaje con el nivel especificado.
     *
     * @param level   Nivel del mensaje (INFO, WARNING, ERROR, DEBUG).
     * @param message El mensaje a registrar.
     */
    public void log(Level level, String message) {
        String formattedMessage = formatLogMessage(level, message);

        // Salida por consola si está activada
        if (consoleOutput) {
            if (level == Level.ERROR) {
                System.err.println(formattedMessage);
            } else {
                System.out.println(formattedMessage);
            }
        }

        // Salida a archivo si está activada
        if (fileOutput) {
            writeToFile(formattedMessage);
        }
    }

    /**
     * Registra un mensaje de error con su excepción asociada.
     *
     * @param message   El mensaje de error.
     * @param throwable La excepción asociada al error.
     */
    public void error(String message, Throwable throwable) {
        log(Level.ERROR, message + " - Excepción: " + throwable.getMessage());
        if (fileOutput) {
            writeExceptionToFile(throwable);
        }
    }

    /**
     * Registra un mensaje informativo.
     *
     * @param message El mensaje a registrar.
     */
    public void info(String message) {
        log(Level.INFO, message);
    }

    /**
     * Registra un mensaje de advertencia.
     *
     * @param message El mensaje a registrar.
     */
    public void warning(String message) {
        log(Level.WARNING, message);
    }

    /**
     * Registra un mensaje de error.
     *
     * @param message El mensaje a registrar.
     */
    public void error(String message) {
        log(Level.ERROR, message);
    }

    /**
     * Registra un mensaje de depuración.
     *
     * @param message El mensaje a registrar.
     */
    public void debug(String message) {
        log(Level.DEBUG, message);
    }

    /**
     * Formatea un mensaje de log con la fecha, hora y nivel.
     *
     * @param level   Nivel del mensaje.
     * @param message El mensaje a formatear.
     * @return El mensaje formateado.
     */
    private String formatLogMessage(Level level, String message) {
        return dateFormat.format(new Date()) + " [" + level + "] " + message;
    }

    /**
     * Escribe un mensaje en el archivo de log.
     *
     * @param message El mensaje a escribir.
     */
    private void writeToFile(String message) {
        try (FileWriter fw = new FileWriter(logFile, true);
                PrintWriter pw = new PrintWriter(fw)) {
            pw.println(message);
        } catch (IOException e) {
            // Si no podemos escribir en el archivo de log, al menos mostramos el error en
            // consola
            System.err.println("Error al escribir en el archivo de log: " + e.getMessage());
        }
    }

    /**
     * Escribe una excepción completa en el archivo de log.
     *
     * @param throwable La excepción a escribir.
     */
    private void writeExceptionToFile(Throwable throwable) {
        try (FileWriter fw = new FileWriter(logFile, true);
                PrintWriter pw = new PrintWriter(fw)) {
            pw.println(dateFormat.format(new Date()) + " [EXCEPTION STACK TRACE]:");
            throwable.printStackTrace(pw);
        } catch (IOException e) {
            System.err.println("Error al escribir la excepción en el archivo de log: " + e.getMessage());
        }
    }
}

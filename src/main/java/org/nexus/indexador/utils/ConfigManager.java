package org.nexus.indexador.utils;

import java.io.*;

public class ConfigManager {

    // Instancia única de ConfigManager (volatile para thread safety)
    private static volatile ConfigManager instance;

    private String graphicsDir;
    private String initDir;
    private String datDir;
    private String exportDir;

    private static final String CONFIG_FILE_NAME = "config.ini";
    private static final String CONFIG_FILE_PATH = Thread.currentThread().getContextClassLoader()
            .getResource(CONFIG_FILE_NAME).getPath();

    private ConfigManager() {
    }

    public static ConfigManager getInstance() {
        if (instance == null) {
            synchronized (ConfigManager.class) {
                if (instance == null) {
                    instance = new ConfigManager();
                }
            }
        }
        return instance;
    }

    // Getters
    public String getGraphicsDir() {
        return graphicsDir;
    }

    public String getInitDir() {
        return initDir;
    }

    public String getDatDir() {
        return datDir;
    }

    public String getExportDir() {
        return exportDir;
    }

    // Setters
    public void setGraphicsDir(String graphicsDir) {
        this.graphicsDir = graphicsDir;
    }

    public void setInitDir(String initDir) {
        this.initDir = initDir;
    }

    public void setDatDir(String datDir) {
        this.datDir = datDir;
    }

    public void setExportDir(String exportDir) {
        this.exportDir = exportDir;
    }

    public void readConfig() throws IOException {
        File configFile = new File(CONFIG_FILE_PATH);
        if (configFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(configFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split("=");
                    if (parts.length == 2) {
                        String key = parts[0].trim();
                        String value = parts[1].trim();

                        if (key.equals("Graficos")) {
                            graphicsDir = value;
                        } else if (key.equals("Init")) {
                            initDir = value;
                        } else if (key.equals("Dat")) {
                            datDir = value;
                        } else if (key.equals("Exportados")) {
                            exportDir = value;
                        }
                    }
                }
            } catch (IOException e) {
                // Manejar la excepción de lectura del archivo
                System.err.println("Error al leer el archivo de configuración: " + e.getMessage());
                throw e; // Lanzar la excepción para que sea manejada en otro lugar si es necesario
            }
        } else {
            // Manejar el caso en el que el archivo de configuración no existe
            System.err.println("El archivo de configuración no existe: " + CONFIG_FILE_PATH);
        }
    }

    public void writeConfig() throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(CONFIG_FILE_PATH))) {
            writer.write("Graficos=" + graphicsDir);
            writer.newLine();
            writer.write("Init=" + initDir);
            writer.newLine();
            writer.write("Dat=" + datDir);
            writer.newLine();
            writer.write("Exportados=" + exportDir);
        }
    }

}

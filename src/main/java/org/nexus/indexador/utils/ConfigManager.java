package org.nexus.indexador.utils;

import java.io.*;

public class ConfigManager {

    // Instancia única de ConfigManager (volatile para thread safety)
    private static volatile ConfigManager instance;

    private String graphicsDir;
    private String initDir;
    private String datDir;
    private String exportDir;
    private String backgroundColor = "#EA3FF7"; // Default magenta
    private String indexingSystem = "MOLD"; // Default: Sistema de Moldes

    private static final String CONFIG_FILE_NAME = "config.ini";
    // Guardar en el directorio de la aplicación
    private static final String CONFIG_FILE_PATH = CONFIG_FILE_NAME;

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

    /**
     * Verifica si existe el archivo de configuración.
     * 
     * @return true si existe, false si es la primera vez
     */
    public boolean configExists() {
        File configFile = new File(CONFIG_FILE_PATH);
        return configFile.exists();
    }

    // Getters - normalizan las rutas para asegurar separador final
    public String getGraphicsDir() {
        return normalizePath(graphicsDir);
    }

    public String getInitDir() {
        return normalizePath(initDir);
    }

    public String getDatDir() {
        return normalizePath(datDir);
    }

    public String getExportDir() {
        return normalizePath(exportDir);
    }

    public String getBackgroundColor() {
        return backgroundColor;
    }

    public String getIndexingSystem() {
        return indexingSystem;
    }

    /**
     * Normaliza una ruta asegurando que termine con separador.
     */
    private String normalizePath(String path) {
        if (path == null || path.isEmpty()) {
            return "";
        }
        if (!path.endsWith(File.separator) && !path.endsWith("/") && !path.endsWith("\\")) {
            return path + File.separator;
        }
        return path;
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

    public void setBackgroundColor(String backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public void setIndexingSystem(String indexingSystem) {
        this.indexingSystem = indexingSystem;
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
                        } else if (key.equals("BackgroundColor")) {
                            backgroundColor = value;
                        } else if (key.equals("IndexingSystem")) {
                            indexingSystem = value;
                        }
                    }
                }
            }
        }
    }

    public void writeConfig() throws IOException {
        // Crear directorio si no existe
        File configFile = new File(CONFIG_FILE_PATH);
        File configDir = configFile.getParentFile();
        if (configDir != null && !configDir.exists()) {
            configDir.mkdirs();
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(CONFIG_FILE_PATH))) {
            writer.write("Graficos=" + graphicsDir);
            writer.newLine();
            writer.write("Init=" + initDir);
            writer.newLine();
            writer.write("Dat=" + datDir);
            writer.newLine();
            writer.write("Exportados=" + exportDir);
            writer.newLine();
            writer.write("BackgroundColor=" + backgroundColor);
            writer.newLine();
            writer.write("IndexingSystem=" + indexingSystem);
        }
    }

}

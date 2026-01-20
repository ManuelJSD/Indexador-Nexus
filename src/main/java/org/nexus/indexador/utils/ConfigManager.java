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
    // Usar directorio del usuario en lugar de resources
    private static final String CONFIG_FILE_PATH = System.getProperty("user.home") +
            File.separator + ".indexador-nexus" + File.separator + CONFIG_FILE_NAME;

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
            }
        }
    }

    public void writeConfig() throws IOException {
        // Crear directorio si no existe
        File configFile = new File(CONFIG_FILE_PATH);
        File configDir = configFile.getParentFile();
        if (!configDir.exists()) {
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
        }
    }

}

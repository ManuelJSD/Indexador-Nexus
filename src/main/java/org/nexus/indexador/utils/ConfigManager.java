package org.nexus.indexador.utils;

import java.io.*;

/**
 * Singleton que mantiene la configuración activa de la aplicación.
 *
 * <p>Con el sistema de perfiles, cada perfil tiene su propio archivo {@code .ini}.
 * Los métodos {@link #readConfig(File)} y {@link #writeConfig(File)} leen/escriben
 * en un archivo arbitrario, mientras que las versiones sin parámetros operan sobre
 * el archivo del perfil activo en {@link ProfileManager}.
 *
 * <p>Antes de cargar un nuevo perfil, llamar {@link #resetToDefaults()} para
 * evitar que los valores del perfil anterior "se filtren" al nuevo.
 */
public class ConfigManager {

  // Instancia única de ConfigManager (volatile para thread safety)
  private static volatile ConfigManager instance;

  private String graphicsDir;
  private String initDir; // Carpeta de Índices (Init/Dat)
  private String exportDir;
  private String backgroundColor = "#EA3FF7"; // Default magenta
  private String indexingSystem = "CLASSIC"; // Default: Sistema Clásico
  private String appTheme = "DARK"; // Default: Tema Oscuro

  /** Archivo de config legacy (usado cuando no hay sistema de perfiles activo). */
  private static final String CONFIG_FILE_PATH = "config.ini";

  private ConfigManager() {
  }

  /** @return Instancia única de {@code ConfigManager}. */
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

  // -------------------------------------------------------------------------
  // Reset
  // -------------------------------------------------------------------------

  /**
   * Restablece todos los campos a sus valores por defecto.
   * Debe llamarse antes de cargar la configuración de un perfil nuevo para
   * evitar que valores anteriores persistan en memoria.
   */
  public void resetToDefaults() {
    graphicsDir = null;
    initDir = null;
    exportDir = null;
    backgroundColor = "#EA3FF7";
    indexingSystem = "CLASSIC";
    appTheme = "DARK";
  }

  // -------------------------------------------------------------------------
  // Verificación de existencia
  // -------------------------------------------------------------------------

  /**
   * Verifica si existe el archivo de configuración del perfil activo.
   * Si no hay perfil activo, comprueba el config.ini legacy.
   *
   * @return {@code true} si el archivo de configuración existe.
   */
  public boolean configExists() {
    ProfileEntry perfilActivo = ProfileManager.getInstance().getPerfilActivo();
    if (perfilActivo != null) {
      return perfilActivo.getConfigFile().exists();
    }
    return new File(CONFIG_FILE_PATH).exists();
  }

  // -------------------------------------------------------------------------
  // Getters (normalizan rutas)
  // -------------------------------------------------------------------------

  /** @return Carpeta de gráficos con separador al final. */
  public String getGraphicsDir() {
    return normalizePath(graphicsDir);
  }

  /** @return Carpeta de índices con separador al final. */
  public String getInitDir() {
    return normalizePath(initDir);
  }

  /** @return Alias de {@link #getInitDir()}. */
  public String getDatDir() {
    return normalizePath(initDir);
  }

  /** @return Carpeta de exportados con separador al final. */
  public String getExportDir() {
    return normalizePath(exportDir);
  }

  /** @return Color de fondo en formato {@code #RRGGBB}. */
  public String getBackgroundColor() {
    return backgroundColor;
  }

  /** @return Código del sistema de indexación ({@code "CLASSIC"} o {@code "MOLD"}). */
  public String getIndexingSystem() {
    return indexingSystem;
  }

  /** @return Código del tema visual ({@code "DARK"} o {@code "LIGHT"}). */
  public String getAppTheme() {
    return appTheme;
  }

  // -------------------------------------------------------------------------
  // Setters
  // -------------------------------------------------------------------------

  /** @param graphicsDir Nueva carpeta de gráficos. */
  public void setGraphicsDir(String graphicsDir) {
    this.graphicsDir = graphicsDir;
  }

  /** @param initDir Nueva carpeta de índices. */
  public void setInitDir(String initDir) {
    this.initDir = initDir;
  }

  /** @param datDir Nueva carpeta de datos (alias de initDir). */
  public void setDatDir(String datDir) {
    this.initDir = datDir;
  }

  /** @param exportDir Nueva carpeta de exportados. */
  public void setExportDir(String exportDir) {
    this.exportDir = exportDir;
  }

  /** @param backgroundColor Nuevo color de fondo en formato {@code #RRGGBB}. */
  public void setBackgroundColor(String backgroundColor) {
    this.backgroundColor = backgroundColor;
  }

  /** @param indexingSystem Nuevo sistema de indexación ({@code "CLASSIC"} o {@code "MOLD"}). */
  public void setIndexingSystem(String indexingSystem) {
    this.indexingSystem = indexingSystem;
  }

  /** @param appTheme Nuevo tema ({@code "DARK"} o {@code "LIGHT"}). */
  public void setAppTheme(String appTheme) {
    this.appTheme = appTheme;
  }

  // -------------------------------------------------------------------------
  // Lectura de configuración
  // -------------------------------------------------------------------------

  /**
   * Lee la configuración desde el archivo del perfil activo en {@link ProfileManager}.
   * Si no hay perfil activo, lee desde el {@code config.ini} legacy.
   *
   * @throws IOException si ocurre un error de E/S.
   */
  public void readConfig() throws IOException {
    ProfileEntry perfilActivo = ProfileManager.getInstance().getPerfilActivo();
    if (perfilActivo != null) {
      readConfig(perfilActivo.getConfigFile());
    } else {
      readConfig(new File(CONFIG_FILE_PATH));
    }
  }

  /**
   * Lee la configuración desde un archivo {@code .ini} específico.
   *
   * @param configFile Archivo {@code .ini} del que leer.
   * @throws IOException si ocurre un error de E/S.
   */
  public void readConfig(File configFile) throws IOException {
    if (!configFile.exists()) {
      return;
    }
    try (BufferedReader reader = new BufferedReader(new FileReader(configFile))) {
      String line;
      while ((line = reader.readLine()) != null) {
        // Ignorar comentarios y líneas vacías
        if (line.trim().isEmpty() || line.trim().startsWith("#")) {
          continue;
        }
        int sep = line.indexOf('=');
        if (sep < 0) {
          continue;
        }
        String key = line.substring(0, sep).trim();
        String value = line.substring(sep + 1).trim();

        switch (key) {
          case "Graficos":
            graphicsDir = value;
            break;
          case "Init":
          case "Dat":
            initDir = value;
            break;
          case "Exportados":
            exportDir = value;
            break;
          case "BackgroundColor":
            backgroundColor = value;
            break;
          case "IndexingSystem":
            indexingSystem = value;
            break;
          case "AppTheme":
            appTheme = value;
            break;
          default:
            break;
        }
      }
    }
  }

  // -------------------------------------------------------------------------
  // Escritura de configuración
  // -------------------------------------------------------------------------

  /**
   * Escribe la configuración en el archivo del perfil activo en {@link ProfileManager}.
   * Si no hay perfil activo, escribe en el {@code config.ini} legacy.
   *
   * @throws IOException si ocurre un error de E/S.
   */
  public void writeConfig() throws IOException {
    ProfileEntry perfilActivo = ProfileManager.getInstance().getPerfilActivo();
    if (perfilActivo != null) {
      writeConfig(perfilActivo.getConfigFile());
    } else {
      writeConfig(new File(CONFIG_FILE_PATH));
    }
  }

  /**
   * Escribe la configuración en el archivo {@code .ini} indicado.
   *
   * @param configFile Destino de la configuración.
   * @throws IOException si ocurre un error de E/S.
   */
  public void writeConfig(File configFile) throws IOException {
    // Crear directorio padre si no existe
    File dir = configFile.getParentFile();
    if (dir != null && !dir.exists()) {
      dir.mkdirs();
    }

    try (BufferedWriter writer = new BufferedWriter(new FileWriter(configFile))) {
      writer.write("Graficos=" + (graphicsDir != null ? graphicsDir : ""));
      writer.newLine();
      writer.write("Init=" + (initDir != null ? initDir : ""));
      writer.newLine();
      writer.write("Exportados=" + (exportDir != null ? exportDir : ""));
      writer.newLine();
      writer.write("BackgroundColor=" + backgroundColor);
      writer.newLine();
      writer.write("IndexingSystem=" + indexingSystem);
      writer.newLine();
      writer.write("AppTheme=" + appTheme);
    }
  }

  // -------------------------------------------------------------------------
  // Helpers
  // -------------------------------------------------------------------------

  /**
   * Normaliza una ruta asegurando que termine con separador de directorio.
   *
   * @param path Ruta a normalizar.
   * @return Ruta normalizada o cadena vacía si es nula.
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
}

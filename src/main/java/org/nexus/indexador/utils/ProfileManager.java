package org.nexus.indexador.utils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Singleton que gestiona la lista de perfiles de configuración.
 *
 * <p>Los perfiles se persisten en {@code profiles.json} (directorio de trabajo de la app).
 * Cada perfil apunta a su propio archivo {@code .ini} dentro de la subcarpeta
 * {@code profiles/}.
 *
 * <p>Serialización propia sin dependencias externas usando JSON minimalista.
 */
public class ProfileManager {

  private static volatile ProfileManager instance;

  /** Archivo de índice de perfiles. */
  private static final String PROFILES_JSON = "profiles.json";

  /** Subcarpeta donde se guardan los .ini de cada perfil. */
  private static final String PROFILES_DIR = "profiles";

  private final List<ProfileEntry> perfiles = new ArrayList<>();
  private ProfileEntry perfilActivo;

  private ProfileManager() {
  }

  /** @return Instancia única de {@code ProfileManager}. */
  public static ProfileManager getInstance() {
    if (instance == null) {
      synchronized (ProfileManager.class) {
        if (instance == null) {
          instance = new ProfileManager();
        }
      }
    }
    return instance;
  }

  // -------------------------------------------------------------------------
  // Acceso a la lista
  // -------------------------------------------------------------------------

  /** @return Lista mutable de perfiles en el orden actual. */
  public List<ProfileEntry> getPerfiles() {
    return perfiles;
  }

  /** @return {@code true} si no hay ningún perfil registrado. */
  public boolean isEmpty() {
    return perfiles.isEmpty();
  }

  // -------------------------------------------------------------------------
  // Perfil activo
  // -------------------------------------------------------------------------

  /** @return Perfil actualmente seleccionado (puede ser {@code null}). */
  public ProfileEntry getPerfilActivo() {
    return perfilActivo;
  }

  /** @param perfil Perfil a marcar como activo. */
  public void setPerfilActivo(ProfileEntry perfil) {
    this.perfilActivo = perfil;
  }

  // -------------------------------------------------------------------------
  // CRUD
  // -------------------------------------------------------------------------

  /**
   * Agrega un nuevo perfil y lo persiste.
   *
   * @param nombre     Nombre descriptivo.
   * @param configPath Ruta al .ini del perfil.
   * @return El perfil creado.
   */
  public ProfileEntry agregarPerfil(String nombre, String configPath) {
    ProfileEntry entry = new ProfileEntry(nombre, configPath);
    perfiles.add(entry);
    guardar();
    return entry;
  }

  /**
   * Renombra el perfil en la posición indicada y persiste.
   *
   * @param indice     Índice en la lista.
   * @param nuevoNombre Nuevo nombre.
   */
  public void renombrarPerfil(int indice, String nuevoNombre) {
    if (indice < 0 || indice >= perfiles.size()) {
      return;
    }
    perfiles.get(indice).setNombre(nuevoNombre);
    guardar();
  }

  /**
   * Elimina el perfil en la posición indicada y persiste.
   * No elimina el archivo .ini del disco.
   *
   * @param indice Índice en la lista.
   */
  public void eliminarPerfil(int indice) {
    if (indice < 0 || indice >= perfiles.size()) {
      return;
    }
    ProfileEntry eliminado = perfiles.remove(indice);
    if (eliminado == perfilActivo) {
      perfilActivo = null;
    }
    guardar();
  }

  /**
   * Mueve el perfil en {@code indice} una posición hacia arriba en la lista y persiste.
   *
   * @param indice Índice del perfil a mover.
   */
  public void moverArriba(int indice) {
    if (indice <= 0 || indice >= perfiles.size()) {
      return;
    }
    ProfileEntry temp = perfiles.get(indice - 1);
    perfiles.set(indice - 1, perfiles.get(indice));
    perfiles.set(indice, temp);
    guardar();
  }

  /**
   * Mueve el perfil en {@code indice} una posición hacia abajo en la lista y persiste.
   *
   * @param indice Índice del perfil a mover.
   */
  public void moverAbajo(int indice) {
    if (indice < 0 || indice >= perfiles.size() - 1) {
      return;
    }
    ProfileEntry temp = perfiles.get(indice + 1);
    perfiles.set(indice + 1, perfiles.get(indice));
    perfiles.set(indice, temp);
    guardar();
  }

  // -------------------------------------------------------------------------
  // Generación de ruta para nuevo perfil
  // -------------------------------------------------------------------------

  /**
   * Genera la ruta relativa para el .ini de un perfil nuevo, garantizando que
   * no colisione con archivos existentes.
   *
   * @param nombre Nombre del perfil.
   * @return Ruta relativa, p.ej. {@code "profiles/servidor_beta.ini"}.
   */
  public String generarConfigPath(String nombre) {
    // Sanitizar nombre: solo alfanuméricos y guiones bajos
    String base = nombre.toLowerCase()
        .replaceAll("[^a-z0-9_áéíóúñ]", "_")
        .replaceAll("_+", "_")
        .replaceAll("^_|_$", "");
    if (base.isEmpty()) {
      base = "perfil";
    }

    // Asegurar que la subcarpeta exista
    File dir = new File(PROFILES_DIR);
    if (!dir.exists()) {
      dir.mkdirs();
    }

    // Evitar colisiones
    String candidate = PROFILES_DIR + "/" + base + ".ini";
    int counter = 1;
    while (new File(candidate).exists()) {
      candidate = PROFILES_DIR + "/" + base + "_" + counter + ".ini";
      counter++;
    }
    return candidate;
  }

  // -------------------------------------------------------------------------
  // Persistencia (JSON manual)
  // -------------------------------------------------------------------------

  /**
   * Carga la lista de perfiles desde {@code profiles.json}.
   * Si el archivo no existe o está malformado, la lista queda vacía.
   */
  public void cargar() {
    perfiles.clear();
    File json = new File(PROFILES_JSON);
    if (!json.exists()) {
      return;
    }

    try (BufferedReader reader = new BufferedReader(new FileReader(json))) {
      StringBuilder sb = new StringBuilder();
      String linea;
      while ((linea = reader.readLine()) != null) {
        sb.append(linea.trim());
      }
      parsearJson(sb.toString());
    } catch (IOException e) {
      Logger.getInstance().error("Error al leer profiles.json", e);
    }
  }

  /**
   * Persiste la lista actual de perfiles en {@code profiles.json}.
   */
  public void guardar() {
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(PROFILES_JSON))) {
      writer.write("[\n");
      for (int i = 0; i < perfiles.size(); i++) {
        ProfileEntry p = perfiles.get(i);
        writer.write("  {\n");
        writer.write("    \"nombre\": " + jsonString(p.getNombre()) + ",\n");
        writer.write("    \"configPath\": " + jsonString(p.getConfigPath()) + "\n");
        writer.write("  }");
        if (i < perfiles.size() - 1) {
          writer.write(",");
        }
        writer.write("\n");
      }
      writer.write("]\n");
    } catch (IOException e) {
      Logger.getInstance().error("Error al guardar profiles.json", e);
    }
  }

  // -------------------------------------------------------------------------
  // Helpers JSON
  // -------------------------------------------------------------------------

  /**
   * Parser JSON minimalista para la estructura conocida del profiles.json.
   * No usa ninguna librería externa.
   */
  private void parsearJson(String json) {
    // Eliminar corchetes externos
    json = json.trim();
    if (json.startsWith("[")) {
      json = json.substring(1);
    }
    if (json.endsWith("]")) {
      json = json.substring(0, json.length() - 1);
    }

    // Dividir por objetos { ... }
    int depth = 0;
    int start = -1;
    for (int i = 0; i < json.length(); i++) {
      char c = json.charAt(i);
      if (c == '{') {
        if (depth == 0) {
          start = i;
        }
        depth++;
      } else if (c == '}') {
        depth--;
        if (depth == 0 && start >= 0) {
          String objeto = json.substring(start + 1, i).trim();
          ProfileEntry entry = parsearObjeto(objeto);
          if (entry != null) {
            perfiles.add(entry);
          }
          start = -1;
        }
      }
    }
  }

  /**
   * Extrae un ProfileEntry del contenido interno de un objeto JSON simple.
   * Espera exactamente las claves "nombre" y "configPath".
   */
  private ProfileEntry parsearObjeto(String contenido) {
    String nombre = extraerValor(contenido, "nombre");
    String configPath = extraerValor(contenido, "configPath");
    if (nombre == null || configPath == null) {
      return null;
    }
    return new ProfileEntry(nombre, configPath);
  }

  /**
   * Extrae el valor de una clave de un fragmento JSON plano (sin anidamiento).
   *
   * @param json Fragmento JSON.
   * @param key  Clave a buscar.
   * @return Valor sin comillas, o {@code null} si no se encuentra.
   */
  private String extraerValor(String json, String key) {
    String buscado = "\"" + key + "\"";
    int idx = json.indexOf(buscado);
    if (idx < 0) {
      return null;
    }
    int colon = json.indexOf(':', idx + buscado.length());
    if (colon < 0) {
      return null;
    }
    String resto = json.substring(colon + 1).trim();
    if (resto.startsWith("\"")) {
      // Valor string: buscar cierre de comillas, respetando escapes
      StringBuilder valor = new StringBuilder();
      int i = 1;
      while (i < resto.length()) {
        char c = resto.charAt(i);
        if (c == '\\' && i + 1 < resto.length()) {
          char siguiente = resto.charAt(i + 1);
          if (siguiente == '"') {
            valor.append('"');
          } else if (siguiente == '\\') {
            valor.append('\\');
          } else if (siguiente == 'n') {
            valor.append('\n');
          } else {
            valor.append(siguiente);
          }
          i += 2;
        } else if (c == '"') {
          break;
        } else {
          valor.append(c);
          i++;
        }
      }
      return valor.toString();
    }
    return null;
  }

  /**
   * Serializa un String de Java como valor JSON con comillas y escapes básicos.
   *
   * @param valor Cadena a serializar.
   * @return Cadena con comillas JSON, p.ej. {@code "\"hola\\\"mundo\""}.
   */
  private String jsonString(String valor) {
    if (valor == null) {
      return "null";
    }
    return "\"" + valor
        .replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("\n", "\\n")
        .replace("\r", "\\r")
        + "\"";
  }
}

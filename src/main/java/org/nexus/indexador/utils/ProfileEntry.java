package org.nexus.indexador.utils;

import java.io.File;

/**
 * Representa un perfil de configuración.
 * Cada perfil tiene un nombre descriptivo y apunta a su propio archivo .ini.
 */
public class ProfileEntry {

  /** Nombre visible del perfil (ej: "AO 0.11.5", "Servidor Beta"). */
  private String nombre;

  /**
   * Ruta relativa al .ini de este perfil (ej: "profiles/ao_0115.ini").
   * Siempre relativa al directorio de trabajo de la aplicación.
   */
  private String configPath;

  /**
   * Crea una entrada de perfil.
   *
   * @param nombre     Nombre descriptivo del perfil.
   * @param configPath Ruta al archivo .ini del perfil.
   */
  public ProfileEntry(String nombre, String configPath) {
    this.nombre = nombre;
    this.configPath = configPath;
  }

  /** @return Nombre del perfil. */
  public String getNombre() {
    return nombre;
  }

  /** @param nombre Nuevo nombre del perfil. */
  public void setNombre(String nombre) {
    this.nombre = nombre;
  }

  /** @return Ruta relativa del archivo .ini del perfil. */
  public String getConfigPath() {
    return configPath;
  }

  /** @param configPath Nueva ruta del archivo .ini. */
  public void setConfigPath(String configPath) {
    this.configPath = configPath;
  }

  /**
   * Devuelve el archivo .ini de este perfil resuelto desde el directorio de trabajo.
   *
   * @return Objeto File apuntando al .ini del perfil.
   */
  public File getConfigFile() {
    return new File(configPath);
  }

  /** Representación textual usada por el ListView. */
  @Override
  public String toString() {
    return nombre;
  }
}

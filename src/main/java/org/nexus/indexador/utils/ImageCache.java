package org.nexus.indexador.utils;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;

import java.io.File;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;

/**
 * Clase que implementa un sistema de caché para imágenes. Utiliza referencias
 * suaves
 * (SoftReference) para permitir al recolector de basura liberar la memoria de
 * las imágenes cuando
 * el sistema lo necesite.
 */
public class ImageCache {
  private static ImageCache instance;
  private final Map<String, SoftReference<Image>> imageCache = new HashMap<>();
  private final Logger logger = Logger.getInstance();

  private ImageCache() {
    // Constructor privado para singleton
    logger.info("ImageCache inicializado");
  }

  /**
   * Obtiene la instancia única de ImageCache.
   *
   * @return La instancia del caché de imágenes.
   */
  public static synchronized ImageCache getInstance() {
    if (instance == null) {
      instance = new ImageCache();
    }
    return instance;
  }

  /**
   * Obtiene una imagen del caché, o la carga si no está disponible.
   *
   * @param imagePath Ruta completa de la imagen a cargar.
   * @return La imagen cargada, o null si no se pudo cargar.
   */
  public Image getImage(String imagePath) {
    // Verificar si la imagen ya está en caché
    if (imageCache.containsKey(imagePath)) {
      SoftReference<Image> ref = imageCache.get(imagePath);
      Image cachedImage = ref.get();

      // Si la referencia no ha sido recolectada por el GC, retornarla
      if (cachedImage != null) {
        // logger.debug("Imagen obtenida del caché: " + imagePath);
        return cachedImage;
      }
      // Si ha sido recolectada, eliminarla del caché
      imageCache.remove(imagePath);
      // logger.debug("Referencia de imagen liberada por GC: " + imagePath);
    }

    // Cargar la imagen desde el archivo
    File imageFile = new File(imagePath);
    if (imageFile.exists()) {
      try {
        // logger.debug("Cargando imagen nueva: " + imagePath);
        Image newImage = new Image(imageFile.toURI().toString());
        // Almacenar en caché con referencia suave
        imageCache.put(imagePath, new SoftReference<>(newImage));
        return newImage;
      } catch (Exception e) {
        logger.error("Error al cargar imagen " + imagePath, e);
        return null;
      }
    } else {
      logger.warning("Archivo de imagen no encontrado: " + imagePath);
      return null;
    }
  }

  /**
   * Obtiene una subimagen (región recortada) de una imagen.
   *
   * @param imagePath Ruta de la imagen original.
   * @param x         Coordenada X de inicio del recorte.
   * @param y         Coordenada Y de inicio del recorte.
   * @param width     Ancho del recorte.
   * @param height    Alto del recorte.
   * @return La imagen recortada, o null si la imagen original no existe.
   */
  public WritableImage getCroppedImage(String imagePath, int x, int y, int width, int height) {
    Image sourceImage = getImage(imagePath);
    if (sourceImage != null) {
      try {
        PixelReader pixelReader = sourceImage.getPixelReader();
        /*
         * logger.debug("Recortando imagen: " + imagePath + " en coordenadas [" + x +
         * "," + y
         * + "] con tamaño " + width + "x" + height);
         */
        return new WritableImage(pixelReader, x, y, width, height);
      } catch (Exception e) {
        logger.error("Error al recortar imagen " + imagePath, e);
        return null;
      }
    }
    return null;
  }

  /**
   * Limpia el caché de imágenes.
   */
  public void clearCache() {
    int size = imageCache.size();
    imageCache.clear();
    logger.info("Caché de imágenes limpiado. Se eliminaron " + size + " referencias.");
  }
}

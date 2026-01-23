package org.nexus.indexador.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Utilidad para resolver rutas de archivos de recursos que pueden tener nombres
 * en español o inglés.
 * Garantiza la compatibilidad universal del indexador.
 */
public class ResourceResolver {

    private static final Logger logger = Logger.getInstance();

    /**
     * Resuelve el archivo correcto buscando en una lista de nombres posibles.
     *
     * @param directory     Directorio donde buscar.
     * @param possibleNames Nombres de archivo sugeridos (sin ruta).
     * @return El objeto File que apunta al primer archivo encontrado, o un File con
     *         el primer nombre sugerido si ninguno existe.
     */
    public static File resolve(String directory, String... possibleNames) {
        if (directory == null)
            directory = "";

        for (String name : possibleNames) {
            File file = new File(directory + name);
            if (file.exists()) {
                logger.info("Recurso resuelto: " + file.getAbsolutePath());
                return file;
            }
        }

        // Si ninguno existe, retornamos el primero por defecto (para que el gestor de
        // errores proceda normalmente)
        String fallbackName = (possibleNames.length > 0) ? possibleNames[0] : "";
        return new File(directory + fallbackName);
    }

    // --- Helper Methods para tipos específicos ---

    public static File getHeadsInd(String dir) {
        return resolve(dir, "cabezas.ind", "Heads.ind", "cabezas.ind");
    }

    public static File getHelmetsInd(String dir) {
        return resolve(dir, "cascos.ind", "Helmets.ind", "cascos.ind");
    }

    public static File getBodiesInd(String dir) {
        return resolve(dir, "personajes.ind", "Bodies.ind", "Characters.ind", "cuerpos.ind");
    }

    public static File getShieldsInd(String dir) {
        return resolve(dir, "escudos.ind", "Shields.ind", "escudos.ind");
    }

    public static File getFxsInd(String dir) {
        return resolve(dir, "fxs.ind", "Fxs.ind", "FX.ind");
    }

    public static File getGraphicsInd(String dir) {
        return resolve(dir, "graficos.ind", "Graphics.ind", "graficos.ind");
    }

    public static File getWeaponsInd(String dir) {
        return resolve(dir, "armas.ind", "Weapons.ind", "armas.ind");
    }

    // --- Versiones de Texto (.ini / .dat) ---

    public static File getHeadsText(String dir) {
        return resolve(dir, "Cabezas.ini", "Heads.ini", "Cabezas.dat", "heads.dat");
    }

    public static File getHelmetsText(String dir) {
        return resolve(dir, "Cascos.ini", "Helmets.ini", "Cascos.dat", "helmets.dat");
    }

    public static File getBodiesText(String dir) {
        return resolve(dir, "Personajes.ini", "Bodies.ini", "Personajes.dat", "bodies.dat", "Cuerpos.ini",
                "Cuerpos.dat");
    }

    public static File getShieldsText(String dir) {
        return resolve(dir, "Escudos.ini", "Shields.ini", "Escudos.dat", "shields.dat");
    }

    public static File getFxsText(String dir) {
        return resolve(dir, "FXs.ini", "Fxs.ini", "FXs.dat", "FX.dat");
    }

    public static File getGraphicsText(String dir) {
        return resolve(dir, "Graficos.ini", "Graphics.ini", "Graficos.dat", "graphics.dat");
    }

    public static File getWeaponsText(String dir) {
        return resolve(dir, "Armas.ini", "Weapons.ini", "Armas.dat", "weapons.dat");
    }
}

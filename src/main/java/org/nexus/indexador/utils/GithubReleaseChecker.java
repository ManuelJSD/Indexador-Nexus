package org.nexus.indexador.utils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.nexus.indexador.Main;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class GithubReleaseChecker {

    private static final Logger logger = Logger.getInstance();

    private static final String REPO_OWNER = "ManuelJSD";
    private static final String REPO_NAME = "Indexador-Nexus";
    // URL Base
    private static final String BASE_URL = "https://api.github.com/repos/" + REPO_OWNER + "/" + REPO_NAME;

    private static ReleaseInfo latestRelease = null;
    private static boolean checkPerformed = false;

    public static class ReleaseInfo {
        public final String tagName;
        public final String htmlUrl;
        public final String name;
        public final String body;
        public final boolean isPrerelease;

        public ReleaseInfo(String tagName, String htmlUrl, String name, String body, boolean isPrerelease) {
            this.tagName = tagName;
            this.htmlUrl = htmlUrl;
            this.name = name;
            this.body = body;
            this.isPrerelease = isPrerelease;
        }
    }

    public static void checkForUpdates(java.util.function.Consumer<ReleaseInfo> onUpdateFound) {
        if (checkPerformed)
            return;
        checkPerformed = true;

        boolean checkPre = false; // Solo lanzamientos estables

        // Decidir URL basado en opciones
        String targetUrl;
        if (checkPre) {
            targetUrl = BASE_URL + "/releases?per_page=1";
        } else {
            targetUrl = BASE_URL + "/releases/latest";
        }

        logger.info("Iniciando búsqueda de actualizaciones... (Pre-releases: " + checkPre + ")");

        new Thread(() -> {
            try {
                URL url = java.net.URI.create(targetUrl).toURL();
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Accept", "application/vnd.github.v3+json");
                connection.setRequestProperty("User-Agent", "Indexador-Nexus");
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);

                logger.info("Verificando actualizaciones desde: " + targetUrl);

                if (connection.getResponseCode() == 200) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    ReleaseInfo release = parseRelease(response.toString());
                    if (release != null) {
                        logger.debug("Etiqueta remota: " + release.tagName);
                        boolean newer = isNewerVersion(release.tagName);
                        logger.debug("Versión local: " + Main.VERSION + " | ¿Es más nueva? " + newer);

                        if (newer) {
                            latestRelease = release;
                            logger.info("Nueva versión encontrada: " + release.tagName);
                            if (onUpdateFound != null) {
                                onUpdateFound.accept(release);
                            }
                        } else {
                            logger.info("No se encontraron actualizaciones o la versión actual está al día.");
                        }
                    } else {
                        logger.warning("El objeto de release es null tras ser procesado.");
                    }
                } else if (connection.getResponseCode() == 404) {
                    logger.warning("Repositorio no encontrado para la búsqueda de actualizaciones.");
                } else {
                    logger.error("Error en la API de GitHub al buscar actualizaciones. Código: " + connection.getResponseCode());
                }
            } catch (Exception e) {
                logger.error("Excepción durante la búsqueda de actualizaciones", e);
            }
        }).start();
    }

    private static ReleaseInfo parseRelease(String json) {
        if (json == null)
            return null;
        try {
            Gson gson = new Gson();
            JsonObject jsonObject = null;

            // Comprobar si comienza con [ (Array) o { (Objeto)
            String trimmed = json.trim();
            if (trimmed.startsWith("[")) {
                com.google.gson.JsonArray jsonArray = gson.fromJson(json, com.google.gson.JsonArray.class);
                if (jsonArray.size() > 0) {
                    jsonObject = jsonArray.get(0).getAsJsonObject();
                }
            } else {
                jsonObject = gson.fromJson(json, JsonObject.class);
            }

            if (jsonObject != null && jsonObject.has("tag_name")) {
                boolean isPre = false;
                if (jsonObject.has("prerelease")) {
                    isPre = jsonObject.get("prerelease").getAsBoolean();
                }

                return new ReleaseInfo(
                        jsonObject.get("tag_name").getAsString(),
                        jsonObject.get("html_url").getAsString(),
                        jsonObject.get("name").getAsString(),
                        jsonObject.get("body").getAsString(),
                        isPre);
            } else {
                logger.warning("No se encontraron releases en la respuesta JSON o no tienen tag_name.");
            }
        } catch (Exception e) {
            logger.error("Error al parsear el JSON de la release", e);
        }
        return null;
    }

    private static boolean isNewerVersion(String tagName) {
        String currentVersion = Main.VERSION;
        // Eliminar el prefijo 'v' si está presente
        String vRemote = tagName.startsWith("v") ? tagName.substring(1) : tagName;
        String vLocal = currentVersion.startsWith("v") ? currentVersion.substring(1) : currentVersion;

        return compareVersions(vRemote, vLocal) > 0;
    }

    /**
     * Comparar dos strings de versión.
     * Devuelve > 0 si v1 > v2, < 0 si v1 < v2, y 0 si son iguales.
     */
    public static int compareVersions(String v1, String v2) {
        if (v1 == null && v2 == null)
            return 0;
        if (v1 == null)
            return -1;
        if (v2 == null)
            return 1;

        // Eliminar sufijos como -beta5 para comparación numérica y normalizar a minúsculas
        String[] v1Parts = v1.toLowerCase().split("-");
        String[] v2Parts = v2.toLowerCase().split("-");

        String[] v1Nums = v1Parts[0].split("\\.");
        String[] v2Nums = v2Parts[0].split("\\.");

        int length = Math.max(v1Nums.length, v2Nums.length);

        for (int i = 0; i < length; i++) {
            int num1 = 0;
            int num2 = 0;

            try {
                if (i < v1Nums.length && !v1Nums[i].isEmpty())
                    num1 = Integer.parseInt(v1Nums[i]);
            } catch (NumberFormatException e) {
                // Si no es un número válido (ej: "beta"), tratar como 0
            }

            try {
                if (i < v2Nums.length && !v2Nums[i].isEmpty())
                    num2 = Integer.parseInt(v2Nums[i]);
            } catch (NumberFormatException e) {
            }

            if (num1 > num2)
                return 1;
            if (num1 < num2)
                return -1;
        }

        // Si las partes principales son iguales, verificar pre-release
        // version con pre-release es MÁS ANTIGUA que la versión sin. e.g. 1.0.0-beta < 1.0.0
        boolean v1HasPre = v1Parts.length > 1;
        boolean v2HasPre = v2Parts.length > 1;

        if (v1HasPre && !v2HasPre)
            return -1;
        if (!v1HasPre && v2HasPre)
            return 1;

        // Si ambas tienen código pre-release, compararlas lexicográficamente
        if (v1HasPre) { // v2HasPre is implied true here
            String pre1 = v1Parts[1];
            String pre2 = v2Parts[1];

            return pre1.compareTo(pre2);
        }

        return 0;
    }

    public static ReleaseInfo getLatestRelease() {
        return latestRelease;
    }

    public static boolean isUpdateAvailable() {
        return latestRelease != null;
    }
}

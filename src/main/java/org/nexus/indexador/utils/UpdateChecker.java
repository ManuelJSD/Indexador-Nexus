package org.nexus.indexador.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class UpdateChecker {

    private static final String REPO_OWNER = "ManuelJSD";
    private static final String REPO_NAME = "Indexador-Nexus";
    private static final String GITHUB_API_URL = "https://api.github.com/repos/" + REPO_OWNER + "/" + REPO_NAME
            + "/releases/latest";

    /**
     * Checks for updates by comparing the current version with the latest tag on
     * GitHub.
     *
     * @param currentVersion The current application version (e.g., "0.9.0").
     * @return The latest version tag if an update is available, or null if up to
     *         date.
     */
    public static String checkForUpdates(String currentVersion) {
        try {
            URL url = java.net.URI.create(GITHUB_API_URL).toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/vnd.github.v3+json");
            connection.setRequestProperty("User-Agent", "Indexador-Nexus");

            if (connection.getResponseCode() == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                JsonObject json = JsonParser.parseString(response.toString()).getAsJsonObject();
                String tagName = json.get("tag_name").getAsString(); // e.g., "v1.0.0"

                String cleanCurrent = currentVersion.replace("v", "").trim();
                String cleanLatest = tagName.replace("v", "").trim();

                if (isNewer(cleanCurrent, cleanLatest)) {
                    return tagName;
                }
            }
        } catch (Exception e) {
            Logger.getInstance().error("Error checking for updates: " + e.getMessage(), e);
        }
        return null;
    }

    private static boolean isNewer(String current, String latest) {
        String[] currentParts = current.split("\\.");
        String[] latestParts = latest.split("\\.");
        int length = Math.max(currentParts.length, latestParts.length);

        for (int i = 0; i < length; i++) {
            int v1 = i < currentParts.length ? Integer.parseInt(currentParts[i]) : 0;
            int v2 = i < latestParts.length ? Integer.parseInt(latestParts[i]) : 0;
            if (v2 > v1)
                return true;
            if (v2 < v1)
                return false;
        }
        return false;
    }
}

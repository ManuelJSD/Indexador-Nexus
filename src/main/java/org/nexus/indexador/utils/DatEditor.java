package org.nexus.indexador.utils;

import org.nexus.indexador.gamedata.DataManager;

import java.io.*;

public class DatEditor {

    private static volatile DatEditor instance;

    private DatEditor() {
    }

    public static DatEditor getInstance() throws IOException {
        if (instance == null) {
            synchronized (DatEditor.class) {
                if (instance == null) {
                    instance = new DatEditor();
                }
            }
        }
        return instance;
    }

    // Método para leer el valor de una variable
    public static String GetVar(String filePath, String section, String variable) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        String line;
        boolean inSection = false;

        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.startsWith("[") && line.endsWith("]")) {
                inSection = line.equalsIgnoreCase("[" + section + "]");
            } else if (inSection && line.startsWith(variable + "=")) {
                reader.close();
                return line.substring(variable.length() + 1).trim();
            }
        }
        reader.close();
        return null; // Retorna null si no se encuentra la variable
    }

    // Método para escribir el valor de una variable
    public static void WriteVar(String filePath, String section, String variable, String value) throws IOException {
        File inputFile = new File(filePath);
        File tempFile = new File(inputFile.getAbsolutePath() + ".tmp");

        BufferedReader reader = new BufferedReader(new FileReader(inputFile));
        PrintWriter writer = new PrintWriter(new FileWriter(tempFile));

        String line;
        boolean inSection = false;
        boolean foundVar = false;

        while ((line = reader.readLine()) != null) {
            String trimmedLine = line.trim();

            if (trimmedLine.startsWith("[") && trimmedLine.endsWith("]")) {
                if (inSection && !foundVar) {
                    writer.println(variable + "=" + value);
                    foundVar = true;
                }
                inSection = trimmedLine.equalsIgnoreCase("[" + section + "]");
            }

            if (inSection && trimmedLine.startsWith(variable + "=")) {
                writer.println(variable + "=" + value);
                foundVar = true;
            } else {
                writer.println(line);
            }
        }

        if (inSection && !foundVar) {
            writer.println(variable + "=" + value);
        }

        reader.close();
        writer.close();

        if (!inputFile.delete()) {
            throw new IOException("Could not delete original file");
        }
        if (!tempFile.renameTo(inputFile)) {
            throw new IOException("Could not rename temporary file");
        }
    }

}

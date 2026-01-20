package org.nexus.indexador.utils;

import org.nexus.indexador.gamedata.models.GrhData;

import java.io.File;
import java.util.*;

/**
 * Servicio para validar la integridad de los datos de GRH.
 * Detecta problemas como frames huérfanos, FileNum inexistentes, duplicados,
 * etc.
 */
public class ValidationService {

    private static volatile ValidationService instance;
    private final Logger logger = Logger.getInstance();

    private ValidationService() {
    }

    public static ValidationService getInstance() {
        if (instance == null) {
            synchronized (ValidationService.class) {
                if (instance == null) {
                    instance = new ValidationService();
                }
            }
        }
        return instance;
    }

    /**
     * Resultado de una validación.
     */
    public static class ValidationResult {
        private final List<ValidationIssue> errors = new ArrayList<>();
        private final List<ValidationIssue> warnings = new ArrayList<>();
        private final List<ValidationIssue> infos = new ArrayList<>();

        public void addError(String message, int grhId) {
            errors.add(new ValidationIssue(Severity.ERROR, message, grhId));
        }

        public void addWarning(String message, int grhId) {
            warnings.add(new ValidationIssue(Severity.WARNING, message, grhId));
        }

        public void addInfo(String message, int grhId) {
            infos.add(new ValidationIssue(Severity.INFO, message, grhId));
        }

        public boolean hasErrors() {
            return !errors.isEmpty();
        }

        public boolean hasWarnings() {
            return !warnings.isEmpty();
        }

        public List<ValidationIssue> getErrors() {
            return errors;
        }

        public List<ValidationIssue> getWarnings() {
            return warnings;
        }

        public List<ValidationIssue> getInfos() {
            return infos;
        }

        public int getTotalIssues() {
            return errors.size() + warnings.size() + infos.size();
        }

        @Override
        public String toString() {
            return String.format("Validación: %d errores, %d advertencias, %d info",
                    errors.size(), warnings.size(), infos.size());
        }
    }

    public enum Severity {
        ERROR, WARNING, INFO
    }

    public static class ValidationIssue {
        private final Severity severity;
        private final String message;
        private final int grhId;

        public ValidationIssue(Severity severity, String message, int grhId) {
            this.severity = severity;
            this.message = message;
            this.grhId = grhId;
        }

        public Severity getSeverity() {
            return severity;
        }

        public String getMessage() {
            return message;
        }

        public int getGrhId() {
            return grhId;
        }

        @Override
        public String toString() {
            return String.format("[%s] GRH %d: %s", severity, grhId, message);
        }
    }

    /**
     * Valida la integridad de la lista de GRH.
     *
     * @param grhList     Lista de GrhData a validar.
     * @param graphicsDir Directorio de gráficos para verificar FileNum.
     * @return Resultado de la validación con errores y advertencias.
     */
    public ValidationResult validate(List<GrhData> grhList, String graphicsDir) {
        logger.info("Iniciando validación de integridad para " + grhList.size() + " GRHs");

        ValidationResult result = new ValidationResult();
        Map<Integer, GrhData> grhMap = new HashMap<>();
        Set<Integer> usedFileNums = new HashSet<>();

        // Construir mapa para búsquedas rápidas
        for (GrhData grh : grhList) {
            grhMap.put(grh.getGrh(), grh);
        }

        for (GrhData grh : grhList) {
            validateGrh(grh, grhMap, graphicsDir, usedFileNums, result);
        }

        // Estadísticas
        int animCount = 0;
        int staticCount = 0;
        for (GrhData grh : grhList) {
            if (grh.getNumFrames() > 1) {
                animCount++;
            } else {
                staticCount++;
            }
        }
        result.addInfo("Total GRHs estáticos: " + staticCount, 0);
        result.addInfo("Total GRHs animados: " + animCount, 0);
        result.addInfo("FileNums únicos utilizados: " + usedFileNums.size(), 0);

        logger.info("Validación completada: " + result);
        return result;
    }

    private void validateGrh(GrhData grh, Map<Integer, GrhData> grhMap,
            String graphicsDir, Set<Integer> usedFileNums,
            ValidationResult result) {

        if (grh.getNumFrames() > 1) {
            // Validar animación
            validateAnimation(grh, grhMap, result);
        } else {
            // Validar GRH estático
            validateStaticGrh(grh, graphicsDir, usedFileNums, result);
        }
    }

    private void validateAnimation(GrhData grh, Map<Integer, GrhData> grhMap,
            ValidationResult result) {
        int[] frames = grh.getFrames();

        if (frames == null || frames.length == 0) {
            result.addError("Animación sin frames definidos", grh.getGrh());
            return;
        }

        // Verificar que los frames referenciados existan
        for (int i = 1; i <= grh.getNumFrames() && i < frames.length; i++) {
            int frameId = frames[i];
            GrhData referencedGrh = grhMap.get(frameId);

            if (referencedGrh == null) {
                result.addError("Frame " + i + " referencia GRH inexistente: " + frameId, grh.getGrh());
            } else if (referencedGrh.getNumFrames() > 1) {
                result.addWarning("Frame " + i + " referencia otra animación: " + frameId, grh.getGrh());
            }
        }

        // Verificar velocidad
        if (grh.getSpeed() <= 0) {
            result.addWarning("Velocidad de animación inválida: " + grh.getSpeed(), grh.getGrh());
        }
    }

    private void validateStaticGrh(GrhData grh, String graphicsDir,
            Set<Integer> usedFileNums, ValidationResult result) {
        int fileNum = grh.getFileNum();

        if (fileNum <= 0) {
            result.addError("FileNum inválido: " + fileNum, grh.getGrh());
            return;
        }

        usedFileNums.add(fileNum);

        // Verificar que el archivo de imagen exista
        if (graphicsDir != null && !graphicsDir.isEmpty()) {
            File imageFile = findImageFile(graphicsDir, fileNum);
            if (imageFile == null) {
                result.addWarning("Imagen no encontrada para FileNum: " + fileNum, grh.getGrh());
            }
        }

        // Verificar dimensiones
        if (grh.getTileWidth() <= 0 || grh.getTileHeight() <= 0) {
            result.addError("Dimensiones inválidas: " + grh.getTileWidth() + "x" + grh.getTileHeight(), grh.getGrh());
        }

        // Verificar coordenadas negativas
        if (grh.getsX() < 0 || grh.getsY() < 0) {
            result.addWarning("Coordenadas negativas: sX=" + grh.getsX() + ", sY=" + grh.getsY(), grh.getGrh());
        }
    }

    /**
     * Busca un archivo de imagen con diferentes extensiones.
     */
    private File findImageFile(String graphicsDir, int fileNum) {
        String[] extensions = { ".png", ".bmp", ".jpg", ".jpeg", ".gif" };

        for (String ext : extensions) {
            File file = new File(graphicsDir, fileNum + ext);
            if (file.exists()) {
                return file;
            }
        }
        return null;
    }

    /**
     * Encuentra GRHs libres (no utilizados) consecutivos.
     *
     * @param grhList       Lista de GrhData existentes.
     * @param maxGrh        Máximo ID de GRH a considerar.
     * @param requiredCount Cantidad de GRHs libres consecutivos requeridos.
     * @return ID del primer GRH de la secuencia libre, o -1 si no se encuentra.
     */
    public int findFreeGrhs(List<GrhData> grhList, int maxGrh, int requiredCount) {
        Set<Integer> usedIds = new HashSet<>();
        for (GrhData grh : grhList) {
            usedIds.add(grh.getGrh());
        }

        int consecutiveCount = 0;
        int startId = -1;

        for (int i = 1; i <= maxGrh; i++) {
            if (!usedIds.contains(i)) {
                if (consecutiveCount == 0) {
                    startId = i;
                }
                consecutiveCount++;

                if (consecutiveCount >= requiredCount) {
                    return startId;
                }
            } else {
                consecutiveCount = 0;
                startId = -1;
            }
        }

        return -1;
    }
}

package org.nexus.indexador.gamedata;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.nexus.indexador.gamedata.loaders.IndexLoader;
import org.nexus.indexador.gamedata.models.*;
import org.nexus.indexador.utils.DatEditor;
import org.nexus.indexador.utils.byteMigration;
import org.nexus.indexador.utils.ConfigManager;
import org.nexus.indexador.utils.Logger;

import java.io.*;

public class DataManager {

    private GrhData grhData;

    private ObservableList<GrhData> grhList = FXCollections.observableArrayList();
    private ObservableList<HeadData> headList = FXCollections.observableArrayList();
    private ObservableList<HelmetData> helmetList = FXCollections.observableArrayList();
    private ObservableList<BodyData> bodyList = FXCollections.observableArrayList();
    private ObservableList<ShieldData> shieldList = FXCollections.observableArrayList();
    private ObservableList<FXData> fxList = FXCollections.observableArrayList();

    private int GrhCount;
    private int GrhVersion;
    private short NumHeads;
    private short NumHelmets;
    private short NumBodys;
    private short NumShields;
    private short NumFXs;
    private short NumObjs;

    private final ConfigManager configManager;
    private final byteMigration byteMigration;
    private final DatEditor datEditor;
    private final Logger logger;

    private IndexLoader indexLoader;

    private static volatile DataManager instance;

    private DataManager() throws IOException {

        // Obtenemos instancias:
        configManager = ConfigManager.getInstance();
        byteMigration = org.nexus.indexador.utils.byteMigration.getInstance();
        datEditor = DatEditor.getInstance();
        logger = Logger.getInstance();

        logger.info("DataManager inicializado");
        initializeIndexLoader();
    }

    /**
     * Inicializa el loader de índices según la configuración.
     */
    private void initializeIndexLoader() throws IOException {
        String systemConfig = configManager.getIndexingSystem();

        if ("TRADITIONAL".equals(systemConfig)) {
            indexLoader = new org.nexus.indexador.gamedata.loaders.TraditionalIndexLoader();
            logger.info("Sistema de indexado: Tradicional");
        } else {
            // Por defecto, usar sistema de moldes
            indexLoader = new org.nexus.indexador.gamedata.loaders.MoldIndexLoader();
            logger.info("Sistema de indexado: Moldes");
        }
    }

    public static DataManager getInstance() throws IOException {
        if (instance == null) {
            synchronized (DataManager.class) {
                if (instance == null) {
                    instance = new DataManager();
                }
            }
        }
        return instance;
    }

    /**
     * Obtiene la lista de gráficos (grh) cargados.
     *
     * @return Una lista observable de objetos GrhData que representan los gráficos
     *         cargados.
     */
    public ObservableList<GrhData> getGrhList() {
        return grhList;
    }

    public ObservableList<HeadData> getHeadList() {
        return headList;
    }

    public ObservableList<HelmetData> getHelmetList() {
        return helmetList;
    }

    public ObservableList<BodyData> getBodyList() {
        return bodyList;
    }

    public ObservableList<ShieldData> getShieldList() {
        return shieldList;
    }

    public ObservableList<FXData> getFXList() {
        return fxList;
    }

    public int getGrhCount() {
        return GrhCount;
    }

    public int getGrhVersion() {
        return GrhVersion;
    }

    public short getNumHeads() {
        return NumHeads;
    }

    public short getNumHelmets() {
        return NumHelmets;
    }

    public short getNumBodys() {
        return NumBodys;
    }

    public short getNumShields() {
        return NumShields;
    }

    public short getNumFXs() {
        return NumFXs;
    }

    public short getNumObjs() {
        return NumObjs;
    }

    public void setGrhCount(int GrhCount) {
        this.GrhCount = GrhCount;
    }

    public void setGrhVersion(int GrhVersion) {
        this.GrhVersion = GrhVersion;
    }

    public void setNumHelmets(short numHelmets) {
        NumHelmets = numHelmets;
    }

    public void setNumHeads(short numHeads) {
        NumHeads = numHeads;
    }

    public void setNumBodys(short numBodys) {
        NumBodys = numBodys;
    }

    public void setNumShields(short numShields) {
        NumShields = numShields;
    }

    public void setNumFXs(short numFXs) {
        NumFXs = numFXs;
    }

    public void setNumObjs(short numObjs) {
        NumObjs = numObjs;
    }

    /**
     * Lee los datos de un archivo binario que contiene información sobre gráficos
     * (grh) y los convierte en objetos grhData.
     * Cada gráfico puede ser una imagen estática o una animación.
     *
     * @return Una lista observable de objetos grhData que representan los gráficos
     *         leídos del archivo.
     * @throws IOException Si ocurre un error de entrada/salida al leer el archivo.
     */
    public ObservableList<GrhData> loadGrhData() throws IOException {

        logger.info("Ejecutando LoadGrhData.");

        grhList = FXCollections.observableArrayList();

        // Creamos un objeto File para el archivo que contiene los datos de los gráficos
        File archivo = new File(configManager.getInitDir() + "graficos.ind");

        try (RandomAccessFile file = new RandomAccessFile(archivo, "r")) {
            logger.info("Comenzando a leer desde " + archivo.getAbsolutePath());

            // Nos posicionamos al inicio del fichero
            // Verificar cabecera con heurística
            handleGrhHeader(file);

            // Leemos la versión del archivo
            GrhVersion = byteMigration.bigToLittle_Int(file.readInt());

            // Leemos la cantidad de Grh indexados
            GrhCount = byteMigration.bigToLittle_Int(file.readInt());

            // Mientras no llegue al final del archivo leemos...
            for (;;) {
                int grh = byteMigration.bigToLittle_Int(file.readInt());
                short numFrames = byteMigration.bigToLittle_Short(file.readShort());

                if (numFrames > 1) { // Es una animación
                    int[] frames = new int[numFrames + 1];
                    for (int i = 1; i <= numFrames; i++) {
                        frames[i] = byteMigration.bigToLittle_Int(file.readInt());
                    }

                    int speed = (int) byteMigration.bigToLittle_Float(file.readFloat());

                    // Creamos un objeto de grhData usando el constructor para animación
                    GrhData grhData = new GrhData(grh, numFrames, frames, speed);

                    grhList.add(grhData);

                } else { // Es una sola imagen
                    int fileNum = byteMigration.bigToLittle_Int(file.readInt());
                    short x = byteMigration.bigToLittle_Short(file.readShort());
                    short y = byteMigration.bigToLittle_Short(file.readShort());
                    short tileWidth = byteMigration.bigToLittle_Short(file.readShort());
                    short tileHeight = byteMigration.bigToLittle_Short(file.readShort());

                    // Creamos un objeto de grhData usando el constructor para imagenes estáticas
                    GrhData grhData = new GrhData(grh, numFrames, fileNum, x, y, tileWidth, tileHeight);

                    grhList.add(grhData);

                }

                // Si he recorrido todos los bytes, salgo del bucle
                if (file.getFilePointer() == file.length())
                    break;
            }
        } catch (FileNotFoundException e) {
            logger.error("Archivo no encontrado: " + archivo.getAbsolutePath(), e);
            throw e; // Relanzar la excepción para manejarla fuera del método

        } catch (EOFException e) {
            logger.info("Fin de fichero alcanzado");

        } catch (IOException e) {
            logger.error("Error de E/S al leer el archivo: " + archivo.getAbsolutePath(), e);
            throw e; // Relanzar la excepción para manejarla fuera del método

        }

        logger.info("Loaded " + grhList.size() + " gráficos exitosamente");
        return grhList;
    }

    /**
     * Lee los datos de cabeza desde un archivo y los devuelve como una lista
     * observable.
     *
     * @return una {@code ObservableList<headData>} que contiene los datos de cabeza
     *         leídos del archivo.
     * @throws IOException si ocurre un error de entrada/salida.
     */
    public ObservableList<HeadData> readHeadFile() throws IOException {
        headList = indexLoader.loadHeads();
        NumHeads = (short) headList.size();
        return headList;
    }

    /**
     * Lee los datos de los cascos desde un archivo y los carga en una lista
     * observable.
     *
     * @return una lista observable de objetos {@code helmetData} que contiene los
     *         datos de los cascos leídos del archivo.
     * @throws IOException si ocurre un error al leer el archivo.
     */
    public ObservableList<HelmetData> readHelmetFile() throws IOException {
        helmetList = indexLoader.loadHelmets();
        NumHelmets = (short) helmetList.size();
        return helmetList;
    }

    public ObservableList<BodyData> readBodyFile() throws IOException {
        logger.info("Cargando datos de cuerpos...");

        bodyList = FXCollections.observableArrayList();

        File archivo = new File(configManager.getInitDir() + "personajes.ind");

        try (RandomAccessFile file = new RandomAccessFile(archivo, "r")) {
            logger.info("Comenzando a leer desde " + archivo.getAbsolutePath());
            handleFixedHeader(file, 20);
            NumBodys = byteMigration.bigToLittle_Short(file.readShort());

            for (int i = 0; i < NumBodys; i++) {
                int[] body = new int[4];
                for (int j = 0; j < 4; j++) {
                    body[j] = byteMigration.bigToLittle_Int(file.readInt());
                }
                short headOffsetX = byteMigration.bigToLittle_Short(file.readShort());
                short headOffsetY = byteMigration.bigToLittle_Short(file.readShort());
                BodyData data = new BodyData(body, headOffsetX, headOffsetY);
                bodyList.add(data);
            }

        } catch (FileNotFoundException e) {
            logger.error("Archivo no encontrado: " + archivo.getAbsolutePath(), e);
            throw e;
        } catch (EOFException e) {
            logger.info("Fin de fichero alcanzado");
        } catch (IOException e) {
            logger.error("Error de E/S al leer el archivo: " + archivo.getAbsolutePath(), e);
            throw e;
        }

        logger.info("Cargados " + bodyList.size() + " cuerpos exitosamente");
        return bodyList;
    }

    public ObservableList<ShieldData> readShieldFile() throws IOException {
        try {
            return readShieldFileBinary();
        } catch (FileNotFoundException | EOFException e) {
            logger.warning("escudos.ind no encontrado o incompleto. Intentando con Escudos.dat...");
            try {
                return readShieldFileText();
            } catch (FileNotFoundException ex) {
                logger.error("No se encontró ni escudos.ind ni Escudos.dat");
                return shieldList; // Devolver lista vacía (inicializada) para evitar NPE
            } catch (Exception ex) {
                logger.error("Error al leer Escudos.dat", ex);
                return shieldList;
            }
        }
    }

    public ObservableList<ShieldData> readShieldFileBinary() throws IOException {
        logger.info("Cargando datos de escudos (Binario)...");

        shieldList.clear();

        File archivo = new File(configManager.getInitDir() + "escudos.ind");

        try (RandomAccessFile file = new RandomAccessFile(archivo, "r")) {
            logger.info("Comenzando a leer desde " + archivo.getAbsolutePath());
            handleFixedHeader(file, 16);
            NumShields = byteMigration.bigToLittle_Short(file.readShort());

            for (int i = 0; i < NumShields; i++) {
                int[] shield = new int[4];
                for (int j = 0; j < 4; j++) {
                    shield[j] = byteMigration.bigToLittle_Int(file.readInt());
                }
                ShieldData data = new ShieldData(shield);
                shieldList.add(data);
            }

        } catch (FileNotFoundException e) {
            throw e;
        } catch (EOFException e) {
            throw e;
        } catch (IOException e) {
            logger.error("Error de E/S al leer el archivo: " + archivo.getAbsolutePath(), e);
            throw e;
        }

        logger.info("Cargados " + shieldList.size() + " escudos exitosamente");
        return shieldList;
    }

    public ObservableList<ShieldData> readShieldFileText() throws IOException {
        logger.info("Cargando datos de escudos (Texto)...");
        shieldList.clear();
        File archivo = new File(configManager.getInitDir() + "Escudos.dat");

        if (!archivo.exists()) {
            throw new FileNotFoundException("Escudos.dat no encontrado");
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(archivo))) {
            String line;
            int[] currentShield = new int[4];
            boolean hasData = false;

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty())
                    continue;

                if (line.startsWith("[")) {
                    // Si ya teníamos datos de un escudo previo, lo guardamos
                    if (hasData) {
                        shieldList.add(new ShieldData(currentShield.clone()));
                        for (int k = 0; k < 4; k++)
                            currentShield[k] = 0;
                        hasData = false;
                    }
                    continue;
                }

                // Limpiar comentarios
                if (line.contains("'")) {
                    line = line.split("'")[0].trim();
                }

                String[] parts = line.split("=");
                if (parts.length < 2)
                    continue;

                String key = parts[0].trim().toUpperCase();
                String value = parts[1].trim();

                try {
                    int val = Integer.parseInt(value);
                    if (key.equals("NUMESCUDOS")) {
                        NumShields = (short) val;
                    } else if (key.startsWith("DIR")) {
                        int dirIndex = Integer.parseInt(key.substring(3)) - 1;
                        if (dirIndex >= 0 && dirIndex < 4) {
                            currentShield[dirIndex] = val;
                            hasData = true;
                        }
                    }
                } catch (NumberFormatException e) {
                    // Ignorar valores no numéricos
                }
            }

            // Guardar el último escudo si existe
            if (hasData) {
                shieldList.add(new ShieldData(currentShield.clone()));
            }
        }

        logger.info("Cargados " + shieldList.size() + " escudos (Texto) exitosamente");
        return shieldList;
    }

    public ObservableList<FXData> readFXsdFile() throws IOException {
        logger.info("Cargando datos de FXs...");

        fxList = FXCollections.observableArrayList();

        File archivo = new File(configManager.getInitDir() + "fxs.ind");

        try (RandomAccessFile file = new RandomAccessFile(archivo, "r")) {
            logger.info("Comenzando a leer desde " + archivo.getAbsolutePath());
            logger.info("Comenzando a leer desde " + archivo.getAbsolutePath());
            handleFixedHeader(file, 8);
            NumFXs = byteMigration.bigToLittle_Short(file.readShort());

            for (int i = 0; i < NumFXs; i++) {

                int fx = byteMigration.bigToLittle_Int(file.readInt());
                short offsetX = byteMigration.bigToLittle_Short(file.readShort());
                short offsetY = byteMigration.bigToLittle_Short(file.readShort());

                FXData data = new FXData(fx, offsetX, offsetY);
                fxList.add(data);

            }

        } catch (FileNotFoundException e) {
            logger.error("Archivo no encontrado: " + archivo.getAbsolutePath(), e);
            throw e;
        } catch (EOFException e) {
            logger.info("Fin de fichero alcanzado");
        } catch (IOException e) {
            logger.error("Error de E/S al leer el archivo: " + archivo.getAbsolutePath(), e);
            throw e;
        }

        logger.info("Cargados " + fxList.size() + " FXs exitosamente");
        return fxList;
    }

    /**
     * Intenta detectar y saltar el encabezado de 263 bytes en Graficos.ind usando
     * heurísticas de valores de versión.
     * Se basa en que el numero de version (primer entero) debe ser un valor bajo (<
     * 1000).
     * Si hay cabecera (texto), el primer entero interpretado será basura (valor muy
     * alto).
     */
    private void handleGrhHeader(RandomAccessFile file) throws IOException {
        long fileSize = file.length();
        if (fileSize < 263 + 8) { // Mínimo para cabecera + versión + cantidad
            file.seek(0);
            return;
        }

        // Leer posible version en 0
        file.seek(0);
        int vNoHeader = byteMigration.bigToLittle_Int(file.readInt());

        // Leer posible version en 263
        file.seek(263);
        int vHeader = byteMigration.bigToLittle_Int(file.readInt());

        // Validar rangos (Versiones de AO suelen ser 0, 1, 2... raramente > 1000)
        boolean validNoHeader = (vNoHeader >= 0 && vNoHeader < 1000);
        boolean validHeader = (vHeader >= 0 && vHeader < 1000);

        if (!validNoHeader && validHeader) {
            file.seek(263); // Detectado Header
            logger.info("Cabecera detectada en Graficos.ind por heurística.");
        } else {
            file.seek(0); // Default
            if (validNoHeader && validHeader) {
                logger.warning("Ambigüedad en detección de cabecera Graficos.ind. Asumiendo SIN cabecera.");
            }
        }
    }

    /**
     * Verifica si el archivo tiene una cabecera de 263 bytes y posiciona el puntero
     * después de ella si es necesario.
     * Utiliza el tamaño del archivo para validar la presencia de la cabecera en
     * archivos de registros fijos.
     *
     * @param file       El archivo abierto.
     * @param recordSize El tamaño en bytes de cada registro individual.
     * @throws IOException Si ocurre un error de lectura.
     */
    private void handleFixedHeader(RandomAccessFile file, int recordSize) throws IOException {
        long fileSize = file.length();

        // 1. Probar teoría: SIN CABECERA
        file.seek(0);
        short numRecordsNoHeader = byteMigration.bigToLittle_Short(file.readShort());
        long expectedSizeNoHeader = 2 + (long) numRecordsNoHeader * recordSize;

        if (fileSize == expectedSizeNoHeader) {
            file.seek(0); // Confirmado sin cabecera
            return;
        }

        // 2. Probar teoría: CON CABECERA
        if (fileSize >= 263 + 2) {
            file.seek(263);
            short numRecordsWithHeader = byteMigration.bigToLittle_Short(file.readShort());
            long expectedSizeWithHeader = 263 + 2 + (long) numRecordsWithHeader * recordSize;

            if (fileSize == expectedSizeWithHeader) {
                file.seek(263); // Confirmado con cabecera
                logger.info("Cabecera .ind (Fixed) detectada y saltada.");
                return;
            }
        }

        // 3. Fallback
        file.seek(0);
        logger.warning("No se pudo determinar cabecera fija por tamaño (Size: " + fileSize + " bytes, Record: "
                + recordSize + "). Asumiendo sin cabecera.");
    }
}

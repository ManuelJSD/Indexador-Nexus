package org.nexus.indexador.gamedata;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.nexus.indexador.gamedata.models.*;
import org.nexus.indexador.utils.DatEditor;
import org.nexus.indexador.utils.byteMigration;
import org.nexus.indexador.utils.ConfigManager;
import org.nexus.indexador.utils.Logger;

import java.io.*;

public class DataManager {

    private GrhData grhData;

    private ObservableList<GrhData> grhList;
    private ObservableList<HeadData> headList;
    private ObservableList<HelmetData> helmetList;
    private ObservableList<BodyData> bodyList;
    private ObservableList<ShieldData> shieldList;
    private ObservableList<FXData> fxList;

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

    private static volatile DataManager instance;

    private DataManager() throws IOException {

        // Obtenemos instancias:
        configManager = ConfigManager.getInstance();
        byteMigration = org.nexus.indexador.utils.byteMigration.getInstance();
        datEditor = DatEditor.getInstance();
        logger = Logger.getInstance();

        logger.info("DataManager inicializado");
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
        File archivo = new File(configManager.getInitDir() + "graphics.ind");

        try (RandomAccessFile file = new RandomAccessFile(archivo, "r")) {
            logger.info("Comenzando a leer desde " + archivo.getAbsolutePath());

            // Nos posicionamos al inicio del fichero
            file.seek(0);

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

        logger.info("Cargando datos de cabezas...");

        // Creamos una lista observable para almacenar los gráficos leídos del archivo
        headList = FXCollections.observableArrayList();

        // Creamos un objeto File para el archivo que contiene los datos de los gráficos
        File archivo = new File(configManager.getInitDir() + "heads.ind");

        try (RandomAccessFile file = new RandomAccessFile(archivo, "r")) {
            logger.info("Comenzando a leer desde " + archivo.getAbsolutePath());

            // Nos posicionamos al inicio del fichero
            file.seek(0);

            NumHeads = byteMigration.bigToLittle_Short(file.readShort());

            for (int i = 0; i < NumHeads; i++) {

                int std;
                short texture;
                short startx;
                short starty;

                std = byteMigration.bigToLittle_Byte(file.readByte());
                texture = byteMigration.bigToLittle_Short(file.readShort());
                startx = byteMigration.bigToLittle_Short(file.readShort());
                starty = byteMigration.bigToLittle_Short(file.readShort());

                // Creamos un objeto de headData
                HeadData headData = new HeadData(std, texture, startx, starty);
                headList.add(headData);
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

        logger.info("Cargadas " + headList.size() + " cabezas exitosamente");
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

        logger.info("Cargando datos de cascos...");

        // Creamos una lista observable para almacenar los gráficos leídos del archivo
        helmetList = FXCollections.observableArrayList();

        // Creamos un objeto File para el archivo que contiene los datos de los gráficos
        File archivo = new File(configManager.getInitDir() + "helmets.ind");

        try (RandomAccessFile file = new RandomAccessFile(archivo, "r")) {
            logger.info("Comenzando a leer desde " + archivo.getAbsolutePath());

            // Nos posicionamos al inicio del fichero
            file.seek(0);

            NumHelmets = byteMigration.bigToLittle_Short(file.readShort());

            for (int i = 0; i < NumHelmets; i++) {

                int std;
                short texture;
                short startx;
                short starty;

                std = byteMigration.bigToLittle_Byte(file.readByte());
                texture = byteMigration.bigToLittle_Short(file.readShort());
                startx = byteMigration.bigToLittle_Short(file.readShort());
                starty = byteMigration.bigToLittle_Short(file.readShort());

                // Creamos un objeto de helmetData
                HelmetData helmetData = new HelmetData(std, texture, startx, starty);
                helmetList.add(helmetData);
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

        logger.info("Cargados " + helmetList.size() + " cascos exitosamente");
        return helmetList;
    }

    public ObservableList<BodyData> readBodyFile() throws IOException {
        logger.info("Cargando datos de cuerpos...");

        bodyList = FXCollections.observableArrayList();

        File archivo = new File(configManager.getInitDir() + "bodys.ind");

        try (RandomAccessFile file = new RandomAccessFile(archivo, "r")) {
            logger.info("Comenzando a leer desde " + archivo.getAbsolutePath());
            file.seek(0);
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
        logger.info("Cargando datos de escudos...");

        shieldList = FXCollections.observableArrayList();

        File archivo = new File(configManager.getInitDir() + "shields.ind");

        try (RandomAccessFile file = new RandomAccessFile(archivo, "r")) {
            logger.info("Comenzando a leer desde " + archivo.getAbsolutePath());
            file.seek(0);
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
            logger.error("Archivo no encontrado: " + archivo.getAbsolutePath(), e);
            throw e;
        } catch (EOFException e) {
            logger.info("Fin de fichero alcanzado");
        } catch (IOException e) {
            logger.error("Error de E/S al leer el archivo: " + archivo.getAbsolutePath(), e);
            throw e;
        }

        logger.info("Cargados " + shieldList.size() + " escudos exitosamente");
        return shieldList;
    }

    public ObservableList<FXData> readFXsdFile() throws IOException {
        logger.info("Cargando datos de FXs...");

        fxList = FXCollections.observableArrayList();

        File archivo = new File(configManager.getInitDir() + "fxs.ind");

        try (RandomAccessFile file = new RandomAccessFile(archivo, "r")) {
            logger.info("Comenzando a leer desde " + archivo.getAbsolutePath());
            file.seek(0);
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

}

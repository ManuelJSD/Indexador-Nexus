package org.nexus.indexador;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.nexus.indexador.controllers.LoadingController;
import org.nexus.indexador.utils.Logger;
import org.nexus.indexador.utils.ProfileManager;

import java.io.IOException;
import javafx.scene.image.Image;
import java.io.InputStream;

public class Main extends Application {

  public static final String VERSION = "0.9.2";
  private static final Logger logger = Logger.getInstance();

  @Override
  public void start(Stage stage) {
    // Activar redirección de logs a la consola de depuración
    org.nexus.indexador.controllers.ConsoleController.activateGlobalRedirection();

    logger.info("Iniciando aplicación Indexador Nexus v" + VERSION);

    // Configurar icono
    setAppIcon(stage);

    // Cargar la lista de perfiles
    ProfileManager profileManager = ProfileManager.getInstance();
    profileManager.cargar();

    if (profileManager.isEmpty()) {
      // Sin perfiles → wizard inicial que creará el primer perfil
      String configPath = profileManager.generarConfigPath("default");
      showInitialSetup(configPath, "Mi Perfil", () -> showLoadingScreen(stage));
    } else {
      // Con perfiles → mostrar selector
      showProfileSelector(stage);
    }

    // Chequear actualizaciones en segundo plano
    org.nexus.indexador.utils.GithubReleaseChecker.checkForUpdates(release -> {
      javafx.application.Platform.runLater(new Runnable() {
        @Override
        public void run() {
          Stage mainStage = org.nexus.indexador.utils.WindowManager.getInstance().getWindow("MainController");
          if (mainStage != null && mainStage.isShowing()) {
            org.nexus.indexador.utils.ToastNotification.show(mainStage, "Actualización " + release.tagName + " disponible en GitHub");
            if (!mainStage.getTitle().contains("Actualización")) {
              mainStage.setTitle(mainStage.getTitle() + " (Hay actualización " + release.tagName + ")");
            }
            logger.info("Notificación visual de actualización enviada de forma no intrusiva.");
          } else {
            // Reintentar en 2 segundos si la UI de MainController aún no está lista
            javafx.animation.PauseTransition delay = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(2));
            delay.setOnFinished(e -> javafx.application.Platform.runLater(this));
            delay.play();
          }
        }
      });
    });
  }

  /**
   * Muestra la pantalla del selector de perfiles.
   *
   * @param stage Stage principal de la aplicación.
   */
  private void showProfileSelector(Stage stage) {
    try {
      FXMLLoader fxmlLoader = new FXMLLoader(
          Main.class.getResource("/org/nexus/indexador/ProfileSelectorController.fxml"));
      Parent root = fxmlLoader.load();
      Scene scene = new Scene(root);

      // Aplicar tema DARK por defecto antes de cargar un perfil
      String darkTheme = Main.class.getResource("styles/dark-theme.css").toExternalForm();
      scene.getStylesheets().add(darkTheme);

      org.nexus.indexador.controllers.ProfileSelectorController controller =
          fxmlLoader.getController();

      Stage selectorStage = new Stage();
      controller.setStage(selectorStage);

      // Al seleccionar un perfil → pantalla de carga
      controller.setOnPerfilSeleccionado(perfil -> showLoadingScreen(stage));

      // Al crear un nuevo perfil → diálogo de nombre → wizard → carga directa
      controller.setOnNuevoPerfil(() -> {
        ProfileManager pm = ProfileManager.getInstance();
        String nuevoNombre = pedirNombrePerfil();
        if (nuevoNombre == null || nuevoNombre.trim().isEmpty()) {
          // Cancelado → volver a mostrar el selector
          showProfileSelector(stage);
          return;
        }
        String configPath = pm.generarConfigPath(nuevoNombre.trim());
        showInitialSetup(configPath, nuevoNombre.trim(), () -> showLoadingScreen(stage));
      });

      // Al editar un perfil existente → wizard en modo edición
      controller.setOnEditarPerfil((indice, perfil) -> {
        showEditSetup(perfil, indice, () -> showLoadingScreen(stage));
      });

      selectorStage.setTitle("Indexador Nexus - Selección de Perfil");
      setAppIcon(selectorStage);
      selectorStage.setScene(scene);
      selectorStage.setResizable(false);
      selectorStage.show();

      logger.info("Selector de perfiles mostrado (" +
          ProfileManager.getInstance().getPerfiles().size() + " perfiles)");
    } catch (IOException e) {
      logger.error("Error al cargar el selector de perfiles", e);
    }
  }

  /**
   * Solicita al usuario el nombre del nuevo perfil mediante un diálogo de texto.
   *
   * @return El nombre introducido, o {@code null} si canceló o dejó vacío.
   */
  public static String pedirNombrePerfil() {
    javafx.scene.control.TextInputDialog dialog =
        new javafx.scene.control.TextInputDialog("Nuevo Perfil");
    dialog.setTitle("Nuevo Perfil");
    dialog.setHeaderText("Crear un nuevo perfil de configuración");
    dialog.setContentText("Nombre del perfil:");
    setAppIcon((Stage) dialog.getDialogPane().getScene().getWindow());
    return dialog.showAndWait().orElse(null);
  }

  /**
   * Muestra el wizard de configuración inicial.
   *
   * @param targetConfigPath Ruta donde se guardará el .ini del nuevo perfil.
   * @param profileName      Nombre propuesto para el perfil.
   * @param onComplete       Callback a ejecutar al finalizar.
   */
  public static void showInitialSetup(String targetConfigPath, String profileName, Runnable onComplete) {
    try {
      FXMLLoader fxmlLoader = new FXMLLoader(
          Main.class.getResource("/org/nexus/indexador/InitialSetupController.fxml"));
      Parent root = fxmlLoader.load();
      Scene scene = new Scene(root);

      // Aplicar tema oscuro
      String darkTheme = Main.class.getResource("styles/dark-theme.css").toExternalForm();
      scene.getStylesheets().add(darkTheme);

      // Configurar controller
      org.nexus.indexador.controllers.InitialSetupController controller =
          fxmlLoader.getController();

      Stage setupStage = new Stage();
      controller.setStage(setupStage);
      controller.setTargetConfigPath(targetConfigPath);
      controller.setProfileName(profileName);
      controller.setOnComplete(onComplete);

      setupStage.setTitle("Indexador Nexus - Configuración Inicial");
      setAppIcon(setupStage);
      setupStage.setScene(scene);
      setupStage.setResizable(false);
      setupStage.show();

      logger.info("Wizard de configuración inicial mostrado (perfil: " + profileName + ")");
    } catch (IOException e) {
      logger.error("Error al cargar wizard de configuración", e);
    }
  }

  /**
   * Muestra el wizard de configuración en modo edición.
   *
   * @param perfil     Perfil a editar.
   * @param index      Índice del perfil en la lista.
   * @param onComplete Callback a ejecutar al finalizar.
   */
  public static void showEditSetup(org.nexus.indexador.utils.ProfileEntry perfil, int index,
      Runnable onComplete) {
    try {
      FXMLLoader fxmlLoader = new FXMLLoader(
          Main.class.getResource("/org/nexus/indexador/InitialSetupController.fxml"));
      Parent root = fxmlLoader.load();
      Scene scene = new Scene(root);

      // Aplicar tema oscuro (el wizard es dark por diseño)
      String darkTheme = Main.class.getResource("styles/dark-theme.css").toExternalForm();
      scene.getStylesheets().add(darkTheme);

      // Configurar controller
      org.nexus.indexador.controllers.InitialSetupController controller =
          fxmlLoader.getController();

      Stage setupStage = new Stage();
      controller.setStage(setupStage);
      controller.setEditMode(index, perfil);
      controller.setOnComplete(onComplete);

      setupStage.setTitle("Indexador Nexus - Editar Perfil");
      setAppIcon(setupStage);
      setupStage.setScene(scene);
      setupStage.setResizable(false);
      setupStage.show();

      logger.info("Wizard de edición mostrado (perfil: " + perfil.getNombre() + ")");
    } catch (IOException e) {
      logger.error("Error al cargar wizard de edición", e);
    }
  }

  /**
   * Muestra la pantalla de carga principal.
   *
   * @param stage Stage principal de la aplicación.
   */
  private void showLoadingScreen(Stage stage) {
    FXMLLoader fxmlLoader =
        new FXMLLoader(Main.class.getResource("/org/nexus/indexador/LoadingController.fxml"));

    try {
      Parent root = fxmlLoader.load();
      Scene scene = new Scene(root);

      // Aplicar tema según la config del perfil activo
      String theme = org.nexus.indexador.utils.ConfigManager.getInstance().getAppTheme();
      org.nexus.indexador.utils.WindowManager.getInstance().applyTheme(scene, theme);

      // Obtener el controlador y pasar el Stage
      LoadingController controller = fxmlLoader.getController();
      controller.setStage(stage);
      controller.init();

      stage.initStyle(StageStyle.UNDECORATED);
      stage.setTitle("Indexador Nexus: Iniciando");
      setAppIcon(stage);
      stage.setResizable(false);
      stage.setScene(scene);
      stage.centerOnScreen();
      stage.show();

      logger.info("Pantalla de carga iniciada correctamente");
    } catch (IOException e) {
      logger.error("Error al cargar la interfaz de usuario", e);
    }
  }

  public static void main(String[] args) {
    Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
      Logger logger = Logger.getInstance();
      logger.error("Excepción no capturada en el hilo: " + thread.getName(), throwable);
    });

    launch();
  }

  /**
   * Establece el icono de la aplicación para un Stage dado.
   *
   * @param stage El escenario al que aplicar el icono.
   */
  public static void setAppIcon(Stage stage) {
    try {
      InputStream iconStream = Main.class.getResourceAsStream("/img/icon.png");
      if (iconStream != null) {
        stage.getIcons().add(new Image(iconStream));
      }
    } catch (Exception e) {
      System.err.println("No se pudo cargar el icono de la aplicación: " + e.getMessage());
    }
  }

}

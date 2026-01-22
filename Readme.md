# Indexador Nexus

![Java](https://img.shields.io/badge/Java-17-orange) ![JavaFX](https://img.shields.io/badge/JavaFX-17-blue) ![Maven](https://img.shields.io/badge/Maven-3.8.6-red)

*[English Version](README.en.md)*

## Descripción

Indexador para Argentum Online totalmente **Multiplataforma** (Windows, Linux, macOS) desarrollado en Java. Esta herramienta permite visualizar, editar y gestionar los recursos gráficos del juego, siendo **compatible con todas las versiones** de Argentum Online gracias a su sistema de detección dinámica de formatos.

## Características

- Visualización de gráficos (GRHs)
- Edición de propiedades de gráficos
- Gestión de animaciones
- Sistema de caché para optimizar el rendimiento
- Visualización de escudos, cascos y otras características del juego
- **Auto-Indexado Inteligente**: Detección automática de objetos y animaciones en hojas de sprites.
    - Soporte para **Auto-Tiling** inteligente.
    - Detección precisa de movimientos y direcciones.
- **Configuración de Color de Fondo**: Selector de color integrado para personalizar el fondo del visor.
- **Gestión de Rutas**: Configuración flexible de rutas para recursos (Gráficos, Init, Dat).
- **Recarga Granular de Recursos**: Opción para recargar recursos específicos (Índices, Cabezas, etc.) o todos a la vez sin reiniciar.
- **Exportación Estandarizada**: Generación de archivos `.ini` con formato optimizado y legible.
- Sistema de registro (logging) centralizado
- Interfaz gráfica intuitiva con JavaFX

## Capturas de Pantalla

<img width="1364" height="798" alt="image" src="https://github.com/user-attachments/assets/06e583c1-6f24-4dad-8988-943fbd5ffbbe" />

## Requisitos del Sistema

- Java Development Kit (JDK) 17 o superior
- Maven 3.6.0 o superior
- Recursos gráficos de Argentum Online versión 0.13 o AOLibre

## Instalación

### Opciones de Instalación

#### 1. Clonar Repositorio (Desarrollo)

1. Clonar el repositorio:
   ```bash
   git clone https://github.com/ManuelJSD/Indexador-Nexus.git
   ```

2. Navegar al directorio del proyecto:
   ```bash
   cd Indexador-Nexus
   ```

3. Compilar el proyecto usando Maven:
   ```bash
   mvn clean package
   ```

#### 2. Descarga Directa (Usuarios)

1. Descargar la última versión desde la [sección de Releases](https://github.com/Lorwik/Indexador-Nexus/releases)
2. Descomprimir el archivo descargado en la ubicación deseada

## Ejecución de la Aplicación

### Desde Línea de Comandos

1. Navegar a la carpeta del proyecto
2. Ejecutar el siguiente comando:
   ```bash
   mvn clean javafx:run
   ```

### Usando el Archivo JAR

1. Navegar a la carpeta donde se encuentra el archivo JAR compilado (usualmente en `/target`)
2. Ejecutar el siguiente comando:
   ```bash
   java -jar indexador-1.0-SNAPSHOT.jar
   ```

### Configuración Inicial

Al iniciar la aplicación por primera vez:

1. Seleccione la ruta donde se encuentran los recursos de Argentum Online.
2. La aplicación cargará automáticamente los gráficos disponibles.
3. Use la interfaz para navegar entre los diferentes recursos.
4. Puede cambiar el color de fondo del visualizador usando el selector de color ubicado sobre el panel de vista previa.

## Estado del Proyecto

Este proyecto está en desarrollo activo. Algunas características planeadas incluyen:

- Importación desde archivos de texto plano
- Optimización de rendimiento para grandes cantidades de gráficos
- Soporte para nuevos formatos de recursos

## Arquitectura

El proyecto está estructurado siguiendo el patrón Modelo-Vista-Controlador (MVC):

- **Modelo**: Clases de datos en `org.nexus.indexador.gamedata.models`
- **Vista**: Interfaces FXML en `resources/org/nexus/indexador`
- **Controlador**: Lógica de controladores en `org.nexus.indexador.controllers`

Además, contiene utilidades para mejorar el rendimiento:
- Sistema de logging centralizado
- Sistema de caché de imágenes con referencias suaves para gestión de memoria

## Contribuir

Si deseas contribuir al proyecto, sigue estos pasos:

1. Haz un Fork del proyecto
2. Crea una nueva rama (`git checkout -b feature/nueva-funcionalidad`)
3. Realiza los cambios necesarios y haz commit (`git commit -am 'Añadir nueva funcionalidad'`)
4. Haz Push a la rama (`git push origin feature/nueva-funcionalidad`)
5. Envía un Pull Request

## Reportar Problemas

Si encuentras algún problema o tienes sugerencias, por favor [crea un issue](https://github.com/Lorwik/Indexador-Nexus/issues/new) con los siguientes detalles:

- Descripción del problema
- Pasos para reproducirlo
- Comportamiento esperado
- Capturas de pantalla (si aplica)
- Versión de Java y sistema operativo

## Licencia

Este proyecto está licenciado bajo la licencia GPL-3.0.

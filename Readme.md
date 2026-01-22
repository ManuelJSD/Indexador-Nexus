# Indexador Nexus

![Java](https://img.shields.io/badge/Java-21-orange) ![JavaFX](https://img.shields.io/badge/JavaFX-21-blue) ![Maven](https://img.shields.io/badge/Maven-3.9.6-red)

*[English Version](README.en.md)*

## Descripción

Indexador para Argentum Online totalmente **Multiplataforma** (Windows, Linux, macOS) desarrollado en Java. Esta herramienta permite visualizar, editar y gestionar los recursos gráficos del juego, siendo **compatible con todas las versiones** de Argentum Online gracias a su sistema de detección dinámica de formatos.

## Características

- **Visualización y Edición**: Gestión completa de gráficos (GRHs), animaciones, escudos, cascos y cuerpos.
- **Auto-Indexado Inteligente**: Detección automática de objetos y animaciones en hojas de sprites con soporte para Auto-Tiling.
- **Interfaz Moderna**:
    - **Soporte de Temas**: Cambia entre modo **Claro** y **Oscuro** dinámicamente.
    - **Diseño Responsivo**: Paneles ajustables y layouts flexibles.
    - **Feedback Visual**: Notificaciones "toast" no intrusivas y barras de carga reales.
- **Rendimiento Mejorado**: 
    - Optimizado para Java 21 (ZGC).
    - Cache de imágenes inteligente.
    - Recarga granular de recursos (sin reiniciar).
- **Herramientas de Desarrollo**:
    - Generación de instaladores nativos (.exe, .msi, .deb).
    - Configuración flexible de rutas.
    - Exportación de archivos `.ini` estandarizados.

## Capturas de Pantalla

<img width="1366" height="830" alt="Captura de pantalla 2026-01-22 202854" src="https://github.com/user-attachments/assets/8012a20e-8d20-4ff8-b4cc-3e0f8b518cbf" />

## Requisitos del Sistema

- **Java Development Kit (JDK) 21 LTS** o superior.
- Maven 3.8.0 o superior.
- Recursos gráficos de Argentum Online (versión 0.13, AOLibre o compatibles).

## Instalación

### Opción 1: Descarga Directa (Recomendado para Usuarios)

1. Descarga el instalador (`.exe` o `.msi`) de la última versión desde la [sección de Releases](https://github.com/Lorwik/Indexador-Nexus/releases).
2. Ejecuta el instalador y sigue las instrucciones.

### Opción 2: Compilación desde Fuente (Desarrollo)

1. Clonar el repositorio:
   ```bash
   git clone https://github.com/ManuelJSD/Indexador-Nexus.git
   cd Indexador-Nexus
   ```

2. Compilar el proyecto (esto descargara las dependencias y validará el código):
   ```bash
   mvn clean compile
   ```

3. Ejecutar la aplicación:
   ```bash
   mvn clean javafx:run
   ```

4. (Opcional) Crear un instalador nativo:
   ```bash
   mvn package
   ```
   *El instalador se generará en la carpeta `target/dist`.*

## Configuración Inicial

Al iniciar la aplicación por primera vez:
1. Seleccione la ruta donde se encuentran los recursos de Argentum Online (carpeta con `Graficos`, `Init`, etc.).
2. La aplicación escaneará y cargará los índices automáticamente.
3. Puedes cambiar entre Tema Claro y Oscuro desde el menú **Opciones > Temas**.

## Contribuir

¡Las contribuciones son bienvenidas!
1. Haz un Fork.
2. Crea una rama (`git checkout -b feature/nueva-cosa`).
3. Asegúrate de seguir el estilo de código (usa `mvn spotless:apply` para formatear).
4. Haz Commit y Push.
5. Abre un Pull Request.

## Licencia

Este proyecto está licenciado bajo la licencia GPL-3.0.

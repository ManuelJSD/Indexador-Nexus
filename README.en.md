# Nexus Indexer

![Java](https://img.shields.io/badge/Java-21-orange) ![JavaFX](https://img.shields.io/badge/JavaFX-21-blue) ![Maven](https://img.shields.io/badge/Maven-3.9.6-red)

*[Spanish Version](Readme.md)*

## Description

Fully **Cross-Platform** (Windows, Linux, macOS) Indexer for Argentum Online developed in Java. This tool allows you to view, edit, and manage game graphic resources, being **compatible with all versions** of Argentum Online thanks to its dynamic format detection system.

## Features

- **Visualization & Editing**: Complete management of graphics (GRHs), animations, shields, helmets, and bodies.
- **Smart Auto-Indexing**: Automatic detection of objects and animations in sprite sheets with Auto-Tiling support.
- **Modern Interface**:
    - **Theme Support**: Switch between **Light** and **Dark** modes dynamically.
    - **Responsive Design**: Adjustable panels and flexible layouts.
    - **Visual Feedback**: Non-intrusive "toast" notifications and real loading bars.
- **Enhanced Performance**:
    - Optimized for Java 21 (ZGC).
    - Smart image caching.
    - Granular resource reloading (hot-reload).
- **Developer Tools**:
    - Native installer generation (.exe, .msi, .deb).
    - Flexible path configuration.
    - Standardized `.ini` export.

## Screenshots

<img width="1366" height="830" alt="Captura de pantalla 2026-01-22 202854" src="https://github.com/user-attachments/assets/e327e577-4f03-4d1e-92cd-7941bf17e530" />

## System Requirements

- **Java Development Kit (JDK) 21 LTS** or higher.
- Maven 3.8.0 or higher.
- Argentum Online graphic resources (version 0.13, AOLibre, or compatible).

## Installation

### Option 1: Direct Download (Recommended for Users)

1. Download the installer (`.exe` or `.msi`) for the latest version from the [Releases section](https://github.com/Lorwik/Indexador-Nexus/releases).
2. Run the installer and follow the instructions.

### Option 2: Build from Source (Development)

1. Clone the repository:
   ```bash
   git clone https://github.com/ManuelJSD/Indexador-Nexus.git
   cd Indexador-Nexus
   ```

2. Compile the project (this will download dependencies and validate code):
   ```bash
   mvn clean compile
   ```

3. Run the application:
   ```bash
   mvn clean javafx:run
   ```

4. (Optional) Create a native installer:
   ```bash
   mvn package
   ```
   *The installer will be generated in the `target/dist` folder.*

## Initial Configuration

When starting the application for the first time:
1. Select the path where the Argentum Online resources are located (`Graficos`, `Init` folders, etc.).
2. The application will scan and load available indexes automatically.
3. You can switch between Light and Dark themes from the **Options > Themes** menu.

## Contributing

Contributions are welcome!
1. Fork the project.
2. Create a branch (`git checkout -b feature/new-thing`).
3. Ensure you follow code style (run `mvn spotless:apply` to format).
4. Commit and Push.
5. Open a Pull Request.

## License

This project is licensed under the GPL-3.0 license.

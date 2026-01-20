# Nexus Indexer

![Java](https://img.shields.io/badge/Java-17-orange) ![JavaFX](https://img.shields.io/badge/JavaFX-17-blue) ![Maven](https://img.shields.io/badge/Maven-3.8.6-red)

*[Spanish Version](Readme.md)*

## Description

Indexer for Argentum Online programmed in Java. This tool allows you to view, edit, and manage the graphic resources used in the Argentum Online game. Currently works with resource types from version 0.13 or AOLibre.

## Features

- Graphics (GRHs) visualization
- Graphic properties editing
- Animation management
- Cache system to optimize performance
- Visualization of shields, helmets, and other game features
- **Background Color Configuration**: Integrated color picker to customize the viewer background.
- **Path Management**: Flexible configuration for resource paths (Graphics, Init, Dat).
- Centralized logging system
- Intuitive graphical interface with JavaFX

## Screenshots

<img width="1364" height="798" alt="image" src="https://github.com/user-attachments/assets/06e583c1-6f24-4dad-8988-943fbd5ffbbe" />

## System Requirements

- Java Development Kit (JDK) 17 or higher
- Maven 3.6.0 or higher
- Argentum Online graphic resources version 0.13 or AOLibre

## Installation

### Installation Options

#### 1. Clone Repository (Development)

1. Clone the repository:
   ```bash
   git clone https://github.com/ManuelJSD/Indexador-Nexus.git
   ```

2. Navigate to the project directory:
   ```bash
   cd Indexador-Nexus
   ```

3. Compile the project using Maven:
   ```bash
   mvn clean package
   ```

#### 2. Direct Download (Users)

1. Download the latest version from the [Releases section](https://github.com/Lorwik/Indexador-Nexus/releases)
2. Unzip the downloaded file to your desired location

## Running the Application

### From Command Line

1. Navigate to the project folder
2. Run the following command:
   ```bash
   mvn clean javafx:run
   ```

### Using the JAR File

1. Navigate to the folder where the compiled JAR file is located (usually in `/target`)
2. Run the following command:
   ```bash
   java -jar indexador-1.0-SNAPSHOT.jar
   ```

### Initial Configuration

When starting the application for the first time:

1. Select the path where the Argentum Online resources are located.
2. The application will automatically load the available graphics.
3. Use the interface to navigate between the different resources.
4. You can change the viewer background color using the color picker located above the preview panel.

## Project Status

This project is in active development. Some planned features include:

- Import from plain text files
- Performance optimization for large amounts of graphics
- Support for new resource formats

## Architecture

The project is structured following the Model-View-Controller (MVC) pattern:

- **Model**: Data classes in `org.nexus.indexador.gamedata.models`
- **View**: FXML interfaces in `resources/fxml`
- **Controller**: Controller logic in `org.nexus.indexador.controllers`

Additionally, it contains utilities to improve performance:
- Centralized logging system
- Image cache system with soft references for memory management

## Contributing

If you wish to contribute to the project, follow these steps:

1. Fork the project
2. Create a new branch (`git checkout -b feature/new-feature`)
3. Make the necessary changes and commit (`git commit -am 'Add new feature'`)
4. Push to the branch (`git push origin feature/new-feature`)
5. Submit a Pull Request

## Reporting Issues

If you find any issues or have suggestions, please [create an issue](https://github.com/Lorwik/Indexador-Nexus/issues/new) with the following details:

- Description of the problem
- Steps to reproduce it
- Expected behavior
- Screenshots (if applicable)
- Java version and operating system

## License

This project is licensed under the GPL-3.0 license.

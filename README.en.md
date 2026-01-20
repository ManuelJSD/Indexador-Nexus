# Indexador Nexus

![Java](https://img.shields.io/badge/Java-17-orange) ![JavaFX](https://img.shields.io/badge/JavaFX-17-blue) ![Maven](https://img.shields.io/badge/Maven-3.8.6-red)

*[Versión en español](README.md)*

## Description

Indexer for Argentum Online programmed in Java. This tool allows you to view, edit, and manage the graphic resources used in the Argentum Online game. Currently works with resource types 0.13 or AOLibre.

## Features

- Visualization of graphics (GRHs)
- Editing of graphic properties
- Animation management
- Cache system to optimize performance
- Visualization of shields, helmets, and other game features
- **Configurable Background Color**: Integrated color picker to customize the viewer background.
- **Path Management**: Flexible configuration for resource paths.
- Centralized logging system
- Intuitive graphical interface with JavaFX

## Screenshots

![Main View](https://github.com/Lorwik/Indexador-Nexus/assets/1338437/ea753277-4461-4e4e-a67d-8397823a2500)

![Shield Editor](https://github.com/Lorwik/Indexador-Nexus/assets/1338437/05fb4355-b4eb-48fb-81a1-d97ddff02761)

![Graphics Editor](https://github.com/Lorwik/Indexador-Nexus/assets/1338437/bff3274b-6dec-459a-8141-245c3754563e)

## System Requirements

- Java Development Kit (JDK) 17 or higher
- Maven 3.6.0 or higher
- Argentum Online graphic resources version 0.13 or AOLibre

## Installation

### Installation Options

#### 1. Repository Cloning (Development)

1. Clone the repository:
   ```bash
   git clone https://github.com/Lorwik/Indexador-Nexus.git
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
2. Uncompress the downloaded file to a location of your choice

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

### Initial Setup

When starting the application for the first time:

1. Select the path where the Argentum Online resources are located
2. The application will automatically load the available graphics
3. Use the interface to navigate between the different resources
4. You can change the viewer background color using the color picker located above the preview pane.

## Project Status

This project is in active development. Some planned features include:

- Import from plain text file
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

If you want to contribute to the project, follow these steps:

1. Fork the project
2. Create a new branch (`git checkout -b feature/new-feature`)
3. Make the necessary changes and commit (`git commit -am 'Add new feature'`)
4. Push to the branch (`git push origin feature/new-feature`)
5. Submit a Pull Request

## Reporting Issues

If you encounter any problems or have suggestions, please [create an issue](https://github.com/Lorwik/Indexador-Nexus/issues/new) with the following details:

- Description of the problem
- Steps to reproduce it
- Expected behavior
- Screenshots (if applicable)
- Java version and operating system

## License

This project is licensed under the GPL-3.0 license.

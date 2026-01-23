module org.nexus.indexador {
	requires javafx.controls;
	requires javafx.fxml;
	requires javafx.base;
	requires java.desktop;
	requires com.google.gson;

	opens org.nexus.indexador to javafx.fxml;

	exports org.nexus.indexador;
	exports org.nexus.indexador.controllers;

	opens org.nexus.indexador.controllers to javafx.fxml;
}
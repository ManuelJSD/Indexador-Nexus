module org.nexus.indexador {
	requires javafx.controls;
	requires javafx.fxml;
	requires javafx.base;
	requires java.desktop;
	requires com.google.gson;
	requires org.kordamp.ikonli.core;
	requires org.kordamp.ikonli.javafx;
	requires org.kordamp.ikonli.fontawesome5;

	opens org.nexus.indexador to javafx.fxml;

	exports org.nexus.indexador;
	exports org.nexus.indexador.controllers;
	exports org.nexus.indexador.utils;

	opens org.nexus.indexador.controllers to javafx.fxml;
	opens org.nexus.indexador.utils to javafx.fxml;
}
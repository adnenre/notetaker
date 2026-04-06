package com.exemple.notetaker;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image; // <-- ADD THIS IMPORT
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/exemple/notetaker/main.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root, 1200, 800);
        scene.getStylesheets().add(getClass().getResource("/com/exemple/notetaker/styles.css").toExternalForm());

        primaryStage.setTitle("NoteTaker - Prise de notes Markdown");

        // ========== ADD ICON HERE (BEFORE show()) ==========
        // Place your icon.png file in:
        // src/main/resources/com/exemple/notetaker/icon.png
        Image icon = new Image(getClass().getResourceAsStream("/com/exemple/notetaker/icon.png"));
        primaryStage.getIcons().add(icon);
        // ===================================================

        primaryStage.setScene(scene);
        primaryStage.show(); // Icon must be set before this line
    }

    public static void main(String[] args) {
        launch(args);
    }
}
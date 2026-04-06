package com.exemple.notetaker;

import com.exemple.notetaker.model.Note;
import com.exemple.notetaker.model.NoteStorage;
import com.exemple.notetaker.util.MarkdownConverter;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.feather.Feather;

import java.net.URL;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class MainController implements Initializable {

    @FXML
    private SplitPane splitPane;
    @FXML
    private ListView<Note> listView;
    @FXML
    private VBox editorPane; // contains contentArea and statusLabel
    @FXML
    private TextArea contentArea;
    @FXML
    private WebView webView;
    @FXML
    private TextField searchField;
    @FXML
    private Label statusLabel;
    @FXML
    private StackPane previewStackPane;

    private NoteStorage storage;
    private List<Note> allNotes;
    private Note currentNote;
    private PauseTransition autoSave;
    private Button fullscreenButton;
    private FontIcon fullscreenIcon;
    private Button eyeButton;
    private FontIcon eyeIcon;
    private boolean previewOnly = false; // true = only preview visible

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        storage = new NoteStorage();
        allNotes = storage.loadNotes();
        currentNote = null;

        // --- ListView cell factory (pencil, title, spacer, delete) ---
        listView.setCellFactory(lv -> new ListCell<Note>() {
            private final HBox content = new HBox(10);
            private final Label titleLabel = new Label();
            private final Button deleteButton = new Button();
            private final Region spacer = new Region();

            {
                content.setAlignment(Pos.CENTER_LEFT);
                FontIcon pencilIcon = new FontIcon(Feather.EDIT);
                pencilIcon.setIconSize(14);
                pencilIcon.setIconColor(javafx.scene.paint.Color.web("#a0a0a0"));

                FontIcon deleteIcon = new FontIcon(Feather.TRASH_2);
                deleteIcon.setIconSize(14);
                deleteIcon.setIconColor(javafx.scene.paint.Color.web("#ff6666"));
                deleteButton.setGraphic(deleteIcon);
                deleteButton.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");

                HBox.setHgrow(spacer, Priority.ALWAYS);
                content.getChildren().addAll(pencilIcon, titleLabel, spacer, deleteButton);

                deleteButton.setOnAction(e -> {
                    Note note = getItem();
                    if (note != null && note.getId() != null) {
                        confirmDelete(note);
                    }
                });
            }

            @Override
            protected void updateItem(Note item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    titleLabel.setText(item.getTitle());
                    setGraphic(content);
                    setText(null);
                }
            }
        });

        // CSS for selected item (blue left border)
        listView.getStylesheets().add(getClass().getResource("/com/exemple/notetaker/styles.css").toExternalForm());
        listView.getStyleClass().add("note-list");

        // Selection listener: load note when clicked
        listView.getSelectionModel().selectedItemProperty().addListener((obs, old, newNote) -> {
            if (newNote != null && newNote.getId() != null) {
                selectNote(newNote);
            }
        });

        // Double-click rename
        listView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Note selected = listView.getSelectionModel().getSelectedItem();
                if (selected != null && selected.getId() != null) {
                    renameNote(selected);
                }
            }
        });

        // --- Buttons on preview pane (eye + fullscreen) ---
        HBox buttonContainer = new HBox(10);
        buttonContainer.setAlignment(Pos.BOTTOM_RIGHT);
        StackPane.setAlignment(buttonContainer, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(buttonContainer, new Insets(10));

        // Eye button
        eyeIcon = new FontIcon(Feather.EYE);
        eyeIcon.setIconSize(16);
        eyeIcon.setIconColor(javafx.scene.paint.Color.WHITE);
        eyeButton = new Button();
        eyeButton.setGraphic(eyeIcon);
        eyeButton.setStyle(
                "-fx-background-color: transparent; -fx-background-radius: 20px; -fx-padding: 8px; -fx-cursor: hand;");
        eyeButton.setOnAction(e -> togglePanelsVisibility());

        // Fullscreen button
        fullscreenIcon = new FontIcon(Feather.MAXIMIZE);
        fullscreenIcon.setIconSize(16);
        fullscreenIcon.setIconColor(javafx.scene.paint.Color.WHITE);
        fullscreenButton = new Button();
        fullscreenButton.setGraphic(fullscreenIcon);
        fullscreenButton.setStyle(
                "-fx-background-color: transparent; -fx-background-radius: 20px; -fx-padding: 8px; -fx-cursor: hand;");
        fullscreenButton.setOnAction(e -> toggleFullScreen());

        buttonContainer.getChildren().addAll(eyeButton, fullscreenButton);
        previewStackPane.getChildren().add(buttonContainer);

        // F11 shortcut
        webView.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.setOnKeyPressed(event -> {
                    if (event.getCode() == KeyCode.F11) {
                        toggleFullScreen();
                        event.consume();
                    }
                });
            }
        });

        // Auto-save
        autoSave = new PauseTransition(Duration.seconds(2));
        autoSave.setOnFinished(e -> saveCurrentNote());

        // Content area listener
        contentArea.textProperty().addListener((obs, old, val) -> {
            if (currentNote != null) {
                currentNote.setContent(val);
                updatePreview();
                autoSave.playFromStart();
            }
        });

        refreshList();

        if (!allNotes.isEmpty()) {
            listView.getSelectionModel().select(0);
            selectNote(allNotes.get(0));
        } else {
            newNote();
        }
    }

    // ---------- Eye button: toggle list and editor panes ----------
    private void togglePanelsVisibility() {
        if (!previewOnly) {
            // Remove list and editor from split pane (only preview remains)
            if (splitPane.getItems().contains(listView)) {
                splitPane.getItems().remove(listView);
            }
            if (splitPane.getItems().contains(editorPane)) {
                splitPane.getItems().remove(editorPane);
            }
            eyeIcon.setIconCode(Feather.EYE_OFF);
            previewOnly = true;
        } else {
            // Restore list and editor at original positions (index 0 and 1)
            splitPane.getItems().add(0, listView);
            splitPane.getItems().add(1, editorPane);
            // Reset dividers to reasonable values
            splitPane.setDividerPositions(0.25, 0.6);
            eyeIcon.setIconCode(Feather.EYE);
            previewOnly = false;
        }
    }

    private void toggleFullScreen() {
        Stage stage = (Stage) fullscreenButton.getScene().getWindow();
        boolean newState = !stage.isFullScreen();
        stage.setFullScreen(newState);
        if (newState) {
            fullscreenIcon.setIconCode(Feather.MINIMIZE);
        } else {
            fullscreenIcon.setIconCode(Feather.MAXIMIZE);
        }
    }

    // ---------- Helper methods ----------
    private boolean isTitleDuplicate(String title, String excludeId) {
        return storage.isTitleDuplicate(allNotes, title, excludeId);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        if (listView.getScene() != null && listView.getScene().getWindow() != null) {
            alert.initOwner(listView.getScene().getWindow());
        }
        alert.showAndWait();
    }

    private void confirmDelete(Note note) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Note");
        alert.setHeaderText("Delete this note?");
        alert.setContentText(
                "Are you sure you want to delete \"" + note.getTitle() + "\"? This action cannot be undone.");
        if (listView.getScene() != null && listView.getScene().getWindow() != null) {
            alert.initOwner(listView.getScene().getWindow());
        }
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            deleteNote(note);
        }
    }

    private void renameNote(Note note) {
        TextInputDialog dialog = new TextInputDialog(note.getTitle());
        dialog.setTitle("Rename Note");
        dialog.setHeaderText("Rename note");
        dialog.setContentText("New title:");
        if (listView.getScene() != null && listView.getScene().getWindow() != null) {
            dialog.initOwner(listView.getScene().getWindow());
        }
        while (true) {
            Optional<String> result = dialog.showAndWait();
            if (!result.isPresent())
                return;
            String newTitle = result.get().trim();
            if (newTitle.isEmpty()) {
                showAlert("Error", "Title cannot be empty.");
                continue;
            }
            if (isTitleDuplicate(newTitle, note.getId())) {
                showAlert("Duplicate Title", "A note with this title already exists. Please choose another title.");
                dialog.getEditor().clear();
                continue;
            }
            note.setTitle(newTitle);
            refreshList();
            autoSave.playFromStart();
            updateStatus("Note renamed");
            listView.getSelectionModel().select(note);
            break;
        }
    }

    private void refreshList() {
        String search = searchField.getText().toLowerCase();
        List<Note> filtered = allNotes.stream()
                .filter(n -> search.isEmpty() ||
                        n.getTitle().toLowerCase().contains(search) ||
                        n.getContent().toLowerCase().contains(search))
                .collect(Collectors.toList());

        filtered.sort(Comparator.comparing(Note::getTitle, String.CASE_INSENSITIVE_ORDER));

        Note selectedNote = listView.getSelectionModel().getSelectedItem();
        listView.getItems().clear();
        listView.getItems().addAll(filtered);

        // Restore selection if possible
        if (selectedNote != null && allNotes.contains(selectedNote)) {
            listView.getSelectionModel().select(selectedNote);
        } else if (currentNote != null && allNotes.contains(currentNote)) {
            listView.getSelectionModel().select(currentNote);
        }
    }

    private void selectNote(Note note) {
        if (note == null || note.getId() == null)
            return;
        currentNote = note;
        contentArea.setText(note.getContent());
        updatePreview();
        updateStatus("Note loaded");
        listView.getSelectionModel().select(note);
        listView.scrollTo(note);
    }

    private void updatePreview() {
        if (currentNote != null) {
            String html = MarkdownConverter.toHtml(currentNote.getContent());
            webView.getEngine().loadContent(html);
        }
    }

    private void saveCurrentNote() {
        if (currentNote != null) {
            storage.saveNotes(allNotes);
            updateStatus("Saved at " + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        }
    }

    private void updateStatus(String msg) {
        Platform.runLater(() -> statusLabel.setText(msg));
    }

    @FXML
    private void newNote() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("New Note");
        dialog.setHeaderText("Add New Note");
        dialog.setContentText("Title:");
        if (listView.getScene() != null && listView.getScene().getWindow() != null) {
            dialog.initOwner(listView.getScene().getWindow());
        }
        while (true) {
            Optional<String> result = dialog.showAndWait();
            if (!result.isPresent())
                return;
            String title = result.get().trim();
            if (title.isEmpty()) {
                showAlert("Error", "Title cannot be empty.");
                continue;
            }
            if (isTitleDuplicate(title, null)) {
                showAlert("Duplicate Title", "A note with this title already exists. Please choose another title.");
                dialog.getEditor().clear();
                continue;
            }
            Note newNote = new Note();
            newNote.setTitle(title);
            allNotes.add(newNote);
            refreshList();
            selectNote(newNote);
            autoSave.playFromStart();
            break;
        }
    }

    private void deleteNote(Note note) {
        allNotes.remove(note);
        storage.saveNotes(allNotes);
        refreshList();
        if (allNotes.isEmpty()) {
            newNote();
        } else {
            Note first = listView.getItems().isEmpty() ? null : listView.getItems().get(0);
            if (first != null) {
                selectNote(first);
            }
        }
    }

    @FXML
    private void filterNotes() {
        refreshList();
    }
}
package com.exemple.notetaker.model;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class NoteStorage {
    private static final String FILE_NAME = "notes.json"; // changed from "New Note"
    private final ObjectMapper mapper;

    public NoteStorage() {
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    public List<Note> loadNotes() {
        File file = new File(FILE_NAME);
        if (!file.exists()) {
            // Return a list with an example note if file doesn't exist
            return getExampleNotes();
        }
        try {
            List<Note> notes = mapper.readValue(file, new TypeReference<List<Note>>() {
            });
            return notes.isEmpty() ? getExampleNotes() : notes;
        } catch (IOException e) {
            System.err.println("Error loading notes: " + e.getMessage());
            return getExampleNotes();
        }
    }

    public void saveNotes(List<Note> notes) {
        try {
            mapper.writeValue(new File(FILE_NAME), notes);
        } catch (IOException e) {
            System.err.println("Error saving notes: " + e.getMessage());
        }
    }

    // Check if a title already exists (case‑insensitive)
    public boolean isTitleDuplicate(List<Note> notes, String title, String excludeId) {
        return notes.stream()
                .filter(n -> !n.getId().equals(excludeId))
                .anyMatch(n -> n.getTitle().equalsIgnoreCase(title.trim()));
    }

    // Example note with Markdown content
    private List<Note> getExampleNotes() {
        List<Note> notes = new ArrayList<>();
        Note example = new Note("Welcome to NoteTaker",
                "# Welcome to NoteTaker!\n\n" +
                        "This is your first note. You can write **Markdown** here.\n\n" +
                        "## Features\n\n" +
                        "- **Real‑time preview** – see your formatted text as you type.\n" +
                        "- **Copy button** for code blocks.\n" +
                        "- **Search** notes by title or content.\n" +
                        "- **Rename** by double‑clicking a note.\n\n" +
                        "```java\n" +
                        "public class HelloWorld {\n" +
                        "    public static void main(String[] args) {\n" +
                        "       System.out.println(\"Hello, NoteTaker!\");\n" +
                        "       System.out.println(\"Real‑time preview works!\");\n" +
                        "       System.out.println(\"Copy button works!\");\n" +
                        "       System.out.println(\"Search notes by title or content, works!\");\n" +
                        "       System.out.println(\"Rename by double‑clicking a note. works!\");\n" +
                        "    }\n" +
                        "}\n" +
                        "```\n\n" +
                        "Enjoy!");
        notes.add(example);
        return notes;
    }
}
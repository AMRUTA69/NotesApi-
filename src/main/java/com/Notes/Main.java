package com.Notes;

import com.google.gson.Gson;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import static spark.Spark.*;

public class Main {

    // Step 3: Store notes in a ConcurrentHashMap
    private static final ConcurrentHashMap<Integer, Note> notes = new ConcurrentHashMap<>();
    private static final AtomicInteger idCounter = new AtomicInteger(1);
    private static final Gson gson = new Gson();

    public static void main(String[] args) {
        
        // Spark server ko port 4567 par start karo
        port(4567);

        // === Step 2: CRUD Routes ===

        // 1. CREATE (POST /notes)
        post("/notes", (req, res) -> {
            res.type("application/json");
            // JSON request body ko Note object mein convert karo
            Note note = gson.fromJson(req.body(), Note.class);
            
            if (note == null || note.getTitle() == null) {
                res.status(400); // Bad Request
                return gson.toJson(new ErrorMessage("Title is required"));
            }

            int id = idCounter.getAndIncrement();
            note.setId(id);
            notes.put(id, note);
            
            res.status(201); // Created
            return gson.toJson(note);
        });

        // 2. READ ALL (GET /notes)
        get("/notes", (req, res) -> {
            res.type("application/json");
            return gson.toJson(notes.values());
        });

        // 3. READ ONE (GET /notes/:id)
        get("/notes/:id", (req, res) -> {
            res.type("application/json");
            int id = Integer.parseInt(req.params(":id"));
            Note note = notes.get(id);

            if (note == null) {
                res.status(404); // Not Found
                return gson.toJson(new ErrorMessage("Note not found"));
            }
            return gson.toJson(note);
        });

        // 4. UPDATE (PUT /notes/:id)
        put("/notes/:id", (req, res) -> {
            res.type("application/json");
            int id = Integer.parseInt(req.params(":id"));
            Note existingNote = notes.get(id);

            if (existingNote == null) {
                res.status(404);
                return gson.toJson(new ErrorMessage("Note not found"));
            }

            Note updatedNote = gson.fromJson(req.body(), Note.class);
            updatedNote.setId(id); // ID same rahegi
            notes.put(id, updatedNote);

            res.status(200); // OK
            return gson.toJson(updatedNote);
        });

        // 5. DELETE (DELETE /notes/:id)
        delete("/notes/:id", (req, res) -> {
            res.type("application/json");
            int id = Integer.parseInt(req.params(":id"));
            Note removed = notes.remove(id);

            if (removed == null) {
                res.status(404);
                return gson.toJson(new ErrorMessage("Note not found"));
            }
            res.status(200);
            return gson.toJson(new Message("Note deleted successfully"));
        });

        System.out.println("Server started at http://localhost:4567");
    }

    // Helper classes for JSON response
    static class Message {
        String message;
        Message(String message) { this.message = message; }
    }

    static class ErrorMessage {
        String error;
        ErrorMessage(String error) { this.error = error; }
    }
}
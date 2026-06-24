package com.example.uniplanner;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

public class Assignment {

    private String id;
    private String title;
    private String description;
    private String deadline;
    private boolean completed;

    public Assignment(String title, String description, String deadline) {
        this.id = UUID.randomUUID().toString();
        this.title = title;
        this.description = description;
        this.deadline = deadline;
        this.completed = false;
    }

    public Assignment(String id, String title, String description, String deadline, boolean completed) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.deadline = deadline;
        this.completed = completed;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title == null ? "" : title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description == null ? "" : description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDeadline() {
        return deadline == null ? "" : deadline;
    }

    public void setDeadline(String deadline) {
        this.deadline = deadline;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public String statusText() {
        return completed ? "Erledigt" : "Offen";
    }

    public JSONObject toJson() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("id", id);
        json.put("title", getTitle());
        json.put("description", getDescription());
        json.put("deadline", getDeadline());
        json.put("completed", completed);
        return json;
    }

    public static Assignment fromJson(JSONObject json) throws JSONException {
        return new Assignment(
                json.optString("id", UUID.randomUUID().toString()),
                json.optString("title", ""),
                json.optString("description", ""),
                json.optString("deadline", ""),
                json.optBoolean("completed", false)
        );
    }
}
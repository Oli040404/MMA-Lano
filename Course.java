package com.example.uniplanner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.UUID;

public class Course {

    private String id;
    private String name;
    private String lecturer;
    private String semester;
    private String room;
    private String description;
    private String examDate;
    private int progress;
    private int color;
    private String documentUri;
    private ArrayList<Assignment> assignments;

    public Course(String name) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.lecturer = "";
        this.semester = "";
        this.room = "";
        this.description = "";
        this.examDate = "";
        this.progress = 0;
        this.color = 0xFFBBDEFB;
        this.documentUri = "";
        this.assignments = new ArrayList<>();
    }

    public Course(
            String id,
            String name,
            String lecturer,
            String semester,
            String room,
            String description,
            String examDate,
            int progress,
            int color,
            String documentUri,
            ArrayList<Assignment> assignments
    ) {
        this.id = id;
        this.name = name;
        this.lecturer = lecturer;
        this.semester = semester;
        this.room = room;
        this.description = description;
        this.examDate = examDate;
        setProgress(progress);
        this.color = color;
        this.documentUri = documentUri;
        this.assignments = assignments == null ? new ArrayList<>() : assignments;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name == null ? "" : name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLecturer() {
        return lecturer == null ? "" : lecturer;
    }

    public void setLecturer(String lecturer) {
        this.lecturer = lecturer;
    }

    public String getSemester() {
        return semester == null ? "" : semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public String getRoom() {
        return room == null ? "" : room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public String getDescription() {
        return description == null ? "" : description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getExamDate() {
        return examDate == null ? "" : examDate;
    }

    public void setExamDate(String examDate) {
        this.examDate = examDate;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        if (progress < 0) {
            this.progress = 0;
        } else if (progress > 100) {
            this.progress = 100;
        } else {
            this.progress = progress;
        }
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public String getDocumentUri() {
        return documentUri == null ? "" : documentUri;
    }

    public void setDocumentUri(String documentUri) {
        this.documentUri = documentUri;
    }

    public ArrayList<Assignment> getAssignments() {
        if (assignments == null) {
            assignments = new ArrayList<>();
        }
        return assignments;
    }

    public void addAssignment(Assignment assignment) {
        getAssignments().add(assignment);
    }

    public JSONObject toJson() throws JSONException {
        JSONObject json = new JSONObject();

        json.put("id", id);
        json.put("name", getName());
        json.put("lecturer", getLecturer());
        json.put("semester", getSemester());
        json.put("room", getRoom());
        json.put("description", getDescription());
        json.put("examDate", getExamDate());
        json.put("progress", progress);
        json.put("color", color);
        json.put("documentUri", getDocumentUri());

        JSONArray assignmentArray = new JSONArray();
        for (Assignment assignment : getAssignments()) {
            assignmentArray.put(assignment.toJson());
        }

        json.put("assignments", assignmentArray);
        return json;
    }

    public static Course fromJson(JSONObject json) throws JSONException {
        ArrayList<Assignment> assignmentList = new ArrayList<>();
        JSONArray assignmentArray = json.optJSONArray("assignments");

        if (assignmentArray != null) {
            for (int i = 0; i < assignmentArray.length(); i++) {
                assignmentList.add(Assignment.fromJson(assignmentArray.getJSONObject(i)));
            }
        }

        return new Course(
                json.optString("id", UUID.randomUUID().toString()),
                json.optString("name", ""),
                json.optString("lecturer", ""),
                json.optString("semester", ""),
                json.optString("room", ""),
                json.optString("description", ""),
                json.optString("examDate", ""),
                json.optInt("progress", 0),
                json.optInt("color", 0xFFBBDEFB),
                json.optString("documentUri", ""),
                assignmentList
        );
    }
}
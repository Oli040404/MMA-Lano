package com.example.uniplanner;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class CourseRepository {

    private static final String PREF_NAME = "uni_planner_storage";
    private static final String KEY_COURSES = "courses";

    private final SharedPreferences sharedPreferences;
    private final ArrayList<Course> courses;

    public CourseRepository(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        courses = new ArrayList<>();
        loadCourses();

        if (courses.isEmpty()) {
            addMockCourses();
            saveCourses();
        }
    }

    public ArrayList<Course> getCourses() {
        return courses;
    }

    public Course getCourseById(String id) {
        for (Course course : courses) {
            if (course.getId().equals(id)) {
                return course;
            }
        }
        return null;
    }

    public void addCourse(Course course) {
        courses.add(course);
        saveCourses();
    }

    public void updateCourse(Course updatedCourse) {
        for (int i = 0; i < courses.size(); i++) {
            if (courses.get(i).getId().equals(updatedCourse.getId())) {
                courses.set(i, updatedCourse);
                saveCourses();
                return;
            }
        }
    }

    public void deleteCourse(String courseId) {
        Course target = null;

        for (Course course : courses) {
            if (course.getId().equals(courseId)) {
                target = course;
                break;
            }
        }

        if (target != null) {
            courses.remove(target);
            saveCourses();
        }
    }

    public void saveCourses() {
        try {
            JSONArray array = new JSONArray();

            for (Course course : courses) {
                array.put(course.toJson());
            }

            sharedPreferences.edit()
                    .putString(KEY_COURSES, array.toString())
                    .apply();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadCourses() {
        courses.clear();

        String jsonString = sharedPreferences.getString(KEY_COURSES, "");

        if (jsonString == null || jsonString.trim().isEmpty()) {
            return;
        }

        try {
            JSONArray array = new JSONArray(jsonString);

            for (int i = 0; i < array.length(); i++) {
                courses.add(Course.fromJson(array.getJSONObject(i)));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addMockCourses() {
        Course android = new Course("Android Entwicklung");
        android.setLecturer("Prof. Beispiel");
        android.setSemester("BME6");
        android.setRoom("Online / Labor");
        android.setDescription("App-Projekt mit mehreren Prototypen.");
        android.setExamDate("2026-07-10");
        android.setProgress(70);
        android.setColor(0xFFC8E6C9);
        android.addAssignment(new Assignment("Assignment 4", "Second Prototype / Beta fertigstellen", "2026-06-21"));

        Course mathe = new Course("Mathematik");
        mathe.setLecturer("Dr. Muster");
        mathe.setSemester("BME6");
        mathe.setRoom("A203");
        mathe.setDescription("Übungen, Klausurvorbereitung und Lernfortschritt.");
        mathe.setExamDate("2026-07-18");
        mathe.setProgress(45);
        mathe.setColor(0xFFFFF9C4);
        mathe.addAssignment(new Assignment("Übungsblatt 8", "Aufgaben 1 bis 5 bearbeiten", "2026-06-15"));

        courses.add(android);
        courses.add(mathe);
    }

    public static long daysUntil(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return Long.MAX_VALUE;
        }

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.GERMANY);
        format.setLenient(false);

        try {
            Date targetDate = format.parse(dateString);
            Date today = format.parse(format.format(new Date()));

            if (targetDate == null || today == null) {
                return Long.MAX_VALUE;
            }

            long diff = targetDate.getTime() - today.getTime();
            return TimeUnit.MILLISECONDS.toDays(diff);

        } catch (ParseException e) {
            return Long.MAX_VALUE;
        }
    }

    public static boolean isValidDate(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return true;
        }

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.GERMANY);
        format.setLenient(false);

        try {
            format.parse(dateString);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }
}
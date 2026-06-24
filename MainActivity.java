package com.example.uniplanner;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_DOCUMENT = 4001;

    private CourseRepository repository;
    private LinearLayout root;
    private LinearLayout content;
    private boolean darkMode = false;
    private String documentCourseId = "";

    private final int[] courseColors = {
            0xFFBBDEFB,
            0xFFC8E6C9,
            0xFFFFF9C4,
            0xFFFFCCBC,
            0xFFE1BEE7,
            0xFFD7CCC8
    };

    private int selectedColor = 0xFFBBDEFB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        repository = new CourseRepository(this);
        buildBaseLayout();
        showDashboard();
    }

    private void buildBaseLayout() {
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(getBackgroundColor());
        setContentView(root);

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(24, 56, 24, 16);
        header.setBackgroundColor(getCardColor());

        Button menuButton = new Button(this);
        menuButton.setText("☰");
        menuButton.setTextSize(20);
        menuButton.setMinHeight(96);
        menuButton.setMinWidth(96);
        menuButton.setOnClickListener(v -> showMenuDialog());

        TextView title = new TextView(this);
        title.setText("Uni Planner");
        title.setTextSize(24);
        title.setTextColor(getTextColor());
        title.setGravity(Gravity.CENTER_VERTICAL);
        title.setPadding(24, 0, 24, 0);

        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1
        );

        Button modeButton = new Button(this);
        modeButton.setText(darkMode ? "💡" : "🔆");
        modeButton.setTextSize(18);
        modeButton.setMinHeight(96);
        modeButton.setMinWidth(96);
        modeButton.setOnClickListener(v -> {
            darkMode = !darkMode;
            AppCompatDelegate.setDefaultNightMode(
                    darkMode
                            ? AppCompatDelegate.MODE_NIGHT_YES
                            : AppCompatDelegate.MODE_NIGHT_NO
            );
            buildBaseLayout();
            showDashboard();
        });

        header.addView(menuButton);
        header.addView(title, titleParams);
        header.addView(modeButton);

        root.addView(header);

        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);

        ScrollView scrollView = new ScrollView(this);
        scrollView.addView(content);

        root.addView(scrollView, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1
        ));

        LinearLayout navigation = new LinearLayout(this);
        navigation.setOrientation(LinearLayout.HORIZONTAL);
        navigation.setPadding(12, 12, 12, 12);
        navigation.setBackgroundColor(getCardColor());

        Button dashboardButton = new Button(this);
        dashboardButton.setText("Dashboard");
        dashboardButton.setOnClickListener(v -> showDashboard());

        Button courseButton = new Button(this);
        courseButton.setText("Kurse");
        courseButton.setOnClickListener(v -> showCourseList());

        Button examButton = new Button(this);
        examButton.setText("Klausuren");
        examButton.setOnClickListener(v -> showExamOverview());

        navigation.addView(dashboardButton, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        navigation.addView(courseButton, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        navigation.addView(examButton, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        root.addView(navigation);
    }

    private void clearContent() {
        content.removeAllViews();
        content.setPadding(24, 24, 24, 24);
        content.setBackgroundColor(getBackgroundColor());
    }

    private void showDashboard() {
        clearContent();

        PlannerDashboardSummary summary = PlannerDashboardSummary.fromCourses(repository.getCourses());

        addTitle("Dashboard");

        content.addView(makeText(
                "Überblick über deine Kurse, nächsten Klausuren und offenen Abgaben.",
                16,
                false
        ));

        addSpace(16);

        addSmallCard(
                "Zusammenfassung",
                "Kurse: " + summary.getCourseCount() + "\n" +
                        "Offene Abgaben: " + summary.getOpenAssignmentCount() + "\n" +
                        "Durchschnittlicher Fortschritt: " + summary.getAverageProgress() + "%",
                getNeutralCardColor()
        );

        addSpace(16);
        addSubtitle("Nächste Klausur");

        Course nextExamCourse = summary.getNextExamCourse();

        if (nextExamCourse == null) {
            content.addView(makeText("Keine kommende Klausur eingetragen.", 15, false));
        } else {
            long days = CourseRepository.daysUntil(nextExamCourse.getExamDate());

            addSmallCard(
                    nextExamCourse.getName(),
                    "Klausur: " + nextExamCourse.getExamDate() + "\n" +
                            "Noch " + days + " Tage",
                    nextExamCourse.getColor()
            );
        }

        addSpace(16);
        addSubtitle("Kommende offene Abgaben");

        ArrayList<Assignment> upcomingAssignments = summary.getUpcomingAssignments();

        if (upcomingAssignments.isEmpty()) {
            content.addView(makeText("Keine offenen Abgaben vorhanden.", 15, false));
        } else {
            for (Assignment assignment : upcomingAssignments) {
                Course relatedCourse = findCourseForAssignment(assignment);

                String courseName = relatedCourse == null ? "-" : relatedCourse.getName();
                int cardColor = relatedCourse == null ? getNeutralCardColor() : relatedCourse.getColor();

                addSmallCard(
                        assignment.getTitle(),
                        "Kurs: " + courseName + "\n" +
                                "Deadline: " + safeOrDash(assignment.getDeadline()) + "\n" +
                                "Status: " + assignment.statusText(),
                        cardColor
                );
            }
        }
    }

    private void showCourseList() {
        clearContent();

        addTitle("Kursübersicht");

        Button addButton = new Button(this);
        addButton.setText("+ Neuer Kurs");
        addButton.setOnClickListener(v -> showCourseForm(null));
        content.addView(addButton);

        addSpace(16);

        if (repository.getCourses().isEmpty()) {
            content.addView(makeText("Noch keine Kurse vorhanden.", 16, false));
            return;
        }

        for (Course course : repository.getCourses()) {
            addCourseCard(course);
        }
    }

    private void addCourseCard(Course course) {
        LinearLayout card = makeCard(course.getColor());

        TextView name = makeText(course.getName(), 20, true);
        name.setTextColor(Color.BLACK);
        card.addView(name);

        TextView info = makeText(
                "Fortschritt: " + course.getProgress() + "%\n" +
                        "Klausur: " + displayExamText(course),
                15,
                false
        );
        info.setTextColor(Color.BLACK);
        card.addView(info);

        ProgressBar progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progressBar.setMax(100);
        progressBar.setProgress(course.getProgress());
        card.addView(progressBar);

        LinearLayout buttons = new LinearLayout(this);
        buttons.setOrientation(LinearLayout.HORIZONTAL);

        Button openButton = new Button(this);
        openButton.setText("Öffnen");
        openButton.setOnClickListener(v -> showCourseDetails(course.getId()));

        Button editButton = new Button(this);
        editButton.setText("Bearbeiten");
        editButton.setOnClickListener(v -> showCourseForm(course));

        Button deleteButton = new Button(this);
        deleteButton.setText("Löschen");
        deleteButton.setOnClickListener(v -> confirmDeleteCourse(course));

        buttons.addView(openButton, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        buttons.addView(editButton, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        buttons.addView(deleteButton, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        card.addView(buttons);

        content.addView(card);
        addSpace(12);
    }

    private void showCourseDetails(String courseId) {
        clearContent();

        Course course = repository.getCourseById(courseId);

        if (course == null) {
            Toast.makeText(this, "Kurs wurde nicht gefunden.", Toast.LENGTH_SHORT).show();
            showCourseList();
            return;
        }

        addTitle(course.getName());

        addSmallCard(
                "Kursinformationen",
                "Dozent: " + safeOrDash(course.getLecturer()) + "\n" +
                        "Semester: " + safeOrDash(course.getSemester()) + "\n" +
                        "Raum: " + safeOrDash(course.getRoom()) + "\n" +
                        "Beschreibung: " + safeOrDash(course.getDescription()),
                course.getColor()
        );

        addSubtitle("Fortschritt");

        ProgressBar progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progressBar.setMax(100);
        progressBar.setProgress(course.getProgress());
        content.addView(progressBar);

        TextView progressText = makeText(course.getProgress() + "% abgeschlossen", 16, true);
        content.addView(progressText);

        LinearLayout progressButtons = new LinearLayout(this);
        progressButtons.setOrientation(LinearLayout.HORIZONTAL);

        Button minus = new Button(this);
        minus.setText("-10%");
        minus.setOnClickListener(v -> {
            course.setProgress(course.getProgress() - 10);
            repository.saveCourses();
            showCourseDetails(course.getId());
        });

        Button plus = new Button(this);
        plus.setText("+10%");
        plus.setOnClickListener(v -> {
            course.setProgress(course.getProgress() + 10);
            repository.saveCourses();
            showCourseDetails(course.getId());
        });

        progressButtons.addView(minus, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        progressButtons.addView(plus, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        content.addView(progressButtons);

        addSpace(16);
        addSubtitle("Klausur");

        content.addView(makeText(displayExamText(course), 16, false));

        addSpace(16);
        addSubtitle("Dokument");

        if (course.getDocumentUri().isEmpty()) {
            content.addView(makeText("Kein Dokument zugeordnet.", 16, false));
        } else {
            content.addView(makeText("Dokument ist zugeordnet.", 16, false));
            Button openDocButton = new Button(this);
            openDocButton.setText("Dokument öffnen");
            openDocButton.setOnClickListener(v -> openDocument(course.getDocumentUri()));
            content.addView(openDocButton);
        }

        Button documentButton = new Button(this);
        documentButton.setText(course.getDocumentUri().isEmpty() ? "Dokument auswählen" : "Dokument ändern");
        documentButton.setOnClickListener(v -> selectDocument(course.getId()));
        content.addView(documentButton);

        addSpace(16);
        addSubtitle("Abgaben");

        Button addAssignmentButton = new Button(this);
        addAssignmentButton.setText("+ Abgabe hinzufügen");
        addAssignmentButton.setOnClickListener(v -> showAssignmentDialog(course));
        content.addView(addAssignmentButton);

        addSpace(8);

        if (course.getAssignments().isEmpty()) {
            content.addView(makeText("Noch keine Abgaben vorhanden.", 15, false));
        } else {
            for (Assignment assignment : course.getAssignments()) {
                addAssignmentRow(course, assignment);
            }
        }

        addSpace(16);

        Button editButton = new Button(this);
        editButton.setText("Kurs bearbeiten");
        editButton.setOnClickListener(v -> showCourseForm(course));
        content.addView(editButton);

        Button backButton = new Button(this);
        backButton.setText("Zurück zur Liste");
        backButton.setOnClickListener(v -> showCourseList());
        content.addView(backButton);
    }

    private void addAssignmentRow(Course course, Assignment assignment) {
        LinearLayout card = makeCard(getCardColor());

        CheckBox checkBox = new CheckBox(this);
        checkBox.setText(
                assignment.getTitle() + "\n" +
                        "Deadline: " + safeOrDash(assignment.getDeadline()) + "\n" +
                        "Beschreibung: " + safeOrDash(assignment.getDescription()) + "\n" +
                        "Status: " + assignment.statusText()
        );
        checkBox.setTextColor(getTextColor());
        checkBox.setTextSize(15);
        checkBox.setChecked(assignment.isCompleted());

        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            assignment.setCompleted(isChecked);
            repository.saveCourses();
            showCourseDetails(course.getId());
        });

        card.addView(checkBox);

        Button deleteAssignButton = new Button(this);
        deleteAssignButton.setText("Abgabe löschen");
        deleteAssignButton.setOnClickListener(v -> {
            course.getAssignments().remove(assignment);
            repository.saveCourses();
            showCourseDetails(course.getId());
        });
        card.addView(deleteAssignButton);

        content.addView(card);
        addSpace(8);
    }

    private void showCourseForm(Course existingCourse) {
        clearContent();

        boolean editMode = existingCourse != null;
        addTitle(editMode ? "Kurs bearbeiten" : "Neuer Kurs");

        EditText nameInput = makeInput("Kursname");
        EditText lecturerInput = makeInput("Dozent");
        EditText semesterInput = makeInput("Semester");
        EditText roomInput = makeInput("Raum");
        EditText descriptionInput = makeInput("Beschreibung");
        EditText examInput = makeInput("Klausurdatum yyyy-MM-dd");
        EditText progressInput = makeInput("Fortschritt 0-100");

        examInput.setFocusable(false);
        examInput.setOnClickListener(v -> showDatePicker(examInput));

        progressInput.setInputType(InputType.TYPE_CLASS_NUMBER);

        selectedColor = editMode ? existingCourse.getColor() : 0xFFBBDEFB;

        if (editMode) {
            nameInput.setText(existingCourse.getName());
            lecturerInput.setText(existingCourse.getLecturer());
            semesterInput.setText(existingCourse.getSemester());
            roomInput.setText(existingCourse.getRoom());
            descriptionInput.setText(existingCourse.getDescription());
            examInput.setText(existingCourse.getExamDate());
            progressInput.setText(String.valueOf(existingCourse.getProgress()));
        }

        content.addView(nameInput);
        content.addView(lecturerInput);
        content.addView(semesterInput);
        content.addView(roomInput);
        content.addView(descriptionInput);
        content.addView(examInput);
        content.addView(progressInput);

        addSubtitle("Kursfarbe");

        LinearLayout colorRow = new LinearLayout(this);
        colorRow.setOrientation(LinearLayout.HORIZONTAL);

        for (int color : courseColors) {
            Button colorButton = new Button(this);
            colorButton.setText(" ");
            colorButton.setBackgroundColor(color);
            colorButton.setOnClickListener(v -> selectedColor = color);
            colorRow.addView(colorButton, new LinearLayout.LayoutParams(0, 90, 1));
        }

        content.addView(colorRow);

        addSpace(16);

        Button saveButton = new Button(this);
        saveButton.setText("Speichern");

        saveButton.setOnClickListener(v -> {
            String name = nameInput.getText().toString().trim();
            String examDate = examInput.getText().toString().trim();

            if (name.isEmpty()) {
                Toast.makeText(this, "Bitte gib einen Kursnamen ein.", Toast.LENGTH_LONG).show();
                return;
            }

            if (!CourseRepository.isValidDate(examDate)) {
                Toast.makeText(this, "Bitte nutze das Datumsformat yyyy-MM-dd.", Toast.LENGTH_LONG).show();
                return;
            }

            int progress = parseProgress(progressInput.getText().toString());

            Course course = editMode ? existingCourse : new Course(name);

            course.setName(name);
            course.setLecturer(lecturerInput.getText().toString().trim());
            course.setSemester(semesterInput.getText().toString().trim());
            course.setRoom(roomInput.getText().toString().trim());
            course.setDescription(descriptionInput.getText().toString().trim());
            course.setExamDate(examDate);
            course.setProgress(progress);
            course.setColor(selectedColor);

            if (editMode) {
                repository.updateCourse(course);
            } else {
                repository.addCourse(course);
            }

            Toast.makeText(this, "Kurs gespeichert.", Toast.LENGTH_SHORT).show();
            showCourseList();
        });

        Button cancelButton = new Button(this);
        cancelButton.setText("Abbrechen");
        cancelButton.setOnClickListener(v -> {
            if (editMode) {
                showCourseDetails(existingCourse.getId());
            } else {
                showCourseList();
            }
        });

        content.addView(saveButton);
        content.addView(cancelButton);
    }

    private void showAssignmentDialog(Course course) {
        LinearLayout dialogLayout = new LinearLayout(this);
        dialogLayout.setOrientation(LinearLayout.VERTICAL);
        dialogLayout.setPadding(40, 20, 40, 10);

        EditText titleInput = makeInput("Titel");
        EditText descriptionInput = makeInput("Beschreibung");
        EditText deadlineInput = makeInput("Deadline yyyy-MM-dd");

        deadlineInput.setFocusable(false);
        deadlineInput.setOnClickListener(v -> showDatePicker(deadlineInput));

        dialogLayout.addView(titleInput);
        dialogLayout.addView(descriptionInput);
        dialogLayout.addView(deadlineInput);

        new AlertDialog.Builder(this)
                .setTitle("Abgabe hinzufügen")
                .setView(dialogLayout)
                .setPositiveButton("Speichern", (dialog, which) -> {
                    String title = titleInput.getText().toString().trim();
                    String deadline = deadlineInput.getText().toString().trim();

                    if (title.isEmpty()) {
                        Toast.makeText(this, "Eine Abgabe braucht einen Titel.", Toast.LENGTH_LONG).show();
                        return;
                    }

                    if (!CourseRepository.isValidDate(deadline)) {
                        Toast.makeText(this, "Bitte nutze das Datumsformat yyyy-MM-dd.", Toast.LENGTH_LONG).show();
                        return;
                    }

                    Assignment assignment = new Assignment(
                            title,
                            descriptionInput.getText().toString().trim(),
                            deadline
                    );

                    course.addAssignment(assignment);
                    repository.saveCourses();
                    showCourseDetails(course.getId());
                })
                .setNegativeButton("Abbrechen", null)
                .show();
    }

    private void showDatePicker(EditText target) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        new DatePickerDialog(this, (view, y, m, d) -> {
            String date = String.format(Locale.GERMANY, "%04d-%02d-%02d", y, m + 1, d);
            target.setText(date);
        }, year, month, day).show();
    }

    private void showExamOverview() {
        clearContent();

        addTitle("Klausurübersicht");

        ArrayList<Course> examCourses = getSortedExamCourses();

        if (examCourses.isEmpty()) {
            content.addView(makeText("Keine kommenden Klausuren vorhanden.", 16, false));
            return;
        }

        for (Course course : examCourses) {
            long days = CourseRepository.daysUntil(course.getExamDate());

            addSmallCard(
                    course.getName(),
                    "Klausurdatum: " + course.getExamDate() + "\nNoch " + days + " Tage",
                    course.getColor()
            );
        }
    }

    private ArrayList<Course> getSortedExamCourses() {
        ArrayList<Course> examCourses = new ArrayList<>();

        for (Course course : repository.getCourses()) {
            long days = CourseRepository.daysUntil(course.getExamDate());

            if (days != Long.MAX_VALUE && days >= 0) {
                examCourses.add(course);
            }
        }

        Collections.sort(examCourses, Comparator.comparingLong(
                course -> CourseRepository.daysUntil(course.getExamDate())
        ));

        return examCourses;
    }

    private void confirmDeleteCourse(Course course) {
        new AlertDialog.Builder(this)
                .setTitle("Kurs löschen")
                .setMessage("Möchtest du den Kurs \"" + course.getName() + "\" wirklich löschen?")
                .setPositiveButton("Löschen", (dialog, which) -> {
                    repository.deleteCourse(course.getId());
                    Toast.makeText(this, "Kurs gelöscht.", Toast.LENGTH_SHORT).show();
                    showCourseList();
                })
                .setNegativeButton("Abbrechen", null)
                .show();
    }

    private void selectDocument(String courseId) {
        documentCourseId = courseId;

        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);

        startActivityForResult(intent, REQUEST_DOCUMENT);
    }

    private void openDocument(String uriString) {
        try {
            Uri uri = Uri.parse(uriString);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(uri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Dokument konnte nicht geöffnet werden.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_DOCUMENT && resultCode == Activity.RESULT_OK && data != null) {
            Uri uri = data.getData();

            if (uri != null) {
                try {
                    getContentResolver().takePersistableUriPermission(
                            uri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                    );
                } catch (Exception ignored) {
                }

                Course course = repository.getCourseById(documentCourseId);

                if (course != null) {
                    course.setDocumentUri(uri.toString());
                    repository.saveCourses();
                    Toast.makeText(this, "Dokument wurde zugeordnet.", Toast.LENGTH_SHORT).show();
                    showCourseDetails(course.getId());
                }
            }
        }
    }

    private void showMenuDialog() {
        String[] items = {
                "Dashboard",
                "Kursübersicht",
                "Klausurübersicht",
                "Neuer Kurs"
        };

        new AlertDialog.Builder(this)
                .setTitle("Navigation")
                .setItems(items, (dialog, which) -> {
                    if (which == 0) showDashboard();
                    if (which == 1) showCourseList();
                    if (which == 2) showExamOverview();
                    if (which == 3) showCourseForm(null);
                })
                .show();
    }

    private String displayExamText(Course course) {
        if (course.getExamDate().isEmpty()) {
            return "Kein Klausurdatum eingetragen";
        }

        long days = CourseRepository.daysUntil(course.getExamDate());

        if (days == Long.MAX_VALUE) {
            return "Ungültiges Klausurdatum";
        }

        if (days < 0) {
            return course.getExamDate() + " - bereits vergangen";
        }

        if (days == 0) {
            return course.getExamDate() + " - heute";
        }

        return course.getExamDate() + " - noch " + days + " Tage";
    }

    private Course findCourseForAssignment(Assignment targetAssignment) {
        for (Course course : repository.getCourses()) {
            for (Assignment assignment : course.getAssignments()) {
                if (assignment == targetAssignment) {
                    return course;
                }

                if (assignment.getId().equals(targetAssignment.getId())) {
                    return course;
                }
            }
        }

        return null;
    }

    private int parseProgress(String input) {
        try {
            return Integer.parseInt(input.trim());
        } catch (Exception e) {
            return 0;
        }
    }

    private EditText makeInput(String hint) {
        EditText input = new EditText(this);
        input.setHint(hint);
        input.setTextColor(getTextColor());
        input.setHintTextColor(darkMode ? Color.LTGRAY : Color.DKGRAY);
        input.setSingleLine(false);
        input.setPadding(16, 12, 16, 12);
        return input;
    }

    private TextView makeText(String text, int size, boolean bold) {
        TextView view = new TextView(this);
        view.setText(text);
        view.setTextSize(size);
        view.setTextColor(getTextColor());
        view.setPadding(0, 6, 0, 6);

        if (bold) {
            view.setTypeface(null, android.graphics.Typeface.BOLD);
        }

        return view;
    }

    private void addTitle(String text) {
        TextView title = makeText(text, 26, true);
        content.addView(title);
        addSpace(12);
    }

    private void addSubtitle(String text) {
        TextView subtitle = makeText(text, 20, true);
        content.addView(subtitle);
        addSpace(6);
    }

    private void addSpace(int height) {
        View space = new View(this);
        content.addView(space, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                height
        ));
    }

    private LinearLayout makeCard(int color) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(24, 20, 24, 20);
        card.setBackgroundColor(color);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, 12);
        card.setLayoutParams(params);

        return card;
    }

    private void addSmallCard(String title, String body, int color) {
        LinearLayout card = makeCard(color);

        TextView titleView = makeText(title, 18, true);
        titleView.setTextColor(Color.BLACK);

        TextView bodyView = makeText(body, 15, false);
        bodyView.setTextColor(Color.BLACK);

        card.addView(titleView);
        card.addView(bodyView);

        content.addView(card);
    }

    private int getBackgroundColor() {
        return darkMode ? 0xFF121212 : 0xFFF5F5F5;
    }

    private int getCardColor() {
        return darkMode ? 0xFF1E1E1E : Color.WHITE;
    }

    private int getNeutralCardColor() {
        return darkMode ? 0xFF424242 : 0xFFECEFF1;
    }

    private int getTextColor() {
        return darkMode ? Color.WHITE : Color.BLACK;
    }

    private String safeOrDash(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "-";
        }
        return value;
    }
}

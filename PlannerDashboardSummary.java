package com.example.uniplanner;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class PlannerDashboardSummary {

    private final int courseCount;
    private final int openAssignmentCount;
    private final int averageProgress;
    private final Course nextExamCourse;
    private final ArrayList<Assignment> upcomingAssignments;

    private PlannerDashboardSummary(
            int courseCount,
            int openAssignmentCount,
            int averageProgress,
            Course nextExamCourse,
            ArrayList<Assignment> upcomingAssignments
    ) {
        this.courseCount = courseCount;
        this.openAssignmentCount = openAssignmentCount;
        this.averageProgress = averageProgress;
        this.nextExamCourse = nextExamCourse;
        this.upcomingAssignments = upcomingAssignments;
    }

    public static PlannerDashboardSummary fromCourses(List<Course> courses) {
        if (courses == null || courses.isEmpty()) {
            return new PlannerDashboardSummary(
                    0,
                    0,
                    0,
                    null,
                    new ArrayList<>()
            );
        }

        int courseCount = courses.size();
        int totalProgress = 0;
        int openAssignmentCount = 0;

        Course nextExamCourse = null;
        LocalDate nextExamDate = null;

        ArrayList<Assignment> upcomingAssignments = new ArrayList<>();

        for (Course course : courses) {
            totalProgress += course.getProgress();

            LocalDate examDate = parseDateOrNull(course.getExamDate());

            if (examDate != null) {
                if (nextExamDate == null || examDate.isBefore(nextExamDate)) {
                    nextExamDate = examDate;
                    nextExamCourse = course;
                }
            }

            for (Assignment assignment : course.getAssignments()) {
                if (!assignment.isCompleted()) {
                    openAssignmentCount++;
                    upcomingAssignments.add(assignment);
                }
            }
        }

        upcomingAssignments.sort(new Comparator<Assignment>() {
            @Override
            public int compare(Assignment first, Assignment second) {
                LocalDate firstDate = parseDateOrNull(first.getDeadline());
                LocalDate secondDate = parseDateOrNull(second.getDeadline());

                if (firstDate == null && secondDate == null) {
                    return 0;
                }

                if (firstDate == null) {
                    return 1;
                }

                if (secondDate == null) {
                    return -1;
                }

                return firstDate.compareTo(secondDate);
            }
        });

        int averageProgress = totalProgress / courseCount;

        return new PlannerDashboardSummary(
                courseCount,
                openAssignmentCount,
                averageProgress,
                nextExamCourse,
                upcomingAssignments
        );
    }

    private static LocalDate parseDateOrNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        try {
            return LocalDate.parse(value);
        } catch (Exception e) {
            return null;
        }
    }

    public int getCourseCount() {
        return courseCount;
    }

    public int getOpenAssignmentCount() {
        return openAssignmentCount;
    }

    public int getAverageProgress() {
        return averageProgress;
    }

    public Course getNextExamCourse() {
        return nextExamCourse;
    }

    public ArrayList<Assignment> getUpcomingAssignments() {
        return upcomingAssignments;
    }
}
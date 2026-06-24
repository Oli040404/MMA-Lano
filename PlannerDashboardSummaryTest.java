package com.example.uniplanner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.junit.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;

public class PlannerDashboardSummaryTest {

    @Test
    public void summaryCalculatesDashboardValuesAndSortsOpenAssignments() {
        LocalDate today = LocalDate.now();

        Assignment late = new Assignment(
                "Spätere Abgabe",
                "",
                today.plusDays(20).toString()
        );

        Assignment done = new Assignment(
                "Erledigte Abgabe",
                "",
                today.plusDays(2).toString()
        );
        done.setCompleted(true);

        Assignment early = new Assignment(
                "Frühere Abgabe",
                "",
                today.plusDays(5).toString()
        );

        Course first = new Course("Mobile App Development");
        first.setProgress(60);
        first.setExamDate(today.plusDays(40).toString());
        first.getAssignments().add(late);
        first.getAssignments().add(done);

        Course second = new Course("Datenbanken");
        second.setProgress(20);
        second.setExamDate(today.plusDays(30).toString());
        second.getAssignments().add(early);

        PlannerDashboardSummary summary = PlannerDashboardSummary.fromCourses(
                Arrays.asList(first, second)
        );

        assertEquals(2, summary.getCourseCount());
        assertEquals(2, summary.getOpenAssignmentCount());
        assertEquals(40, summary.getAverageProgress());
        assertSame(second, summary.getNextExamCourse());
        assertSame(early, summary.getUpcomingAssignments().get(0));
        assertSame(late, summary.getUpcomingAssignments().get(1));
    }

    @Test
    public void centralMvpWorkflowAddsAndCompletesAssignment() {
        Course course = new Course("Software Engineering");
        course.setSemester("SS 2026");
        course.setProgress(10);

        Assignment assignment = new Assignment(
                "Alpha-Prototyp dokumentieren",
                "Progress Report ergänzen",
                LocalDate.now().plusDays(7).toString()
        );

        course.getAssignments().add(assignment);

        PlannerDashboardSummary beforeCompletion = PlannerDashboardSummary.fromCourses(
                Arrays.asList(course)
        );

        assignment.setCompleted(true);

        PlannerDashboardSummary afterCompletion = PlannerDashboardSummary.fromCourses(
                Arrays.asList(course)
        );

        assertEquals(1, beforeCompletion.getOpenAssignmentCount());
        assertEquals(0, afterCompletion.getOpenAssignmentCount());
        assertEquals("Erledigt", assignment.statusText());
    }
}

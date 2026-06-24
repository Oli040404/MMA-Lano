package com.example.uniplanner;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CourseTest {

    @Test
    public void progressBelowZeroIsSetToZero() {
        Course course = new Course("Android");
        course.setProgress(-10);

        assertEquals(0, course.getProgress());
    }

    @Test
    public void progressAboveHundredIsSetToHundred() {
        Course course = new Course("Android");
        course.setProgress(150);

        assertEquals(100, course.getProgress());
    }

    @Test
    public void progressInsideRangeIsSavedCorrectly() {
        Course course = new Course("Android");
        course.setProgress(65);

        assertEquals(65, course.getProgress());
    }

    @Test
    public void assignmentCanBeAddedToCourse() {
        Course course = new Course("Android");
        Assignment assignment = new Assignment("Assignment 4", "Beta fertigstellen", "2026-06-21");

        course.addAssignment(assignment);

        assertEquals(1, course.getAssignments().size());
        assertEquals("Assignment 4", course.getAssignments().get(0).getTitle());
    }
}

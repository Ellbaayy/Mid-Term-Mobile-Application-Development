package com.example.midtermapp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Simple unit tests for the student model.
 */
public class StudentTest {

    @Test
    public void modelKeepsEveryAttribute() {
        Student student = new Student(1L, "Budi Santoso", "2025001", true);

        assertEquals(1L, student.getId());
        assertEquals("Budi Santoso", student.getName());
        assertEquals("2025001", student.getStudentId());
        assertTrue(student.isEnrolled());
    }

    @Test
    public void notEnrolledStatusIsReadCorrectly() {
        Student student = new Student(2L, "Siti Aminah", "2025002", false);

        assertFalse(student.isEnrolled());
    }
}
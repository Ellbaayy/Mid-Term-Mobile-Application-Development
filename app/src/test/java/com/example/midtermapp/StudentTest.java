package com.example.midtermapp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Simple unit tests for the student model.
 */
public class StudentTest {

    @Test
    public void modelKeepsEveryAttribute() {
        Student student = new Student("doc-1", "Budi Santoso", "2025001", true);

        assertEquals("doc-1", student.getDocumentId());
        assertEquals("Budi Santoso", student.getName());
        assertEquals("2025001", student.getStudentId());
        assertTrue(student.isEnrolled());
    }

    @Test
    public void notEnrolledStatusIsReadCorrectly() {
        Student student = new Student("doc-2", "Siti Aminah", "2025002", false);

        assertFalse(student.isEnrolled());
    }

    @Test
    public void emptyConstructorLeavesDocumentIdUnset() {
        Student student = new Student();

        assertNull(student.getDocumentId());
        assertNull(student.getName());
        assertFalse(student.isEnrolled());
    }

    @Test
    public void settersReplaceStoredValues() {
        Student student = new Student();
        student.setDocumentId("doc-3");
        student.setName("Rina Wijaya");
        student.setStudentId("2025003");
        student.setEnrolled(true);

        assertEquals("doc-3", student.getDocumentId());
        assertEquals("Rina Wijaya", student.getName());
        assertEquals("2025003", student.getStudentId());
        assertTrue(student.isEnrolled());
    }
}

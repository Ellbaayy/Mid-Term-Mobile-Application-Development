package com.example.midtermapp;

import com.google.firebase.firestore.Exclude;

/**
 * Student data model for case 2: name, student ID, and enrollment status.
 *
 * Each instance maps to one document in the Firestore "Students" collection.
 * The document ID stays outside the stored fields, so a student ID can be
 * edited without moving the record to a different document.
 */
public class Student {

    @Exclude
    private String documentId;

    private String name;
    private String studentId;
    private boolean enrolled;

    /** Empty constructor required by Firestore when reading documents. */
    public Student() {
    }

    public Student(String documentId, String name, String studentId, boolean enrolled) {
        this.documentId = documentId;
        this.name = name;
        this.studentId = studentId;
        this.enrolled = enrolled;
    }

    @Exclude
    public String getDocumentId() {
        return documentId;
    }

    @Exclude
    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public boolean isEnrolled() {
        return enrolled;
    }

    public void setEnrolled(boolean enrolled) {
        this.enrolled = enrolled;
    }
}

package com.example.midtermapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * SQLite helper that owns the student table.
 *
 * Table students:
 * - _id        : auto increment primary key
 * - name       : student name (required)
 * - student_id : student ID (required, unique)
 * - enrolled   : enrollment flag (1 = enrolled, 0 = not enrolled)
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "student_data.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_STUDENTS = "students";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_STUDENT_ID = "student_id";
    public static final String COLUMN_ENROLLED = "enrolled";

    private static final String CREATE_TABLE_STUDENTS =
            "CREATE TABLE " + TABLE_STUDENTS + " ("
                    + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + COLUMN_NAME + " TEXT NOT NULL, "
                    + COLUMN_STUDENT_ID + " TEXT NOT NULL UNIQUE, "
                    + COLUMN_ENROLLED + " INTEGER NOT NULL DEFAULT 0"
                    + ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_STUDENTS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_STUDENTS);
        onCreate(db);
    }

    /** Inserts one student record. Returns the new row id, or -1 when the insert fails. */
    public long insertStudent(String name, String studentId, boolean enrolled) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_STUDENT_ID, studentId);
        values.put(COLUMN_ENROLLED, enrolled ? 1 : 0);
        return db.insert(TABLE_STUDENTS, null, values);
    }

    /** Updates one student record by primary key. Returns the number of affected rows. */
    public int updateStudent(long id, String name, String studentId, boolean enrolled) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_STUDENT_ID, studentId);
        values.put(COLUMN_ENROLLED, enrolled ? 1 : 0);
        return db.update(TABLE_STUDENTS, values, COLUMN_ID + " = ?",
                new String[]{String.valueOf(id)});
    }

    /** Deletes one student record by primary key. Returns the number of affected rows. */
    public int deleteStudent(long id) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(TABLE_STUDENTS, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
    }

    /** Returns every student record, ordered by name. */
    @NonNull
    public List<Student> getAllStudents() {
        List<Student> students = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor cursor = db.query(
                TABLE_STUDENTS,
                new String[]{COLUMN_ID, COLUMN_NAME, COLUMN_STUDENT_ID, COLUMN_ENROLLED},
                null, null, null, null,
                COLUMN_NAME + " COLLATE NOCASE ASC, " + COLUMN_ID + " ASC")) {

            int idIndex = cursor.getColumnIndexOrThrow(COLUMN_ID);
            int nameIndex = cursor.getColumnIndexOrThrow(COLUMN_NAME);
            int studentIdIndex = cursor.getColumnIndexOrThrow(COLUMN_STUDENT_ID);
            int enrolledIndex = cursor.getColumnIndexOrThrow(COLUMN_ENROLLED);

            while (cursor.moveToNext()) {
                students.add(new Student(
                        cursor.getLong(idIndex),
                        cursor.getString(nameIndex),
                        cursor.getString(studentIdIndex),
                        cursor.getInt(enrolledIndex) == 1));
            }
        }
        return students;
    }

    /** Checks whether a student ID is already used, optionally ignoring one record. */
    public boolean isStudentIdUsed(String studentId, long excludedId) {
        SQLiteDatabase db = getReadableDatabase();
        String selection = COLUMN_STUDENT_ID + " = ? COLLATE NOCASE";
        List<String> args = new ArrayList<>();
        args.add(studentId);
        if (excludedId > 0) {
            selection += " AND " + COLUMN_ID + " <> ?";
            args.add(String.valueOf(excludedId));
        }
        try (Cursor cursor = db.query(TABLE_STUDENTS, new String[]{COLUMN_ID},
                selection, args.toArray(new String[0]), null, null, null)) {
            return cursor.moveToFirst();
        }
    }

    /** Number of records with the given enrollment status. */
    public int countByEnrolled(boolean enrolled) {
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM " + TABLE_STUDENTS + " WHERE " + COLUMN_ENROLLED + " = ?",
                new String[]{enrolled ? "1" : "0"})) {
            return cursor.moveToFirst() ? cursor.getInt(0) : 0;
        }
    }
}
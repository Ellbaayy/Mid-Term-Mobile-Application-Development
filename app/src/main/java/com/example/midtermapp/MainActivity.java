package com.example.midtermapp;

import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.List;

/**
 * Main screen of the student data application.
 * Provides add, edit, delete, and view operations backed by an SQLite database.
 */
public class MainActivity extends AppCompatActivity implements StudentAdapter.OnStudentClickListener {

    private static final long NO_SELECTION = -1L;

    private DatabaseHelper databaseHelper;
    private StudentAdapter adapter;

    private TextInputLayout nameLayout;
    private TextInputLayout studentIdLayout;
    private TextInputEditText nameInput;
    private TextInputEditText studentIdInput;
    private MaterialCheckBox enrolledCheckBox;
    private MaterialButton editButton;
    private MaterialButton deleteButton;
    private TextView formModeText;
    private TextView summaryText;
    private TextView emptyText;

    private long selectedId = NO_SELECTION;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        databaseHelper = new DatabaseHelper(this);

        nameLayout = findViewById(R.id.til_name);
        studentIdLayout = findViewById(R.id.til_student_id);
        nameInput = findViewById(R.id.et_name);
        studentIdInput = findViewById(R.id.et_student_id);
        enrolledCheckBox = findViewById(R.id.cb_enrolled);
        formModeText = findViewById(R.id.tv_form_mode);
        summaryText = findViewById(R.id.tv_summary);
        emptyText = findViewById(R.id.tv_empty);

        RecyclerView recyclerView = findViewById(R.id.recycler_students);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new StudentAdapter(this);
        recyclerView.setAdapter(adapter);

        MaterialButton addButton = findViewById(R.id.button_add);
        editButton = findViewById(R.id.button_edit);
        deleteButton = findViewById(R.id.button_delete);
        MaterialButton viewButton = findViewById(R.id.button_view);

        addButton.setOnClickListener(v -> addStudent());
        editButton.setOnClickListener(v -> updateStudent());
        deleteButton.setOnClickListener(v -> confirmDeleteStudent());
        viewButton.setOnClickListener(v -> viewStudents());

        clearForm();
        loadStudents();
    }

    @Override
    protected void onDestroy() {
        databaseHelper.close();
        super.onDestroy();
    }

    /** Saves a new record from the form. */
    private void addStudent() {
        if (!validateForm(NO_SELECTION)) {
            return;
        }
        String name = getText(nameInput);
        String studentId = getText(studentIdInput);
        boolean enrolled = enrolledCheckBox.isChecked();

        long newId = databaseHelper.insertStudent(name, studentId, enrolled);
        if (newId > 0) {
            Toast.makeText(this, R.string.msg_added, Toast.LENGTH_SHORT).show();
            hideKeyboard();
            clearForm();
            loadStudents();
        } else {
            Toast.makeText(this, R.string.msg_add_failed, Toast.LENGTH_SHORT).show();
        }
    }

    /** Updates the record currently selected in the list. */
    private void updateStudent() {
        if (selectedId == NO_SELECTION) {
            Toast.makeText(this, R.string.error_no_selection, Toast.LENGTH_SHORT).show();
            return;
        }
        if (!validateForm(selectedId)) {
            return;
        }
        String name = getText(nameInput);
        String studentId = getText(studentIdInput);
        boolean enrolled = enrolledCheckBox.isChecked();

        int rows = databaseHelper.updateStudent(selectedId, name, studentId, enrolled);
        if (rows > 0) {
            Toast.makeText(this, R.string.msg_updated, Toast.LENGTH_SHORT).show();
            hideKeyboard();
            clearForm();
            loadStudents();
        } else {
            Toast.makeText(this, R.string.msg_update_failed, Toast.LENGTH_SHORT).show();
        }
    }

    /** Asks for confirmation before deleting the selected record. */
    private void confirmDeleteStudent() {
        if (selectedId == NO_SELECTION) {
            Toast.makeText(this, R.string.error_no_selection, Toast.LENGTH_SHORT).show();
            return;
        }
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.dialog_delete_title)
                .setMessage(R.string.msg_delete_confirmation)
                .setNegativeButton(R.string.dialog_no, null)
                .setPositiveButton(R.string.dialog_yes, (dialog, which) -> deleteStudent())
                .show();
    }

    /** Deletes the selected record. */
    private void deleteStudent() {
        int rows = databaseHelper.deleteStudent(selectedId);
        if (rows > 0) {
            Toast.makeText(this, R.string.msg_deleted, Toast.LENGTH_SHORT).show();
            clearForm();
            loadStudents();
        }
    }

    /** Reloads every record from the database. */
    private void viewStudents() {
        loadStudents();
        int total = adapter.getItemCount();
        if (total == 0) {
            Toast.makeText(this, R.string.msg_view_empty, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, getString(R.string.format_view_result, total),
                    Toast.LENGTH_SHORT).show();
        }
    }

    /** Loads the student list and refreshes the summary counters. */
    private void loadStudents() {
        List<Student> students = databaseHelper.getAllStudents();
        adapter.submitList(students);
        adapter.setSelectedId(selectedId);

        if (students.isEmpty()) {
            summaryText.setText(R.string.summary_empty);
        } else {
            int enrolled = databaseHelper.countByEnrolled(true);
            int notEnrolled = students.size() - enrolled;
            summaryText.setText(getString(R.string.format_summary,
                    students.size(), enrolled, notEnrolled));
        }
        emptyText.setVisibility(students.isEmpty() ? View.VISIBLE : View.GONE);
    }

    /** Validates the form; excludedId keeps the selected record from being flagged as duplicate. */
    private boolean validateForm(long excludedId) {
        nameLayout.setError(null);
        studentIdLayout.setError(null);

        String name = getText(nameInput);
        String studentId = getText(studentIdInput);
        boolean valid = true;

        if (name.isEmpty()) {
            nameLayout.setError(getString(R.string.error_name_required));
            valid = false;
        }
        if (studentId.isEmpty()) {
            studentIdLayout.setError(getString(R.string.error_id_required));
            valid = false;
        } else if (databaseHelper.isStudentIdUsed(studentId, excludedId)) {
            studentIdLayout.setError(getString(R.string.error_id_duplicate));
            valid = false;
        }
        return valid;
    }

    /** Clears the form and leaves edit mode. */
    private void clearForm() {
        nameInput.setText(null);
        studentIdInput.setText(null);
        enrolledCheckBox.setChecked(false);
        nameLayout.setError(null);
        studentIdLayout.setError(null);
        selectedId = NO_SELECTION;
        formModeText.setText(R.string.label_form_mode_new);
        adapter.setSelectedId(NO_SELECTION);
        updateActionState();
    }

    /** Edit and Delete stay disabled until the user selects a record. */
    private void updateActionState() {
        boolean hasSelection = selectedId != NO_SELECTION;
        editButton.setEnabled(hasSelection);
        deleteButton.setEnabled(hasSelection);
    }

    private String getText(TextInputEditText input) {
        return input.getText() == null ? "" : input.getText().toString().trim();
    }

    private void hideKeyboard() {
        View focus = getCurrentFocus();
        if (focus != null) {
            InputMethodManager manager =
                    (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (manager != null) {
                manager.hideSoftInputFromWindow(focus.getWindowToken(), 0);
            }
            focus.clearFocus();
        }
    }

    @Override
    public void onStudentClick(@NonNull Student student) {
        selectedId = student.getId();
        nameInput.setText(student.getName());
        studentIdInput.setText(student.getStudentId());
        enrolledCheckBox.setChecked(student.isEnrolled());
        nameLayout.setError(null);
        studentIdLayout.setError(null);
        formModeText.setText(R.string.label_form_mode_edit);
        adapter.setSelectedId(selectedId);
        updateActionState();
    }
}
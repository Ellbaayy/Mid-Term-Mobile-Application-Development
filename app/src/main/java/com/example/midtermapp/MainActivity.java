package com.example.midtermapp;

import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Collections;
import java.util.List;

/**
 * Main screen of the student data application.
 * Provides add, edit, delete, and view operations backed by Cloud Firestore.
 */
public class MainActivity extends AppCompatActivity implements StudentAdapter.OnStudentClickListener {

    private StudentRepository repository;
    private StudentAdapter adapter;

    private TextInputLayout nameLayout;
    private TextInputLayout studentIdLayout;
    private TextInputEditText nameInput;
    private TextInputEditText studentIdInput;
    private MaterialCheckBox enrolledCheckBox;
    private MaterialButton addButton;
    private MaterialButton editButton;
    private MaterialButton deleteButton;
    private MaterialButton viewButton;
    private TextView formModeText;
    private TextView summaryText;
    private TextView emptyText;

    /** Document ID of the record selected in the list, or null when nothing is selected. */
    @Nullable
    private String selectedDocumentId;

    /** True while a Firestore request is in flight, so the buttons cannot be tapped twice. */
    private boolean busy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        repository = new StudentRepository();

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

        addButton = findViewById(R.id.button_add);
        editButton = findViewById(R.id.button_edit);
        deleteButton = findViewById(R.id.button_delete);
        viewButton = findViewById(R.id.button_view);

        addButton.setOnClickListener(v -> addStudent());
        editButton.setOnClickListener(v -> updateStudent());
        deleteButton.setOnClickListener(v -> confirmDeleteStudent());
        viewButton.setOnClickListener(v -> refreshStudents(true));

        clearForm();
        refreshStudents(false);
    }

    /** Saves a new document built from the form. */
    private void addStudent() {
        if (!validateForm()) {
            return;
        }
        final String name = getText(nameInput);
        final String studentId = getText(studentIdInput);
        final boolean enrolled = enrolledCheckBox.isChecked();

        setBusy(true);
        repository.isStudentIdUsed(studentId, null, (used, error) -> {
            if (error != null) {
                setBusy(false);
                showToast(R.string.msg_firestore_failed);
                return;
            }
            if (Boolean.TRUE.equals(used)) {
                setBusy(false);
                studentIdLayout.setError(getString(R.string.error_id_duplicate));
                return;
            }
            repository.addStudent(name, studentId, enrolled, (documentId, addError) -> {
                setBusy(false);
                if (addError != null) {
                    showToast(R.string.msg_add_failed);
                    return;
                }
                showToast(R.string.msg_added);
                hideKeyboard();
                clearForm();
                refreshStudents(false);
            });
        });
    }

    /** Writes the form values to the selected document. */
    private void updateStudent() {
        final String documentId = selectedDocumentId;
        if (documentId == null) {
            showToast(R.string.error_no_selection);
            return;
        }
        if (!validateForm()) {
            return;
        }
        final String name = getText(nameInput);
        final String studentId = getText(studentIdInput);
        final boolean enrolled = enrolledCheckBox.isChecked();

        setBusy(true);
        repository.isStudentIdUsed(studentId, documentId, (used, error) -> {
            if (error != null) {
                setBusy(false);
                showToast(R.string.msg_firestore_failed);
                return;
            }
            if (Boolean.TRUE.equals(used)) {
                setBusy(false);
                studentIdLayout.setError(getString(R.string.error_id_duplicate));
                return;
            }
            repository.updateStudent(documentId, name, studentId, enrolled, (ignored, updateError) -> {
                setBusy(false);
                if (updateError != null) {
                    showToast(R.string.msg_update_failed);
                    return;
                }
                showToast(R.string.msg_updated);
                hideKeyboard();
                clearForm();
                refreshStudents(false);
            });
        });
    }

    /** Asks for confirmation before deleting the selected document. */
    private void confirmDeleteStudent() {
        if (selectedDocumentId == null) {
            showToast(R.string.error_no_selection);
            return;
        }
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.dialog_delete_title)
                .setMessage(R.string.msg_delete_confirmation)
                .setNegativeButton(R.string.dialog_no, null)
                .setPositiveButton(R.string.dialog_yes, (dialog, which) -> deleteStudent())
                .show();
    }

    /** Deletes the selected document. */
    private void deleteStudent() {
        final String documentId = selectedDocumentId;
        if (documentId == null) {
            return;
        }
        setBusy(true);
        repository.deleteStudent(documentId, (ignored, error) -> {
            setBusy(false);
            if (error != null) {
                showToast(R.string.msg_delete_failed);
                return;
            }
            showToast(R.string.msg_deleted);
            clearForm();
            refreshStudents(false);
        });
    }

    /**
     * Reads the Students collection once and refreshes the list and the counters.
     *
     * @param announce when true, a toast tells how many records were read (View button)
     */
    private void refreshStudents(boolean announce) {
        setBusy(true);
        repository.fetchStudents((students, error) -> {
            setBusy(false);
            if (isFinishing() || isDestroyed()) {
                return;
            }
            if (error != null) {
                showToast(R.string.msg_load_failed);
                return;
            }
            List<Student> records = students == null ? Collections.emptyList() : students;
            adapter.submitList(records);
            adapter.setSelectedDocumentId(selectedDocumentId);
            showSummary(records);

            if (announce) {
                if (records.isEmpty()) {
                    showToast(R.string.msg_view_empty);
                } else {
                    Toast.makeText(this, getString(R.string.format_view_result, records.size()),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /** Refreshes the summary counters and the empty-state label. */
    private void showSummary(@NonNull List<Student> students) {
        if (students.isEmpty()) {
            summaryText.setText(R.string.summary_empty);
        } else {
            int enrolled = 0;
            for (Student student : students) {
                if (student.isEnrolled()) {
                    enrolled++;
                }
            }
            int notEnrolled = students.size() - enrolled;
            summaryText.setText(getString(R.string.format_summary,
                    students.size(), enrolled, notEnrolled));
        }
        emptyText.setVisibility(students.isEmpty() ? View.VISIBLE : View.GONE);
    }

    /** Checks the required fields; the duplicate check runs against Firestore afterwards. */
    private boolean validateForm() {
        nameLayout.setError(null);
        studentIdLayout.setError(null);

        boolean valid = true;
        if (getText(nameInput).isEmpty()) {
            nameLayout.setError(getString(R.string.error_name_required));
            valid = false;
        }
        if (getText(studentIdInput).isEmpty()) {
            studentIdLayout.setError(getString(R.string.error_id_required));
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
        selectedDocumentId = null;
        formModeText.setText(R.string.label_form_mode_new);
        if (adapter != null) {
            adapter.setSelectedDocumentId(null);
        }
        updateActionState();
    }

    /** Edit and Delete stay disabled until a record is selected and no request is running. */
    private void updateActionState() {
        boolean hasSelection = selectedDocumentId != null;
        addButton.setEnabled(!busy);
        viewButton.setEnabled(!busy);
        editButton.setEnabled(!busy && hasSelection);
        deleteButton.setEnabled(!busy && hasSelection);
    }

    private void setBusy(boolean busy) {
        this.busy = busy;
        updateActionState();
    }

    private String getText(TextInputEditText input) {
        return input.getText() == null ? "" : input.getText().toString().trim();
    }

    private void showToast(int messageRes) {
        Toast.makeText(this, messageRes, Toast.LENGTH_SHORT).show();
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
        selectedDocumentId = student.getDocumentId();
        nameInput.setText(student.getName());
        studentIdInput.setText(student.getStudentId());
        enrolledCheckBox.setChecked(student.isEnrolled());
        nameLayout.setError(null);
        studentIdLayout.setError(null);
        formModeText.setText(R.string.label_form_mode_edit);
        adapter.setSelectedDocumentId(selectedDocumentId);
        updateActionState();
    }
}

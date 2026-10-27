package com.example.midtermapp;

import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView adapter that renders the student list.
 */
public class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.StudentViewHolder> {

    public interface OnStudentClickListener {
        void onStudentClick(@NonNull Student student);
    }

    private final List<Student> students = new ArrayList<>();
    private final OnStudentClickListener listener;
    private long selectedId = -1L;

    public StudentAdapter(@NonNull OnStudentClickListener listener) {
        this.listener = listener;
    }

    public void submitList(@NonNull List<Student> newStudents) {
        students.clear();
        students.addAll(newStudents);
        notifyDataSetChanged();
    }

    public void setSelectedId(long id) {
        selectedId = id;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public StudentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_student, parent, false);
        return new StudentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StudentViewHolder holder, int position) {
        holder.bind(students.get(position));
    }

    @Override
    public int getItemCount() {
        return students.size();
    }

    class StudentViewHolder extends RecyclerView.ViewHolder {

        private final MaterialCardView card;
        private final TextView nameText;
        private final TextView studentIdText;
        private final TextView statusText;

        StudentViewHolder(@NonNull View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.card_student);
            nameText = itemView.findViewById(R.id.tv_student_name);
            studentIdText = itemView.findViewById(R.id.tv_student_id);
            statusText = itemView.findViewById(R.id.tv_status);
        }

        void bind(@NonNull Student student) {
            nameText.setText(student.getName());
            studentIdText.setText(itemView.getContext()
                    .getString(R.string.format_student_id, student.getStudentId()));

            int textColorRes;
            if (student.isEnrolled()) {
                statusText.setText(R.string.status_enrolled);
                statusText.setBackgroundResource(R.drawable.bg_chip_enrolled);
                textColorRes = R.color.status_enrolled_text;
            } else {
                statusText.setText(R.string.status_not_enrolled);
                statusText.setBackgroundResource(R.drawable.bg_chip_not_enrolled);
                textColorRes = R.color.status_not_enrolled_text;
            }
            statusText.setTextColor(ContextCompat.getColor(itemView.getContext(), textColorRes));

            Resources resources = itemView.getResources();
            int strokeWidth = Math.round(resources.getDisplayMetrics().density
                    * (student.getId() == selectedId ? 2f : 1f));
            card.setStrokeWidth(strokeWidth);
            card.setStrokeColor(ContextCompat.getColor(itemView.getContext(),
                    student.getId() == selectedId ? R.color.purple_primary : R.color.outline));

            card.setOnClickListener(v -> listener.onStudentClick(student));
        }
    }
}
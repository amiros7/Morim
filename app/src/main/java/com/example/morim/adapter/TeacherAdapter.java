package com.example.morim.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.morim.databinding.TeacherItemBinding;
import com.example.morim.model.Teacher;
import com.example.morim.model.User;
import com.squareup.picasso.Picasso;

import java.util.List;

public class TeacherAdapter extends RecyclerView.Adapter<TeacherAdapter.TeacherViewHolder> {

    public interface ScheduleClickListener {
        void onRequestScheduleWithTeacher(Teacher teacher);
    }

    public interface TeacherAdapterListener extends ScheduleClickListener {
        void onViewTeacher(Teacher t);
    }

    private List<Teacher> teachers;
    private final ScheduleClickListener listener;

    public TeacherAdapter(List<Teacher> teachers, ScheduleClickListener listener) {
        this.teachers = teachers;
        this.listener = listener;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setTeacherData(List<Teacher> teachers) {
        this.teachers = teachers;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TeacherViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        TeacherItemBinding binding = TeacherItemBinding.inflate(LayoutInflater.from(parent.getContext()));
        return new TeacherViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull TeacherViewHolder holder, int position) {
        Teacher teacher = teachers.get(position);
        holder.bind(teacher, listener);
    }

    @Override
    public int getItemCount() {
        return teachers.size();
    }

    public static class TeacherViewHolder extends RecyclerView.ViewHolder {
        private final TeacherItemBinding binding;

        public TeacherViewHolder(TeacherItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        @SuppressLint("DefaultLocale")
        public void bind(Teacher teacher, ScheduleClickListener listener) {
            if (!teacher.getImage().isEmpty())
                Picasso.get()
                        .load(teacher.getImage())
                        .into(binding.ivTeacherItem);
            else
                Picasso.get()
                        .load(User.DEFAULT_IMAGE)
                        .into(binding.ivTeacherItem);


            if (listener instanceof TeacherAdapterListener) {
                binding.viewProfile.setVisibility(View.VISIBLE);
                binding.viewProfile.setOnClickListener((v) -> {
                    ((TeacherAdapterListener) listener).onViewTeacher(teacher);
                });
            } else {
                binding.viewProfile.setVisibility(View.GONE);
            }

            binding.btnSchedule.setText(String.format("Schedule with %s", teacher.getFullName()));

            binding.tvPrice.setText(String.format("Hourly Fee: %.1f$", teacher.getPrice()));
            binding.rbTeacherItem.setRating((float) teacher.getAverageRating());
            binding.tvTeacherItemName.setText(teacher.getFullName());
            binding.tvTeacherItemSubjects.setText(String.join(",", teacher.getTeachingSubjects()));
            binding.tvTeacherItemEducation.setText(teacher.getEducation());
            binding.btnSchedule.setOnClickListener(view -> listener.onRequestScheduleWithTeacher(teacher));
        }
    }
}

package com.example.morim.ui.main;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.morim.adapter.TeacherAdapter;
import com.example.morim.databinding.FragmentTeacherBinding;
import com.example.morim.model.Teacher;
import com.example.morim.model.User;
import com.example.morim.ui.BaseFragment;
import com.example.morim.viewmodel.MainViewModel;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class TeacherFragment extends BaseFragment implements TeacherAdapter.ScheduleClickListener {


    private FragmentTeacherBinding binding;

    private MainViewModel mainViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentTeacherBinding.inflate(inflater, container, false);
        mainViewModel = activityScopedViewModel(MainViewModel.class);
        return binding.getRoot();
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getArguments() != null) {
            String teacherStr = TeacherFragmentArgs.fromBundle(getArguments()).getTeacher();
            Gson g = new Gson();
            binding.backBtn.setOnClickListener(v -> findNavController().popBackStack());
            Teacher t = g.fromJson(teacherStr, Teacher.class);
            binding.tvPrice.setText(String.format("Hourly Fee: %.1f$", t.getPrice()));
            binding.titleTeacher.setText(String.format("%s's Profile", t.getFullName()));
            binding.tvTeacherItemName.setText(t.getFullName());
            binding.btnSchedule.setText(String.format("Schedule with %s", t.getFullName()));
            binding.rbTeacherItem.setRating((float) t.getAverageRating());
            if(!t.getImage().isEmpty()) {
                Picasso.get()
                        .load(t.getImage())
                        .into(binding.ivTeacherItem);
            }
            else {
                Picasso.get()
                        .load(User.DEFAULT_IMAGE)
                        .into(binding.ivTeacherItem);
            }

            binding.btnSchedule.setOnClickListener(v ->
                    onRequestScheduleWithTeacher(t));
            if (t.getId().equals(FirebaseAuth.getInstance().getUid())) {
                binding.rbTeacherItem.setEnabled(false);
                binding.rateThis.setText(String.format("%s", "This is your rating (:"));
            } else if (t.getRatingStudents().contains(FirebaseAuth.getInstance().getUid())) {
                binding.rbTeacherItem.setEnabled(false);
                binding.rateThis.setText(String.format("%s", "You have rated this teacher"));
            } else {
                binding.rbTeacherItem.setOnRatingBarChangeListener((ratingBar, rating, b) -> {
                    mainViewModel.rateTeacher(t, rating);
                    binding.rbTeacherItem.setRating((float) t.getAverageRating());
                    Snackbar.make(binding.getRoot(), "Rated teacher!", Snackbar.LENGTH_LONG).show();
                });
            }

        } else {
            findNavController()
                    .popBackStack();
        }

    }

    @Override
    public void onRequestScheduleWithTeacher(Teacher teacher) {
        String cid = FirebaseAuth.getInstance().getUid();
        if (cid != null && cid.equals(teacher.getId())) {
            Snackbar.make(binding.getRoot(), "Cannot schedule with your self", Snackbar.LENGTH_LONG).show();
            return;
        }
        mainViewModel.showScheduleMeetingDialog(
                requireContext(),
                getChildFragmentManager(),
                teacher
        );

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}

package com.example.morim.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.morim.adapter.TeacherAdapter;
import com.example.morim.database.OnDataCallback;
import com.example.morim.databinding.FragmentHomeBinding;
import com.example.morim.model.Meeting;
import com.example.morim.model.Teacher;
import com.example.morim.model.User;
import com.example.morim.ui.BaseFragment;
import com.example.morim.ui.dialog.ScheduleMeetingDialog;
import com.example.morim.ui.dialog.SubjectDialog;
import com.example.morim.util.DateUtils;
import com.example.morim.util.ScreenUtils;
import com.example.morim.viewmodel.MainViewModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class HomeFragment extends BaseFragment implements TeacherAdapter.TeacherAdapterListener {
    private MainViewModel mainViewModel;
    private FragmentHomeBinding viewBinding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        viewBinding = FragmentHomeBinding.inflate(inflater, container, false);
        mainViewModel = activityScopedViewModel(MainViewModel.class);
        return viewBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewBinding.btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mainViewModel.signOut();
            }
        });

        viewBinding.rvTeachers.setLayoutManager(new LinearLayoutManager(getContext()));


        mainViewModel.getCurrentUser().observe(getViewLifecycleOwner(), new Observer<User>() {
            @Override
            public void onChanged(User user) {
                if (user != null)
                    viewBinding.greetingTv.setText(ScreenUtils.greetUser(user.getFullName()));
            }
        });
        mainViewModel.getTeachers()
                .observe(getViewLifecycleOwner(), teachers -> {
                    TeacherAdapter adapter = new TeacherAdapter(teachers, HomeFragment.this);
                    viewBinding.rvTeachers.setAdapter(adapter);
                });
    }

    @Override
    public void onRequestScheduleWithTeacher(Teacher teacher) {
        String cid = FirebaseAuth.getInstance().getUid();
        if (cid != null && cid.equals(teacher.getId())) {
            Snackbar.make(viewBinding.getRoot(), "Cannot schedule with your self", Snackbar.LENGTH_LONG).show();
            return;
        }

        mainViewModel.showScheduleMeetingDialog(
                requireContext(),
                getChildFragmentManager(),
                teacher
        );

    }

    @Override
    public void onViewTeacher(Teacher t) {
        Gson g = new Gson();
        HomeFragmentDirections.ActionHomeFragmentToTeacherFragment intent = HomeFragmentDirections.actionHomeFragmentToTeacherFragment(g.toJson(t));
        findNavController()
                .navigate(intent);
    }
}


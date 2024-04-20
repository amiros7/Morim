package com.example.morim.ui.main;

import android.annotation.SuppressLint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.morim.MorimApp;
import com.example.morim.adapter.MeetingAdapter;
import com.example.morim.adapter.TeacherAdapter;
import com.example.morim.databinding.FragmentTeacherSearchBinding;
import com.example.morim.model.Teacher;
import com.example.morim.ui.BaseFragment;
import com.example.morim.ui.SubjectSpinner;
import com.example.morim.viewmodel.MainViewModel;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;

import java.util.ArrayList;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class TeacherSearch extends BaseFragment {


    private FragmentTeacherSearchBinding binding;
    private MainViewModel mainViewModel;

    private TeacherAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentTeacherSearchBinding.inflate(inflater, container, false);
        mainViewModel = activityScopedViewModel(MainViewModel.class);
        return binding.getRoot();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Spinner spinner = new SubjectSpinner(getContext(), new SubjectSpinner.OnItemSelected() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i == 0) return;
                String subject = MorimApp.ALL_SUBJECTS.get(i);
                mainViewModel.filterTeachers(subject);
            }
        });
        spinner.setOnTouchListener((v, motionEvent) -> {
            spinner.setSelection(0);
            spinner.performClick();
            return true;
        });
        spinner.setPadding(16, 0, 16, 0);
        binding.subjectSpinnerLayout.addView(spinner);
        TextView selectedSubjectV;
        selectedSubjectV = new TextView(getContext());
        selectedSubjectV.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        selectedSubjectV.setTypeface(null, Typeface.BOLD);
        selectedSubjectV.setPadding(8, 8, 8, 8);
        selectedSubjectV.setTextSize(14);
        selectedSubjectV.setMaxWidth(200);

        binding.subjectSpinnerLayout.addView(selectedSubjectV);
        adapter = new TeacherAdapter(new ArrayList<>(), new TeacherAdapter.TeacherAdapterListener() {
            @Override
            public void onViewTeacher(Teacher t) {
                Gson g = new Gson();
                TeacherSearchDirections.ActionTeacherSearchToTeacherFragment intent = TeacherSearchDirections.actionTeacherSearchToTeacherFragment(g.toJson(t));
                findNavController()
                        .navigate(intent);
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
        });
        binding.rvTeachers.setAdapter(adapter);
        mainViewModel.getTeacherSearchResults()
                .observe(getViewLifecycleOwner(), teachers -> {
                    adapter.setTeacherData(teachers);
                });
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                findNavController().popBackStack();
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}

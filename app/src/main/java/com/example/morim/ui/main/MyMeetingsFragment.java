package com.example.morim.ui.main;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.Observer;

import com.example.morim.adapter.MeetingAdapter;
import com.example.morim.databinding.FragmentMyMeetingsBinding;
import com.example.morim.model.Meeting;
import com.example.morim.model.MyMeetingsData;
import com.example.morim.model.OtherUser;
import com.example.morim.model.User;
import com.example.morim.ui.BaseFragment;
import com.example.morim.util.DateUtils;
import com.example.morim.viewmodel.MainViewModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MyMeetingsFragment extends BaseFragment implements MeetingAdapter.IMeetingActions {

    @Inject
    protected SharedPreferences sp;
    private FragmentMyMeetingsBinding binding;
    private MainViewModel mainViewModel;

    private MeetingAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentMyMeetingsBinding.inflate(inflater, container, false);
        mainViewModel = activityScopedViewModel(MainViewModel.class);
        return binding.getRoot();
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        adapter = new MeetingAdapter(new MyMeetingsData(), this);
        binding.rvMeetings.setAdapter(adapter);
        mainViewModel.getMyMeetingsData()
                .observe(getViewLifecycleOwner(), meetingsData -> {
                    Set<String> alreadyInDb = new HashSet<>(sp.getStringSet("alreadyInDb", new HashSet<>()));
                    if (meetingsData.allResourcesAvailable()) {
                        for (Meeting m : meetingsData.getMyMeetings()) {
                            if (alreadyInDb.contains(m.getId()))
                                m.setTeacherSeen(true);
                            alreadyInDb.add(m.getId());
                        }
                        adapter.setMeetingsData(meetingsData, alreadyInDb);
                        sp.edit()
                                .putStringSet("alreadyInDb", alreadyInDb)
                                .apply();
                    }
                    String upcomingLessons = String.format("New (%d) Upcoming (%d)",
                            meetingsData.getMyMeetings()
                                    .stream().filter(m -> !m.isTeacherSeen())
                                    .count(),
                            meetingsData.getMyMeetings()
                                    .stream().filter(m -> m.getMeetingDate() > System.currentTimeMillis() && !m.isCanceled())
                                    .count());
                    binding.newMeetingsTv.setText(upcomingLessons);

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

    @Override
    public void cancel(Meeting meeting, User otherUser) {
        new AlertDialog.Builder(getContext())
                .setTitle("Cancel Meeting")
                .setMessage(String.format(
                        "Are you sure want to cancel the meeting with %s at %s",
                        otherUser.getFullName(),
                        DateUtils.formatEpochMillis(meeting.getMeetingDate())
                ))
                .setPositiveButton("Yes",
                        (dialogInterface, i) -> mainViewModel.cancelMeeting(meeting)
                                .addOnSuccessListener(unused -> Snackbar.make(binding.getRoot(), "Meeting canceled", Snackbar.LENGTH_LONG).show()))
                .setNegativeButton("No", null)
                .show();
    }

    @Override
    public void dial(User otherUser) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse(String.format("tel: %s", otherUser.getPhone())));
        if (ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{android.Manifest.permission.CALL_PHONE}, 105);
        }
        else {
            startActivity(intent);
        }
    }

    @Override
    public void mail(User otherUser) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:")); // only email apps should handle this
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{otherUser.getEmail()}); // recipients
        intent.putExtra(Intent.EXTRA_SUBJECT, "Morim - Online Lesson Meeting");
        intent.putExtra(Intent.EXTRA_TEXT, "Hello " + otherUser.getFullName() + " ...");
        if (intent.resolveActivity(requireActivity().getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Toast.makeText(requireContext(), "No email client installed!", Toast.LENGTH_SHORT).show();
        }
    }
}

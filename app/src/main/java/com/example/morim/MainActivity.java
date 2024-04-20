package com.example.morim;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.lifecycle.Observer;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.morim.databinding.ActivityMainBinding;
import com.example.morim.model.Meeting;
import com.example.morim.model.MyMeetingsData;
import com.example.morim.model.OtherUser;
import com.example.morim.model.User;
import com.example.morim.util.DateUtils;
import com.example.morim.util.NotificationHelper;
import com.example.morim.viewmodel.MainViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainActivity extends BaseActivity {
    private ActivityMainBinding viewBinding;

    private MainViewModel mainViewModel;

    @Inject
    protected SharedPreferences sp;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());
        mainViewModel = viewModel(MainViewModel.class);

        mainViewModel.getCurrentUser()
                .observe(this, new Observer<User>() {
                    @Override
                    public void onChanged(User user) {
                        if (user == null) {
                            startActivity(new Intent(MainActivity.this, AuthActivity.class));
                            finish();
                        }
                    }
                });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.main_nav_host_fragment);
        NavController navController = navHostFragment.getNavController();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_DENIED) {
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 106);
            }

        NavigationUI.setupWithNavController(bottomNavigationView, navController);

        mainViewModel.getMyMeetingsData().observe(this, myMeetingsData -> {
            List<OtherUser> users = myMeetingsData.getUsers();
            List<Meeting> cancelledMeetings = myMeetingsData.getMyMeetings()
                    .stream().filter(Meeting::isCanceled)
                    .collect(Collectors.toList());

            Set<String> cancelledMeetingsInStore = sp.getStringSet("cancelledMeetings", new HashSet<>());
            Set<Meeting> freshlyCancelledMeetings = new HashSet<>();
            for (Meeting cancelled : cancelledMeetings) {
                if (!cancelledMeetingsInStore.contains(cancelled.getId())) {
                    freshlyCancelledMeetings.add(cancelled);
                }
            }
            Handler h = new Handler();
            for (Meeting freshlyCancelled : freshlyCancelledMeetings) {
                Optional<OtherUser> u = users.stream().filter(x -> x.getId().equals(
                                freshlyCancelled.getTeacherId().equals(FirebaseAuth.getInstance().getUid()) ?
                                        freshlyCancelled.getStudentId() : freshlyCancelled.getTeacherId()
                        ))
                        .findFirst();
                u.ifPresent(otherUser -> h.postDelayed(() -> NotificationHelper.sendNotification(
                        MainActivity.this,
                        String.format("Meeting scheduled at %s was cancelled by %s",
                                DateUtils.formatEpochMillis(freshlyCancelled.getMeetingDate()),
                                otherUser.getId().equals(FirebaseAuth.getInstance().getUid())
                                        ? "You" : otherUser.getFullName())
                ), 2000));
            }
            HashSet<String> newStoredCancelled = new HashSet<>(cancelledMeetingsInStore);
            newStoredCancelled.addAll(freshlyCancelledMeetings.stream().map(Meeting::getId).collect(Collectors.toList()));
            sp.edit()
                    .putStringSet("cancelledMeetings", newStoredCancelled)
                    .apply();
        });
    }
}
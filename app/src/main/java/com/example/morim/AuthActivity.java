package com.example.morim;


import static com.example.morim.database.remote.FirebaseUserManager.CURRENT_USER_TYPE_KEY;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;

import com.example.morim.databinding.ActivityAuthBinding;
import com.example.morim.model.Teacher;
import com.example.morim.model.User;
import com.example.morim.util.LoadingState;
import com.example.morim.viewmodel.AuthViewModel;
import com.google.android.material.snackbar.Snackbar;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AuthActivity extends BaseActivity {

    private ActivityAuthBinding viewBinding;
    private AuthViewModel authViewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewBinding = ActivityAuthBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());
        authViewModel = viewModel(AuthViewModel.class);

        authViewModel.getCurrentUser()
                .observe(this, user -> {
                    if (user != null) {
                        Intent intent = new Intent(AuthActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                        authViewModel.getCurrentUser()
                                .removeObservers(AuthActivity.this);
                    }
                });


        authViewModel.getExceptions().observe(this, e -> {
            if (e.getMessage() != null)
                Snackbar.make(viewBinding.getRoot(), e.getMessage(), Snackbar.LENGTH_LONG).show();
        });

        authViewModel.getLoadingState().observe(this, loadingState -> {
            viewBinding.pbAuth.setVisibility(loadingState == LoadingState.Loading ? View.VISIBLE : View.GONE);
        });
    }
}

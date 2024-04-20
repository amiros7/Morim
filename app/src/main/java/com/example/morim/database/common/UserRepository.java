package com.example.morim.database.common;

import static com.example.morim.database.remote.FirebaseUserManager.CURRENT_USER_TYPE_KEY;

import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.morim.database.OnDataCallback;
import com.example.morim.database.local.CurrentUserDao;
import com.example.morim.database.local.StudentDao;
import com.example.morim.database.local.TeacherDao;
import com.example.morim.database.local.UserDao;
import com.example.morim.database.remote.FirebaseUserManager;
import com.example.morim.dto.UserLoginForm;
import com.example.morim.dto.UserRegisterForm;
import com.example.morim.model.OtherUser;
import com.example.morim.model.Student;
import com.example.morim.model.Teacher;
import com.example.morim.model.User;
import com.example.morim.util.FirebaseListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class UserRepository {

    private SharedPreferences sp;
    private ScheduledThreadPoolExecutor executor;
    private final StudentDao studentDao;

    private final UserDao userDao;
    private final TeacherDao teacherDao;
    private final CurrentUserDao currentUserDao;

    private final FirebaseUserManager remoteDb;

    public UserRepository(
            SharedPreferences sp,
            ScheduledThreadPoolExecutor executor,
            StudentDao studentDao,
            TeacherDao teacherDao,
            CurrentUserDao currentUserDao,
            UserDao userDao,
            FirebaseUserManager remoteDb
    ) {
        this.sp = sp;
        this.executor = executor;
        this.studentDao = studentDao;
        this.teacherDao = teacherDao;
        this.currentUserDao = currentUserDao;
        this.userDao = userDao;
        this.remoteDb = remoteDb;
    }


    public FirebaseListener<List<OtherUser>> listenUsers() {
        return new FirebaseListener<>(
                userDao.listenAllUsers(),
                remoteDb.listenAllUsers(new OnDataCallback<List<OtherUser>>() {
                    @Override
                    public void onData(List<OtherUser> value) {
                        if (value != null) {
                            executor.execute(() -> {
                                userDao.insert(value);
                            });
                        }
                    }

                    @Override
                    public void onException(Exception e) {
                        e.printStackTrace();
                    }
                })
        );
    }


    public FirebaseListener<List<Teacher>> listenTeachers() {
        return new FirebaseListener<>(
                teacherDao.listenAllTeachers(),
                remoteDb.listenTeachers(new OnDataCallback<List<Teacher>>() {
                    @Override
                    public void onData(List<Teacher> value) {
                        if (value != null) {
                            executor.execute(() -> {
                                teacherDao.insert(value);
                            });
                        }
                    }

                    @Override
                    public void onException(Exception e) {
                        e.printStackTrace();
                    }
                })
        );
    }


    public void signIn(UserLoginForm form, OnDataCallback<AuthResult> callback) {
        remoteDb.signIn(form, callback);
    }

    public void createNewUser(UserRegisterForm form, OnDataCallback<User> callback) {
        remoteDb.createNewUser(form, callback);
    }

    public void updateUser(User user, Uri uri, OnDataCallback<User> callback) {
        remoteDb.saveUser(user.getId(), user, uri, callback);
    }

    public FirebaseListener<User> listenCurrentUser(
            OnSuccessListener<Void> logoutListener
    ) {
        return new FirebaseListener<>(
                currentUserDao.listenCurrentUser(),
                remoteDb.listenCurrentUser(new OnDataCallback<User>() {
                    @Override
                    public void onData(User value) {
                        if (value != null) {
                            executor.execute(() -> {
                                currentUserDao.insert(value);
                            });
                        } else {
                            logoutListener.onSuccess(null);
                        }
                    }

                    @Override
                    public void onException(Exception e) {
                        e.printStackTrace();
                    }
                })
        );
    }
}

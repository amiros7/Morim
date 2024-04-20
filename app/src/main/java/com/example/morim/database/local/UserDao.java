package com.example.morim.database.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.morim.model.OtherUser;
import com.example.morim.model.Student;

import java.util.List;

@Dao
public interface UserDao {

    @Query("SELECT * from other_user")
    LiveData<List<OtherUser>> listenAllUsers();
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(List<OtherUser> value);

}

package com.example.morim.database.local;


import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.morim.model.Teacher;

import java.util.List;

@Dao
public interface TeacherDao {
    @Query("SELECT * from teachers")
    LiveData<List<Teacher>> listenAllTeachers();



    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(List<Teacher> value);
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Teacher value);
}

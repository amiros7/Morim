package com.example.morim.model;

import androidx.room.Entity;

@Entity(tableName = "students")
public class Student extends User {
    public Student(String id, String email, String fullName, String address, String phone, String image) {
        super(id, email, fullName, address, phone, image, false);
    }

    public Student() {}
}

package com.example.morim.model;


import androidx.room.Entity;

@Entity(tableName = "other_user")
public class OtherUser extends User {
    public OtherUser(String id, String email, String fullName, String phone, String address, String image, boolean isTeacher) {
        super(id, email, fullName, phone, address, image, isTeacher);
    }
    public OtherUser() {
    }

}

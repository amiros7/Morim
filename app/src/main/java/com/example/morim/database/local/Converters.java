package com.example.morim.database.local;

import androidx.room.TypeConverter;

import com.example.morim.model.Location;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;

public class Converters {
    private final Gson g = new Gson();

    @TypeConverter
    public List<String> fromStringToListOfStrings(String str) {
        return g.fromJson(str, new TypeToken<List<String>>() {}.getType());
    }
    @TypeConverter
    public String fromListOfStringsToString(List<String> str) {
        return g.toJson(str);
    }



    @TypeConverter
    public Location fromStringToLocation(String str) {
        return g.fromJson(str, Location.class);
    }
    @TypeConverter
    public String fromLocationToString(Location location) {
        return g.toJson(location);
    }



}

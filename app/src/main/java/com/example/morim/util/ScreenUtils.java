package com.example.morim.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Point;
import android.view.Display;
import android.view.WindowManager;

import java.time.LocalTime;

public class ScreenUtils {

    @SuppressLint("DefaultLocale")
    public static String greetUser(String userName) {
        LocalTime now = LocalTime.now();
        int hour = now.getHour();

        String greeting;

        if (hour >= 5 && hour < 12) {
            greeting = "Good morning";
        } else if (hour >= 12 && hour < 18) {
            greeting = "Good afternoon";
        } else if (hour >= 18 && hour < 22) {
            greeting = "Good evening";
        } else {
            greeting = "Good night ";
        }


        String h = now.getHour() < 10 ? "0" + now.getHour() : String.valueOf(now.getHour());
        String m = now.getMinute() < 10 ? "0" + now.getMinute() : String.valueOf(now.getMinute());

        return String.format("%s, %s! The hour is %s:%s", greeting, userName, h, m);
    }

    public static Point getScreenSize(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (windowManager != null) {
            Display display = windowManager.getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            return size;  // size.x is the width, size.y is the height
        }
        return new Point();  // In case window manager is not available
    }
}

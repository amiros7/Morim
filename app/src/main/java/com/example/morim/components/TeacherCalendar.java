package com.example.morim.components;

import android.content.Context;

import java.time.LocalDate;
import java.util.List;

public class TeacherCalendar extends NACalendarView {

    private OnDateSelectedListener dateSelectedListener;

    public interface OnDateSelectedListener {
        void onDateSelected(LocalDate date);
    }

    protected TeacherCalendar(Context context) {
        super(context);
    }

    public TeacherCalendar(Context context,
                           List<LocalDate> unavailableDates,
                           OnDateSelectedListener onDateSelectedListener) {
        super(context, unavailableDates);
        this.dateSelectedListener = onDateSelectedListener;
        calendarDays.forEach(calendarDate -> calendarDate.getTextView().setOnClickListener(v -> {
            if (!unavailableDates.contains(calendarDate.getDate())) {
                calendarDate.toggleSelection();
                dateSelectedListener.onDateSelected(calendarDate.getDate());
            }
        }));
    }
}

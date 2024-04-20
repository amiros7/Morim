package com.example.morim.components;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.morim.util.DateUtils;
import com.example.morim.util.ScreenUtils;

import java.time.LocalDate;
import java.time.chrono.ChronoLocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class NACalendarView extends GridLayout {

    private final int screenWidth;

    class CalendarDate {
        private LocalDate date;
        private boolean selected;
        private TextView textView;

        public CalendarDate(LocalDate date, TextView textView, boolean selected) {
            this.date = date;
            this.selected = selected;
            this.textView = textView;
            textView.setLayoutParams(new LinearLayout.LayoutParams(screenWidth / 7, WRAP_CONTENT));
            textView.setPadding(8, 32, 8, 32);
            textView.setTextAlignment(TEXT_ALIGNMENT_CENTER);
            textView.setText(String.valueOf(date.getDayOfMonth()));
            setSelection(selected);
            if (!unavailableDates.contains(date) && date.isAfter(LocalDate.now()))
                textView.setOnClickListener(v -> toggleSelection());
            else {
                textView.setBackgroundColor(Color.LTGRAY);
                textView.setTextColor(Color.GRAY);
            }
        }

        private void setSelection(boolean selected) {
            this.selected = selected;
            if (selected) {
                textView.setBackgroundColor(Color.parseColor("#00AB00"));
                textView.setTextColor(Color.WHITE);
                textView.setTypeface(null, Typeface.BOLD);
                calendarDays.forEach(otherCalendarDate -> {
                    if (!otherCalendarDate.equals(this)
                            && !unavailableDates.contains(otherCalendarDate.date)
                            && otherCalendarDate.date.isAfter(LocalDate.now())) {
                        otherCalendarDate.setSelection(false);
                    }
                });
            } else {
                textView.setTextColor(Color.BLACK);
                textView.setTypeface(null, Typeface.NORMAL);
                textView.setBackgroundColor(Color.TRANSPARENT);
            }
        }

        public void toggleSelection() {
            setSelection(!selected);
        }

        public TextView getTextView() {
            return textView;
        }

        public LocalDate getDate() {
            return date;
        }
    }

    protected List<CalendarDate> calendarDays;
    protected final Set<LocalDate> unavailableDates;

    protected NACalendarView(Context context) {
        super(context);
        unavailableDates = new HashSet<>();
        screenWidth = ScreenUtils.getScreenSize(context).x;
    }

    public NACalendarView(Context context,
                          List<LocalDate> unavailableDates) {
        super(context);
        this.unavailableDates = new HashSet<>(unavailableDates);
        screenWidth = ScreenUtils.getScreenSize(context).x;
        GridLayout.LayoutParams params = new GridLayout.LayoutParams(
                spec(1, 1),
                spec(1, 1));
        params.setGravity(Gravity.CENTER);
        setLayoutParams(params);
        setColumnCount(7);
        setRowCount(7);
        createWeekDatesWithRespectTo(LocalDate.now());
        renderCalendarDays();
    }

    private void createWeekDatesWithRespectTo(LocalDate ref) {
        calendarDays = DateUtils.monthDates(ref).stream()
                .map(date -> new CalendarDate(date, new TextView(getContext()), false))
                .collect(Collectors.toList());
    }

    private void renderCalendarDays() {
        for (int day = 1; day <= 7; ++day) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(screenWidth / 7, WRAP_CONTENT);
            TextView tvDay = new TextView(getContext());
            tvDay.setPadding(8, 64, 8, 16);
            tvDay.setLayoutParams(params);
            tvDay.setTypeface(null, Typeface.BOLD);
            tvDay.setTextAlignment(TEXT_ALIGNMENT_CENTER);
            tvDay.setTextSize(12);

            tvDay.setText(DateUtils.getDayByInteger(day));
            addView(tvDay);
        }
        for (CalendarDate date : calendarDays) {
            addView(date.getTextView());
        }
    }

}

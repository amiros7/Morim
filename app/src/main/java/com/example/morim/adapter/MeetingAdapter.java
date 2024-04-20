package com.example.morim.adapter;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.morim.databinding.MeetingItemBinding;
import com.example.morim.model.Meeting;
import com.example.morim.model.MyMeetingsData;
import com.example.morim.model.OtherUser;
import com.example.morim.model.User;
import com.example.morim.util.DateUtils;
import com.google.firebase.auth.FirebaseAuth;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.grpc.Status;
import io.grpc.StatusException;

public class MeetingAdapter extends RecyclerView.Adapter<MeetingAdapter.MeetingsViewHolder> {
    private IMeetingActions actions;
    private MyMeetingsData meetingsData;

    private Set<String> alreadySeen;

    private final HashMap<String, User> users = new HashMap<>();

    public interface IMeetingActions {
        void cancel(Meeting meeting, User otherUser);
        void dial(User otherUser);
        void mail(User otherUser);
    }

    public MeetingAdapter(MyMeetingsData meetingsData, IMeetingActions actions) {
        this.meetingsData = meetingsData;
        for (User user : meetingsData.getUsers()) {
            users.put(user.getId(), user);
        }
        this.actions = actions;
        this.alreadySeen = new HashSet<>();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setMeetingsData(MyMeetingsData data, Set<String> alreadySeen) {
        users.clear();
        this.alreadySeen = alreadySeen;
        for (User user : data.getUsers()) {
            users.put(user.getId(), user);
        }
        this.meetingsData = data;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MeetingsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        MeetingItemBinding binding = MeetingItemBinding.inflate(LayoutInflater.from(parent.getContext()));
        return new MeetingsViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MeetingsViewHolder holder, int position) {
        Meeting meeting = meetingsData.getMyMeetings().get(position);
        holder.bind(meeting);
    }

    @Override
    public int getItemCount() {
        return meetingsData.getMyMeetings().size();
    }

    class MeetingsViewHolder extends RecyclerView.ViewHolder {
        private final MeetingItemBinding binding;

        private final String uid;

        public MeetingsViewHolder(MeetingItemBinding binding) {
            super(binding.getRoot());
            uid = FirebaseAuth.getInstance().getUid();
            this.binding = binding;
        }

        public void bind(Meeting meeting) {
            binding.tvMeetingDate.setText(DateUtils.formatEpochMillis(meeting.getMeetingDate()));
            // We are the student viewing the teacher.
            // Get the teacher
            User other;
            if (meeting.getStudentId().equals(uid)) {
                other = users.get(meeting.getTeacherId());
                if (other == null) {
                    Log.d("MeetingsAdapter:bind ", "Teacher is Null!");
                    return;
                }
            }
            // We are the teacher viewing the student.
            // Get the student
            else if (meeting.getTeacherId().equals(uid)) {
                other = users.get(meeting.getStudentId());
                if (other == null) {
                    Log.d("MeetingsAdapter:bind ", "Student is Null!");
                    return;
                }
            }
            //  We are a malicious intruder
            //  somehow viewing someone else's meeting schedule
            else {
                throw new RuntimeException("MeetingsAdapter:bind Permission to view meeting denied");
            }

            if (alreadySeen.contains(meeting.getId())) {
                binding.newLayout.setVisibility(View.GONE);
            } else {
                binding.newLayout.setVisibility(View.VISIBLE);
            }

            binding.tvMeetingSubject.setText(meeting.getMeetingSubject());

            // meeting may be canceled
            if (meeting.isCanceled()) {
                binding.lessonTag.setTextColor(Color.RED);
                binding.lessonTag.setText(
                        String.format("%s", "CANCELED")
                );
                binding.btnCancelMeeting.setVisibility(View.GONE);
            } else {
                binding.lessonTag.setTextColor(Color.parseColor("#25A92A"));
                binding.lessonTag.setText(
                        String.format("%s", "Online Lesson")
                );
                binding.btnCancelMeeting.setVisibility(View.VISIBLE);
            }

            binding.tvMeetingEmail.setOnClickListener(v -> actions.mail(other));
            binding.tvMeetingPhone.setOnClickListener(v -> actions.dial(other));

            binding.tvMeetingEmail.setText(other.getEmail());
            binding.tvMeetingPhone.setText(other.getPhone());
            binding.tvMeetingParticipant.setText(other.getFullName());

            binding.btnCancelMeeting.setOnClickListener(view ->
                    actions.cancel(meeting,other)
            );
        }
    }
}

package com.example.ma2025.task;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class TaskListPagerAdapter extends FragmentStateAdapter {

    private final String currentUserId;
    private final Context context;
    public TaskListPagerAdapter(@NonNull FragmentActivity fragmentActivity, String currentUserId) {
        super(fragmentActivity);
        this.currentUserId = currentUserId;
        this.context = fragmentActivity;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Bundle args = new Bundle();
        args.putString("userId", currentUserId);

        Fragment fragment;
        if (position == 0) {
            fragment = new OneTimeTaskListFragment();  // fragment za jednokratne zadatke
        } else {
            fragment = new RepeatingTaskListFragment(); // fragment za ponavljajuće
        }
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getItemCount() {
        return 2; // dva taba
    }
}

package com.example.ma2025.task;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class TaskListPagerAdapter extends FragmentStateAdapter {

    public TaskListPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            return new OneTimeTaskListFragment();  // fragment za jednokratne zadatke
        } else {
            return new RepeatingTaskListFragment(); // fragment za ponavljajuće
        }
    }

    @Override
    public int getItemCount() {
        return 2; // dva taba
    }
}

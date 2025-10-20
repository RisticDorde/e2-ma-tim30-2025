package com.example.ma2025.task;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.ma2025.auth.AuthManager;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.example.ma2025.R;

public class TaskListActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private TaskListPagerAdapter pagerAdapter;
    private String currentUserId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_list);

        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);
        currentUserId = AuthManager.getCurrentUser(this).getUid();

        // Adapter za ViewPager
        pagerAdapter = new TaskListPagerAdapter(this, currentUserId);
        viewPager.setAdapter(pagerAdapter);

        // Povezivanje TabLayout sa ViewPager-om
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    if (position == 0) {
                        tab.setText("Jednokratni");
                    } else {
                        tab.setText("Ponavljajući");
                    }
                }).attach();
    }
}
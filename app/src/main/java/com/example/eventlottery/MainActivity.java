package com.example.eventlottery;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNav = findViewById(R.id.bottom_nav);

        // Default landing screen = Dashboard/Home
        if (savedInstanceState == null) {
            switchTo(new DashboardFragment());
            bottomNav.setSelectedItemId(R.id.nav_home);
        }

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                switchTo(new DashboardFragment());
                return true;
            }

            if (id == R.id.nav_scan) {
                Toast.makeText(this, "Scan page not wired yet", Toast.LENGTH_SHORT).show();
                return true;
            }

            if (id == R.id.nav_notifications) {
                // Your notifications page (fragment)
                switchTo(new EntrantInvitationFragment());
                return true;
            }

            if (id == R.id.nav_profile) {
                // Teammate’s profile page (fragment)
                switchTo(new ProfileFragment());
                return true;
            }

            return false;
        });
    }

    private void switchTo(@NonNull Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}
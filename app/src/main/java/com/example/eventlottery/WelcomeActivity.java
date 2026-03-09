package com.example.eventlottery;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

/**
 * WelcomeActivity
 *
 * Entry screen for role selection (Entrant / Organizer / Admin).
 * UI-only for now: routes directly to the appropriate home activity.
 *
 * Later (device identity + Firebase):
 * - this screen can still choose a role, then route into profile creation / routing.
 * @author: Kenneth Joseph
 * @version: 1.0
 * @see: activity_welcome.xml
 */
public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        Button entrantBtn = findViewById(R.id.btnEntrant);
        Button organizerBtn = findViewById(R.id.btnOrganizer);
        Button adminBtn = findViewById(R.id.btnAdmin);

        entrantBtn.setOnClickListener(v ->
                startActivity(new Intent(this, MainActivity.class))
        );

        organizerBtn.setOnClickListener(v ->
                startActivity(new Intent(this, OrganizerMainActivity.class))
        );

        adminBtn.setOnClickListener(v ->
                startActivity(new Intent(this, AdminMainActivity.class))
        );
    }
}
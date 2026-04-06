package com.example.educode.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.View; // Import View
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.educode.R;
import com.example.educode.data.DatabaseHelper;
import com.example.educode.ui.learn.CategoryActivity;
import com.example.educode.ui.learn.FavoritesActivity;
import com.example.educode.ui.dashboard.DashboardActivity;
import com.example.educode.ui.auth.LoginActivity; // Import LoginActivity

public class ProfileActivity extends AppCompatActivity {

    TextView tvWelcome, tvScore;
    Button btnStartDetection, btnLearn, btnFavorites;
    DatabaseHelper db;
    String currentUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        db = new DatabaseHelper(this);
        tvWelcome = findViewById(R.id.tvWelcome);
        tvScore = findViewById(R.id.tvScore);

        btnStartDetection = findViewById(R.id.btnStartDetection);
        btnLearn = findViewById(R.id.btnLearn);
        btnFavorites = findViewById(R.id.btnFavorites);

        // Get username first so we can use it in intents
        currentUsername = getIntent().getStringExtra("USERNAME");
        tvWelcome.setText("Bienvenue, " + currentUsername + " !");

        // 1. Learn Button
        btnLearn.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, CategoryActivity.class);
            intent.putExtra("USERNAME", currentUsername);
            startActivity(intent);
        });

        // 2. Favorites Button
        btnFavorites.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, FavoritesActivity.class);
            intent.putExtra("USERNAME", currentUsername);
            startActivity(intent);
        });

        // 3. Detection Button
        btnStartDetection.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
            startActivity(intent);
        });

        // 4. Quiz Button
        Button btnQuiz = findViewById(R.id.btnStartQuiz);
        btnQuiz.setOnClickListener(v -> {
            Intent intent = new Intent(this, com.example.educode.ui.quiz.QuizActivity.class);
            intent.putExtra("USERNAME", currentUsername);
            startActivity(intent);
        });

        // 5. DASHBOARD BUTTON
        Button btnDashboard = findViewById(R.id.btnDashboard);
        if (btnDashboard != null) {
            btnDashboard.setOnClickListener(v -> {
                Intent intent = new Intent(ProfileActivity.this, DashboardActivity.class);
                intent.putExtra("USERNAME", currentUsername);
                startActivity(intent);
            });
        }

        // 6. Badges Button
        Button btnBadges = findViewById(R.id.btnBadges);
        btnBadges.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, com.example.educode.ui.dashboard.BadgesActivity.class);
            intent.putExtra("USERNAME", currentUsername);
            startActivity(intent);
        });

        // --- 7. LOGOUT BUTTON (NEW CODE) ---
        View btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> {
            // A. Clear User Session
            android.content.SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            android.content.SharedPreferences.Editor editor = prefs.edit();
            editor.clear(); // Wipes the username
            editor.apply();

            // B. Redirect to Login
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            // Clear back stack so user can't go back
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
        // -----------------------------------

        loadUserData();
    }

    private void loadUserData() {
        int[] stats = db.getUserStats(currentUsername);
        tvScore.setText("Score Total : " + stats[0] + " pts");
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserData();
    }
}
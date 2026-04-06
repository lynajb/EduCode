package com.example.educode.ui.quiz;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.educode.R;
import com.example.educode.data.DatabaseHelper;
import com.example.educode.ui.main.ProfileActivity;
import com.example.educode.utils.BadgeManager;

public class ResultActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        // 1. Initialize Views
        TextView tvScore = findViewById(R.id.tvFinalScore);
        TextView tvTotal = findViewById(R.id.tvTotalQuestions);
        TextView tvComment = findViewById(R.id.tvResultComment);
        Button btnReplay = findViewById(R.id.btnReplay);
        Button btnHome = findViewById(R.id.btnHome);

        // 2. Retrieve data
        int score = getIntent().getIntExtra("SCORE", 0);
        int total = getIntent().getIntExtra("TOTAL", 10);
        String username = getIntent().getStringExtra("USERNAME");

        // 3. Set Score Text
        tvScore.setText(String.valueOf(score));
        tvTotal.setText("sur " + total);

        // --- DATABASE LOGIC ---
        // ➤ REMOVED: db.addQuizResult and db.updateUserScore
        // (Because this is already done in QuizActivity now)

        // 4. Check for Badges (We keep this here so the user sees the badge unlock immediately)
        if (username != null) {
            BadgeManager badgeManager = new BadgeManager(this);
            badgeManager.checkAndUnlockBadges(username);
        }
        // ----------------------

        // 5. Logic for comments/colors
        if (score >= 8) {
            tvComment.setText("⭐ Excellent ! Vous êtes un expert.");
        } else if (score >= 5) {
            tvComment.setText("🙂 Pas mal, mais peut mieux faire.");
            tvComment.setTextColor(Color.parseColor("#FFD740"));
        } else {
            tvComment.setText("😕 Oups... Il faut réviser !");
            tvComment.setTextColor(Color.parseColor("#FF5252"));
        }

        // Replay Button
        btnReplay.setOnClickListener(v -> {
            Intent intent = new Intent(ResultActivity.this, QuizActivity.class);
            intent.putExtra("USERNAME", username);
            startActivity(intent);
            finish();
        });

        // Home Button
        btnHome.setOnClickListener(v -> {
            Intent intent = new Intent(ResultActivity.this, ProfileActivity.class);
            intent.putExtra("USERNAME", username);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }
}
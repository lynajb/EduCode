package com.example.educode.ui.dashboard;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.educode.R;
import com.example.educode.data.DatabaseHelper;

import java.util.List;

public class DashboardActivity extends AppCompatActivity {

    // UI Components
    TextView tvSigns, tvQuiz, tvBestScore, tvDetection, tvBadgeName, tvProgressPercent, tvStreak, tvNoMistakes;
    ImageView imgBadge;
    ProgressBar progressBarMaster;
    CardView cardBadge;
    LinearLayout layoutMistakes;

    // Chart Views
    View barMon, barTue, barWed, barThu, barFri, barSat, barSun;

    DatabaseHelper db;
    String currentUsername;
    @Override
    protected void onResume() {
        super.onResume();
        // This ensures that every time the user sees this screen,
        // the database re-counts the rows and updates the UI
        try {
            if (db != null) {
                loadDashboardData();
                Log.d("Dashboard", "Data refreshed via onResume");
            }
        } catch (Exception e) {
            Log.e("Dashboard", "Error reloading data: " + e.getMessage());
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            setContentView(R.layout.activity_dashboard);

            // 1. Initialisation
            db = new DatabaseHelper(this);

            // Récupérer le username
            currentUsername = getIntent().getStringExtra("USERNAME");
            if (currentUsername == null) currentUsername = "Invité";

            // 2. Liaison des vues
            tvSigns = findViewById(R.id.tvStatSigns);
            tvQuiz = findViewById(R.id.tvStatQuiz);
            tvBestScore = findViewById(R.id.tvStatBestScore);
            tvDetection = findViewById(R.id.tvStatDetection);
            tvBadgeName = findViewById(R.id.tvBadgeName);
            tvProgressPercent = findViewById(R.id.tvProgressPercent);
            imgBadge = findViewById(R.id.imgBadge);
            progressBarMaster = findViewById(R.id.progressBarMaster);
            cardBadge = findViewById(R.id.cardBadge);

            // NEW VIEWS
            tvStreak = findViewById(R.id.tvStreak);
            layoutMistakes = findViewById(R.id.layoutMistakes);
            tvNoMistakes = findViewById(R.id.tvNoMistakes);

            // Chart Bars
            barMon = findViewById(R.id.barMon);
            barTue = findViewById(R.id.barTue);
            barWed = findViewById(R.id.barWed);
            barThu = findViewById(R.id.barThu);
            barFri = findViewById(R.id.barFri);
            barSat = findViewById(R.id.barSat);
            barSun = findViewById(R.id.barSun);

            // CHECK FOR MISSING VIEWS
            if (tvSigns == null || tvQuiz == null || progressBarMaster == null) {
                Toast.makeText(this, "Erreur: ID manquant dans activity_dashboard.xml", Toast.LENGTH_LONG).show();
                return;
            }

            // 3. Charger les données
            loadDashboardData();

            // 4. Lancer les animations
            startAnimations();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e("DashboardCrash", "Error: ", e);
        }
    }
    private void loadDashboardData() {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        currentUsername = prefs.getString("username", "default_user");

        Log.d("Dashboard", "Loading stats for: " + currentUsername);

        // 1. Get the Manual Count (Panneaux)
        int learnedSigns = db.getSignsLearnedCount(currentUsername);

        // 2. Get the History Count (Detections)
        int historyCount = db.getDetectionCount(currentUsername);

        // 3. Get Quiz & Score
        int quizCount = db.getQuizCompletedCount(currentUsername);
        int bestScore = db.getBestScore(currentUsername);

        // 4. Update UI
        if (tvSigns != null) tvSigns.setText(String.valueOf(learnedSigns));        // Blue Box
        if (tvDetection != null) tvDetection.setText(String.valueOf(historyCount)); // Orange Box
        if (tvQuiz != null) tvQuiz.setText(String.valueOf(quizCount));
        if (tvBestScore != null) tvBestScore.setText(String.valueOf(bestScore));

        // ... (Keep your existing Badge, Progress Bar, and Chart logic below) ...
        // Badge Logic
        String badge = BadgeUtils.getBadgeName(learnedSigns, quizCount, bestScore);
        if (tvBadgeName != null) {
            tvBadgeName.setText(badge);
            tvBadgeName.setTextColor(Color.parseColor(BadgeUtils.getBadgeColor(badge)));
        }
        if (imgBadge != null) imgBadge.setImageResource(BadgeUtils.getBadgeIcon(badge));

        // Progress Bar
        int progress = 0;
        progress += Math.min(learnedSigns * 2, 50);
        progress += Math.min(quizCount * 5, 40);
        progress += Math.min(bestScore, 10);
        if (progressBarMaster != null) progressBarMaster.setProgress(progress);
        if (tvProgressPercent != null) tvProgressPercent.setText(progress + "%");

        loadStreakLogic();
        loadMistakesLogic();
        loadChartLogic();
    }
    private void loadChartLogic() {
        // 1. Use the class variable 'currentUsername' directly
        // (We already ensured it's set in loadDashboardData, so no need to fetch prefs again)

        // 2. Get stats from DB
        int[] weeklyStats = db.getWeeklyStats(currentUsername);

        // 3. SCALING FIX: Set a minimum target (e.g., 10 activities per day)
        // This prevents the bar from jumping to 100% with just 1 detection.
        int maxActivity = 10;

        for (int count : weeklyStats) {
            if (count > maxActivity) maxActivity = count;
        }

        // 4. Animate bars dynamically
        int maxHeightDp = 100;

        animateBar(barMon, (weeklyStats[0] * maxHeightDp) / maxActivity);
        animateBar(barTue, (weeklyStats[1] * maxHeightDp) / maxActivity);
        animateBar(barWed, (weeklyStats[2] * maxHeightDp) / maxActivity);
        animateBar(barThu, (weeklyStats[3] * maxHeightDp) / maxActivity);
        animateBar(barFri, (weeklyStats[4] * maxHeightDp) / maxActivity);
        animateBar(barSat, (weeklyStats[5] * maxHeightDp) / maxActivity);
        animateBar(barSun, (weeklyStats[6] * maxHeightDp) / maxActivity);

        // --- COLOR LOGIC (Your existing logic is good!) ---

        // A. Reset all bars to Gray first
        int colorGray = android.graphics.Color.parseColor("#E0E0E0");
        int colorBlue = android.graphics.Color.parseColor("#00BCD4");

        barMon.setBackgroundTintList(android.content.res.ColorStateList.valueOf(colorGray));
        barTue.setBackgroundTintList(android.content.res.ColorStateList.valueOf(colorGray));
        barWed.setBackgroundTintList(android.content.res.ColorStateList.valueOf(colorGray));
        barThu.setBackgroundTintList(android.content.res.ColorStateList.valueOf(colorGray));
        barFri.setBackgroundTintList(android.content.res.ColorStateList.valueOf(colorGray));
        barSat.setBackgroundTintList(android.content.res.ColorStateList.valueOf(colorGray));
        barSun.setBackgroundTintList(android.content.res.ColorStateList.valueOf(colorGray));

        // B. Find out what day is TODAY
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        int dayOfWeek = calendar.get(java.util.Calendar.DAY_OF_WEEK);

        // C. Paint ONLY today's bar Blue
        if (dayOfWeek == java.util.Calendar.MONDAY) {
            barMon.setBackgroundTintList(android.content.res.ColorStateList.valueOf(colorBlue));
        } else if (dayOfWeek == java.util.Calendar.TUESDAY) {
            barTue.setBackgroundTintList(android.content.res.ColorStateList.valueOf(colorBlue));
        } else if (dayOfWeek == java.util.Calendar.WEDNESDAY) {
            barWed.setBackgroundTintList(android.content.res.ColorStateList.valueOf(colorBlue));
        } else if (dayOfWeek == java.util.Calendar.THURSDAY) {
            barThu.setBackgroundTintList(android.content.res.ColorStateList.valueOf(colorBlue));
        } else if (dayOfWeek == java.util.Calendar.FRIDAY) {
            barFri.setBackgroundTintList(android.content.res.ColorStateList.valueOf(colorBlue));
        } else if (dayOfWeek == java.util.Calendar.SATURDAY) {
            barSat.setBackgroundTintList(android.content.res.ColorStateList.valueOf(colorBlue));
        } else if (dayOfWeek == java.util.Calendar.SUNDAY) {
            barSun.setBackgroundTintList(android.content.res.ColorStateList.valueOf(colorBlue));
        }
    }
    private void animateBar(View bar, int targetHeightDp) {
        if (bar == null) return;

        // Minimum height so the bar is visible even if value is 0 (optional)
        if (targetHeightDp < 5) targetHeightDp = 5;

        // Convert DP to Pixels
        float density = getResources().getDisplayMetrics().density;
        int heightPx = (int) (targetHeightDp * density);

        // Set height
        ViewGroup.LayoutParams params = bar.getLayoutParams();
        params.height = heightPx;
        bar.setLayoutParams(params);

        // Simple scale animation from bottom
        ScaleAnimation anim = new ScaleAnimation(1f, 1f, 0f, 1f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 1f);
        anim.setDuration(1000);
        anim.setFillAfter(true);
        bar.startAnimation(anim);
    }
    private void loadStreakLogic() {
        // Use class variable 'currentUsername'
        int streakDays = db.getCurrentStreak(currentUsername);

        if (tvStreak != null) {
            tvStreak.setText("🔥 " + streakDays + " Jours");
        }
    }

    private void loadMistakesLogic() {
        if (layoutMistakes == null) return;

        List<String> mistakes = db.getRecentMistakes(currentUsername);

        if (mistakes.isEmpty()) {
            if (tvNoMistakes != null) tvNoMistakes.setVisibility(View.VISIBLE);
            layoutMistakes.removeAllViews();
            layoutMistakes.addView(tvNoMistakes);
        } else {
            if (tvNoMistakes != null) tvNoMistakes.setVisibility(View.GONE);
            layoutMistakes.removeAllViews();

            for (String signName : mistakes) {
                addMistakeCard(signName);
            }
        }
    }

    private void addMistakeCard(String signName) {
        // Create CardView programmatically
        CardView card = new CardView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(200, 250);
        params.setMargins(0, 0, 30, 0);
        card.setLayoutParams(params);
        card.setRadius(20f);
        card.setCardElevation(6f);
        card.setCardBackgroundColor(Color.WHITE);

        // Create Layout inside Card
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setPadding(20, 20, 20, 20);

        // Icon - DYNAMICALLY LOADED
        ImageView icon = new ImageView(this);

        // Try to find a drawable named "sign_a", "sign_b", etc.
        // Assumes your drawables are named "sign_" + the letter
        String resourceName = "sign_" + signName.toLowerCase();
        int resId = getResources().getIdentifier(resourceName, "drawable", getPackageName());

        if (resId != 0) {
            icon.setImageResource(resId);
        } else {
            // Fallback if image not found
            icon.setImageResource(android.R.drawable.ic_delete);
            icon.setColorFilter(Color.RED);
        }

        icon.setLayoutParams(new LinearLayout.LayoutParams(80, 80));

        // Text
        TextView text = new TextView(this);
        text.setText(signName);
        text.setGravity(Gravity.CENTER);
        text.setTextColor(Color.BLACK);
        text.setTextSize(12f);

        layout.addView(icon);
        layout.addView(text);
        card.addView(layout);

        layoutMistakes.addView(card);
    }

    private void startAnimations() {
        try {
            Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
            if (cardBadge != null) cardBadge.startAnimation(fadeIn);

            Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_bottom);
            if (tvSigns != null) findViewById(R.id.tvStatSigns).startAnimation(slideUp);
            if (tvQuiz != null) findViewById(R.id.tvStatQuiz).startAnimation(slideUp);
        } catch (Exception e) {
            Log.e("Dashboard", "Animation missing");
        }
    }
}
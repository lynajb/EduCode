package com.example.educode.utils;

import android.content.Context;
import android.widget.Toast;
import com.example.educode.data.DatabaseHelper;

public class BadgeManager {

    private Context context;
    private DatabaseHelper db;

    public BadgeManager(Context context) {
        this.context = context;
        this.db = new DatabaseHelper(context);
    }

    public void checkAndUnlockBadges(String username) {
        // 1. Get the same stats used by the Dashboard
        int learnedSigns = db.getSignsLearnedCount(username);
        int quizCount = db.getQuizCompletedCount(username);
        int bestScore = db.getBestScore(username);

        // 2. Define Badge IDs (Must match your Database IDs from insertDefaultBadges)
        int BADGE_BRONZE_ID = 1;  // Débutant
        int BADGE_SILVER_ID = 2;  // Intermédiaire
        int BADGE_GOLD_ID = 3;    // Expert
        int BADGE_MASTER_ID = 4;  // Maître de la Route

        // 3. Apply the logic from BadgeUtils to unlock them in the DB

        // Always unlock Bronze/Beginner as the starting point
        unlockIfNew(username, BADGE_BRONZE_ID, "Badge Débutant débloqué !");

        // Check for Intermédiaire (Silver)
        if (quizCount >= 3 || learnedSigns >= 10) {
            unlockIfNew(username, BADGE_SILVER_ID, "Niveau Intermédiaire atteint ! Badge Argent !");
        }

        // Check for Expert (Gold)
        if (quizCount >= 10 && bestScore >= 8) {
            unlockIfNew(username, BADGE_GOLD_ID, "Niveau Expert atteint ! Badge Or !");
        }

        // Check for Master
        if (quizCount >= 20 && bestScore == 10 && learnedSigns >= 50) {
            unlockIfNew(username, BADGE_MASTER_ID, "Félicitations Maître de la Route !");
        }
    }

    private void unlockIfNew(String username, int badgeId, String message) {
        // Only unlock if they don't have it yet
        if (!db.checkUserHasBadge(username, badgeId)) {
            db.unlockBadge(username, badgeId);
            Toast.makeText(context, "🏆 " + message, Toast.LENGTH_LONG).show();
        }
    }
}
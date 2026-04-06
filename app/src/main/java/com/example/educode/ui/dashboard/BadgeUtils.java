package com.example.educode.ui.dashboard;

import com.example.educode.R;

public class BadgeUtils {

    public static final String BADGE_BEGINNER = "Débutant";
    public static final String BADGE_INTERMEDIATE = "Intermédiaire";
    public static final String BADGE_EXPERT = "Expert";
    public static final String BADGE_MASTER = "Maître de la Route";

    public static String getBadgeName(int learnedSigns, int quizzesCompleted, int bestScore) {
        if (quizzesCompleted >= 20 && bestScore == 10 && learnedSigns >= 50) {
            return BADGE_MASTER;
        } else if (quizzesCompleted >= 10 && bestScore >= 8) {
            return BADGE_EXPERT;
        } else if (quizzesCompleted >= 3 || learnedSigns >= 10) {
            return BADGE_INTERMEDIATE;
        } else {
            return BADGE_BEGINNER;
        }
    }

    public static int getBadgeIcon(String badgeName) {
        switch (badgeName) {
            case BADGE_MASTER:
                // Highest level -> uses 'master' image
                return R.drawable.ic_badge_master;
            case BADGE_EXPERT:
                // Expert level -> uses 'ic_master_gold' image
                return R.drawable.ic_badge_gold;
            case BADGE_INTERMEDIATE:
                // Intermediate level -> uses 'silver' image
                return R.drawable.ic_badge_silver;
            default:
                // Beginner level -> uses 'bronze' image
                return R.drawable.ic_badge_bronze;
        }
    }

    public static String getBadgeColor(String badgeName) {
        switch (badgeName) {
            case BADGE_MASTER: return "#E91E63"; // Pink
            case BADGE_EXPERT: return "#FFD700"; // Gold
            case BADGE_INTERMEDIATE: return "#C0C0C0"; // Silver
            default: return "#CD7F32"; // Bronze
        }
    }
}
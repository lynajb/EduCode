package com.example.educode.ui.dashboard;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.educode.R;
import com.example.educode.adapters.BadgeAdapter;
import com.example.educode.data.DatabaseHelper;
import com.example.educode.models.Badge;
import com.example.educode.utils.BadgeManager;
import java.util.List;

public class BadgesActivity extends AppCompatActivity {

    RecyclerView rvBadges;
    DatabaseHelper db;
    BadgeManager badgeManager;
    String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_badges);

        rvBadges = findViewById(R.id.rvBadges);
        db = new DatabaseHelper(this);
        badgeManager = new BadgeManager(this);
        username = getIntent().getStringExtra("USERNAME");

        // 1. Vérifier si de nouveaux badges sont débloqués maintenant
        badgeManager.checkAndUnlockBadges(username);

        // 2. Charger la liste
        loadBadges();
    }

    private void loadBadges() {
        List<Badge> list = db.getAllBadgesWithStatus(username);

        BadgeAdapter adapter = new BadgeAdapter(this, list);
        rvBadges.setLayoutManager(new GridLayoutManager(this, 2)); // Grille 2 colonnes
        rvBadges.setAdapter(adapter);
    }
}
package com.example.educode.ui.learn;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.educode.R;
import com.example.educode.data.DatabaseHelper;
import com.example.educode.models.Sign;
import com.example.educode.adapters.SignAdapter;

import java.util.List;

public class SignListActivity extends AppCompatActivity {

    RecyclerView rvSigns;
    TextView tvCategoryTitle;
    LinearLayout headerLayout;
    DatabaseHelper db;
    SignAdapter adapter;
    int categoryId;
    String currentUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_list);

        rvSigns = findViewById(R.id.rvSigns);
        tvCategoryTitle = findViewById(R.id.tvCategoryTitle);
        headerLayout = findViewById(R.id.headerLayout);

        db = new DatabaseHelper(this);

        // Get Intent Data
        categoryId = getIntent().getIntExtra("CAT_ID", -1);
        String catName = getIntent().getStringExtra("CAT_NAME");
        currentUsername = getIntent().getStringExtra("USERNAME");

        // --- CRITICAL FIX: Ensure username is never null ---
        // If the previous activity didn't pass the username correctly,
        // we grab it from storage so the database update works.
        if (currentUsername == null || currentUsername.isEmpty()) {
            android.content.SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            currentUsername = prefs.getString("username", "default_user");
        }
        // ---------------------------------------------------

        // <--- GET THE COLOR ---
        String catColor = getIntent().getStringExtra("CAT_COLOR");

        if (catName != null) {
            tvCategoryTitle.setText(catName);
        }

        // <--- APPLY THE COLOR TO THE HEADER ---
        if (catColor != null) {
            try {
                headerLayout.setBackgroundColor(Color.parseColor(catColor));

                // Optional: If the background is very light, change text to black
                if (catColor.equals("#FFE0B2") || catColor.equals("#FFF3E0") || catColor.equals("#FFD180")) {
                    tvCategoryTitle.setTextColor(Color.parseColor("#333333"));
                } else {
                    tvCategoryTitle.setTextColor(Color.WHITE);
                }

            } catch (IllegalArgumentException e) {
                // Fallback to default orange if color code is invalid
                headerLayout.setBackgroundColor(Color.parseColor("#FF9800"));
            }
        }

        List<Sign> signList = db.getSignsByCategory(categoryId);

        // Pass the guaranteed valid username to the adapter
        adapter = new SignAdapter(this, signList, currentUsername);

        rvSigns.setLayoutManager(new GridLayoutManager(this, 2));
        rvSigns.setAdapter(adapter);
    }
}
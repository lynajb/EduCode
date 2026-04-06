package com.example.educode.ui.history;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.educode.R;
import com.example.educode.adapters.DetectionsAdapter; // Importing from your adapters package
import com.example.educode.data.DatabaseHelper;
import com.example.educode.models.DetectionResult;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

public class DetectionsActivity extends AppCompatActivity {
    private String loggedInUser;
    private RecyclerView recyclerView;
    private DetectionsAdapter adapter;
    private DatabaseHelper dbHelper;
    private SearchView searchView;
    private ChipGroup chipGroup;

    // 0 = Recent, 1 = Confidence
    private int currentFilter = 0;
    private String currentQuery = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detections);
// Retrieve the username (Assuming you saved it during login)
        loggedInUser = getSharedPreferences("UserPrefs", MODE_PRIVATE)
                .getString("username", "guest");
        // 1. Setup Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Historique");
        }

        // 2. Initialize Views
        recyclerView = findViewById(R.id.recyclerViewDetections);
        searchView = findViewById(R.id.searchView);
        chipGroup = findViewById(R.id.filterChipGroup);

        dbHelper = new DatabaseHelper(this);

        // 3. Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DetectionsAdapter(this, new ArrayList<>());
        recyclerView.setAdapter(adapter);

        // 4. Load Initial Data
        loadDetections();

        // 5. Setup Search Listener
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                currentQuery = query;
                loadDetections();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                currentQuery = newText;
                loadDetections();
                return true;
            }
        });

        // 6. Setup Filter Chips Listener
        chipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chipConfidence) {
                currentFilter = 1; // Sort by Confidence
            } else {
                currentFilter = 0; // Sort by Date (Default)
            }
            loadDetections();
        });
    }

    private void loadDetections() {
        List<DetectionResult> results = dbHelper.getDetections(loggedInUser, currentFilter, currentQuery);
        adapter.updateList(results);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish(); // Close activity when back arrow is clicked
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
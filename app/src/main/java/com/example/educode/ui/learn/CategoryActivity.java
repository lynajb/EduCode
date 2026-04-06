package com.example.educode.ui.learn;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.educode.R;
import com.example.educode.adapters.CategoryAdapter;
import com.example.educode.data.DatabaseHelper;
import com.example.educode.models.Category;

import java.util.List;

public class CategoryActivity extends AppCompatActivity {

    RecyclerView rvCategories;
    DatabaseHelper db;
    CategoryAdapter adapter;
    String currentUsername; // <--- 1. Add variable

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        rvCategories = findViewById(R.id.rvCategories);
        db = new DatabaseHelper(this);

        // <--- 2. GET USERNAME FROM PROFILE ---
        currentUsername = getIntent().getStringExtra("USERNAME");
        // -------------------------------------

        List<Category> categoryList = db.getAllCategories();

        // <--- 3. PASS USERNAME TO ADAPTER ---
        adapter = new CategoryAdapter(this, categoryList, currentUsername);
        // ------------------------------------

        rvCategories.setLayoutManager(new LinearLayoutManager(this));
        rvCategories.setAdapter(adapter);
    }
}
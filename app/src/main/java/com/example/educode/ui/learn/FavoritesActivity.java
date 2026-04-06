package com.example.educode.ui.learn;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.educode.R;
import com.example.educode.adapters.FavoriteAdapter;
import com.example.educode.data.DatabaseHelper;
import com.example.educode.models.Sign;
import java.util.List;

public class FavoritesActivity extends AppCompatActivity {

    RecyclerView rvFavorites;
    DatabaseHelper db;
    FavoriteAdapter adapter;
    String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        rvFavorites = findViewById(R.id.rvFavorites);
        db = new DatabaseHelper(this);
        username = getIntent().getStringExtra("USERNAME");

        loadFavorites();
    }

    private void loadFavorites() {
        List<Sign> list = db.getUserFavorites(username);

        adapter = new FavoriteAdapter(this, list, username, (signId, position) -> {
            // Callback suppression
            db.removeFavorite(username, signId);
            list.remove(position);
            adapter.notifyItemRemoved(position);
            Toast.makeText(this, "Retiré des favoris", Toast.LENGTH_SHORT).show();
        });

        rvFavorites.setLayoutManager(new LinearLayoutManager(this));
        rvFavorites.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recharger au cas où on revient du détail
        loadFavorites();
    }
}
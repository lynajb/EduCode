package com.example.educode.ui.learn;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.educode.R;
import com.example.educode.data.DatabaseHelper;
import com.example.educode.models.Sign;

public class SignDetailActivity extends AppCompatActivity {

    ImageView imgSign;
    TextView tvName, tvDesc;
    Button btnFav, btnFlashcards;
    DatabaseHelper db;

    int signId;
    int categoryId = -1;
    String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_detail);

        // Initialize Views
        imgSign = findViewById(R.id.imgDetailSign);
        tvName = findViewById(R.id.tvDetailName);
        tvDesc = findViewById(R.id.tvDetailDesc);
        btnFav = findViewById(R.id.btnFav);
        btnFlashcards = findViewById(R.id.btnStartFlashcards);

        db = new DatabaseHelper(this);

        // Get ID passed from Adapter
        signId = getIntent().getIntExtra("SIGN_ID", -1);

        // Get Username passed from Intent
        username = getIntent().getStringExtra("USERNAME");

        // Load Data
        loadSignData();

        // Check status using the USERNAME
        updateFavoriteButtonState();

        // --- DELETED CODE HERE ---
        // I removed db.incrementSignsLearned(username);
        // because it is already done in SignAdapter!
        // -------------------------

        // Favorite Logic
        btnFav.setOnClickListener(v -> {
            if (username == null) {
                Toast.makeText(this, "Erreur : Utilisateur non connecté", Toast.LENGTH_SHORT).show();
                return;
            }

            if (db.isFavorite(username, signId)) {
                db.removeFavorite(username, signId);
                Toast.makeText(this, "Retiré des favoris", Toast.LENGTH_SHORT).show();
            } else {
                db.addFavorite(username, signId);
                Toast.makeText(this, "Ajouté aux favoris !", Toast.LENGTH_SHORT).show();
            }
            updateFavoriteButtonState();
        });

        // Flashcard Button Logic
        btnFlashcards.setOnClickListener(v -> {
            if (categoryId != -1) {
                Intent intent = new Intent(SignDetailActivity.this, FlashcardActivity.class);
                intent.putExtra("CAT_ID", categoryId);
                intent.putExtra("USERNAME", username);
                startActivity(intent);
            }
        });
    }

    private void updateFavoriteButtonState() {
        if (username != null && db.isFavorite(username, signId)) {
            btnFav.setText("Retirer des favoris");
            btnFav.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF3333"))); // Red
        } else {
            btnFav.setText("Ajouter aux favoris");
            btnFav.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF9800"))); // Orange
        }
    }

    private void loadSignData() {
        Sign sign = db.getSignById(signId);
        if (sign != null) {
            tvName.setText(sign.getName());
            tvDesc.setText(sign.getDescription());
            categoryId = sign.getCategoryId();

            int resId = getResources().getIdentifier(sign.getImageName(), "drawable", getPackageName());
            if (resId == 0) resId = R.mipmap.ic_launcher;
            imgSign.setImageResource(resId);
        }
    }
}
package com.example.educode.ui.learn;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.educode.R;
import com.example.educode.data.DatabaseHelper;
import com.example.educode.models.Sign;
import java.util.Collections;
import java.util.List;

public class FlashcardActivity extends AppCompatActivity {

    ImageView imgSign;
    TextView tvAnswer;
    Button btnShow, btnNext;
    DatabaseHelper db;

    List<Sign> signList;
    int currentIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flashcards);

        imgSign = findViewById(R.id.imgFlashcard);
        tvAnswer = findViewById(R.id.tvFlashcardAnswer);
        btnShow = findViewById(R.id.btnShowAnswer);
        btnNext = findViewById(R.id.btnNextCard);
        db = new DatabaseHelper(this);

        // Get Category ID passed from Detail or List
        int catId = getIntent().getIntExtra("CAT_ID", -1);

        if (catId != -1) {
            signList = db.getSignsByCategory(catId);
        } else {
            // Fallback: load all signs if no category specified (optional)
            // For now, we just close if error
            Toast.makeText(this, "Erreur de catégorie", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // BONUS: Randomize the order
        Collections.shuffle(signList);

        if (signList.isEmpty()) {
            Toast.makeText(this, "Aucun panneau dans cette catégorie", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            showCard();
        }

        btnShow.setOnClickListener(v -> {
            tvAnswer.setVisibility(View.VISIBLE);
            btnShow.setEnabled(false); // Disable show button
            btnNext.setEnabled(true);  // Enable next button
        });

        btnNext.setOnClickListener(v -> {
            currentIndex++;
            if (currentIndex < signList.size()) {
                showCard();
            } else {
                Toast.makeText(this, "Révision terminée !", Toast.LENGTH_LONG).show();
                finish(); // Close activity
            }
        });
    }

    private void showCard() {
        Sign currentSign = signList.get(currentIndex);

        // Reset UI
        tvAnswer.setText(currentSign.getName());
        tvAnswer.setVisibility(View.INVISIBLE);
        btnShow.setEnabled(true);
        btnNext.setEnabled(false); // Force user to see answer first

        // Load Image
        int resId = getResources().getIdentifier(currentSign.getImageName(), "drawable", getPackageName());
        if (resId == 0) resId = R.mipmap.ic_launcher;
        imgSign.setImageResource(resId);
    }
}
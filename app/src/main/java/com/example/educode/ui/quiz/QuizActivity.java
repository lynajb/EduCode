package com.example.educode.ui.quiz;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.educode.R;
import com.example.educode.data.DatabaseHelper;
import com.example.educode.models.Question;

import java.util.List;

public class QuizActivity extends AppCompatActivity {

    TextView tvQuestion, tvProgressText;
    ImageView imgQuestion;
    Button btnA, btnB, btnC;
    ProgressBar progressBar;

    DatabaseHelper db;
    List<Question> questionList;
    int currentQuestionIndex = 0;
    int score = 0;
    boolean isAnswered = false; // Pour éviter le double clic
    String currentUsername; // Variable pour stocker l'utilisateur

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        // Init Views
        tvQuestion = findViewById(R.id.tvQuizQuestion);
        tvProgressText = findViewById(R.id.tvProgressText);
        imgQuestion = findViewById(R.id.imgQuizQuestion);
        btnA = findViewById(R.id.btnOptionA);
        btnB = findViewById(R.id.btnOptionB);
        btnC = findViewById(R.id.btnOptionC);
        progressBar = findViewById(R.id.progressBarQuiz);

        db = new DatabaseHelper(this);

        // 1. Récupérer le username passé par l'intent (IMPORTANT)
        currentUsername = getIntent().getStringExtra("USERNAME");

        // 2. Charger 10 questions aléatoires
        questionList = db.getRandomQuestions(10);

        if (questionList.isEmpty()) {
            tvQuestion.setText("Erreur : Pas de questions trouvées.");
            return;
        }

        // 3. Afficher la première question
        showQuestion();

        // 4. Listeners
        btnA.setOnClickListener(v -> checkAnswer("A", btnA));
        btnB.setOnClickListener(v -> checkAnswer("B", btnB));
        btnC.setOnClickListener(v -> checkAnswer("C", btnC));
    }

    private void showQuestion() {
        // Animation d'entrée
        View rootView = findViewById(android.R.id.content);
        Animation scaleIn = AnimationUtils.loadAnimation(this, R.anim.scale_in);
        rootView.startAnimation(scaleIn);

        // Reset UI
        isAnswered = false;
        resetButtonColors();

        // Get current question
        Question q = questionList.get(currentQuestionIndex);

        // Set Text
        tvQuestion.setText(q.getQuestionText());
        btnA.setText(q.getOptionA());
        btnB.setText(q.getOptionB());
        btnC.setText(q.getOptionC());

        // Set Image
        int resId = getResources().getIdentifier(q.getImageName(), "drawable", getPackageName());
        if (resId == 0) resId = R.mipmap.ic_launcher;
        imgQuestion.setImageResource(resId);

        // Update Progress
        int questionNum = currentQuestionIndex + 1;
        tvProgressText.setText("Question " + questionNum + "/" + questionList.size());
        progressBar.setProgress(questionNum);
    }

    private void checkAnswer(String selectedOption, Button selectedButton) {
        if (isAnswered) return; // Bloquer les clics multiples
        isAnswered = true;

        Question q = questionList.get(currentQuestionIndex);

        if (q.getCorrectOption().equals(selectedOption)) {
            // BONNE RÉPONSE
            score++;
            selectedButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#4CAF50"))); // Vert
            Animation anim = AnimationUtils.loadAnimation(this, R.anim.correct_answer);
            selectedButton.startAnimation(anim);
        } else {
            // MAUVAISE RÉPONSE
            selectedButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#F44336"))); // Rouge
            Animation anim = AnimationUtils.loadAnimation(this, R.anim.wrong_answer);
            selectedButton.startAnimation(anim);

            // Montrer la bonne réponse en vert
            showCorrectAnswer(q.getCorrectOption());

            // --- NEW: LOG MISTAKE TO DATABASE ---
            if (currentUsername != null) {
                // We need to find the text of the correct answer to save it
                String correctAnswerText = "";
                if (q.getCorrectOption().equals("A")) correctAnswerText = q.getOptionA();
                else if (q.getCorrectOption().equals("B")) correctAnswerText = q.getOptionB();
                else if (q.getCorrectOption().equals("C")) correctAnswerText = q.getOptionC();

                // Save to DB
                db.logMistake(currentUsername, correctAnswerText);
            }
            // ------------------------------------
        }

        // Attendre 1.5 secondes avant la suite
        new Handler().postDelayed(() -> {
            if (currentQuestionIndex < questionList.size() - 1) {
                currentQuestionIndex++;
                showQuestion();
            } else {
                // --- FIN DU QUIZ ---
// 1. SAUVEGARDER LE SCORE DANS LA DB
                if (currentUsername != null) {
                    // Update total user points (Keep this)
                    db.updateUserScore(currentUsername, score);

                    // ➤ ADD THIS LINE: Save this specific attempt to history
                    db.addQuizResult(currentUsername, score);
                }

                // 2. Ouvrir l'écran de résultat
                Intent intent = new Intent(QuizActivity.this, ResultActivity.class);
                intent.putExtra("SCORE", score);
                intent.putExtra("TOTAL", questionList.size());
                // On repasse le username pour qu'il ne se perde pas si on veut revenir à l'accueil
                intent.putExtra("USERNAME", currentUsername);
                startActivity(intent);
                finish();
            }
        }, 1500);
    }

    private void showCorrectAnswer(String correctOption) {
        if (correctOption.equals("A")) btnA.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#4CAF50")));
        if (correctOption.equals("B")) btnB.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#4CAF50")));
        if (correctOption.equals("C")) btnC.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#4CAF50")));
    }

    private void resetButtonColors() {
        ColorStateList defaultColor = ColorStateList.valueOf(Color.WHITE);
        btnA.setBackgroundTintList(defaultColor);
        btnB.setBackgroundTintList(defaultColor);
        btnC.setBackgroundTintList(defaultColor);
    }
}
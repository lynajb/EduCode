package com.example.educode.ui.auth;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.graphics.Shader;
import android.graphics.LinearGradient;
import android.graphics.Color;
import android.widget.TextView;

import com.example.educode.R;
import com.example.educode.data.DatabaseHelper;

public class RegisterActivity extends AppCompatActivity {

    EditText etUser, etPass, etConfirmPass;
    Button btnRegister;
    DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        TextView tvTitle = findViewById(R.id.tvTitle);
        db = new DatabaseHelper(this);
        etUser = findViewById(R.id.etRegUser);
        etPass = findViewById(R.id.etRegPass);
        etConfirmPass = findViewById(R.id.etRegConfirmPass);
        btnRegister = findViewById(R.id.btnRegister);

        // --- GRADIENT TEXT SETUP ---
        // This creates a gradient from EduCode Blue (#2196F3) to a Deep Purple (#9C27B0)
        Shader textShader = new LinearGradient(0, 0, 0, tvTitle.getTextSize(),
                new int[]{
                        Color.parseColor("#2196F3"), // Start: Blue
                        Color.parseColor("#9C27B0")  // End: Purple
                },
                null, Shader.TileMode.CLAMP);

        tvTitle.getPaint().setShader(textShader);
        // ---------------------------

        // Add this helper method inside your class or just use the logic
        View.OnTouchListener touchListener = (v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).start();
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                v.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
            }
            return false;
        };

        // Apply to your button
        btnRegister.setOnTouchListener(touchListener);
        btnRegister.setOnClickListener(v -> {
            String user = etUser.getText().toString().trim();
            String pass = etPass.getText().toString().trim();
            String confirm = etConfirmPass.getText().toString().trim();

            if (user.isEmpty() || pass.isEmpty() || confirm.isEmpty()) {
                Toast.makeText(this, "Remplissez tous les champs", Toast.LENGTH_SHORT).show();
            } else {
                if (pass.equals(confirm)) {
                    if (!db.checkUsernameExists(user)) {
                        boolean insert = db.addUser(user, pass);
                        if (insert) {
                            Toast.makeText(this, "Inscription réussie !", Toast.LENGTH_SHORT).show();
                            finish(); // Retour au login
                        } else {
                            Toast.makeText(this, "Erreur lors de l'inscription", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        etUser.setError("Cet utilisateur existe déjà");
                    }
                } else {
                    etConfirmPass.setError("Les mots de passe ne correspondent pas");
                }
            }
        });
    }
}
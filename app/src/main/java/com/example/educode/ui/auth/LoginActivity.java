package com.example.educode.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

// --- FIX IS HERE ---
// Change the import to point to the correct sub-folder (ui.main)
import com.example.educode.ui.main.ProfileActivity;
// -------------------

import com.example.educode.R;
import com.example.educode.data.DatabaseHelper;

public class LoginActivity extends AppCompatActivity {

    EditText etUsername, etPassword;
    Button btnLogin;
    TextView tvRegister;
    DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        db = new DatabaseHelper(this);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);

        // Animation simple : Fade In
        btnLogin.setAlpha(0f);
        btnLogin.animate().alpha(1f).setDuration(1000);

        View.OnTouchListener touchListener = (v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).start();
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                v.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
            }
            return false;
        };

        btnLogin.setOnTouchListener(touchListener);
        btnLogin.setOnClickListener(v -> {
            String user = etUsername.getText().toString().trim();
            String pass = etPassword.getText().toString().trim();

            if (user.isEmpty() || pass.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            } else {
                boolean check = db.checkUser(user, pass);
                if (check) {
                    // --- FIX STARTS HERE ---
                    // 1. Save the username to SharedPreferences so other activities can find it
                    android.content.SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                    android.content.SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("username", user);
                    editor.apply();
                    // --- FIX ENDS HERE ---

                    Toast.makeText(LoginActivity.this, "Connexion réussie !", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(LoginActivity.this, ProfileActivity.class);
                    intent.putExtra("USERNAME", user);
                    startActivity(intent);
                    finish();
                } else {
                    etPassword.setError("Mot de passe ou utilisateur incorrect");
                    etPassword.requestFocus();
                }
            }
        });
        tvRegister.setOnClickListener(v -> {
            // Assuming RegisterActivity is in the same 'ui.auth' folder
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }
}
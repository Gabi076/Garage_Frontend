package com.example.garage_app.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.garage_app.R;
import com.example.garage_app.model.User;
import com.example.garage_app.repository.UserRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {
    private EditText emailField, passwordField, usernameField;
    private Button registerBtn, loginRedirectBtn;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        emailField = findViewById(R.id.register_email);
        usernameField = findViewById(R.id.register_username);
        passwordField = findViewById(R.id.register_password);
        registerBtn = findViewById(R.id.register_button);
        loginRedirectBtn = findViewById(R.id.to_login_button);
        auth = FirebaseAuth.getInstance();

        registerBtn.setOnClickListener(v -> {
            String email = emailField.getText().toString();
            String pass = passwordField.getText().toString();
            String username = usernameField.getText().toString();

            auth.createUserWithEmailAndPassword(email, pass)
                    .addOnSuccessListener(authResult -> {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            UserRepository.syncUser(new User(user.getEmail(), username));
                            SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);
                            prefs.edit().putBoolean("is_logged_in", true).apply();
                            startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                            finish();
                        }
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Register failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
        });

        loginRedirectBtn.setOnClickListener(v ->
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class)));
    }
}


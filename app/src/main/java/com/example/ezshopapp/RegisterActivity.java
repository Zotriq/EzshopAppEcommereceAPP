package com.example.ezshopapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity {

    private EditText nameRegister, emailRegister, passwordRegister;
    private Button registerButton;
    private TextView goToLogin;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        nameRegister = findViewById(R.id.nameRegister);
        emailRegister = findViewById(R.id.emailRegister);
        passwordRegister = findViewById(R.id.passwordRegister);
        registerButton = findViewById(R.id.registerButton);
        goToLogin = findViewById(R.id.goToLogin);
        progressBar = findViewById(R.id.registerProgress);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });

        goToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Returns to LoginActivity
            }
        });
    }

    private void registerUser() {
        String name = nameRegister.getText().toString().trim();
        String email = emailRegister.getText().toString().trim();
        String password = passwordRegister.getText().toString().trim();

        if (name.isEmpty()) {
            nameRegister.setError("Name is required");
            nameRegister.requestFocus();
            return;
        }

        if (email.isEmpty()) {
            emailRegister.setError("Email is required");
            emailRegister.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            passwordRegister.setError("Password is required");
            passwordRegister.requestFocus();
            return;
        }

        if (password.length() < 6) {
            passwordRegister.setError("Password must be at least 6 characters");
            passwordRegister.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        registerButton.setVisibility(View.GONE);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    progressBar.setVisibility(View.GONE);
                    registerButton.setVisibility(View.VISIBLE);

                    if (task.isSuccessful()) {
                        Toast.makeText(RegisterActivity.this, "Registration Successful", Toast.LENGTH_SHORT).show();
                        // You can save the 'name' to Firebase Database/Firestore here if needed later
                        startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                        finishAffinity();
                    } else {
                        Toast.makeText(RegisterActivity.this, "Registration failed: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
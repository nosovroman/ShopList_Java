package com.example.firstapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private FirebaseUser curUser;
    private FirebaseAuth mAuth;

    private EditText Email, Password;
    private Button LoginButton;
    private TextView RegLink;

    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        InitializeElements();

        // переход на регистрацию
        RegLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToRegisterActivity();
            }
        });

        // авторизация
        LoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AllowUserToLogin();
            }
        });
    }

    private void AllowUserToLogin() {
        String email = Email.getText().toString();
        String password = Password.getText().toString();

        if (email.isEmpty()) {
            Toast.makeText(this, "Введите адрес эл. почты", Toast.LENGTH_SHORT).show();
        }
        else if (password.isEmpty()) {
            Toast.makeText(this, "Введите пароль", Toast.LENGTH_SHORT).show();
        }
        else {
            loadingBar.setTitle("Вход");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // переход к спискам
                                //Toast.makeText(LoginActivity.this, mAuth.getCurrentUser().getUid(), Toast.LENGTH_SHORT).show();
                                SendUserToBigList();
                            }
                            else {
                                String message = task.getException().toString();
                                Toast.makeText(LoginActivity.this, "Error : " + message, Toast.LENGTH_LONG).show();
                            }
                            loadingBar.dismiss();
                        }
                    });
        }
    }

    // инициализация элементов управления
    private void InitializeElements() {
        Email = findViewById(R.id.email_et);
        Password = findViewById(R.id.password_et);
        RegLink = findViewById(R.id.registration_tv_link);
        LoginButton = findViewById(R.id.login_button);
        loadingBar = new ProgressDialog(this);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (curUser != null) {
            SendUserToMainActivity();
        }
    }

    // переходы на другие активности
    private void SendUserToRegisterActivity() {
        Intent registrationIntent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(registrationIntent);
        //overridePendingTransition(0, 0);
        this.finish();
    }

    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(mainIntent);
        this.finish();
    }

    private void SendUserToBigList() {
        Intent bigListIntent = new Intent(LoginActivity.this, BigList.class);
        startActivity(bigListIntent);
        this.finish();
    }
}
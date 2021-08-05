package com.example.firstapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private FirebaseUser curUser;
    private FirebaseAuth mAuth;

    private TextView Description1, Description2;
    private Button LogRegButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        curUser = mAuth.getCurrentUser();

        // инициализация эл-тов
        InitializeElements();

        //Description1.setText("Загрузка");

        // обработка нажатия на кнопку входа/регистрации
        LogRegButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToLoginActivity();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        // переход в списки пользователя
        if (curUser != null) {
            // отлючаем загрузку
            findViewById(R.id.loadingHello).setVisibility(View.GONE);
            //Toast.makeText(this, curUser.getUid(), Toast.LENGTH_LONG).show();
            SendUserToBigList();
        }
        // приветственное окно
        else  {
            // отлючаем загрузку
            findViewById(R.id.loadingHello).setVisibility(View.GONE);
            // вывод текста описания
            SetDescription();
            LogRegButton.setVisibility(View.VISIBLE);
            //Toast.makeText(this, "else", Toast.LENGTH_LONG).show();
        }
    }

    // вывод текста описания
    private void SetDescription() {
        Description1.setText(Html.fromHtml(getString(R.string.description)));
        Description2.setText(Html.fromHtml(getString(R.string.description2)));
    }

    // инициализация элементов управления
    private void InitializeElements() {
        Description1 = findViewById(R.id.description);
        Description2 = findViewById(R.id.description2);
        LogRegButton = findViewById(R.id.logRegButton);
    }

    // переходы на другие активности
    private void SendUserToLoginActivity() {
        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(loginIntent);
        overridePendingTransition(0, 0);
        this.finish();
    }

    private void SendUserToBigList() {
        Intent bigListIntent = new Intent(MainActivity.this, BigList.class);
        startActivity(bigListIntent);
        overridePendingTransition(0, 0);
        this.finish();
    }
}
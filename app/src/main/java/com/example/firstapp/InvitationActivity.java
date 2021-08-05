package com.example.firstapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class InvitationActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference RootRef, CurListUsersRef, UsersRef;

    private String curListName, curListId, temp, emailFriend, friendId;
    private boolean accessExist;

    private TextView invInfo, title_alert_tv;
    private Button addUserButton, cancelButton;
    private ImageButton homeButton, exitButton;
    private EditText inputEmail;
    private CheckBox checkBox_alert;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invitation);

        curListId = getIntent().getExtras().get("listId").toString();
        curListName = getIntent().getExtras().get("listName").toString();

        mAuth = FirebaseAuth.getInstance();
        RootRef = FirebaseDatabase.getInstance().getReference();
        CurListUsersRef = RootRef.child("Lists").child(curListId).child("users");
        UsersRef = RootRef.child("Users");

        InitializeElements();

        // переход на главную страницу
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToBigList();
            }
        });

        // выход из аккаунта
        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOutUser();
            }
        });

        // вывод текста с описанием
        setInfoText(curListName);

        // добавление пользователя для редактирования списка
        addUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addUserAccess();
            }
        });

        // переход обратно в список
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToSmallListActivity();
            }
        });
    }

    // инициализация элементов управления
    private void InitializeElements() {
        invInfo = findViewById(R.id.invitationInfo_tv);
        addUserButton = findViewById(R.id.add_user_btn);
        inputEmail = findViewById(R.id.input_invitation_et);
        cancelButton = findViewById(R.id.cancel_fromInv_button);
        homeButton = findViewById(R.id.home_imgbtn_bar);
        exitButton = findViewById(R.id.exit_imgbtn_bar);
    }

    // добавление пользователя для редактирования списка
    private void addUserAccess() {
        // получаем почту
        emailFriend = inputEmail.getText().toString();
        accessExist = false;
        friendId = "";


        // проверка пустого ввода
        if (!emailFriend.equals("")) {

            // --- проверка, нет ли уже у пользователя доступа к списку
            CurListUsersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        //Toast.makeText(InvitationActivity.this, "Q: " + dataSnapshot.toString(), Toast.LENGTH_SHORT).show();
                        for (DataSnapshot item : dataSnapshot.getChildren()) {
                            if (emailFriend.equals(item.getValue().toString())) {
                                accessExist = true;
                                break;
                                //Toast.makeText(InvitationActivity.this, item.getValue().toString(), Toast.LENGTH_SHORT).show();
                                //Toast.makeText(InvitationActivity.this, "z: " + String.valueOf(accessExist), Toast.LENGTH_SHORT).show();
                            }
                        }

                        // --- случай, когда пользователь еще не имел доступа к этому списку
                        // проверяем существование данной почты в бд
                        if (!accessExist) {
                            UsersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()) {
                                        for (DataSnapshot item : snapshot.getChildren()) {
                                            if (emailFriend.equals(item.child("email").getValue())) {
                                                friendId = item.getKey();
                                                break;
                                            }
                                        }

                                        // если зарегистрирован, открываем доступ
                                        if (!friendId.equals("")) {
                                            UsersRef.child(friendId).child("Lists").child(curListId).child("nameList").setValue(curListName);

                                            CurListUsersRef.child(friendId).setValue(emailFriend);

                                            Toast.makeText(InvitationActivity.this, "Пользователь " + emailFriend + " получил доступ к списку " + curListName, Toast.LENGTH_LONG).show();
                                        }
                                        else {
                                            Toast.makeText(InvitationActivity.this, "Пользователь " + emailFriend + " не зарегистрирован", Toast.LENGTH_LONG).show();
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }
                        else {
                            Toast.makeText(InvitationActivity.this, "У пользователя " + emailFriend + " уже есть доступ к данному списку", Toast.LENGTH_LONG).show();
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
        else {
            Toast.makeText(InvitationActivity.this, "Введите название эл. почты пользователя, которому необходимо предоставить доступ к списку", Toast.LENGTH_LONG).show();
        }
    }

    // установка описательного текста
    private void setInfoText (String curListName) {
        //String listName = getIntent().getExtras().get("listName").toString();
        String listName = " <i>";
        listName = listName.concat(curListName);
        listName = listName.concat("</i>");


       // Spanned temp = Html.fromHtml(getString(R.string.inv_info_text));
        //temp = temp + Html.fromHtml(curListName);

        temp = getString(R.string.inv_info_text);
        temp = temp.concat(listName);

        invInfo.setText(Html.fromHtml(temp));
    }

    // выход из аккаунта
    private void signOutUser() {
        LayoutInflater inflater_sOut = getLayoutInflater();
        View view_signOut = inflater_sOut.inflate(R.layout.layout_dialog_chbox, null);

        // инициализация элементов всплывающего сообщения
        checkBox_alert = view_signOut.findViewById(R.id.checkBox_alert_chBox);
        title_alert_tv = view_signOut.findViewById(R.id.title_alert_chBox);

        title_alert_tv.setText(getString(R.string.exit_account_text));
        checkBox_alert.setText(getString(R.string.sure_exit_text));

        AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(InvitationActivity.this);
        builder
                .setView(view_signOut)
                .setCancelable(true)
                .setNegativeButton("Отмена",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        })
                .setPositiveButton("Ок",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                if (checkBox_alert.isChecked()) {
                                    mAuth.signOut();
                                    dialog.cancel();
                                    SendUserToLoginActivity();
                                }
                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    // на главную страницу
    private void SendUserToBigList() {
        Intent bigListIntent = new Intent(InvitationActivity.this, BigList.class);
        startActivity(bigListIntent);
        overridePendingTransition(0, 0);
    }

    // переход на странцу логина
    private void SendUserToLoginActivity() {
        Intent loginIntent = new Intent(InvitationActivity.this, LoginActivity.class);
        startActivity(loginIntent);
        overridePendingTransition(0, 0);
        this.finish();
    }

    // переходы на другие активности
    private void SendUserToSmallListActivity() {
        finish();
    }
}
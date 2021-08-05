package com.example.firstapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BigList extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference RootRef, CurUserRef, ListsRef, UsersRef;

    private final List<BListStructure> bigList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private BigListAdapter bigListAdapter;
    private RecyclerView userBigList;

    private String curUid, curEmail;

    private TextView email_tv, title_alert_tv, message_alert_tv;
    private EditText inputBigList;
    private Button addBigList;
    private ImageButton homeButton, exitButton;
    private CheckBox checkBox_alert;

    // компоненты видимости
    LinearLayout linearComponentVisible;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_big_list);

        mAuth = FirebaseAuth.getInstance();
        RootRef = FirebaseDatabase.getInstance().getReference();
        ListsRef = RootRef.child("Lists");
        UsersRef = RootRef.child("Users");

        InitializeElements();

        // видимые и невидимые элементы
        findViewById(R.id.loadingBList).setVisibility(View.VISIBLE);
        linearComponentVisible.setVisibility(View.GONE);

        // выход из аккаунта
        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOutUser();
            }
        });

        curUid = mAuth.getCurrentUser().getUid();
        CurUserRef = RootRef.child("Users").child(curUid);

        // создание списка продуктов
        addBigList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNewList();
            }
        });

        // вывод почты
        CurUserRef.child("email").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                curEmail = dataSnapshot.getValue().toString();
                email_tv.setText(curEmail);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        //------------------------------------------------------------------------------------------------------------
        CurUserRef.child("Lists").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot item : snapshot.getChildren()) {
                        //String curUserKey = item.getKey();

                        BListStructure bigListItems = item.getValue(BListStructure.class);
                        bigList.add(bigListItems);
                        bigListAdapter.notifyDataSetChanged();
                    }
                }
                findViewById(R.id.loadingBList).setVisibility(View.GONE);
                linearComponentVisible.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        //------------------------------------------------------------------------------------------------------------



        // отображение списка покупок
//        CurUserRef.child("Lists").addChildEventListener(new ChildEventListener() {
//            @Override
//            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
//                BListStructure bigListItems = dataSnapshot.getValue(BListStructure.class);
//                bigList.add(bigListItems);
//                bigListAdapter.notifyDataSetChanged();
//                //Toast.makeText(BigList.this, bigListItems.toString(), Toast.LENGTH_SHORT).show();
//                //userBigList.smoothScrollToPosition(userBigList.getAdapter().getItemCount());
//            }
//
//            // пустые методы
//            @Override
//            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
//
//            }
//
//            @Override
//            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
//
//            }
//
//            @Override
//            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
//
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        // --- Обработка касаний
        Query query = FirebaseDatabase.getInstance()
                .getReference()
                .child("Users").child(curUid)
                .child("Lists");

        FirebaseRecyclerOptions<BListStructure> options =
                new FirebaseRecyclerOptions.Builder<BListStructure>()
                        .setQuery(query, BListStructure.class)
                        .build();

        FirebaseRecyclerAdapter<BListStructure, BigListViewHolder> adapter = new FirebaseRecyclerAdapter<BListStructure, BigListViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull BigListViewHolder holder, int position, @NonNull BListStructure model) {
                holder.bigListItem.setText(model.getNameList());

                // переход к конкретному списку
                holder.bigListItem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String listId = getRef(holder.getAdapterPosition()).getKey();
                        String listName = model.getNameList();
                        Intent smallListIntent = new Intent(BigList.this, SmallList.class);
                        smallListIntent.putExtra("listId", listId);
                        smallListIntent.putExtra("listName", listName);
                        startActivity(smallListIntent);
                    }
                });

                // удаление списка
                holder.delListButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String listId = getRef(holder.getAdapterPosition()).getKey();
                        String listName = model.getNameList();
                        LayoutInflater inflater_sOut = getLayoutInflater();
                        View view_signOut = inflater_sOut.inflate(R.layout.layout_dialog_chbox, null);

                        // инициализация элементов всплывающего сообщения
                        checkBox_alert = view_signOut.findViewById(R.id.checkBox_alert_chBox);
                        title_alert_tv = view_signOut.findViewById(R.id.title_alert_chBox);

                        // составление сообщения
                        String tempMessage = getString(R.string.sure_del_list_text);
                        tempMessage = tempMessage + " <i>" + listName + "</i>?";

                        // установка заголовка и сообщения
                        title_alert_tv.setText(getString(R.string.del_list_text));
                        checkBox_alert.setText(Html.fromHtml(tempMessage));

                        sureDeleteList(listId, listName);
                    }
                });
            }

            @NonNull
            @Override
            public BigListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom__big_list__layout, parent, false);
                BigListViewHolder viewHolder = new BigListViewHolder(view);
                return viewHolder;
            }
        };

        userBigList.setAdapter(adapter);

        adapter.startListening();
    }

    // инициализация элементов слушателя
    public static class BigListViewHolder extends RecyclerView.ViewHolder {
        TextView bigListItem, delListButton;
        public BigListViewHolder(@NonNull View itemView) {
            super(itemView);
            bigListItem = itemView.findViewById(R.id.bigList_item_tv);
            delListButton = itemView.findViewById(R.id.del_list_button);
        }
    }

    // инициализация элементов управления
    private void InitializeElements() {
        email_tv = findViewById(R.id.email_tv);
        inputBigList = findViewById(R.id.bigList_et);
        addBigList = findViewById(R.id.add_bigList_btn);
        homeButton = findViewById(R.id.home_imgbtn_bar);
        exitButton = findViewById(R.id.exit_imgbtn_bar);

        bigListAdapter = new BigListAdapter(bigList);
        userBigList = findViewById(R.id.bigList_recycler);
        linearLayoutManager = new LinearLayoutManager(this);
        userBigList.setLayoutManager(linearLayoutManager);
        userBigList.setAdapter(bigListAdapter);

        // видимость компонентов
        linearComponentVisible = findViewById(R.id.component_visible_BList);
    }

    // создание нового списка
    private void addNewList() {
        String inputList = inputBigList.getText().toString().trim();
        if (!inputList.isEmpty()) {
            DatabaseReference keyListRef = ListsRef.push();
            String listPushId = keyListRef.getKey();

            HashMap<String, Object> usersAccessList = new HashMap();
            usersAccessList.put(curUid, curEmail);

            RootRef.child("Lists").child(listPushId).child("users").updateChildren(usersAccessList);
            ListsRef.child(listPushId).child("nameList").setValue(inputList);

            CurUserRef.child("Lists").child(listPushId).child("nameList").setValue(inputList);

            Toast.makeText(BigList.this, "Список '" + inputList + "' создан", Toast.LENGTH_SHORT).show();

            inputBigList.setText("");
        }
        else {
            Toast.makeText(BigList.this, "Введите название списка покупок", Toast.LENGTH_SHORT).show();
        }
    }

    // запрос подтверждения удаления списка
    private void sureDeleteList(String listId, String listName) {
        LayoutInflater inflater_sOut = getLayoutInflater();
        View view_signOut = inflater_sOut.inflate(R.layout.layout_dialog_chbox, null);

        // инициализация элементов всплывающего сообщения
        checkBox_alert = view_signOut.findViewById(R.id.checkBox_alert_chBox);
        title_alert_tv = view_signOut.findViewById(R.id.title_alert_chBox);

        // составление сообщения
        String tempMessage = getString(R.string.sure_del_list_text);
        tempMessage = tempMessage + " <i>" + listName + "</i>?";

        // установка заголовка и сообщения
        title_alert_tv.setText(getString(R.string.del_list_text));
        checkBox_alert.setText(Html.fromHtml(tempMessage));

        AlertDialog.Builder delBuilder = new androidx.appcompat.app.AlertDialog.Builder(BigList.this);
        delBuilder
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
                                    deleteList(listId, listName);
                                }
                            }
                        });
        AlertDialog alert = delBuilder.create();
        alert.show();
    }

    // удаление списка
    private void deleteList(String listId, String listName) {
        RootRef.child("Lists").child(listId).child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot item : snapshot.getChildren()) {
                        String curUserKey = item.getKey();

                        // удаление списка у пользователей, которые имеют доступ к списку
                        UsersRef.child(curUserKey).child("Lists").child(listId).getRef().removeValue();
                    }

                    // удаление списка
                    RootRef.child("Lists").child(listId).getRef().removeValue();
                    Toast.makeText(BigList.this,  "Список '" + listName + "' удален", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
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

        AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(BigList.this);
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

    // переходы на другие активности
    private void SendUserToLoginActivity() {
        Intent loginIntent = new Intent(BigList.this, LoginActivity.class);
        startActivity(loginIntent);
        overridePendingTransition(0, 0);
        this.finish();
    }
}
package com.example.firstapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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

public class SmallList extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference RootRef, CurListItemsRef;

    private final List<SListStructure> smallList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private SmallListAdapter smallListAdapter;
    private RecyclerView userSmallList;

    private TextView curList_tv, title_alert_tv, message_alert_tv;
    private EditText inputSmallList, inputEditItem;
    private Button addSmallList;
    private ImageButton homeButton, exitButton, addFriendsButton;
    private CheckBox checkBox, checkBox_alert;

    private String curListId, curListName;

    // компоненты видимости
    LinearLayout linearComponentVisible;

    private boolean here = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_small_list);

        curListId = getIntent().getExtras().get("listId").toString();
        curListName = getIntent().getExtras().get("listName").toString();

        mAuth = FirebaseAuth.getInstance();
        RootRef = FirebaseDatabase.getInstance().getReference();
        CurListItemsRef = RootRef.child("Lists").child(curListId).child("Items");

        InitializeElements();

            // видимые и невидимые элементы
        findViewById(R.id.loadingSList).setVisibility(View.VISIBLE);
        linearComponentVisible.setVisibility(View.GONE);

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

        // выводим название текущего списка
        curList_tv.setText(curListName);

        // добавление продукта в список
        addSmallList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNewItem();
            }
        });

        //---------------------------//////////////////////////////////////////
        RootRef.child("Lists").child(curListId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists() && here) {
                    Toast.makeText(SmallList.this, "Список '" + curListName + "' был удален", Toast.LENGTH_LONG).show();
                    SendUserToBigList();
                    //finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        //------------------------------------------------------------------------------------------------------------
        CurListItemsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot item : snapshot.getChildren()) {
                        //String curUserKey = item.getKey();

                        SListStructure smallListItems = item.getValue(SListStructure.class);
                        smallList.add(smallListItems);
                        smallListAdapter.notifyDataSetChanged();
                    }
                }
                findViewById(R.id.loadingSList).setVisibility(View.GONE);
                linearComponentVisible.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        //------------------------------------------------------------------------------------------------------------



        // отображение списка покупок
//        CurListItemsRef.addChildEventListener(new ChildEventListener() {
//            @Override
//            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
//                SListStructure smallListItems = dataSnapshot.getValue(SListStructure.class);
//               // String smallListItems = dataSnapshot.getValue().toString();
//                //Toast.makeText(SmallList.this, " " + smallListItems.getNameItem(), Toast.LENGTH_SHORT).show();
//                smallList.add(smallListItems);
//                smallListAdapter.notifyDataSetChanged();
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

        // переход на страницу приглашения друга
        addFriendsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToInvitationActivity();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        here = true;

        // --- Обработка касаний
        Query query = FirebaseDatabase.getInstance()
                .getReference()
                .child("Lists").child(curListId).child("Items");

        FirebaseRecyclerOptions<SListStructure> options =
                new FirebaseRecyclerOptions.Builder<SListStructure>()
                        .setQuery(query, SListStructure.class)
                        .build();


        FirebaseRecyclerAdapter<SListStructure, SmallList.SmallListViewHolder> adapter = new FirebaseRecyclerAdapter<SListStructure, SmallList.SmallListViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull SmallList.SmallListViewHolder holder, int position, @NonNull SListStructure model) {
                boolean isDone = model.getDone();

                if (isDone) {
                    String htmlItemText = "<s>" + model.getNameItem() + "</s>";
                    holder.smallListItem.setText(Html.fromHtml(htmlItemText));
                    holder.chBox.setText(getString(R.string.item_checkbox_isDone_emoji));
                    holder.smallLinearItem.setBackground(getDrawable(R.drawable.item_of_list_ischecked_layout));//setBackgroundColor(Color.WHITE);//getColor(R.color.item_is_checked));
                }
                else {
                    holder.smallListItem.setText(model.getNameItem());
                    holder.chBox.setText(getString(R.string.item_checkbox_noDone_emoji));
                    holder.smallLinearItem.setBackground(getDrawable(R.drawable.item_of_list_nochecked_layout));
                }

                // установка/сброс маркера на продукте --- ТЕКСТ (item)
                holder.smallListItem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String itemId = getRef(holder.getAdapterPosition()).getKey();
                        boolean isDoneState = !model.getDone();
                        // установка/сброс маркера
                        RootRef.child("Lists").child(curListId).child("Items").child(itemId).child("done").setValue(isDoneState);
                    }
                });

                // установка/сброс маркера на продукте --- ЧЕКБОКС
                holder.chBox.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String itemId = getRef(holder.getAdapterPosition()).getKey();
                        boolean isDoneState = !model.getDone();
                        // установка/сброс маркера
                        RootRef.child("Lists").child(curListId).child("Items").child(itemId).child("done").setValue(isDoneState);
                    }
                });

                // редактированиие продукта
                holder.editItem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String itemId = getRef(holder.getAdapterPosition()).getKey();
                        String itemName = model.getNameItem();

                        LayoutInflater inflater_sOut = getLayoutInflater();
                        View view_signOut = inflater_sOut.inflate(R.layout.layout_dialog_edit_item, null);

                        inputEditItem = view_signOut.findViewById(R.id.input_edit_item);
                        inputEditItem.setText(itemName);

                        AlertDialog.Builder delBuilder = new androidx.appcompat.app.AlertDialog.Builder(SmallList.this);
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
                                                if (!inputEditItem.equals("")) {
                                                    String newNameItem = inputEditItem.getText().toString();
                                                    updateItem(itemId, newNameItem);
                                                }
                                                else {
                                                    Toast.makeText(SmallList.this, "Название продукта не может быть незаполненным", Toast.LENGTH_LONG).show();
                                                }
                                            }
                                        });
                        AlertDialog alert = delBuilder.create();
                        alert.show();
                    }
                });

                // удаление продукта из списка
                holder.delItemButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String itemId = getRef(holder.getAdapterPosition()).getKey();
                        String itemName = model.getNameItem();

                        LayoutInflater inflater_sOut = getLayoutInflater();
                        View view_signOut = inflater_sOut.inflate(R.layout.layout_dialog_chbox, null);

                        // инициализация элементов всплывающего сообщения
                        checkBox_alert = view_signOut.findViewById(R.id.checkBox_alert_chBox);
                        title_alert_tv = view_signOut.findViewById(R.id.title_alert_chBox);

                        // составление сообщения
                        String tempMessage = getString(R.string.sure_del_item_text);
                        tempMessage = tempMessage + " <i>" + itemName + "</i>?";

                        // установка заголовка и сообщения
                        title_alert_tv.setText(getString(R.string.del_item_text));
                        checkBox_alert.setText(Html.fromHtml(tempMessage));

                        //Toast.makeText(SmallList.this, itemId, Toast.LENGTH_SHORT).show();

                        sureDeleteItem(itemId, itemName);
                    }
                });
            }

            @NonNull
            @Override
            public SmallList.SmallListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom__small_list__layout, parent, false);
                SmallList.SmallListViewHolder viewHolder = new SmallList.SmallListViewHolder(view);
                return viewHolder;
            }
        };

        userSmallList.setAdapter(adapter);

        adapter.startListening();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        here = false;
    }

    // инициализация элементов слушателя
    public static class SmallListViewHolder extends RecyclerView.ViewHolder {
        TextView smallListItem, delItemButton, chBox, editItem;
        LinearLayout smallLinearItem;
        public SmallListViewHolder(@NonNull View itemView) {
            super(itemView);
            smallListItem = itemView.findViewById(R.id.smallList_item_tv);
            delItemButton = itemView.findViewById(R.id.del_item_button);
            chBox = itemView.findViewById(R.id.item_checkBox);
            editItem = itemView.findViewById(R.id.edit_item_button);
            smallLinearItem = itemView.findViewById(R.id.small_linear_item);
        }
    }

    // инициализация элементов управления
    private void InitializeElements() {
        curList_tv = findViewById(R.id.curList_tv);
        inputSmallList = findViewById(R.id.smallList_et);
        addSmallList = findViewById(R.id.add_smallList_btn);
        homeButton = findViewById(R.id.home_imgbtn_bar);
        exitButton = findViewById(R.id.exit_imgbtn_bar);
        addFriendsButton = findViewById(R.id.addFriends_iBtn);

        smallListAdapter = new SmallListAdapter(smallList);
        userSmallList = findViewById(R.id.smallList_recycler);
        linearLayoutManager = new LinearLayoutManager(this);
        userSmallList.setLayoutManager(linearLayoutManager);
        userSmallList.setAdapter(smallListAdapter);

        // видимость компонентов
        linearComponentVisible = findViewById(R.id.component_visible);
    }

    // добавление нового продукта
    private void addNewItem() {
        String inputItem = inputSmallList.getText().toString().trim();
        if (!inputItem.isEmpty()) {
            DatabaseReference keyItemRef = CurListItemsRef.push(); //CurUserRef.child("Lists").push();
            String itemPushId = keyItemRef.getKey();

            HashMap<String, Object> newListItem = new HashMap();
            newListItem.put("done", false);
            newListItem.put("nameItem", inputItem);

            CurListItemsRef.child(itemPushId).updateChildren(newListItem);

            Toast.makeText(SmallList.this, "Продукт '" + inputItem + "' добавлен в список", Toast.LENGTH_SHORT).show();

            inputSmallList.setText("");
        }
        else {
            Toast.makeText(SmallList.this, "Введите название продукта", Toast.LENGTH_SHORT).show();
        }
    }

    // редактирование
//    private void showEditAlert(String itemId, String itemName) {
//        LayoutInflater inflater_sOut = getLayoutInflater();
//        View view_signOut = inflater_sOut.inflate(R.layout.layout_dialog_edit_item, null);
//
//        inputEditItem = view_signOut.findViewById(R.id.input_edit_item);
//        inputEditItem.setText(itemName);
//
//        AlertDialog.Builder delBuilder = new androidx.appcompat.app.AlertDialog.Builder(SmallList.this);
//        delBuilder
//                .setView(view_signOut)
//                .setCancelable(true)
//                .setNegativeButton("Отмена",
//                        new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int id) {
//                                dialog.cancel();
//                            }
//                        })
//                .setPositiveButton("Ок",
//                        new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int id) {
//                                if (!inputEditItem.equals("")) {
//                                    model.setDone(false);
//                                    String newNameItem = inputEditItem.getText().toString();
//                                    updateItem(itemId, newNameItem);
//                                }
//                                else {
//                                    Toast.makeText(SmallList.this, "Название продукта не может быть незаполненным", Toast.LENGTH_LONG).show();
//                                }
//                            }
//                        });
//        AlertDialog alert = delBuilder.create();
//        alert.show();
//    }

    // запрос подтверждения удаления списка
    private void sureDeleteItem(String itemId, String itemName) {
        LayoutInflater inflater_sOut = getLayoutInflater();
        View view_signOut = inflater_sOut.inflate(R.layout.layout_dialog_chbox, null);

        // инициализация элементов всплывающего сообщения
        checkBox_alert = view_signOut.findViewById(R.id.checkBox_alert_chBox);
        title_alert_tv = view_signOut.findViewById(R.id.title_alert_chBox);

        // составление сообщения
        String tempMessage = getString(R.string.sure_del_item_text);
        tempMessage = tempMessage + " <i>" + itemName + "</i>?";

        // установка заголовка и сообщения
        title_alert_tv.setText(getString(R.string.del_item_text));
        checkBox_alert.setText(Html.fromHtml(tempMessage));

        AlertDialog.Builder delBuilder = new androidx.appcompat.app.AlertDialog.Builder(SmallList.this);
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
                                    deleteItem(itemId, itemName);
                                }
                            }
                        });
        AlertDialog alert = delBuilder.create();
        alert.show();
    }

    // обновление параметров продукта
    private void updateItem(String itemId, String newNameItem) {
        HashMap<String, Object> newItemInfo = new HashMap();
        newItemInfo.put("done", false);
        newItemInfo.put("nameItem", newNameItem);

        CurListItemsRef.child(itemId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                snapshot.getRef().updateChildren(newItemInfo);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    // удаление списка
    private void deleteItem(String itemId, String itemName) {
        RootRef.child("Lists").child(curListId).child("Items").child(itemId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    snapshot.getRef().removeValue();
                    Toast.makeText(SmallList.this,  "Продукт '" + itemName + "' удален", Toast.LENGTH_SHORT).show();
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

        AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(SmallList.this);
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

    // переходы на страницу приглашения
    private void SendUserToInvitationActivity() {
        Intent invIntent = new Intent(SmallList.this, InvitationActivity.class);
        invIntent.putExtra("listName", curListName);
        invIntent.putExtra("listId", curListId);
        startActivity(invIntent);
        overridePendingTransition(0, 0);
    }

    // переход на странцу логина
    private void SendUserToLoginActivity() {
        Intent loginIntent = new Intent(SmallList.this, LoginActivity.class);
        startActivity(loginIntent);
        overridePendingTransition(0, 0);
        this.finish();
    }

    // на главную страницу
    private void SendUserToBigList() {
        //Intent bigListIntent = new Intent(SmallList.this, BigList.class);
        //bigListIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        //startActivity(bigListIntent);
        //overridePendingTransition(0, 0);
        here = false;
        finish();
    }
}
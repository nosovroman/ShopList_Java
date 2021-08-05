package com.example.firstapp;

import android.graphics.Color;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class SmallListAdapter extends RecyclerView.Adapter<SmallListAdapter.SmallListViewHolder> {
    private List<SListStructure> userSmallList;
    private FirebaseAuth mAuth;
    private DatabaseReference userRef;

    public SmallListAdapter(List<SListStructure> userSmallList) {
        this.userSmallList = userSmallList;
    }

    public class SmallListViewHolder extends RecyclerView.ViewHolder {
        public TextView smallListItem;

        public SmallListViewHolder(@NonNull View itemView) {
            super(itemView);
            smallListItem = itemView.findViewById(R.id.smallList_item_tv);
        }
    }

    @NonNull
    @Override
    public SmallListViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.custom__small_list__layout, viewGroup, false);
        mAuth = FirebaseAuth.getInstance();
        return new SmallListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final SmallListViewHolder smallListViewHolder, final int position) {

        SListStructure items = userSmallList.get(position);

        String messageSenderId = mAuth.getCurrentUser().getUid();

        userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(messageSenderId);
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        smallListViewHolder.smallListItem.setVisibility(View.VISIBLE);
        smallListViewHolder.smallListItem.setTextColor(Color.BLACK);
        smallListViewHolder.smallListItem.setText(items.getNameItem());
        smallListViewHolder.smallListItem.setMovementMethod(LinkMovementMethod.getInstance());
    }

    @Override
    public int getItemCount() {
        return userSmallList.size();
    }
}
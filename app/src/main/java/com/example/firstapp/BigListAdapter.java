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

public class BigListAdapter extends RecyclerView.Adapter<BigListAdapter.BigListViewHolder> {
    private List<BListStructure> userBigList;
    private FirebaseAuth mAuth;
    private DatabaseReference userRef;

    public BigListAdapter(List<BListStructure> userBigList) {
        this.userBigList = userBigList;
    }

    public class BigListViewHolder extends RecyclerView.ViewHolder {
        public TextView bigListItem;

        public BigListViewHolder(@NonNull View itemView) {
            super(itemView);
            bigListItem = itemView.findViewById(R.id.bigList_item_tv);
        }
    }

    @NonNull
    @Override
    public BigListViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.custom__big_list__layout, viewGroup, false);
        mAuth = FirebaseAuth.getInstance();
        return new BigListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final BigListViewHolder bigListViewHolder, final int position) {

        BListStructure items = userBigList.get(position);

        bigListViewHolder.bigListItem.setVisibility(View.VISIBLE);
        //messageViewHolder.receiverMessageText.setBackgroundResource(R.drawable.receiver_messages_layout);
        bigListViewHolder.bigListItem.setTextColor(Color.BLACK);
        bigListViewHolder.bigListItem.setText(items.getNameList());
        bigListViewHolder.bigListItem.setMovementMethod(LinkMovementMethod.getInstance());
    }

    @Override
    public int getItemCount() {
        return userBigList.size();
    }
}
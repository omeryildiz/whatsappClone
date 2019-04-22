package com.example.vihaan.whatsappclone.ui.groupscreen;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.vihaan.whatsappclone.R;

import java.util.ArrayList;

public class GroupChatAdapter extends RecyclerView.Adapter<GroupChatViewHolder> {

    ArrayList<GroupChatMessage> mGroupChatMessagesList;
    LayoutInflater inflater;

    public GroupChatAdapter(Context context, ArrayList<GroupChatMessage> messageList) {
        inflater = LayoutInflater.from(context);
        this.mGroupChatMessagesList = messageList;

    }

    @Override
    public GroupChatViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_chat_group_sent_message, parent, false);
        GroupChatViewHolder holder = new GroupChatViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(GroupChatViewHolder holder, int position) {
        GroupChatMessage selectedProduct = mGroupChatMessagesList.get(position);
        holder.setData(selectedProduct, position);

    }

    @Override
    public int getItemCount() {
        return mGroupChatMessagesList.size();
    }
}

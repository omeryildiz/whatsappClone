package com.example.vihaan.whatsappclone.ui.groupscreen;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.example.vihaan.whatsappclone.R;

class GroupChatViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private TextView userName;
    private TextView messageBlock;
    private TextView dateTimeValue;

    public GroupChatViewHolder(View itemView) {
        super(itemView);
        userName = (TextView) itemView.findViewById(R.id.groupChatName);
        messageBlock = (TextView) itemView.findViewById(R.id.groupChatTv);
        dateTimeValue = (TextView) itemView.findViewById(R.id.groupChatDate);
    }

    public void setData(GroupChatMessage message, int position) {
        this.userName.setText(message.getName());
        this.messageBlock.setText(message.getMessage());
        this.dateTimeValue.setText(message.getStringDate()+ " " + message.getStringTime());
    }

    @Override
    public void onClick(View v) {

    }
}

package com.example.vihaan.whatsappclone.ui.userListScreen;

import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.vihaan.whatsappclone.R;
import com.example.vihaan.whatsappclone.ui.models.User;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

/**
 * Created by vihaan on 18/06/17.
 */

public class UserViewHolder extends RecyclerView.ViewHolder {

    public ImageView userIV;
    public TextView usernameTV;
    public TextView statusTV;
    public TextView onlinePoint;

    public UserViewHolder(View itemView) {
        super(itemView);

        userIV = (ImageView) itemView.findViewById(R.id.userIV);
        usernameTV = (TextView) itemView.findViewById(R.id.userNameTV);
        statusTV = (TextView) itemView.findViewById(R.id.statusTV);
        onlinePoint = (TextView) itemView.findViewById(R.id.group_status_bar);
    }

    public void bindToUser(User user, View.OnClickListener starClickListener) {

        if(!TextUtils.isEmpty(user.getProfilePicUrl()))
        {
            Picasso.with(userIV.getContext()).load(user.getProfilePicUrl()).into(userIV);
        }
        usernameTV.setText(user.getName());
        if(user.getStatus().equals("Online")) {
            onlinePoint.setText("  ");
            onlinePoint.setVisibility(TextView.VISIBLE);
        } else {
            onlinePoint.setVisibility(TextView.INVISIBLE);
        }
        //statusTV.setText(user.getStatus());


    }
}

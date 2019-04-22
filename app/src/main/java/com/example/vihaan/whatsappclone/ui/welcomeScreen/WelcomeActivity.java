package com.example.vihaan.whatsappclone.ui.welcomeScreen;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.example.vihaan.whatsappclone.R;
import com.example.vihaan.whatsappclone.ui.createUserScreen.CreateUserActivity;
import com.example.vihaan.whatsappclone.ui.homescreen.MainActivity;
import com.example.vihaan.whatsappclone.ui.verifyPhoneScreen.VerifyPhoneActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by vihaan on 15/06/17.
 */

public class WelcomeActivity extends AppCompatActivity implements View.OnClickListener{

    private String currentRole;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        findViewById(R.id.agreeNContinueTVBtn).setOnClickListener(this);
        init();
        getUserInfo();

    }

    private FirebaseAuth mAuth;
    private void init()
    {
        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getCurrentUser().getUid());
    }


    @Override
    public void onClick(View v) {
        switch(v.getId())
        {
            case R.id.agreeNContinueTVBtn:
                Intent intent = new Intent(this, CreateUserActivity.class);
                intent.putExtra("userRole", currentRole);
                startActivity(intent);
                finish();
                break;
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        // Check auth on Activity start
        if (mAuth.getCurrentUser() != null) {
            onAuthSuccess(mAuth.getCurrentUser());
        }
    }

    private void onAuthSuccess(FirebaseUser user) {
        startActivity(new Intent(WelcomeActivity.this, MainActivity.class));
        finish();
    }

    public void getUserInfo() {



        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    currentRole = dataSnapshot.child("role").getValue().toString();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }
}

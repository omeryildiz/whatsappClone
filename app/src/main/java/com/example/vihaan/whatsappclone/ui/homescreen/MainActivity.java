package com.example.vihaan.whatsappclone.ui.homescreen;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.vihaan.whatsappclone.R;
import com.example.vihaan.whatsappclone.ui.chatscreen.ChatActivity;
import com.example.vihaan.whatsappclone.ui.common.adapters.ViewPagerTabAdapter;
import com.example.vihaan.whatsappclone.ui.groupscreen.GroupsFragment;
import com.example.vihaan.whatsappclone.ui.models.User;
import com.example.vihaan.whatsappclone.ui.userListScreen.UserListActivity;
import com.example.vihaan.whatsappclone.ui.welcomeScreen.WelcomeActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import static com.example.vihaan.whatsappclone.ui.chatscreen.ChatActivity.tag;

public class MainActivity extends AppCompatActivity {
    private String currentRole;
    private DatabaseReference usersRef;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //currentUserRole = getIntent().getExtras().get("userRole").toString();
        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getCurrentUser().getUid());
        getUserInfo();
        init();
    }

    private void init() {
        initData();
        initViews();
    }

    private void initData() {
        Bundle bundle = getIntent().getExtras();
        if(bundle != null)
        {
            User user = bundle.getParcelable(ChatActivity.EXTRAS_USER);
            if(user != null)
            {
                Intent intent = new Intent(this, ChatActivity.class);
                intent.putExtra(ChatActivity.EXTRAS_USER, user);
                startActivity(intent);
            }
        }
    }

    private void initViews() {
        initToolbar();
        initNewMessageFloatingButton();
        initViewPager();
        initTabLayout();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    private void initNewMessageFloatingButton() {
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, UserListActivity.class);
                startActivity(intent);
            }
        });
    }

    private ViewPager mViewPager;

    private void initViewPager() {
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        List<String> tabNames = new ArrayList<String>();
        tabNames.add("Chats");
        tabNames.add("Groups");
        //tabNames.add("Status");
        //tabNames.add("Calls");
        ViewPagerTabAdapter viewPagerTabAdapter = new ViewPagerTabAdapter(getSupportFragmentManager(), getFragments(), tabNames);
        mViewPager.setOffscreenPageLimit(2);
        mViewPager.setAdapter(viewPagerTabAdapter);
    }


    private void initTabLayout() {
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
        tabLayout.setSelectedTabIndicatorColor(Color.parseColor("#FFFFFF"));
    }


    private List<Fragment> mFragments;

    private List<Fragment> getFragments() {

        mFragments = new ArrayList<Fragment>();
        mFragments.add(UserChatsFragment.newInstance(""));
        mFragments.add(GroupsFragment.newInstance());
        //mFragments.add(StatusFragment.newInstance());
        //mFragments.add(CallsFragment.newInstance());

        return mFragments;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_logout) {
            DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();;
            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            try {
            mDatabase.child("users").child(firebaseUser.getUid()).child("status").setValue("Offline");
            }catch (Exception e) {
                e.printStackTrace();
            }
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, WelcomeActivity.class));
            finish();
            return true;
        }

        if (id == R.id.action_create_group) {
            RequestNewGroup();
        }

        if (id == R.id.set_duration_time) {
            if (currentRole.equals("admin"))
                setDurationTime();
        }

        return super.onOptionsItemSelected(item);
    }

    private void RequestNewGroup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.AlertDialog);
        builder.setTitle("Enter group name :");
        final EditText groupNameField = new EditText(MainActivity.this);
        groupNameField.setHint("Pro-pro");
        builder.setView(groupNameField);
        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String groupName = groupNameField.getText().toString();
                if (TextUtils.isEmpty(groupName)) {
                    Toast.makeText(MainActivity.this, "Please write group name...", Toast.LENGTH_SHORT).show();
                } else {
                    CreateGroup(groupName);

                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();

    }

    private void setDurationTime() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.AlertDialog);
        builder.setTitle("Enter the time in seconds :");
        final EditText timeField = new EditText(MainActivity.this);
        timeField.setHint("10");
        builder.setView(timeField);
        builder.setPositiveButton("Set", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String time = timeField.getText().toString();
                if (TextUtils.isEmpty(time)) {
                    Toast.makeText(MainActivity.this, "Please write time in seconds...", Toast.LENGTH_SHORT).show();
                } else {
                    SetTimeInFirebase(time);

                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();

    }

    private DatabaseReference rootRef;

    private void CreateGroup(final String groupName) {
        rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Groups").child(groupName).setValue("")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(MainActivity.this,  groupName + " is created successfully.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void SetTimeInFirebase(final String time) {
        rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("state").child("duration").setValue(time)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(MainActivity.this,  time + " is created successfully.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(tag, " onStart()");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(tag, " onResume()");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(tag, " onPause()");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(tag, " onStop()");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(tag, " onDestroy()");

    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
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
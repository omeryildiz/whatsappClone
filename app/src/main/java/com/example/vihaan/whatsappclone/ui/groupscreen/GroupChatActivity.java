package com.example.vihaan.whatsappclone.ui.groupscreen;

import android.app.ProgressDialog;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.vihaan.whatsappclone.R;
import com.example.vihaan.whatsappclone.ui.Database;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

public class GroupChatActivity extends AppCompatActivity {

    private  Toolbar mToolbar;

    private RecyclerView displayGroupMessage;
    private ArrayAdapter<String> arrayAdapter;
    private ArrayList<String> listOfGroups = new ArrayList<>();
    private ArrayList<GroupChatMessage> groupChatList;

    private FloatingActionButton sendMessageButton, sendMicRecordButton, sendTimerButton;
    private EditText userMessageInput;
    private ScrollView mScrollView;
    private TextView displayTextMessages;
    private FirebaseAuth mAuth;
    private static DatabaseReference usersRef, groupNameRef, groupMessageKeyRef, statusRef;
    private static String currentGroupName, currentUserId, currentUserName, currentDate, currentTime,currentTimeSettings, currentUserRole, timerStatus = "";
    private StorageReference mStorage;

    private ProgressDialog mProgress;


    private  MediaRecorder mRecorder = null;
    private  String mFilename = null;
    private  static  final String LOG_TAG = "Record_log";

    private static final String SEND_IMAGE = "send_image";
    private static final String MIC_IMAGE = "mic_image";
    private TextView groupHeaderChatName;
    private TextView groupFooterChatDateTime;
    private Integer timeSetting = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);
        currentGroupName = getIntent().getExtras().get("groupName").toString();
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        currentUserName = mAuth.getCurrentUser().getEmail();
        mAuth.getCurrentUser().getDisplayName();
        mStorage = FirebaseStorage.getInstance().getReference();
        mProgress = new ProgressDialog(this);


        usersRef = FirebaseDatabase.getInstance().getReference().child("users").child(currentUserId);
        statusRef = FirebaseDatabase.getInstance().getReference().child("state");
        groupNameRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(currentGroupName);


        currentGroupName = getIntent().getExtras().get("groupName").toString();
        currentUserRole = getIntent().getExtras().get("userRole").toString();
        timerStatus = getIntent().getExtras().get("status").toString();
        currentTimeSettings = getIntent().getExtras().get("setTime").toString();
        try{
            timeSetting = Integer.parseInt(currentTimeSettings);

        }catch (Exception e) {
            e.printStackTrace();
        }
        timeSetting *= 1000;

        Toast.makeText(GroupChatActivity.this, currentGroupName, Toast.LENGTH_SHORT).show();
        //getUserInfo();
        initializeFields();
        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String tag = (String) sendMessageButton.getTag();
                Log.d("fab tag", tag);
                if (tag.equalsIgnoreCase(SEND_IMAGE)) {
                    saveMessageInfo2Database();
                    userMessageInput.setText("");
                }

            }
        });

        sendTimerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                statusRef.child("status").setValue((String) "true");
            }
        });

        sendMicRecordButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    startRecording();
                    return true;
                } else if (event.getAction() == MotionEvent.ACTION_UP){
                    stopRecording();
                    return true;
                }
                return false;

            }
        });
    }

private  void startRecording() {
    Toast.makeText(GroupChatActivity.this, "Starting record process...", Toast.LENGTH_SHORT).show();
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setOutputFile(mFilename);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare failed.");
        }

        mRecorder.start();

}

private void stopRecording() {
        //userMessageInput.setText("Voice record performed successfully...");
        //saveMessageInfo2Database();
        userMessageInput.setText("");
        Toast.makeText(GroupChatActivity.this, "Stopped record process...", Toast.LENGTH_SHORT).show();
    try {
        mRecorder.stop();
        uploadAudio();
    } catch(RuntimeException e) {
        new File (mFilename).delete();  //you must delete the outputfile when the recorder stop failed.
    } finally {
        mRecorder.release();
        mRecorder = null;
        sendMicRecordButton.setVisibility(FloatingActionButton.INVISIBLE);
    }


}

private void uploadAudio() {
        mProgress.setMessage("Uploading audio...");
        //DatabaseReference filePath = audioRef.child("audio").child("new_audio.3gp");
        updateDateAndTime();
        String fileNameOfVoiceRecord = currentTime+"-audio";
        StorageReference filepath = mStorage.child("audio").child(currentUserName).child(fileNameOfVoiceRecord);
        Uri uri = Uri.fromFile(new File(mFilename));
        filepath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                mProgress.dismiss();
                Toast.makeText(GroupChatActivity.this, "Uploading finished.", Toast.LENGTH_SHORT).show();
            }
        });
    }


private void saveMessageInfo2Database() {

        String message = userMessageInput.getText().toString();
        String messageKey = groupNameRef.push().getKey();
        if(!TextUtils.isEmpty(message)) {
            updateDateAndTime();

            HashMap<String, Object> groupMessageKey = new HashMap<>();
            groupNameRef.updateChildren(groupMessageKey);

            groupMessageKeyRef = groupNameRef.child(messageKey);

            HashMap<String, Object> messageInfoMap = new HashMap<>();
            messageInfoMap.put("name", currentUserName);
            messageInfoMap.put("message", message);
            messageInfoMap.put("date", currentDate);
            messageInfoMap.put("time", currentTime);
            groupMessageKeyRef.updateChildren(messageInfoMap);

        } else {
            Log.e(LOG_TAG, "empty message...");
        }
    }

private void updateDateAndTime() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat currentDateFormat = new SimpleDateFormat("dd-MM-yyyy");
        currentDate = currentDateFormat.format(calendar.getTime());

        SimpleDateFormat currentTimeFormat = new SimpleDateFormat("HH:mm:ss");
        currentTime = currentTimeFormat.format(calendar.getTime());
    }


    @Override
    protected void onStart() {
        super.onStart();
        retriveMessages();
        //getUserInfo();

    }

    private void displayMessages(DataSnapshot dataSnapshot) {
        Iterator iterator = dataSnapshot.getChildren().iterator();
        while (iterator.hasNext()) {
            String chatDate = (String) ((DataSnapshot) iterator.next()).getValue();
            String chatMessage = (String) ((DataSnapshot) iterator.next()).getValue();
            String chatname = (String) ((DataSnapshot) iterator.next()).getValue();
            String chatTime = (String) ((DataSnapshot) iterator.next()).getValue();
            groupHeaderChatName.append(chatname);
            displayTextMessages.append(chatMessage);
            groupFooterChatDateTime.append(chatDate + " " + chatTime);

            mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
            displayGroupMessage.notify();

        }
    }

    public void retriveMessages() {
        statusRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                if(dataSnapshot.exists()) {
                    try {
                        timerStatus = dataSnapshot.getValue().toString();
                        if (timerStatus.equals("true")) {

                            sendMicRecordButton.setVisibility(FloatingActionButton.VISIBLE);
                            try {


                                sendMicRecordButton.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        sendMicRecordButton.setVisibility(FloatingActionButton.INVISIBLE);
                                    }
                                }, timeSetting);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            /*
                            sendMicRecordButton.getHandler().postAtTime(new Runnable() {
                                public void run() {
                                    sendMicRecordButton.setVisibility(View.VISIBLE);
                                }
                            }, 10000);
                            */
                            //sendMicRecordButton.setVisibility(FloatingActionButton.VISIBLE);
                            statusRef.child("status").setValue((String) "false");
                        }
                        /*
                        else {
                            sendMicRecordButton.setVisibility(FloatingActionButton.INVISIBLE);
                        }
                        */
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        //asagidakini veritabanindan liste doldurmak icin kullaniyorum.
        groupNameRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if(dataSnapshot.exists()) {
                    Iterator iterator = dataSnapshot.getChildren().iterator();
                    while (iterator.hasNext()) {
                        GroupChatMessage groupChatMessage = new GroupChatMessage();
                        groupChatMessage.setStringDate((String) ((DataSnapshot) iterator.next()).getValue());
                        groupChatMessage.setMessage((String) ((DataSnapshot) iterator.next()).getValue());
                        groupChatMessage.setName((String) ((DataSnapshot) iterator.next()).getValue());
                        groupChatMessage.setStringTime((String) ((DataSnapshot) iterator.next()).getValue());
                        groupChatList.add(groupChatMessage);
                        mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
                    }
                }

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                if(dataSnapshot.exists()) {
                    Iterator iterator = dataSnapshot.getChildren().iterator();
                    while (iterator.hasNext()) {
                        GroupChatMessage groupChatMessage = new GroupChatMessage();
                        groupChatMessage.setStringDate((String) ((DataSnapshot) iterator.next()).getValue());
                        groupChatMessage.setMessage((String) ((DataSnapshot) iterator.next()).getValue());
                        groupChatMessage.setName((String) ((DataSnapshot) iterator.next()).getValue());
                        groupChatMessage.setStringTime((String) ((DataSnapshot) iterator.next()).getValue());
                        groupChatList.add(groupChatMessage);
                        mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
                        displayGroupMessage.notify();
                    }
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void initializeFields() {
       //mToolbar = (Toolbar) findViewById(R.id.group_toolbar);
       //setSupportActionBar(mToolbar);
       //getSupportActionBar().setTitle("Group Name");
        getSupportActionBar().setTitle(currentGroupName);

        displayGroupMessage = (RecyclerView) findViewById(R.id.message_display_group);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        displayGroupMessage.setLayoutManager(linearLayoutManager);
        groupChatList = new ArrayList<>();

        //yukarida doldurulan verileri asagida adaptore yerlesitiryorum.
        GroupChatAdapter adapterOfGroup = new GroupChatAdapter(this,groupChatList);
        //adapteru set ediyorum
        displayGroupMessage.setAdapter(adapterOfGroup);
        adapterOfGroup.notifyDataSetChanged();
        //@todo: diger display ile ilgili kod parcalarinin kapanmasi gerekiyor.

        sendMessageButton = (FloatingActionButton) findViewById(R.id.floating_button);
        sendTimerButton = (FloatingActionButton) findViewById(R.id.floating_button_timer);
        sendMessageButton.setTag(SEND_IMAGE);
        userMessageInput = (EditText) findViewById(R.id.group_edit_text);
        sendMicRecordButton = (FloatingActionButton) findViewById(R.id.floating_button_rec);

        if (currentUserRole.equals("admin")) {
            sendMicRecordButton.setVisibility(FloatingActionButton.INVISIBLE);
            sendTimerButton.setVisibility(FloatingActionButton.VISIBLE);
        }
        else {
            sendMicRecordButton.setVisibility(FloatingActionButton.INVISIBLE);
        }

        updateDateAndTime();
        mFilename = Environment.getExternalStorageDirectory().getAbsolutePath();
        mFilename += "/"+currentDate+"_"+currentTime.replace(':','_')+"_audio.3gp";

        userMessageInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                GroupChatActivity.this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

            }
        });

        userMessageInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (s.length() == 0) {
                    showSendButton();
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                showSendButton();
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 0) {
                    showSendButton();
                }
            }
        });
       mScrollView = (ScrollView) findViewById(R.id.my_scroll_view);



    }


    private void showSendButton() {
        sendMessageButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.input_send));
        sendMessageButton.setTag(SEND_IMAGE);
    }



    private void showAudioButton() {
        sendMessageButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.input_mic_white));
        sendMessageButton.setTag(MIC_IMAGE);
    }
}

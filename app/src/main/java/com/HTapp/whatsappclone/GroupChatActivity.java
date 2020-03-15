package com.HTapp.whatsappclone;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
//import android.widget.Toolbar;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;

public class GroupChatActivity extends AppCompatActivity {

    private Toolbar mtoolbar;
    private EditText messageInput;
    private ImageButton sendMsgBttn;
    private ScrollView mscrollview;
    private TextView displayTextMessage;
    private String currentGroupName, currentUserID, currentUserName, currentdate, currenttime;
    private FirebaseAuth mauth;
    private DatabaseReference userref, groupnameref, groupmessageKeyref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        mauth = FirebaseAuth.getInstance();
        currentUserID = mauth.getCurrentUser().getUid();

        currentGroupName = getIntent().getExtras().get("groupname").toString();

        userref = FirebaseDatabase.getInstance().getReference().child("Users");
        groupnameref = FirebaseDatabase.getInstance().getReference().child("Group").child(currentGroupName);


        initializefields();

        getUserInfo();



        sendMsgBttn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                savemsgInfoDatabse();
                messageInput.setText("");
                mscrollview.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });


    }

    @Override
    protected void onStart() {
        super.onStart();


        groupnameref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                if(dataSnapshot.exists()){
                    Toast.makeText(GroupChatActivity.this, "new", Toast.LENGTH_SHORT).show();
                    displaymessages(dataSnapshot);
                   // mscrollview.fullScroll(ScrollView.FOCUS_DOWN);

                }

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                displaymessages(dataSnapshot);
                //mscrollview.fullScroll(ScrollView.FOCUS_DOWN);
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void initializefields(){

        mtoolbar = (Toolbar) findViewById(R.id.group_chat_bar_layout);
        setSupportActionBar(mtoolbar);
        getSupportActionBar().setTitle(currentGroupName);

        sendMsgBttn = (ImageButton) findViewById(R.id.grp_send_button);
        messageInput = (EditText) findViewById(R.id.input_grp_msg);
        displayTextMessage = (TextView) findViewById(R.id.group_chat_text_display);
        mscrollview = (ScrollView) findViewById(R.id.scroll_view);
        mscrollview.fullScroll(ScrollView.FOCUS_DOWN);

    }

    private void getUserInfo(){

        userref.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists()){
                    currentUserName = dataSnapshot.child("name").getValue().toString();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void savemsgInfoDatabse(){

        String message = messageInput.getText().toString();
        String messageKey = groupnameref.push().getKey();

        if(TextUtils.isEmpty(message)){
            Toast.makeText(this,"Enter message", Toast.LENGTH_SHORT).show();
        }
        else
        {
            Calendar calfordate = Calendar.getInstance();
            SimpleDateFormat curdate = new SimpleDateFormat("MMM dd, yyyy");
            currentdate = curdate.format(calfordate.getTime());

            Calendar calfortime = Calendar.getInstance();
            SimpleDateFormat curtime = new SimpleDateFormat("hh:mm a");
            currenttime = curtime.format(calfordate.getTime());


            HashMap<String, Object> groupmessageKey = new HashMap<>();
            groupnameref.updateChildren(groupmessageKey);

            groupmessageKeyref = groupnameref.child(messageKey);

            HashMap<String, Object>  msgInfoMap = new HashMap<>();
            msgInfoMap.put("name", currentUserName);
            msgInfoMap.put("message", message);
            msgInfoMap.put("date", currentdate);
            msgInfoMap.put("time", currenttime);

            groupmessageKeyref.updateChildren(msgInfoMap);


        }

    }

    private void displaymessages( DataSnapshot dataSnapshot ){

        Iterator iterator = dataSnapshot.getChildren().iterator();

        while (iterator.hasNext()){

            String chatdate = (String) ((DataSnapshot)iterator.next()).getValue();
            String chatmsg = (String) ((DataSnapshot)iterator.next()).getValue();
            String sendername = (String) ((DataSnapshot)iterator.next()).getValue();
            String chattime = (String) ((DataSnapshot)iterator.next()).getValue();

            displayTextMessage.append("\n" + sendername + ":\n" + chatmsg + "\n" + chattime + "\t" + chatdate);

            mscrollview.fullScroll(ScrollView.FOCUS_DOWN);



        }

    }


}

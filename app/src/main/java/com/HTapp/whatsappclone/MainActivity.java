package com.HTapp.whatsappclone;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
//resolve the package to be imported
//import android.support.v7.widget.Toolbar;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;


public class MainActivity extends AppCompatActivity {

    private Toolbar mytoobar;
    private ViewPager myviewpager;
    private TabLayout mytablayout;
    private TabsAccessAdapter mytabaccessadapter;

    private FirebaseAuth myauth;
    private DatabaseReference rooref;
    private String currentUserID;



    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myauth= FirebaseAuth.getInstance();


        rooref = FirebaseDatabase.getInstance().getReference();

        mytoobar=  (Toolbar) findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mytoobar);
        String title = "Whatsappclone";

        // there is an exception here to check how to add tile to this tool bar right now surrounded unser try and catch block to avoid
        // the applicatik to stop  #solved
        try {
            getSupportActionBar().setTitle(title);
        }
        catch (Exception e){
            Toast.makeText(this, "exception at toolbar", Toast.LENGTH_SHORT).show();

        }

        myviewpager = (ViewPager) findViewById(R.id.main_tabs_pager);
        mytabaccessadapter = new TabsAccessAdapter(getSupportFragmentManager());
        myviewpager.setAdapter(mytabaccessadapter);

        mytablayout =(TabLayout)findViewById(R.id.main_tabs);
        mytablayout.setupWithViewPager(myviewpager);

    }

    protected void onStart() {

        super.onStart();
        FirebaseUser currentuser = myauth.getCurrentUser();
        if(currentuser == null){
            SendtoLoginactivity();
        }
        else{
            UpdateUserStatus("Online");
            verifyuserexistence();

        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        FirebaseUser currentuser = myauth.getCurrentUser();
        if (currentuser != null){
            UpdateUserStatus("Offline");

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FirebaseUser currentuser = myauth.getCurrentUser();
        if (currentuser != null){
            UpdateUserStatus("Offline");
        }
    }

    private void verifyuserexistence(){

        String currentUserID = myauth.getCurrentUser().getUid();
        rooref.child("Users").child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if ((dataSnapshot.child("name").exists())){

                    Toast.makeText(MainActivity.this, "Welcome", Toast.LENGTH_SHORT).show();
                }
                else{

                    startSettingsActivity();


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
         super.onOptionsItemSelected(item);

         if(item.getItemId() == R.id.main_find_friend_option){

             startFindFriendsActivity();

         }
         if (item.getItemId() == R.id.main_setting_option){

             startSettingsActivity();

         }
         if (item.getItemId() == R.id.main_creategroup_option){

             requestNewGroup();

         }
         if (item.getItemId() == R.id.main_logout_option){

             UpdateUserStatus("Offline");
             myauth.signOut();
            SendtoLoginactivity();

         }
         // to add a return statement to remove error
        return true;

    }

    private void requestNewGroup(){

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.AlertDialog);
        builder.setTitle("Enter gorup name");
        final EditText  groupNameField = new EditText(MainActivity.this);
        groupNameField.setHint("eg. Trip to XX");
        builder.setView(groupNameField);

        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                String groupname = groupNameField.getText().toString();

                if(TextUtils.isEmpty(groupname)){
                    Toast.makeText(MainActivity.this, "Provide Group Name", Toast.LENGTH_SHORT).show();

                }
                else
                {
                    createNewGroup(groupname);
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

    private void createNewGroup(String groupName){

        rooref.child("Group").child(groupName).setValue("")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(MainActivity.this, "group Created", Toast.LENGTH_SHORT).show();

                        }
                    }
                });
    }

    private void SendtoLoginactivity(){

        Intent loginintent = new Intent(MainActivity.this,LoginActivity.class);
        loginintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginintent);
        finish();
    }
    private void startSettingsActivity(){

        Intent settingintent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(settingintent);

    }

    private void startFindFriendsActivity(){

        Intent findfriendsintent = new Intent(MainActivity.this, FindFriendsActivity.class);
        startActivity(findfriendsintent);
    }

    private void UpdateUserStatus(String state){
        String saveCurrentDate, saveCurrentTime;
        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calendar.getTime());

        HashMap<String, Object> onlineStateMap = new HashMap<>();
        onlineStateMap.put("time",saveCurrentTime);
        onlineStateMap.put("date",saveCurrentDate);
        onlineStateMap.put("state",state);

        currentUserID = myauth.getCurrentUser().getUid();
        rooref.child("Users").child(currentUserID).child("userState")
                .updateChildren(onlineStateMap);



    }

}

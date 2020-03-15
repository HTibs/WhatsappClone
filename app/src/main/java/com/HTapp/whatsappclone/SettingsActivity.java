package com.HTapp.whatsappclone;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

//import android.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private Button updateAccount;
    private EditText username;
    private EditText status;
    private CircleImageView profileimage;

    private String currentUserID;
    private FirebaseAuth myauth;
    private DatabaseReference rooref;

    private static final int galleryPick = 1;

    private StorageReference UserProfileImageRef;
    private ProgressDialog loadingbar;

    private Toolbar settingsToolbar;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        myauth = FirebaseAuth.getInstance();
        currentUserID = myauth.getCurrentUser().getUid();
        rooref = FirebaseDatabase.getInstance().getReference();
        UserProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");

        initializefields();


        updateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                UpdateSettings();

            }
        });
        username.setVisibility(View.VISIBLE);
        // check this small change that ifthe edittext is workinhg or not, it should not be working
        username.setEnabled(false);

        retrieveUserInfo();

        profileimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, galleryPick);


            }
        });


    }

    private  void initializefields(){

        updateAccount = (Button) findViewById(R.id.update_settings_button);
        username = (EditText) findViewById(R.id.set_user_name);
        status = (EditText) findViewById(R.id.set_profile_status);
        profileimage = (CircleImageView) findViewById(R.id.set_profile_image);
        loadingbar = new ProgressDialog(this);
        settingsToolbar = (Toolbar) findViewById(R.id.settings_toolbar);
        setSupportActionBar(settingsToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setTitle("Account Info");


    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==galleryPick && resultCode==RESULT_OK && data!= null){
            Uri ImageUri = data.getData();

            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if(resultCode==RESULT_OK){

                loadingbar.setTitle("Set Profile Image");
                loadingbar.setMessage("Please wait, Image Updating");
                loadingbar.setCanceledOnTouchOutside(false);
                loadingbar.show();

                Uri resultUri = result.getUri();

                StorageReference filepath = UserProfileImageRef.child(currentUserID + ".jpg");

                filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                        if (task.isSuccessful()){
                            Toast.makeText(SettingsActivity.this, "Profile Uploaded Successfully", Toast.LENGTH_SHORT).show();
                            // download url is to be checked
                            final String downloadUrl = UserProfileImageRef.getDownloadUrl().toString();
                            rooref.child("Users").child(currentUserID).child("image")
                                    .setValue(downloadUrl)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if(task.isSuccessful()){
                                                Toast.makeText(SettingsActivity.this, "Image saved in FireBase Datatbase", Toast.LENGTH_SHORT).show();
                                                loadingbar.dismiss();

                                            }
                                            else
                                            {
                                                 String message = task.getException().toString();
                                                Toast.makeText(SettingsActivity.this, "Error"+ message, Toast.LENGTH_SHORT).show();
                                                loadingbar.dismiss();

                                            }

                                        }
                                    });

                        }

                        else
                        {
                            String message = task.getException().toString();
                            Toast.makeText(SettingsActivity.this, "Error" + message, Toast.LENGTH_SHORT).show();
                            loadingbar.dismiss();

                        }

                    }
                });

            }
        }
    }

    private void UpdateSettings(){

        String setusername = username.getText().toString();
        String setsatus = status.getText().toString();

        if(TextUtils.isEmpty(setusername)){

            Toast.makeText(SettingsActivity.this, "Name Required", Toast.LENGTH_SHORT).show();

        }
        if (TextUtils.isEmpty(setsatus)){

            Toast.makeText(SettingsActivity.this, "Status Required", Toast.LENGTH_SHORT).show();

        }
        else
        {
            HashMap<String, Object> profilemap = new HashMap<>();
            profilemap.put("uid", currentUserID);
            profilemap.put("name", setusername);
            profilemap.put("status", setsatus);
            rooref.child("Users").child(currentUserID).updateChildren(profilemap)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()){

                                Toast.makeText(SettingsActivity.this, "Profile Updated", Toast.LENGTH_SHORT).show();
                                startmainActivity();

                            }
                            else{
                                String message = task.getException().toString();
                                Toast.makeText(SettingsActivity.this, "Error"+ message, Toast.LENGTH_SHORT).show();

                            }

                        }
                    });

        }

    }

    private void retrieveUserInfo(){

        rooref.child("Users").child(currentUserID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if((dataSnapshot.exists()) && (dataSnapshot.hasChild("name") && (dataSnapshot.hasChild("image")))){

                            String retrievedname = dataSnapshot.child("name").getValue().toString();
                            String retrievedsatus = dataSnapshot.child("status").getValue().toString();
                            String retrievedimage = dataSnapshot.child("image").getValue().toString();

                            username.setText(retrievedname);
                            status.setText(retrievedsatus);
                           // Picasso.get().load(retrievedimage).into(profileimage);
                            // different version of picasso statement

                            //Picasso.get().load(retrievedimage).resize(250,250).centerCrop().into(profileimage);
                            //using glide library

                            Glide.with(SettingsActivity.this).load(retrievedimage).transform(new CircleCrop()).into(profileimage);


                        }

                        else if((dataSnapshot.exists()) && (dataSnapshot.hasChild("name"))){

                            String retrievedname = dataSnapshot.child("name").getValue().toString();
                            String retrievedsatus = dataSnapshot.child("status").getValue().toString();

                            username.setText(retrievedname);
                            status.setText(retrievedsatus);


                        }
                        else{
                            username.setVisibility(View.VISIBLE);
                            Toast.makeText(SettingsActivity.this, "Update Profile", Toast.LENGTH_SHORT).show();
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void startmainActivity(){

        Intent mainintent = new Intent(SettingsActivity.this, MainActivity.class);
        mainintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainintent);
        finish();
    }

}

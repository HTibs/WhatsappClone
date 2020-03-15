package com.HTapp.whatsappclone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class LoginActivity extends AppCompatActivity {


    private Button loginbutton;
    private ProgressDialog loadingbar;
    private EditText loginusername, loginpassword;
    private TextView forgotpasslink, registerlink, phoneloginlink;
    private FirebaseAuth myauth;
    private DatabaseReference usersRef;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initializefields();
        myauth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");


        registerlink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Startregisteractivity();
            }
        });
        loginbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginauthentication();
            }
        });
        phoneloginlink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent phoneloginIntent = new Intent(LoginActivity.this, PhoneLoginActivity.class);
                startActivity(phoneloginIntent);
            }
        });

    }

    private void initializefields(){

        loginbutton = (Button) findViewById(R.id.login_button);
        loginusername = (EditText)findViewById(R.id.login_username);
        loginpassword = (EditText) findViewById(R.id.login_password);
        forgotpasslink = (TextView) findViewById(R.id.forget_password);
        registerlink = (TextView) findViewById(R.id.register_link);
        phoneloginlink = (TextView) findViewById(R.id.phone_login);
        loadingbar = new ProgressDialog(this);


    }



    private void loginauthentication(){


        String email = loginusername.getText().toString();
        String password = loginpassword.getText().toString();

        if(TextUtils.isEmpty(email)){
            Toast.makeText(this, "Enter Email", Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(password)){
            Toast.makeText(this,"Enter Password,", Toast.LENGTH_SHORT).show();
        }
        else{

            loadingbar.setTitle("Logging In");
            loadingbar.setMessage("Please wait");
            loadingbar.setCanceledOnTouchOutside(true);
            loadingbar.show();


            myauth.signInWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                String currentUserID = myauth.getCurrentUser().getUid();
                                String deviceToken = FirebaseInstanceId.getInstance().getToken();
                                usersRef.child(currentUserID)
                                            .child("device_token").setValue(deviceToken)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if(task.isSuccessful()){
                                                            Startmainactivity();
                                                            Toast.makeText(LoginActivity.this,"Login Successfull", Toast.LENGTH_SHORT);
                                                            loadingbar.dismiss();
                                                        }
                                                    }
                                                });
                            }
                            else{
                                String message = task.getException().toString();
                                Toast.makeText(LoginActivity.this,"Error"+message, Toast.LENGTH_SHORT);
                                loadingbar.dismiss();

                            }
                        }
                    });

        }

    }

    private void Startmainactivity(){

        Intent mainintent = new Intent(LoginActivity.this, MainActivity.class);
        mainintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainintent);
        finish();
    }


    private void Startregisteractivity(){

        Intent registerintent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(registerintent);

    }
}

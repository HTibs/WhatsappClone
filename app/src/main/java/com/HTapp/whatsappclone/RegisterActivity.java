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

public class RegisterActivity extends AppCompatActivity {

    private Button registerbutton;
    private EditText registeruser, registerpass;
    private TextView loginlink;
    private FirebaseAuth myauth;
    private ProgressDialog loadingbar;
    private DatabaseReference rootref;






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        myauth= FirebaseAuth.getInstance();
        rootref = FirebaseDatabase.getInstance().getReference();
        initializefields();

        loginlink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startloginactivity();
            }
        });

        registerbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createnewaccount();
            }
        });

    }

    private void initializefields(){
        registerbutton = (Button) findViewById(R.id.register_button);
        registeruser = (EditText) findViewById(R.id.register_username);
        registerpass = (EditText) findViewById(R.id.register_password);
        loginlink = (TextView) findViewById(R.id.login_link);
        loadingbar = new ProgressDialog(this);

    }

    private void createnewaccount(){

        String email = registeruser.getText().toString();
        String password = registerpass.getText().toString();

        if(TextUtils.isEmpty(email)){
            Toast.makeText(this, "Enter Email", Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(password)){
            Toast.makeText(this,"Enter Password,", Toast.LENGTH_SHORT).show();
        }
        else{
            // aving the data to firebase
            loadingbar.setTitle("Creating Account");
            loadingbar.setMessage("Please wait");
            loadingbar.setCanceledOnTouchOutside(true);
            loadingbar.show();

            myauth.createUserWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){

                                String deviceToken = FirebaseInstanceId.getInstance().getToken();


                                String currentuserID = myauth.getCurrentUser().getUid();
                                rootref.child("Users").child(currentuserID).setValue("");

                                rootref.child("Users").child(currentuserID).child("device_token")
                                        .setValue(deviceToken);

                                startmainactivity();
                                Toast.makeText(RegisterActivity.this, "Account Created", Toast.LENGTH_SHORT).show();
                                loadingbar.dismiss();
                            }
                            else
                            {
                                String message = task.getException().toString();
                                Toast.makeText(RegisterActivity.this,"Error"+ message,Toast.LENGTH_SHORT).show();
                                loadingbar.dismiss();
                            }
                        }
                    });
        }

    }

    private void startloginactivity(){
        Intent loginintent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(loginintent);
    }

    private void startmainactivity(){
        Intent mainintent = new Intent(RegisterActivity.this, MainActivity.class);
        mainintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainintent);
        finish();
    }
}

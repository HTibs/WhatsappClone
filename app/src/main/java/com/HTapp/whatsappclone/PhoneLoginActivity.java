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
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class PhoneLoginActivity extends AppCompatActivity {

    private Button sendVerificationCode, verifyCode;
    private EditText inputPhonenumber, inputverificationCode;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private FirebaseAuth mAuth;
    private ProgressDialog loadingbar;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);


        mAuth = FirebaseAuth.getInstance();
        loadingbar = new ProgressDialog(this);
        initializefields();

        sendVerificationCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                String phonenumber;
                phonenumber = "+91" + inputPhonenumber.getText().toString();

                if(TextUtils.isEmpty(phonenumber)){

                    Toast.makeText(PhoneLoginActivity.this,"Enter Phone Number", Toast.LENGTH_SHORT).show();
                }

                else{


                    loadingbar.setTitle("Phone Verification");
                    loadingbar.setMessage("Please Wait");
                    loadingbar.setCanceledOnTouchOutside(false);
                    loadingbar.show();
                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                            phonenumber,        // Phone number to verify
                            60,                 // Timeout duration
                            TimeUnit.SECONDS,   // Unit of timeout
                            PhoneLoginActivity.this,               // Activity (for callback binding)
                            mCallbacks);        // OnVerificationStateChangedCallbacks


                }

            }
        });


        verifyCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                sendVerificationCode.setVisibility(View.INVISIBLE);
                inputPhonenumber.setVisibility(View.INVISIBLE);

                String verificationCode = inputverificationCode.getText().toString();

                if(TextUtils.isEmpty(verificationCode)){
                    Toast.makeText(PhoneLoginActivity.this, "Enter verification Code", Toast.LENGTH_SHORT).show();

                }
                else{

                    loadingbar.setTitle("COde Verification");
                    loadingbar.setMessage("Matching the entered verification code");
                    loadingbar.setCanceledOnTouchOutside(false);
                    loadingbar.show();

                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, verificationCode);
                    signInWithPhoneAuthCredential(credential);
                }

            }
        });


        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {

                //when verification is completed successfully
                signInWithPhoneAuthCredential(phoneAuthCredential);

            }

            @Override
            public void onVerificationFailed(FirebaseException e) {

                //when verificaiton fails
                loadingbar.dismiss();
                Toast.makeText(PhoneLoginActivity.this, "Enter valid phone number", Toast.LENGTH_SHORT).show();

                sendVerificationCode.setVisibility(View.VISIBLE);
                inputPhonenumber.setVisibility(View.VISIBLE);
                verifyCode.setVisibility(View.INVISIBLE);
                inputverificationCode.setVisibility(View.INVISIBLE);

            }

            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {

                   // Save verification ID and resending token so we can use them later
                loadingbar.dismiss();
                mVerificationId = verificationId;
                mResendToken = token;
                Toast.makeText(PhoneLoginActivity.this, "Code sent successfully", Toast.LENGTH_SHORT).show();


                sendVerificationCode.setVisibility(View.INVISIBLE);
                inputPhonenumber.setVisibility(View.INVISIBLE);
                verifyCode.setVisibility(View.VISIBLE);
                inputverificationCode.setVisibility(View.VISIBLE);


            }
        };






    }



    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            loadingbar.dismiss();
                            Toast.makeText(PhoneLoginActivity.this, "Phone Login Successful", Toast.LENGTH_SHORT).show();
                            sendusertoMainActivity();
                        } else {

                            String message;
                            message = task.getException().toString();
                            Toast.makeText(PhoneLoginActivity.this, "Error" + message, Toast.LENGTH_SHORT ).show();
                            // Sign in failed, display a message and update the UI

                        }
                    }
                });
    }



    private void initializefields(){

        sendVerificationCode = (Button) findViewById(R.id.send_veryfication_code_button);
        verifyCode = (Button) findViewById(R.id.verify_button);
        inputPhonenumber = (EditText) findViewById(R.id.phone_number_input);
        inputverificationCode = (EditText) findViewById(R.id.verificationcode_input);
    }

    private void sendusertoMainActivity(){
        Intent mainactivityintent = new Intent(PhoneLoginActivity.this, MainActivity.class);
        startActivity(mainactivityintent);
        finish();

    }

}

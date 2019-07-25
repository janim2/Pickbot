package com.uber.pickbot;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class Verificationpin extends Activity {

    ImageView forward;
    TextView digitcode;
    String phonenumber;
    FirebaseAuth mAuth;
    String mVerificationId;
    PhoneAuthProvider.ForceResendingToken mResendToken;
    String code;
    EditText codeText;
    ProgressDialog mProgressDialog, verifydialogue;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finishActivity(0);
        }return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verificationpin);
//        getActionBar().setDisplayShowHomeEnabled(true);
//        getActionBar().setDisplayHomeAsUpEnabled(true);

        //keep the screen portrait at all times
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //trying to change the back arrow color
//        final Drawable upArrow = getResources().getDrawable(R.drawable.back);
//        upArrow.setColorFilter(getResources().getColor(R.color.black), PorterDuff.Mode.SRC_ATOP);
//        getActionBar().setHomeAsUpIndicator(upArrow);

        //trying to change the status bar background color
        Window window = getWindow();
        // clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        // finally change the color
        window.setStatusBarColor(ContextCompat.getColor(Verificationpin.this,R.color.black));

        //changing the action bar color
//        getActionBar().setBackgroundDrawable(getDrawable(R.color.white));

//        getActionBar().setTitle("");

        forward = (ImageView)findViewById(R.id.moveforward);
        digitcode = (TextView) findViewById(R.id.digitcode);
        codeText = (EditText) findViewById(R.id.codetext);
        phonenumber = getIntent().getStringExtra("number");

        digitcode.setText("Enter the 6-digit code sent to you on " + phonenumber);

        mAuth = FirebaseAuth.getInstance();

        forward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                code = codeText.getText().toString();
                verifyPhoneNumberWithCode(mVerificationId,code);
//                finish();
//                startActivity(new Intent(Verificationpin.this,Map.class));
            }
        });

    }

    private void verifyPhoneNumberWithCode(String verificationId, String code) {
        // [START verify_with_code]
        verifydialogue = new ProgressDialog(Verificationpin.this);
        verifydialogue.setMessage("Verifying Code...");
        verifydialogue.show();
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        // [END verify_with_code]
            signInWithPhoneAuthCredential(credential);
    }

    private void sendPhoneNumberVerification(String phoneNumber) {
        // [START start_phone_auth]
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks);        // OnVerificationStateChangedCallbacks
        // [END start_phone_auth]
                mProgressDialog = new ProgressDialog(Verificationpin.this);
                mProgressDialog.setMessage("Sending verification code...");
                mProgressDialog.show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        sendPhoneNumberVerification(phonenumber);          // call function for receive OTP 6 digit code
    }

    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verificaiton without
                //     user action.
//                signInWithPhoneAuthCredential(credential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.


                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    // Invalid request

                } else if (e instanceof FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded

                }

                // Show a message and update the UI

            }

            @Override
            public void onCodeSent(String verificationId,
                                   PhoneAuthProvider.ForceResendingToken token) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.

                mProgressDialog.dismiss();
                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;
            }
        };


    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            mProgressDialog.dismiss();

                            //saving userid to database
                            String userid = mAuth.getCurrentUser().getUid();
                            DatabaseReference current_user_db  = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(userid);
                            current_user_db.setValue(true);
                            java.util.Map userinfo = new HashMap();
                            startActivity(new Intent(Verificationpin.this,Map.class));

                            userinfo.put("first_name","Jonathan");
                            userinfo.put("profileImageUrl","");
                            userinfo.put("last_name","Grey");
                            userinfo.put("phone",phonenumber);
                            userinfo.put("email","+ Add Email");
                            userinfo.put("password","******");
                            current_user_db.updateChildren(userinfo);
                        } else {
                            // Sign in failed, display a message and update the UI

                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid

                            }


                        }
                    }
                });
    }
}


package com.uber.pickbot;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.hbb20.CountryCodePicker;

public class getNumber extends Activity {
    LinearLayout sociallayout;
    EditText mobilenumber;
    String smobilenumer;
    FragmentManager fragmentManager = getFragmentManager();
    FirebaseAuth mAuth;
    FirebaseAuth.AuthStateListener firebaseauthlistener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //no title bar
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        //make the screen fill the whole screen. ie full screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_get_number);

        //keep the screen portrait at all times
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mAuth = FirebaseAuth.getInstance();

        firebaseauthlistener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if(user!=null){
                    startActivity(new Intent(getNumber.this,Map.class));
                }
            }
        };

        sociallayout = (LinearLayout)findViewById(R.id.sociallayout);
        mobilenumber = (EditText) findViewById(R.id.mobilenumber);

        mobilenumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fragmentManager.beginTransaction().replace(R.id.getnumberframe,new Numbercapture()).commit();
            }
        });

        sociallayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getNumber.this,Chooseaccount.class));
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(firebaseauthlistener);
    }
}

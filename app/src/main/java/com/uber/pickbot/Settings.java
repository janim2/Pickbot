package com.uber.pickbot;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

public class Settings extends Activity {

    TextView signout,fullname,userphonenumber;
    ProgressDialog progressDialog;
    DatabaseReference mCustomerdatabase;
    String sfirstname,slastname,sphone,userID;
    FirebaseAuth mAuth;
    ImageView goback;
    LinearLayout namelayout;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        //trying to change the status bar background color
        Window window = getWindow();
        // clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        // finally change the color
        window.setStatusBarColor(ContextCompat.getColor(Settings.this,R.color.black));

        mAuth = FirebaseAuth.getInstance();
        userID = mAuth.getCurrentUser().getUid();
        mCustomerdatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(userID);

        progressDialog = new ProgressDialog(this);
        signout = (TextView) findViewById(R.id.signout);
        fullname = (TextView) findViewById(R.id.fullname);
        userphonenumber = (TextView) findViewById(R.id.userphonenumber);
        goback = (ImageView) findViewById(R.id.goback);
        namelayout = (LinearLayout) findViewById(R.id.namelayout);

        getuserInfo();

        namelayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Settings.this,Edit_account.class));
            }
        });

        goback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        signout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder signu  = new AlertDialog.Builder(Settings.this);
                signu.setTitle("Sign out?");
                signu.setMessage("Are you sure you want to sign out?. All local data would be lost. Do you wish to continue?");
                signu.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                signu.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(setSignout()){
                            progressDialog.dismiss();
                            startActivity(new Intent(Settings.this,getNumber.class));
                            finish();
                        }
                    }
                });
                signu.show();
            }
        });
    }

    private void getuserInfo() {
        mCustomerdatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
//                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    java.util.Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if(map.get("first_name") != null && map.get("last_name") != null){
                        sfirstname = map.get("first_name").toString();
                        slastname = map.get("last_name").toString();
                        fullname.setText(sfirstname + " " + slastname);
                    }else{
                        fullname.setText("Jonathan Grey");
                    }

                    if(map.get("phone") != null){
                        sphone = map.get("phone").toString();
                        userphonenumber.setText(sphone);
                    } else{
                        userphonenumber.setText("233 24 567 8900");
                    }
                }else{

                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    public boolean setSignout(){
        progressDialog.setMessage("Signing out");
        progressDialog.show();
        FirebaseAuth.getInstance().signOut();
        return true;
    }
}

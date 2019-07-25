package com.uber.pickbot;

import android.app.Activity;
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
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class Edit_account extends Activity {
    TextView firstname,lastname,phone,email,password;
    DatabaseReference mCustomerdatabase;
    FirebaseAuth mAuth;
    String userID,sfirstname,slastname,sphone,semail,spassword,mprofileImageUrl;
    CircleImageView  profileimage;
    ImageView editprofile,goback;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_account);

        //trying to change the status bar background color
        Window window = getWindow();
        // clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        // finally change the color
        window.setStatusBarColor(ContextCompat.getColor(Edit_account.this,R.color.black));

        mAuth = FirebaseAuth.getInstance();
        userID = mAuth.getCurrentUser().getUid();
        mCustomerdatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(userID);

        firstname = (TextView) findViewById(R.id.fname);
        lastname = (TextView) findViewById(R.id.lname);
        phone = (TextView) findViewById(R.id.phone);
        email = (TextView) findViewById(R.id.email);
        password = (TextView) findViewById(R.id.password_);
        profileimage = (CircleImageView) findViewById(R.id.profile_image);
        editprofile = (ImageView) findViewById(R.id.edit_profile);
        goback = (ImageView) findViewById(R.id.goback);

        goback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        editprofile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent goEditUser = new Intent(Edit_account.this,Update_customer_info.class);
                String ff_name = "",llname = "",user_email ="",pass_word="";
                ff_name = firstname.getText().toString().trim();
                llname = lastname.getText().toString().trim();
                user_email = email.getText().toString().trim();
                pass_word = password.getText().toString().trim();
                goEditUser.putExtra("profileurl",mprofileImageUrl);
                goEditUser.putExtra("ffname",ff_name);
                goEditUser.putExtra("llname",llname);
                goEditUser.putExtra("useremail",user_email);
                goEditUser.putExtra("passwordifany",pass_word);
                startActivity(goEditUser);
            }
        });
        getUserinfo();
    }

    private void getUserinfo() {
        mCustomerdatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
//                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    java.util.Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if(map.get("profileImageUrl") != null || map.get("profileImageUrl") != ""){
                        mprofileImageUrl = map.get("profileImageUrl").toString();
                        Glide.with(getApplication()).load(mprofileImageUrl).into(profileimage);
                    }else{
                        profileimage.setImageDrawable(getResources().getDrawable(R.drawable.profile));
                    }
                    if(map.get("first_name") != null){
                        sfirstname = map.get("first_name").toString();
                        firstname.setText(sfirstname);
                    }else{
                        firstname.setText("Jonathan");
                    }

                    if(map.get("last_name") != null){
                        slastname = map.get("last_name").toString();
                        lastname.setText(slastname);
                    }else{
                        lastname.setText("Grey");
                    }

                    if(map.get("phone") != null){
                        sphone = map.get("phone").toString();
                        phone.setText(sphone);
                    } else{
                        phone.setText("233 24 567 8900");
                    }
                    if(map.get("email") != null){
                        semail = map.get("email").toString();
                        email.setText(semail);
                    }else{
                        email.setText("+ Add mail");
                    }
                    if(map.get("password") != null){
                        spassword = map.get("password").toString();
                        password.setText(spassword);
                    }else{
                        password.setText("******");
                    }

                }else{

                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
}

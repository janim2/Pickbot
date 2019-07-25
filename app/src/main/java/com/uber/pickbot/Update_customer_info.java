package com.uber.pickbot;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class Update_customer_info extends Activity {
    EditText first_name,last_name,phone,email;
    TextView password;
    Button update;
    FirebaseAuth mAuth;
    DatabaseReference mCustomerdatabase;
    String userID;
    String fname,lname,sphone,semail,spassword,profleurl;
    String mprofileImageUrl;
    CircleImageView profileimage;
    Uri resultUri;


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.update_customer_info);


        //trying to change the status bar background color
        Window window = getWindow();
        // clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        // finally change the color
        window.setStatusBarColor(ContextCompat.getColor(Update_customer_info.this,R.color.black));


        first_name = (EditText)findViewById(R.id.first_name);
        last_name = (EditText)findViewById(R.id.lastname);
        phone = (EditText)findViewById(R.id.customerphone);
        email = (EditText) findViewById(R.id.email);
        password = (TextView) findViewById(R.id.pasword_);
        profileimage = (CircleImageView) findViewById(R.id.profile_image);
        update = (Button) findViewById(R.id.update);

        mAuth = FirebaseAuth.getInstance();
        userID = mAuth.getCurrentUser().getUid();
        mCustomerdatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(userID);

        profleurl = getIntent().getStringExtra("profileurl");
        fname = getIntent().getStringExtra("ffname");
        lname = getIntent().getStringExtra("llname");
        semail = getIntent().getStringExtra("useremail");
        spassword = getIntent().getStringExtra("passwordifany");

        Glide.with(getApplication()).load(profleurl).into(profileimage);
        first_name.setText(fname);
        last_name.setText(lname);
        email.setText(semail);
        password.setText(spassword);

        profileimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent,1);
            }
        });

        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               saveuserinformation();
            }
        });
    }

    private void saveuserinformation() {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Updating...");
        progressDialog.show();
        fname = first_name.getText().toString();
        lname = last_name.getText().toString();
        semail = email.getText().toString();
        spassword = password.getText().toString();

        Map userinfo  = new HashMap();
        userinfo.put("first_name",fname);
        userinfo.put("last_name",lname);
        userinfo.put("email",semail);
        userinfo.put("password",spassword);
        mCustomerdatabase.updateChildren(userinfo);

        if(resultUri != null){
            StorageReference filePath = FirebaseStorage.getInstance().getReference().child("profile_images").child(userID);
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getApplication().getContentResolver(),resultUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 20, baos);
            byte[] data = baos.toByteArray();
            UploadTask uploadTask = filePath.putBytes(data);

            uploadTask.addOnFailureListener(new OnFailureListener(){
                @Override
                public void onFailure(@NonNull Exception e) {
                    finish();
                    return;
                }
            });

            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Task<Uri> downloadurl = taskSnapshot.getStorage().getDownloadUrl();
                    Map newImage = new HashMap();
                    newImage.put("profileImageUrl", downloadurl.toString());
                    mCustomerdatabase.updateChildren(newImage);
                    progressDialog.dismiss();
                    finish();
                    return;
                }
            });
        }else{
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1 && resultCode == Activity.RESULT_OK){
            final Uri imageuri = data.getData();
            resultUri = imageuri;
            profileimage.setImageURI(resultUri);
        }
    }
}

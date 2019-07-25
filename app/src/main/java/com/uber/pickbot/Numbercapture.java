package com.uber.pickbot;


import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hbb20.CountryCodePicker;


/**
 * A simple {@link Fragment} subclass.
 */
public class Numbercapture extends Fragment {
    ImageView goback,moveforward;
    LinearLayout moveforwardlayout;
    String countrycode;
    String sphonenumber;
    EditText phoneumber;
    TextView error,wronglength;
    CountryCodePicker countryCodePicker;

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()){
//            case android.R.id.home:
//                isDetached();
//        }return true;
//    }

    public Numbercapture() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View captureimage =  inflater.inflate(R.layout.fragment_numbercapture, container, false);

        //keep the screen portrait at all times
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        goback = (ImageView) captureimage.findViewById(R.id.back);
        moveforward = (ImageView) captureimage.findViewById(R.id.moveforward);
        moveforwardlayout = (LinearLayout) captureimage.findViewById(R.id.moveforwardlayout);
        phoneumber = (EditText) captureimage.findViewById(R.id.mobilenumber);
        error = (TextView) captureimage.findViewById(R.id.error);
        wronglength = (TextView) captureimage.findViewById(R.id.wronglength);
        countryCodePicker = (CountryCodePicker) captureimage.findViewById(R.id.codepicker);

        countrycode = countryCodePicker.getSelectedCountryCodeWithPlus();
        goback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isDetached();
            }
        });
//        phoneumber.requestFocus();

        phoneumber.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_DONE){
                    View view = getActivity().getCurrentFocus();
                    if(view != null){
                         InputMethodManager keyboard = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                         keyboard.hideSoftInputFromWindow(view.getWindowToken(),0);
                    }
                }
                return true;
            }
        });

        moveforwardlayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sphonenumber = phoneumber.getText().toString().trim();
                String fullnumber = countrycode+" "+sphonenumber;
                if(!sphonenumber.equals("") || sphonenumber.length() == 10 || sphonenumber.length() == 9){
                    Intent thenumberverification =  new Intent(getActivity(),Verificationpin.class);
                    thenumberverification.putExtra("number",fullnumber);
                    isDetached();
                    startActivity(thenumberverification);
                }else if(sphonenumber.length() < 9){
                    error.setVisibility(View.GONE);
                    wronglength.setVisibility(View.VISIBLE);
                }
                else{
                    wronglength.setVisibility(View.GONE);
                    error.setVisibility(View.VISIBLE);
                }
            }
        });
        return  captureimage;
    }

}

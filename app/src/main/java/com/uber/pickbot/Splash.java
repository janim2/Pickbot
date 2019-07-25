package com.uber.pickbot;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

public class Splash extends Activity {
    protected boolean _active = true;
    protected  int _splashTime = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //no title bar
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        //keep the screen portrait at all times
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);


        //make the screen fill the whole screen. ie full screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_splash);

        Thread splashTread = new Thread(){
            @Override
            public void run() {
                try{
                    int waited = 0;
                    while(_active && waited < _splashTime){
                        sleep(100);
                        if(_active){
                            waited += 100;
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }finally {
                    startActivity(new Intent(Splash.this,getNumber.class));
                    finish();
                }
            };
        };
        splashTread.start();
    }

}


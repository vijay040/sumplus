package com.vsoftcoders.sum.activity;

import android.app.Dialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;

import com.vsoftcoders.sum.R;
import com.vsoftcoders.sum.util.VsoftApp;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        VsoftApp.Context().SetFullScreen(this);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        Button title= (Button) findViewById(R.id.btnTitle);
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.rotate_anim);
        title.startAnimation(animation);
        Handler h = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message message) {
                return false;
            }
        });

        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(SplashActivity.this, GameMenuActivity.class));
                finish();
            }
        }, 3000);

    }

}

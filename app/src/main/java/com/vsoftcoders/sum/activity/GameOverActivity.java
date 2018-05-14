package com.vsoftcoders.sum.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.vsoftcoders.sum.R;
import com.vsoftcoders.sum.util.AudioPlayer;
import com.vsoftcoders.sum.util.Shprefrences;
import com.vsoftcoders.sum.util.VsoftApp;
import com.vsoftcoders.sum.view.GameView;

/**
 * Created by VIJAY on 4/13/2018.
 */

public class GameOverActivity extends AppCompatActivity implements View.OnClickListener {
    Button btnSound, btnResume, btnHome, btnRestart;
    TextView title;
    Shprefrences sh;
    InterstitialAd mInterstitialAd;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        VsoftApp.Context().SetFullScreen(this);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_setting);
        sh = new Shprefrences(this);
        btnSound = (Button) findViewById(R.id.btnSound);
        btnResume = (Button) findViewById(R.id.btnResume);
        btnHome = (Button) findViewById(R.id.btnHome);
        btnRestart = (Button) findViewById(R.id.btnRestart);
        title = (TextView) findViewById(R.id.title);

        btnSound.setOnClickListener(this);
        btnResume.setOnClickListener(this);
        btnHome.setOnClickListener(this);
        btnRestart.setOnClickListener(this);
        btnResume.setVisibility(View.GONE);
        int score = sh.getInt("TOPSCORE", 0);
        if (score < GameView.mScore)
            sh.setInt("TOPSCORE", GameView.mScore);
        GameView.mScore = 0;
        GameView.mCoinCount = 0;
        title.setText("Game Over");
        if (AudioPlayer.isSoundEnabled) {
            btnSound.setBackground(getResources().getDrawable(R.drawable.circle_drawble_sound_on));
        } else {
            sh.setBoolean("ISENABLED", true);
            btnSound.setBackground(getResources().getDrawable(R.drawable.circle_drawable_sound_off));
        }

        MobileAds.initialize(this, getString(R.string.ADMOB_APP_ID));
        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getString(R.string.FULL_ADMOB_APP_ID));//"ca-app-pub-3940256099942544/1033173712"
        mInterstitialAd.loadAd(new AdRequest.Builder().build());

    }


    @Override
    public void onBackPressed() {

    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.btnSound:
                if (AudioPlayer.isSoundEnabled) {
                    sh.setBoolean("ISENABLED", false);
                    btnSound.setBackground(getResources().getDrawable(R.drawable.circle_drawable_sound_off));
                } else {
                    sh.setBoolean("ISENABLED", true);
                    btnSound.setBackground(getResources().getDrawable(R.drawable.circle_drawble_sound_on));
                }
                AudioPlayer.isSoundEnabled = sh.getBoolean("ISENABLED", false);
                break;
            case R.id.btnResume:
                mInterstitialAd.show();
                finish();
                break;
            case R.id.btnHome:
                mInterstitialAd.show();
                finish();
                break;
            case R.id.btnRestart:
                mInterstitialAd.show();
                GameView.mScore = 0;
                GameView.mCoinCount = 0;
                startActivity(new Intent(GameOverActivity.this, GamePlayActivity.class));
                finish();

                break;
        }

    }
}

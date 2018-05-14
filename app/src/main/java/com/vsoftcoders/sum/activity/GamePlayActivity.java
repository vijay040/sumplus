package com.vsoftcoders.sum.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.vsoftcoders.sum.BuildConfig;
import com.vsoftcoders.sum.R;
import com.vsoftcoders.sum.model.User;
import com.vsoftcoders.sum.util.AudioPlayer;
import com.vsoftcoders.sum.util.Shprefrences;
import com.vsoftcoders.sum.util.VsoftApp;
import com.vsoftcoders.sum.view.GameView;

import org.jsoup.Jsoup;

import java.io.IOException;

import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * Created by Vijay on 10/10/2017.
 */

public class GamePlayActivity extends Activity {
    GameView gameView;
    Button btnSound, btnCrown;
    String currentVersion, onlineVersion;
    AlertDialog alertDialog;
    Button btnRestore;
    public InterstitialAd mInterstitialAd;
    Shprefrences sh;
    public TextView txtScore, txtCoin, txtCounter;

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        VsoftApp.Context().SetFullScreen(this);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_gameview);
        txtScore = (TextView) findViewById(R.id.txtScore);
        txtCoin = (TextView) findViewById(R.id.txtCoin);
        txtCounter = (TextView) findViewById(R.id.txtCounter);
        gameView = (GameView) findViewById(R.id.gameView);
        gameView.isPaused = false;
       /* gameView=new GameView(this);
        setContentView(gameView);*/
        sh = new Shprefrences(this);
        AudioPlayer.isSoundEnabled = sh.getBoolean("ISENABLED", true);
        btnCrown = (Button) findViewById(R.id.btnCrown);
        btnRestore = (Button) findViewById(R.id.btnRestore);
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.rotate_anim);
        btnCrown.startAnimation(animation);
        btnRestore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                GameView.mScore = 0;
                GameView.mCoinCount = 0;
                startActivity(new Intent(GamePlayActivity.this, GamePlayActivity.class));
                mInterstitialAd.show();
            }
        });

        final FirebaseRemoteConfig mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder().setDeveloperModeEnabled(BuildConfig.DEBUG).build();
        mFirebaseRemoteConfig.setConfigSettings(configSettings);
        mFirebaseRemoteConfig.setDefaults(R.xml.default_config);

        long cacheExpiration = 3600;
        mFirebaseRemoteConfig.fetch(cacheExpiration)
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // After config data is successfully fetched, it must be activated before newly fetched
                            // values are returned.
                            mFirebaseRemoteConfig.activateFetched();
                        } else {

                        }
                        onlineVersion = mFirebaseRemoteConfig.getString("version");
                        checkVersionUpdate();
                    }
                });

        MobileAds.initialize(this, getString(R.string.ADMOB_APP_ID));
        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getString(R.string.FULL_ADMOB_APP_ID));//"ca-app-pub-3940256099942544/1033173712"
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
    }

    public View settingPopup(View v) {
        showSettingDialog(false, "Settings");
        return v;
    }

    @Override
    public void onBackPressed() {
        boolean b = sh.getBoolean("ISREGISTERED", false);
        if (b == false) {
            showPlayerDialog();
        } else {
            mInterstitialAd.show();
            if (gameView.timer != null)
                gameView.timer.cancel();
            int score = sh.getInt("TOPSCORE", 0);
            if (score < GameView.mScore)
                sh.setInt("TOPSCORE", GameView.mScore);

            super.onBackPressed();
        }
    }


    private class GetVersionCode extends AsyncTask<Void, String, String> {
        @Override
        protected String doInBackground(Void... voids) {

            String newVersion = null;
            try {
                //newVersion = Jsoup.connect("https://play.google.com/store/apps/details?id=" + getPackageName() + "&hl=it")
                Log.e("getPackageName()", "" + getPackageName());
                newVersion = Jsoup.connect("https://play.google.com/store/apps/details?id=" + getPackageName() + "&hl=en")
                        .timeout(30000)
                        .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                        .referrer("http://www.google.com")
                        .get()
                        .select("div[itemprop=softwareVersion]")
                        .first()
                        .ownText();
                Log.e("onlineVersion", "onlineVersion**********" + newVersion);
                onlineVersion = newVersion;
                return newVersion;
            } catch (Exception e) {
                Log.e("GetVersionCode", "Exception**********" + e.getMessage());
                return newVersion;
            }
        }


        @Override
        protected void onPostExecute(String onlineVersion) {
            super.onPostExecute(onlineVersion);
            if (onlineVersion != null && !onlineVersion.isEmpty()) {
                GamePlayActivity.this.onlineVersion = onlineVersion;
                if (currentVersion != null && onlineVersion != null) {
                    if (Float.valueOf(currentVersion) < Float.valueOf(onlineVersion)) {
                        //showDialog();
                    }
                }
            }
        }
    }

    public void checkVersionUpdate() {
        try {
            currentVersion = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        Log.e("currentVersion" + currentVersion, "******onlineVersion" + onlineVersion);

        if (currentVersion != null && onlineVersion != null) {
            if (Float.valueOf(currentVersion) < Float.valueOf(onlineVersion)) {
                SweetAlertDialog alert = new SweetAlertDialog(GamePlayActivity.this, SweetAlertDialog.WARNING_TYPE);
                alert.setTitleText("New Update");
                alert.setContentText("New Update  Available!\n Please Update Now.");//getString(R.string.version_update_msg)
                alert.setConfirmText("Ok");
                // alert.setCancelable(false);
                alert.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {

                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.vsoftcoders.sum"));
                        startActivity(intent);
                    }
                }).show();


              /*  AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
                // ...Irrelevant code for customizing the buttons and title
                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View dialogView = inflater.inflate(R.layout.version_update_dialog, null);
                Button btnUpgrade = (Button) dialogView.findViewById(R.id.btnUpgrade);
                dialogBuilder.setView(dialogView);
                alertDialog = dialogBuilder.create();
                alertDialog.setCancelable(false);
                alertDialog.show();

                btnUpgrade.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.vsoftcoders.sum"));
                        startActivity(intent);
                        if (alertDialog != null && alertDialog.isShowing())
                            alertDialog.dismiss();
                    }
                });*/
            }
        }
    }


    public void showSettingDialog(boolean isGameOver, String strTitle) {


        gameView.isPaused = true;
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        // ...Irrelevant code for customizing the buttons and title
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.dialog_setting, null);
        btnSound = (Button) dialogView.findViewById(R.id.btnSound);
        Button btnResume = (Button) dialogView.findViewById(R.id.btnResume);
        Button btnHome = (Button) dialogView.findViewById(R.id.btnHome);
        Button btnRestart = (Button) dialogView.findViewById(R.id.btnRestart);
        TextView title = (TextView) dialogView.findViewById(R.id.title);

        MobileAds.initialize(this, getString(R.string.ADMOB_APP_ID));
        AdView mAdView = (AdView) dialogView.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);


        if (isGameOver) {
            btnResume.setVisibility(View.GONE);
            int score = sh.getInt("TOPSCORE", 0);
            if (score < GameView.mScore)
                sh.setInt("TOPSCORE", GameView.mScore);
            GameView.mScore = 0;
            GameView.mCoinCount = 0;
        } else
            btnResume.setVisibility(View.VISIBLE);
        title.setText(strTitle);
        dialogBuilder.setView(dialogView);
        alertDialog = dialogBuilder.create();
        alertDialog.setCancelable(true);
        alertDialog.show();

        if (AudioPlayer.isSoundEnabled)
            btnSound.setBackground(getResources().getDrawable(R.drawable.circle_drawble_sound_on));
        else
            btnSound.setBackground(getResources().getDrawable(R.drawable.circle_drawable_sound_off));
        btnSound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (AudioPlayer.isSoundEnabled) {
                    sh.setBoolean("ISENABLED", false);
                    btnSound.setBackground(getResources().getDrawable(R.drawable.circle_drawable_sound_off));
                } else {
                    sh.setBoolean("ISENABLED", true);
                    btnSound.setBackground(getResources().getDrawable(R.drawable.circle_drawble_sound_on));
                }
                AudioPlayer.isSoundEnabled = sh.getBoolean("ISENABLED", false);

            }
        });

        btnRestart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
                mInterstitialAd.show();
                gameView.isPaused = false;
                if (gameView.timer != null)
                    gameView.timer.cancel();
                finish();
                GameView.mScore = 0;
                GameView.mCoinCount = 0;
                startActivity(new Intent(GamePlayActivity.this, GamePlayActivity.class));
            }
        });
        btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mInterstitialAd.show();
                gameView.isPaused = false;
                alertDialog.dismiss();
                if (gameView.timer != null)
                    gameView.timer.cancel();
                finish();
            }
        });
        btnResume.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
                gameView.isPaused = false;

            }
        });

        alertDialog.setOnKeyListener(new Dialog.OnKeyListener() {

            @Override
            public boolean onKey(DialogInterface arg0, int keyCode,
                                 KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    gameView.isPaused = false;
                    return true;
                }
                return false;
            }
        });

    }


    public void showPlayerDialog() {
        boolean b = sh.getBoolean("ISREGISTERED", false);
        if (b == false) {
            final Dialog dialog = new Dialog(GamePlayActivity.this, android.R.style.Theme_Black);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.requestWindowFeature(Window.FEATURE_PROGRESS);
            dialog.setContentView(R.layout.register_player_dialog);
            dialog.getWindow().setBackgroundDrawableResource(
                    R.color.transparent);
            dialog.setCancelable(false);
            dialog.show();
            Button submit = (Button) dialog.findViewById(R.id.btnSubmit);
            final EditText edtName = (EditText) dialog.findViewById(R.id.edtName);
            final EditText edtCountry = (EditText) dialog.findViewById(R.id.edtCountry);
            final EditText edtEmai = (EditText) dialog.findViewById(R.id.edtEmai);
            gameView.isPaused = true;
            submit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String name = edtName.getText() + "";
                    String country = edtCountry.getText() + "";
                    String email = edtEmai.getText() + "";

                    if (name.length() < 2) {
                        edtName.setError("Enter Valid Name!");
                        return;
                    } else if (country.length() < 2) {
                        edtCountry.setError("Enter Valid Country Name!");
                        return;
                    } else if (email.length() > 1 && isValidEmail(email) == false) {
                        edtEmai.setError("Enter Valid Email!");
                        return;
                    }

                    addUser(name, country, email);
                    dialog.dismiss();
                    gameView.isPaused = false;
                }
            });
        }
    }

    public void addUser(String name, String country, String email) {
        String android_id = Settings.Secure.getString(getContentResolver(),
                Settings.Secure.ANDROID_ID);
        User user = new User();
        user.setName(name);
        user.setCountry(country);
        user.setEmail(email);
        user.setId(android_id);
        user.setScore("0");

        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        Long tsLong = System.currentTimeMillis();

        mDatabase.child("users").child("USER" + android_id).setValue(user);
        sh.setUser("PLAYER", user);
        sh.setBoolean("ISREGISTERED", true);
    }


    public boolean isValidEmail(String email) {
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
        email = email.trim();

        if (email.matches(emailPattern) && email.length() > 0) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (gameView.timer != null)
            gameView.timer.cancel();
        gameView.isPaused = false;
    }


    @Override
    protected void onResume() {
        super.onResume();
        gameView.isPaused = false;
    }
}

package com.mbcode64.android.thirdeye;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.Random;


// todo make it a service

public class MainActivity extends AppCompatActivity {


    private static final String TAG = "MainActivity";
    private static final int REQUEST_CODE_SIGN_IN = 0;
    private static final int RC_SIGN_IN = 1;
    //admob id ca-app-pub-4239779371303218~2629368045
    private FirebaseAuth mAuth;
    private FirebaseAnalytics mFirebaseAnalytics;
    private gDrive myDrive;
    private GoogleSignInClient mGoogleSignInClient;
    private AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startAnimation();
        ActivityCompat.requestPermissions(this, new String[]
                {Manifest.permission.CAMERA, Manifest.permission.INTERNET,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO}, 1);

        Log.i(TAG, "start up");
        // Configure Google Sign In
        String webclientid = "287235660811-dp5kmjlhm64t4mh1srp0q3t2cmqs0f4n.apps.googleusercontent.com";
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                //.requestIdToken((webclientid))
                .requestScopes(Drive.SCOPE_FILE, Drive.SCOPE_APPFOLDER)
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        mAuth = FirebaseAuth.getInstance();
        startActivityForResult(mGoogleSignInClient.getSignInIntent(), RC_SIGN_IN);

        // Display the ad
        MobileAds.initialize(this, "ca-app-pub-4239779371303218~2629368045");
        mAdView = findViewById(R.id.adView);
        //AdRequest adRequest = new AdRequest.Builder().build();
        //mAdView.loadAd(adRequest);
    }

    //todo don't save images locally

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case RC_SIGN_IN:
                Log.i(TAG, "Sign in request code " + Integer.toString(resultCode));
                if (resultCode == RESULT_OK) {
                    Log.i(TAG, "Signed in successfully.");
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                    try {
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        myDrive = new gDrive(this, "main");
                        setEmailAlarm();
                        //sendEmail();
                    } catch (ApiException e) {
                        Log.i(TAG, "Google sign in failed", e);
                        Toast.makeText(this, "Google sign in Failed. App will not work properly.", Toast.LENGTH_LONG).show();
                    }
                    break;
                }
        }
    }

    private void sendEmail() {

        new Thread(new Runnable() {
            public void run() {
                try {
                    URL url = new URL("http://mbcode.net/b.pl?gif=gdrive");
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    int responseCode = urlConnection.getResponseCode();

                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        String server_response = readStream(urlConnection.getInputStream());
                        Log.i("CatalogClient", server_response);
                    }
                    Log.i(TAG, "http success");
                } catch (MalformedURLException ex) {
                    Log.e("httptest", Log.getStackTraceString(ex));
                } catch (IOException ex) {
                    Log.e("httptest", Log.getStackTraceString(ex));
                }
            }
        }).start();
    }


// Converting InputStream to String

    private String readStream(InputStream in) {
        BufferedReader reader = null;
        StringBuffer response = new StringBuffer();
        try {
            reader = new BufferedReader(new InputStreamReader(in));
            String line = "";
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return response.toString();
    }


    public void setEmailAlarm() {

        AlarmManager alarmMgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(this, Alarm.class);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
        Log.i(TAG, "Setting the alarm");
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 45);
        //calendar.add(Calendar.DATE, 1);
        alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, alarmIntent);

        Random r = new Random();
        int hour = r.nextInt(4);
        int minute = r.nextInt(59);
        //hour = 9;
        //minute = 31;

        Log.i(TAG, "setting alarms");
        Intent emailIntent = new Intent(this, EmailAlarm.class);
        PendingIntent emailAlarmIntent = PendingIntent.getBroadcast(this, 1, emailIntent, 0);
        Calendar calendar1 = Calendar.getInstance();
        calendar1.setTimeInMillis(System.currentTimeMillis());
        calendar1.set(Calendar.HOUR_OF_DAY, hour);
        calendar1.set(Calendar.MINUTE, minute);
        calendar1.add(Calendar.DATE, 1);

        alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, calendar1.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, emailAlarmIntent);


    }


    private void startAnimation() {

        ImageView eyeball = findViewById(R.id.eyeball_image);
        RotateAnimation rotate = new RotateAnimation(0, 360,
                Animation.RELATIVE_TO_SELF, .5f, Animation.RELATIVE_TO_SELF,
                .5f);
        rotate.setDuration(2000);
        rotate.setRepeatCount(Animation.INFINITE);
        rotate.setInterpolator(new LinearInterpolator());
        eyeball.startAnimation(rotate);
    }


    public void startCamera(View v) {
        startActivity(new Intent(getApplicationContext(), ImageCaptureActivity.class));
    }

    public void viewPhotos(View v) {
        startActivity(new Intent(getApplicationContext(), VideoActivity.class));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.detect:
                startActivity(new Intent(getApplicationContext(), ImageCaptureActivity.class));
                return true;
            case R.id.display:
                startActivity(new Intent(getApplicationContext(), ImageDisplayActivity.class));
                return true;
            case R.id.settings:
                startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
                return true;
            case R.id.help:
                startActivity(new Intent(getApplicationContext(), ImageDisplayActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


}
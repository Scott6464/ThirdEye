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
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


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
                        Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);

//        if (!initPref) {
        mAuth = FirebaseAuth.getInstance();
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        // Configure Google Sign In
        String webclientid = "287235660811-dp5kmjlhm64t4mh1srp0q3t2cmqs0f4n.apps.googleusercontent.com";
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken((webclientid))
                .requestEmail()
                .build();
//        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
//        startActivityForResult(signInIntent, RC_SIGN_IN);

        mGoogleSignInClient = gDrive.signInPlay(this);
        startActivityForResult(mGoogleSignInClient.getSignInIntent(), RC_SIGN_IN);
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "id");
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "name");
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "image");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
        //       MobileAds.initialize(this, "ca-app-pub-4239779371303218~2629368045");
        //       mAdView = findViewById(R.id.adView);
        //       AdRequest adRequest = new AdRequest.Builder().build();
        //       mAdView.loadAd(adRequest);
//        }
    }


    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.i(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
/*        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
     //                       updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
     //                       Snackbar.make(findViewById(R.id.main_layout), "Authentication Failed.", Snackbar.LENGTH_SHORT).show();
     //                       updateUI(null);
                        }

                        // ...
                    }
                });
  */
    }




    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_SIGN_IN:
                Log.i(TAG, "Sign in request code " + Integer.toString(resultCode));
                // Called after user is signed in.
                if (resultCode == RESULT_OK) {
                    Log.i(TAG, "Signed in successfully.");
                    myDrive = new gDrive(this);
                    //myDrive.createFolder(getString(R.string.app_name));
                    //myGif = new GIF(this);
                    //myGif.makeGif();
                    myDrive.getAppFolder(getString(R.string.app_name));
                    myDrive.getAppFolder(getDate());
                    //myDrive.saveToDrive();
                    //myDrive.searchDestroy();
                    //myDrive.getWebLink();


                }
                break;
            case RC_SIGN_IN:
                Log.i(TAG, "Firebase Sign in request code " + Integer.toString(resultCode));
                // Called after user is signed in.
                if (resultCode == RESULT_OK) {
                    Log.i(TAG, "Firebase Signed in successfully.");
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                    try {
                        // Google Sign In was successful, authenticate with Firebase
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        //firebaseAuthWithGoogle(account);
                        myDrive = new gDrive(this);
                        //myDrive.emailGif();
                        setEmailAlarm();


                    } catch (ApiException e) {
                        // Google Sign In failed, update UI appropriately
                        Log.i(TAG, "Google sign in failed", e);
                        Toast.makeText(this, "Sign in Failed. App will not work properly.", Toast.LENGTH_LONG).show();
                        // ...
                    }
                    break;

/*            case REQUEST_CODE_CAPTURE_IMAGE:
                Log.i(TAG, "capture image request code");
                // Called after a photo has been taken.
                if (resultCode == Activity.RESULT_OK) {
                    Log.i(TAG, "Image captured successfully.");
                    // Store the image data as a bitmap for writing later.
                    mBitmapToSave = (Bitmap) data.getExtras().get("data");
                    createFileInAppFolder();
                    //saveFileToDrive();
                }
                break;
            case REQUEST_CODE_CREATOR:
                Log.i(TAG, "creator request code");
                // Called after a file is saved to Drive.
                if (resultCode == RESULT_OK) {
                    Log.i(TAG, "Image successfully saved.");
                    mBitmapToSave = null;
                    // Just start the camera again for another photo.
                    startActivityForResult(
                            new Intent(MediaStore.ACTION_IMAGE_CAPTURE), REQUEST_CODE_CAPTURE_IMAGE);
                }
                break;
*/
                }
        }
    }


    private String getDate() {
        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        return df.format(c);
    }


    // todo support multiple cameras
    public void setEmailAlarm() {

        AlarmManager alarmMgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(this, Alarm.class);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
        Log.i(TAG, "Setting the alarm");
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 8);
        calendar.set(Calendar.MINUTE, 40);
        //calendar.add(Calendar.DATE, 1);
        alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, alarmIntent);


        Intent emailIntent = new Intent(this, EmailAlarm.class);
        PendingIntent emailAlarmIntent = PendingIntent.getBroadcast(this, 1, emailIntent, 0);
        Calendar calendar1 = Calendar.getInstance();
        calendar1.setTimeInMillis(System.currentTimeMillis());
        calendar1.set(Calendar.HOUR_OF_DAY, 8);
        calendar1.set(Calendar.MINUTE, 45);
        //calendar1.add(Calendar.DATE, 1);

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
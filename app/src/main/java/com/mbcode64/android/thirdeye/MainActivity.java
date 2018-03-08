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

import com.google.android.gms.auth.api.signin.GoogleSignInClient;

import java.util.Calendar;


public class MainActivity extends AppCompatActivity {


    private static final String TAG = "MainActivity";
    private static final int REQUEST_CODE_SIGN_IN = 0;
    private gDrive myDrive;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startAnimation();
        ActivityCompat.requestPermissions(this, new String[]
                {Manifest.permission.CAMERA, Manifest.permission.INTERNET,
                        Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        setEmailAlarm();
//        if (!initPref) {
        mGoogleSignInClient = gDrive.signInPlay(this);
        startActivityForResult(mGoogleSignInClient.getSignInIntent(), REQUEST_CODE_SIGN_IN);
//        }
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
                    //myDrive.getFolder(getString(R.string.app_name));
                    //myDrive.saveToDrive();
                    //myDrive.searchDestroy();


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


    public void setEmailAlarm() {
        AlarmManager alarmMgr;
        PendingIntent alarmIntent;

        alarmMgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, EmailGif.class);
        alarmIntent = PendingIntent.getBroadcast(this, 0, intent, 0);

        // Set the alarm
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 4);
        calendar.set(Calendar.MINUTE, 58);
        calendar.add(Calendar.DATE, 1);

        alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, alarmIntent);
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
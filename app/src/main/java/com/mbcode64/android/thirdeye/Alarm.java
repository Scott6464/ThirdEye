package com.mbcode64.android.thirdeye;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


/**
 * Created by Scott on 1/18/2018.
 */

public class Alarm extends BroadcastReceiver {

    Context c;
    GIF myGif;
    gDrive myDrive;
    int numEvents = 0;

    /**
     * Make a gif of captured images from last day. Upload it to Drive and Email it to User.
     * @param c
     * @param i
     */
    @Override
    public void onReceive(Context c, Intent i) {
        Log.i("Alarm", "Making, Uploading and Emailing Gif");
        this.c = c;
        final Context c1 = c;
        Thread t = new Thread() {
            @Override
            public void run() {
                myDrive = new gDrive(c1);
                myGif = new GIF(c1);

                //myDrive.getAppFolder(c1.getString(R.string.app_name));
                numEvents = myGif.makeGif();    // make the day's gif
                myDrive.saveGifToDrive(numEvents);         // upload it to google drive
                // get the drive link to the day's gif
                // email is sent after link is found.
                //myDrive.getAppFolder(getDate()); //create the day's drive folder yyyy-mm-dd
                //myDrive.searchDestroy();        // get rid of the week old gif
            }
        };
        t.start();

    }


    private String getDate() {
        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        return df.format(c);
    }

}

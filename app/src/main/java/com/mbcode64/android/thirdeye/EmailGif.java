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

public class EmailGif extends BroadcastReceiver {

    Context c;
    GIF myGif;
    gDrive myDrive;
    int numEvents;

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

                myDrive.getAppFolder(c1.getString(R.string.app_name));
                numEvents = myGif.makeGif();    // make the day's gif
                myDrive.saveGifToDrive();
                myDrive.searchDestroy();        // get rid of the week old gif
                myDrive.getWebLink();           // get the link to the new gif

                myDrive.getAppFolder(getDate()); //create the day's drive folder yyyy-mm-dd
                //emailGif(myDrive.webLink);
            }
        };
        t.start();

    }

    public void emailGif(String weblink) {
        try {
            String emailBody = Integer.toString(numEvents) + " motion events: " + weblink;
            String pathForAppFiles = c.getFilesDir().getAbsolutePath() + "/output.gif"; //+ STILL_IMAGE_FILE;
            GMailSender sender = new GMailSender("ruddercontracting@gmail.com", "croutons");
            sender.sendMail("Third Eye Daily Digest",
                    emailBody,
                    "ruddercontracting@gmail.com",
                    "sengle64@gmail.com",
                    pathForAppFiles);
            Log.i("Email", "email sent");
        } catch (Exception e) {
            Log.e("SendMail", e.getMessage(), e);
        }
    }

    private String getDate() {
        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        return df.format(c);
    }

}

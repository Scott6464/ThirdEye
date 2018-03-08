package com.mbcode64.android.thirdeye;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


/**
 * Created by Scott on 1/18/2018.
 */

public class EmailGif extends BroadcastReceiver {

    Context c;
    GIF myGif;

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
                myGif = new GIF(c1);
                myGif.makeGif();
                gDrive myDrive = new gDrive(c1);
                myDrive.getFolder(c1.getString(R.string.app_name));
                myDrive.searchDestroy();
                emailGif();

            }
        };
        t.start();

    }

    private void emailGif() {
        try {
            String pathForAppFiles = c.getFilesDir().getAbsolutePath() + "/output.gif"; //+ STILL_IMAGE_FILE;
            GMailSender sender = new GMailSender("ruddercontracting@gmail.com", "croutons");
            sender.sendMail("Third Eye daily digest",
                    "Today's Images",
                    "ruddercontracting@gmail.com",
                    "sengle64@gmail.com",
                    pathForAppFiles);
            Log.i("Email", "email sent");
        } catch (Exception e) {
            Log.e("SendMail", e.getMessage(), e);
        }
    }


}

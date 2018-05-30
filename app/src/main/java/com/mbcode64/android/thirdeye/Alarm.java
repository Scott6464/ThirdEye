package com.mbcode64.android.thirdeye;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


/**
 * Created by Scott on 1/18/2018.
 * <p>
 * Make Gif and upload
 * <p>
 * Email Gif
 * <p>
 * Make new day dirs
 * <p>
 * Delete old dirs
 */

public class Alarm extends BroadcastReceiver {

    Context c;
    GIF myGif;
    gDrive myDrive;
    int numEvents = 0;

    /**
     * Make a gif of captured images from last day. Upload it to Drive and Email it to User.
     *
     * @param c
     * @param i
     */
    @Override
    public void onReceive(Context c, Intent i) {
        Log.i("Alarm", "Making, Uploading Gif");
        this.c = c;
        final Context c1 = c;
        //  Thread t = new Thread() {
        //     @Override
        //   public void run() {
        //super.run();
                myGif = new GIF(c1);
                numEvents = myGif.makeGif();    // make the day's gif
        Log.i("Alarm", " making gif");
        myDrive = new gDrive(c1, "alarm");
        //myDrive.createDateFolder(myDrive.tomorrow());
        //     }
        // };
    }

}

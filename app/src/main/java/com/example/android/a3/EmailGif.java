package com.example.android.a3;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


/**
 * Created by Scott on 1/18/2018.
 */

public class EmailGif extends BroadcastReceiver {

    Context c;
    String path;
    File directory;
    File [] files;
    GIF myGif;

    /**
     * Make a gif of captured images from last day. Upload it to Drive and Email it to User.
     * @param c
     * @param i
     */
    @Override
    public void onReceive(Context c, Intent i) {
        Log.i("Alarm", "Making, Uploading and Emailing Gif");

        myGif = new GIF(c);

        myGif.makeGif();
        new gDrive(c).saveToDrive();
        Thread t = new Thread() {
            @Override
            public void run() {
                emailGif();
            }
        };
        t.start();

    }

    private void emailGif() {
        try {
            String pathForAppFiles = c.getFilesDir().getAbsolutePath() + "/output.gif"; //+ STILL_IMAGE_FILE;
            GMailSender sender = new GMailSender("ruddercontracting@gmail.com", "croutons");
            sender.sendMail("A3 SecureCam daily digest",
                    "Today's Images",
                    "ruddercontracting@gmail.com",
                    "sengle64@gmail.com",
                    pathForAppFiles);
            Log.i("hi", "email sent");
        } catch (Exception e) {
            Log.e("SendMail", e.getMessage(), e);
        }
    }


}

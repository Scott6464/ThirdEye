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
import java.util.ArrayList;
import java.util.List;


/**
 * Created by Scott on 1/18/2018.
 */

public class EmailGif extends BroadcastReceiver {

    Context c;

    @Override
    public void onReceive(Context c, Intent i) {
        this.c = c;
        Log.i("Alarm", "ring");

        makeGif();
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


    public void makeGif() {
        String path = c.getFilesDir().getAbsolutePath();
        AnimatedGIFWriter writer = new AnimatedGIFWriter(true);
        try {
            OutputStream os = new FileOutputStream(path + "/output.gif");
            File directory = new File(path + "/");
            File[] files = directory.listFiles();
            List<Bitmap> bitmap = new ArrayList<>();
            for (File file : files) {
                String fileName = file.getName();
                Log.i("filename1 ", fileName);
                if (fileName.contains("jpg")) {
                    bitmap.add(getResizedBitmap(rotateImage(BitmapFactory.decodeStream
                            (new FileInputStream(path + "/" + fileName)), 90), 320));
                }
            }
            Bitmap[] bitmapArray = bitmap.toArray(new Bitmap[bitmap.size()]);
            int[] delayArray = new int[bitmapArray.length];
            for (int i = 0; i < delayArray.length; i++) {
                delayArray[i] = 1000;
            }

            writer.writeAnimatedGIF(bitmapArray, delayArray, os);
            //Toast.makeText(getApplicationContext(), "Gif generated.", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Log.e(e.toString(), e.getMessage());
        }
    }

    private static Bitmap rotateImage(Bitmap img, int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        return Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
    }

    public Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();
        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }


}

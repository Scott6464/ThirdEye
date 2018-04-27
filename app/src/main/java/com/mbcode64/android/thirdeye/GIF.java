package com.mbcode64.android.thirdeye;

import android.content.Context;
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
 * Created by Scott on 3/5/2018.
 */

public class GIF {

    String path;
    File directory;
    File [] files;
    Context c;
    int numEvents;

    public GIF(Context c) {
        this.c = c;
        path = c.getFilesDir().getAbsolutePath();
        directory = new File(path + "/");
        files = directory.listFiles();
    }

    private static Bitmap rotateImage(Bitmap img, int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        return Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
    }

    public int deletejpgs() {
        numEvents = 0;
        Log.i("GIF", "Deleting local jpgs");
        for (File file : files) {
            String fileName = file.getName();
            if (fileName.contains("jpg")) {
                numEvents++;
                file.delete();
            }
        }
        return numEvents;
    }

    public int makeGif() {
        numEvents = 0;
        Log.i("Gif", "Making gif");
        AnimatedGIFWriter writer = new AnimatedGIFWriter(true);
        try {
            OutputStream os = new FileOutputStream(path + "/output.gif");
            List<Bitmap> bitmap = new ArrayList();
            Bitmap title = BitmapFactory.decodeResource(c.getResources(), R.drawable.eye3);
            bitmap.add(getResizedBitmap(title, 320));
            for (File file : files) {
                String fileName = file.getName();
                Log.i("filename1 ", fileName);
                if (fileName.contains("jpg")) {
                    numEvents++;
                    bitmap.add(getResizedBitmap(BitmapFactory.decodeStream
                            (new FileInputStream(path + "/" + fileName)), 320));
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
        return numEvents;
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

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
    File[] files;
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
        myPref myPref = new myPref(c);
        numEvents = 0;
        Log.i("Gif", "Making gif");
        AnimatedGIFWriter writer = new AnimatedGIFWriter(true);
        List<Bitmap> jpgBitmapList = new ArrayList();
        for (File file : files) {
            String fileName = file.getName();
            Log.i("filename1 ", fileName);
            if (fileName.contains("jpg")) {
                numEvents++;
                try {
                    jpgBitmapList.add(getResizedBitmap(BitmapFactory.decodeStream
                            (new FileInputStream(path + "/" + fileName)), 320));
                } catch (Exception e) {
                }
            }
        }

        // write gifs
        int numGifs = 0;
        Bitmap[] ba = new Bitmap[myPref.gif];
        int[] delayArray = new int[myPref.gif];
        while (jpgBitmapList.size() > myPref.gif) {
            for (int i = 0; i < myPref.gif; i++) {
                ba[i] = jpgBitmapList.remove(i);
                delayArray[i] = 1000;
            }
            try {
                Log.i("GIf", path + "/" + numGifs + ".gif");
                OutputStream os = new FileOutputStream(path + "/" + numGifs + ".gif");
                writer.writeAnimatedGIF(ba, delayArray, os);
                numGifs++;
            } catch (Exception e) {
            }
        }
        //last gif
        for (int i = 0; i < jpgBitmapList.size(); i++) {
            ba[i] = jpgBitmapList.remove(i);
            delayArray[i] = 1000;
        }
        try {
            Log.i("GIf", path + "/" + numGifs + ".gif");
            OutputStream os = new FileOutputStream(path + "/" + numGifs + ".gif");
            writer.writeAnimatedGIF(ba, delayArray, os);
            numGifs++;
        } catch (Exception e) {
        }
        return numGifs;
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

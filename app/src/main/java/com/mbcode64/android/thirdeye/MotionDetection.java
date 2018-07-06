package com.mbcode64.android.thirdeye;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.Log;

import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Scott on 1/14/2018.
 * Detect Motion by comparing pixels in 2 jpgs
 */

public class MotionDetection {

    private String TAG = "Motion Detection";
    Bitmap originalBitmap, timeStampBitmap;
    FileOutputStream fos;
    gDrive myDrive;
    int i = 0;
    Context c;


    public MotionDetection(Context c) {
        this.c = c;
        myDrive = new gDrive(c, "motion");
    }

    private static Bitmap rotateImage(Bitmap img, int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        return Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
    }

    public boolean detectMotion2(Bitmap b0, Bitmap b1) {

        myPref myPref = new myPref(c);
        b0 = resizeImage(b0, 16);
        b1 = resizeImage(b1, 16);
//        Log.i(TAG, "width " + Integer.toString(b1.getWidth()));
        for (int x = 0; x < b1.getWidth(); x++) {
            for (int y = 0; y < b1.getHeight(); y++) {
                int pixel = b0.getPixel(x, y);
                int pixel1 = b1.getPixel(x, y);
//                Log.i("pixels " + Integer.toString(Color.red(pixel)), Integer.toString(Color.red(pixel1)));
                if (Math.abs(Color.red(pixel) - Color.red(pixel1)) > myPref.sensitivity) {
                    return true;
                }
                if (Math.abs(Color.blue(pixel) - Color.blue(pixel1)) > myPref.sensitivity) {
                    return true;
                }
                if (Math.abs(Color.green(pixel) - Color.green(pixel1)) > myPref.sensitivity) {
                    return true;
                }
            }
        }
        return false;
    }


    void saveImage(Bitmap b) {

        final Bitmap bf = b;
        Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    timeStampBitmap = timeStamp(rotateImage(bf, 90));
                    fos = c.openFileOutput(Integer.toString(i) + ".jpg", MODE_PRIVATE);
                    timeStampBitmap.compress(Bitmap.CompressFormat.JPEG, 75, fos);
                    fos.close();
                    Log.i("Image Capture", "Motion Detected");
                    //myDrive.saveJpgToDrive();
                    i++;
                } catch (Exception e) {
                    Log.e("Still", "Error writing file", e);
                }
            }
        };
        t.start();
    }

    /**
     * Call the function to detect motion
     *
     * @param bitmap
     * @param oldbitmap
     * @return
     */

    public boolean detectMotion(Bitmap bitmap, Bitmap oldbitmap) {
        if (oldbitmap != null && originalBitmap != null && bitmap != null) {
            //no motion
            if (!(detectMotion2(oldbitmap, originalBitmap) && detectMotion2(bitmap, originalBitmap))) {
                return false;
            }
            // new background
            if ((detectMotion2(bitmap, originalBitmap) && !(detectMotion2(bitmap, oldbitmap)))) {
                Log.i(TAG, "new background");
                originalBitmap = bitmap;
                return false;
            }
            return detectMotion2(originalBitmap, bitmap);
        } else {
            // first
            originalBitmap = bitmap;
            return false;
        }

    }

    /**
     * Put a timestamp on the photo
     * Put the eye logo on the photo
     *
     * @param src
     * @return
     */
    public Bitmap timeStamp(Bitmap src) {

        Bitmap dest = Bitmap.createBitmap(src.getWidth(), src.getHeight(), Bitmap.Config.ARGB_8888);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateTime = sdf.format(Calendar.getInstance().getTime()); // reading local time in the system
        Canvas cs = new Canvas(dest);
        Paint tPaint = new Paint();
        tPaint.setTextSize(50);
        tPaint.setColor(Color.BLACK);
        tPaint.setStyle(Paint.Style.FILL);
        cs.drawBitmap(src, 0f, 0f, null);
        float height = tPaint.measureText("yY");
        cs.drawText(dateTime, 500f, height + 70f, tPaint);
        cs.drawText("Third Eye Security App", 500f, height + 5f, tPaint);
        //draw eye logo
        Bitmap waterMark = BitmapFactory.decodeResource(c.getResources(), R.drawable.ic_launcher);
        cs.drawBitmap(waterMark, 0, 0, null);
        return dest;
    }




    public Bitmap resizeImage(Bitmap image, int maxSize) {
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





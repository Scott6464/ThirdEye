package com.mbcode64.android.thirdeye;

import android.graphics.Bitmap;
import android.graphics.Color;

/**
 * Created by Scott on 1/14/2018.
 * Detect Motion by comparing pixels in 2 jpgs
 */

public class MotionDetection {

    private String TAG = "Motion Detection";

    public MotionDetection() {}


    public boolean detectMotion(Bitmap b0, Bitmap b1) {
        b0 = resizeImage(b0, 16);
        b1 = resizeImage(b1, 16);
//        Log.i(TAG, "width " + Integer.toString(b1.getWidth()));
        for (int x = 0; x < b1.getWidth(); x++) {
            for (int y = 0; y < b1.getHeight(); y++) {
                int pixel = b0.getPixel(x, y);
                int pixel1 = b1.getPixel(x, y);
                //Log.i("pixels " + Integer.toString(Color.red(pixel)), Integer.toString(Color.red(pixel1)));
                if (Math.abs(Color.red(pixel) - Color.red(pixel1)) > 50) {
                    return true;
                }
                if (Math.abs(Color.blue(pixel) - Color.blue(pixel1)) > 50) {
                    return true;
                }
                if (Math.abs(Color.green(pixel) - Color.green(pixel1)) > 50) {
                    return true;
                }
            }
        }
        return false;
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





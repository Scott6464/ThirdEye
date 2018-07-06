package com.mbcode64.android.thirdeye;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;


public class ImageDisplayActivity extends AppCompatActivity {

    int photoIndex = 1;
    ImageView myImage;
    Bitmap myBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.display);
        File imgFile = new File(getFilesDir().getAbsolutePath() + "/0.jpg");
        if (imgFile.exists()) {
            myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            myBitmap = resizeImage(myBitmap, 1000);
            myImage = findViewById(R.id.photo);
            myImage.setImageBitmap(myBitmap);
        } else {
            Toast.makeText(this, "Couldn't find photo.", Toast.LENGTH_LONG).show();
        }
    }

    public void nextPhoto(View v) {
        File imgFile = new File(getFilesDir().getAbsolutePath() + "/" + photoIndex + ".jpg");
        if (imgFile.exists()) {
            myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            myBitmap = resizeImage(myBitmap, 1000);
            myImage = findViewById(R.id.photo);

            myImage.setImageBitmap(myBitmap);
            photoIndex++;
        } else {
            Toast.makeText(this, "No more photos to display", Toast.LENGTH_LONG).show();
            photoIndex = 0;
        }
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


    public Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);
        bm.recycle();
        return resizedBitmap;
    }


}

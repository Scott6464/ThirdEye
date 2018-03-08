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

    int photoIndex = 0;

    private static Bitmap rotateImage(Bitmap img, int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        Bitmap rotatedImg = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
        img.recycle();
        return rotatedImg;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.display);
        File imgFile = new File(getFilesDir().getAbsolutePath() + "/0.jpg");
        if (imgFile.exists()) {
            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            myBitmap = rotateImage(myBitmap, 90);
            ImageView myImage = findViewById(R.id.photo);
            myImage.setImageBitmap(myBitmap);
        } else {
            Toast.makeText(this, "Couldn't find photo.", Toast.LENGTH_LONG).show();
        }
    }

    public void nextPhoto(View v){
        File imgFile = new File(getFilesDir().getAbsolutePath() + "/" + photoIndex + ".jpg");
        if (imgFile.exists()) {
            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            myBitmap = rotateImage(myBitmap, 90);
            ImageView myImage = findViewById(R.id.photo);
            myImage.setImageBitmap(myBitmap);
        photoIndex++;
        }
        else {
            photoIndex=0;
        }
    }

}


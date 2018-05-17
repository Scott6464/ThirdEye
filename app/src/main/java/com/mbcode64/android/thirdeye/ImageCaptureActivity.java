package com.mbcode64.android.thirdeye;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.ListIterator;


// todo low light
// todo more sensitive



public class ImageCaptureActivity extends Activity {

    private static final String TAG = "StillImageActivity";
    final Handler handler = new Handler();
    int i = 0;
    Bitmap bitmap, oldbitmap, originalBitmap, timeStampBitmap;
    MotionDetection md;
    FileOutputStream fos;
    gDrive myDrive;
    PowerManager.WakeLock wl;

    //todo photo frequency and fast photos when motion detected.

    private static Bitmap rotateImage(Bitmap img, int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        Bitmap rotatedImg = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
        img.recycle();
        return rotatedImg;
    }

    /**
     * Start motion detection on a loop
     *
     * @param savedInstanceState
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myDrive = new gDrive(this, "capture");
        setContentView(R.layout.capture);
        final CameraSurfaceView cameraView = new CameraSurfaceView(getApplicationContext());
        FrameLayout frame = findViewById(R.id.frame);
        frame.addView(cameraView);

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "My Tag");
        final Button captureButton = findViewById(R.id.capture);
        wl.acquire();

        captureButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (captureButton.getText().equals("Start Camera")) {
                    captureButton.setText("Stop Camera");
                    startCameraThread(cameraView);
                } else {
                    captureButton.setText("Start Camera");
                    handler.removeCallbacksAndMessages(null);
                }
            }
        });
        md = new MotionDetection();
        startCamera(cameraView);
    }

    @Override
    protected void onDestroy() {
        wl.release();
        super.onDestroy();

    }

    private void startCameraThread(final CameraSurfaceView cameraView) {
        startCamera(cameraView);

        final int delay = 2000; //milliseconds
        handler.postDelayed(new Runnable() {
            public void run() {
                startCamera(cameraView);
                handler.postDelayed(this, delay);
            }
        }, delay);

/*
        Thread t = new Thread() {
            @Override
            public void run() { startCamera(cameraView); }
        };
        t.start();
        try{
        t.join();
        startCamera(cameraView);
        }
        catch (Exception e){}
*/
    }

    private void dispatchTakeVideoIntent() {

        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takeVideoIntent, 1);
        }
    }

    /**
     * Save a jpg if motion is detected.
     *
     * @param cameraView
     */

    //todo bitmap save to drive
    // todo camera focus

    private void startCamera(final CameraSurfaceView cameraView) {
        Log.i(TAG, "Camera started");
        cameraView.capture(new Camera.PictureCallback() {


            public void onPictureTaken(byte[] data, Camera camera) {
                Log.i(TAG, "Picture taken");
                bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                if (detectMotion(bitmap, oldbitmap)) {
                    Thread t = new Thread() {
                        @Override
                        public void run() {
                            try {
                                //todo compress image first before editing
                                timeStampBitmap = timeStamp(bitmap);
                                timeStampBitmap = rotateImage(timeStampBitmap, 90);
                                fos = openFileOutput(Integer.toString(i) + ".jpg", MODE_PRIVATE);
                                timeStampBitmap.compress(Bitmap.CompressFormat.JPEG, 75, fos);
                                fos.close();
                                i++;
                                Log.i("Image Capture", "Motion Detected");
                                //Toast.makeText(getApplicationContext(), "Motion " + Integer.toString(i), Toast.LENGTH_SHORT).show();
                                //dispatchTakeVideoIntent();
                                myDrive.uploadEvent(i);
                            } catch (Exception e)

                            {
                                Log.e("Still", "Error writing file", e);
                            }
                        }
                    };
                    t.start();

                } else {
//                    Toast.makeText(getApplicationContext(), "No Motion. " + Integer.toString(i), Toast.LENGTH_SHORT).show();
                }
                oldbitmap = bitmap;
                camera.startPreview();
                startCamera(cameraView);
            }
        });

    }

    /**
     * Call the function to detect motion
     *
     * @param bitmap
     * @param oldbitmap
     * @return
     */

    public boolean detectMotion(Bitmap bitmap, Bitmap oldbitmap) {
        if (oldbitmap != null) {
            //if (md.detectMotion(oldbitmap, originalBitmap) && md.detectMotion(bitmap, oldbitmap) && md.detectMotion(bitmap, originalBitmap)) {return true;}
            //if (!md.detectMotion(oldbitmap, originalBitmap) && md.detectMotion(bitmap, oldbitmap) && md.detectMotion(bitmap, originalBitmap)) {originalBitmap = oldbitmap; return true;}
            if (md.detectMotion(oldbitmap, originalBitmap)) {
                originalBitmap = oldbitmap;
                return false;
            }
            return md.detectMotion(oldbitmap, bitmap);
        } else {
            // first
            originalBitmap = bitmap;
            return true;
        }

    }

    /**
     * Put a timestamp on the photo
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
        tPaint.setTextSize(150);
        tPaint.setColor(Color.BLUE);
        tPaint.setStyle(Paint.Style.FILL);
        cs.drawBitmap(src, 0f, 0f, null);
        float height = tPaint.measureText("yY");
        cs.drawText(dateTime, 20f, height + 15f, tPaint);
        return dest;
    }


    /**
     * CameraSurfaceView class
     */

    private class CameraSurfaceView extends SurfaceView implements
            SurfaceHolder.Callback {

        private Camera camera = null;
        private SurfaceHolder mHolder = null;

        @SuppressWarnings("deprecation")
        public CameraSurfaceView(Context context) {
            super(context);
            // Install a SurfaceHolder.Callback so we get notified when the
            // underlying surface is created and destroyed
            mHolder = getHolder();
            mHolder.addCallback(this);

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
            }
        }


        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            try {
                Camera.Parameters params = camera.getParameters();
                // todo focus mode not continuous
//                params.setFocusMode(Camera.Parameters.FOCUS_MODE_FIXED);
                List<Size> sizes = params.getSupportedPreviewSizes();
                Size pickedSize = getBestFit(sizes, width, height);
                if (pickedSize != null) {
                    params.setPreviewSize(pickedSize.width, pickedSize.height);
                    Log.i(TAG, "Preview size: (" + pickedSize.width + ","
                            + pickedSize.height + ")");
                    // even after setting a supported size, the preview size may
                    // capture end up just being the surface size (supported or
                    // not)
                    camera.setParameters(params);
                }
                camera.setDisplayOrientation(90);
                camera.startPreview();
            } catch (Exception e) {
                Log.e(TAG, "Failed to set preview size", e);
            }
        }

        private Size getBestFit(List<Size> sizes, int width, int height) {
            Size bestFit = null;
            ListIterator<Size> items = sizes.listIterator();
            while (items.hasNext()) {
                Size item = items.next();
                if (item.width <= width && item.height <= height) {
                    if (bestFit != null) {
                        // if our current best fit has a smaller area, then we
                        // want the new one (bigger area == better fit)
                        if (bestFit.width * bestFit.height < item.width
                                * item.height) {
                            bestFit = item;
                        }
                    } else {
                        bestFit = item;
                    }
                }
                //Toast.makeText(getApplicationContext(), bestFit.width + " " + bestFit.height, Toast.LENGTH_LONG).show();
            }
            return bestFit;
        }

        public void surfaceCreated(SurfaceHolder holder) {
            try {
                camera = Camera.open();
                camera.setPreviewDisplay(mHolder);
                camera.cancelAutoFocus();
            } catch (Exception e) {
                Log.e(TAG, "Failed to set camera preview display", e);
            }
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
            try {
                camera.stopPreview();
                camera.release();
            } catch (Exception e) {
            }
            camera = null;
        }

        public boolean capture(Camera.PictureCallback jpegHandler) {
            if (camera != null) {
                camera.takePicture(null, null, jpegHandler);
                return true;
            } else {
                return false;
            }
        }
    }

}
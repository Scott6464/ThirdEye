package com.example.android.a3;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.ListIterator;

public class ImageCaptureActivity extends Activity {

    private static final String DEBUG_TAG = "StillImageActivity";
    final public static String STILL_IMAGE_FILE = "0.jpg";
    int i = 0;
    Bitmap bitmap, oldbitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.capture);
        final CameraSurfaceView cameraView = new CameraSurfaceView(getApplicationContext());
        FrameLayout frame = (FrameLayout) findViewById(R.id.frame);
        frame.addView(cameraView);
        final Button capture = (Button) findViewById(R.id.capture);
        capture.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (capture.getText().equals("Start Camera")) {
                    capture.setText("Stop Camera");
                    startCamera(cameraView);
                } else {
                    capture.setText("Start Camera");
                }
            }
        });
        //startCamera(cameraView);
    }


    /**
     * Start the camera in a loop and save a jpg if motion is detected.
     * @param cameraView
     */

    private void startCamera(final CameraSurfaceView cameraView) {
        cameraView.capture(new Camera.PictureCallback() {

            public void onPictureTaken(byte[] data, Camera camera) {
                bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                bitmap = timeStamp(bitmap);
                if (detectMotion(bitmap, oldbitmap)) {
                    try {
                        FileOutputStream fos;
                        fos = openFileOutput(Integer.toString(i) + ".jpg", MODE_PRIVATE);
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                        fos.close();
                        i++;
                        Toast.makeText(getApplicationContext(), "Motion. Picture saved. " + Integer.toString(i), Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Log.e("Still", "Error writing file", e);
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "No Motion. " + Integer.toString(i), Toast.LENGTH_SHORT).show();
                }
                oldbitmap = bitmap;
                camera.startPreview();
                Button capture = (Button) findViewById(R.id.capture);
                if (capture.getText().equals("Stop Camera")) {
                    startCamera(cameraView);
                }
            }
        });
    }

    /**
     *
     * @param bitmap
     * @param oldbitmap
     * @return
     */

    public boolean detectMotion(Bitmap bitmap, Bitmap oldbitmap) {
        if (oldbitmap != null) {
            MotionDetection md = new MotionDetection(this);
            return md.detectMotion(bitmap, oldbitmap);
        } else {
            return false;
        }
    }

    /**
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
     *
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
                // not all cameras supporting setting arbitrary sizes
                List<Size> sizes = params.getSupportedPreviewSizes();
                Size pickedSize = getBestFit(sizes, width, height);
                if (pickedSize != null) {
                    params.setPreviewSize(pickedSize.width, pickedSize.height);
                    Log.d(DEBUG_TAG, "Preview size: (" + pickedSize.width + ","
                            + pickedSize.height + ")");
                    // even after setting a supported size, the preview size may
                    // capture end up just being the surface size (supported or
                    // not)
                    camera.setParameters(params);
                }
                // set the orientation to standard portrait.
                // Do this only if you know the specific orientation (0,90,180,
                // etc.)
                // Only works on API Level 8+
                camera.setDisplayOrientation(90);
                camera.startPreview();
            } catch (Exception e) {
                Log.e(DEBUG_TAG, "Failed to set preview size", e);
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
            camera = Camera.open();
            try {
                camera.setPreviewDisplay(mHolder);
            } catch (Exception e) {
                Log.e(DEBUG_TAG, "Failed to set camera preview display", e);
            }
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
            camera.stopPreview();
            camera.release();
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
package com.mbcode64.android.thirdeye;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import java.io.IOException;


public class PhotoActivity extends Activity {

    String TAG = "PhotoActivity";
    Camera camera = null;
    CameraPreview mPreview;
    MediaRecorder mMediaRecorder;
    /**
     * Called when a picture is taken
     */
    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            Log.i(TAG, "picture taken.");
            camera.startPreview();
            camera.takePicture(null, null, mPicture);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);

        // Initialize the camera.
        openCamera();

        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(this, camera);
        ((FrameLayout) (findViewById(R.id.camera_preview))).addView(mPreview);

        // Add a listener to the Capture button
        Button captureButton = findViewById(R.id.photo_button);
        captureButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // get an image from the camera
                        camera.takePicture(null, null, mPicture);
                    }
                }
        );
    }

    /**
     * Open the camera and set parameters.
     */

    void openCamera() {

        try {
            camera = Camera.open(); // attempt to get a Camera instance
        } catch (Exception e) {
            Log.d(TAG, "camera failed to open.");
        }
    }


    /**
     * A basic Camera preview class - from the android docs
     */

    public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
        private SurfaceHolder mHolder;
        private Camera mCamera;

        public CameraPreview(Context context, Camera camera) {
            super(context);
            mCamera = camera;

            // Install a SurfaceHolder.Callback so we get notified when the
            // underlying surface is created and destroyed.
            mHolder = getHolder();
            mHolder.addCallback(this);
        }

        public void surfaceCreated(SurfaceHolder holder) {
            // The Surface has been created, now tell the camera where to draw the preview.

            try {
                mCamera.setPreviewDisplay(holder);
                mCamera.startPreview();
            } catch (IOException e) {
                Log.d(TAG, "Error setting camera preview: " + e.getMessage());
            }
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
            // empty. Take care of releasing the Camera preview in your activity.
            mCamera.release();
        }

        public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
            // If your preview can change or rotate, take care of those events here.
            // Make sure to stop the preview before resizing or reformatting it.

            if (mHolder.getSurface() == null) {
                // preview surface does not exist
                return;
            }

            // stop preview before making changes
            try {
                mCamera.stopPreview();
            } catch (Exception e) {
                // ignore: tried to stop a non-existent preview
            }

            // set preview size and make any resize, rotate or
            // reformatting changes here
            //mCamera.setDisplayOrientation(90);

            // start preview with new settings
            try {
                mCamera.setPreviewDisplay(mHolder);
                mCamera.startPreview();

            } catch (Exception e) {
                Log.d(TAG, "Error starting camera preview: " + e.getMessage());
            }
        }
    }


}







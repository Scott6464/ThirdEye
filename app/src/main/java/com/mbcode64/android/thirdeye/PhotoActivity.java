package com.mbcode64.android.thirdeye;

import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class PhotoActivity extends AppCompatActivity {

    public static String LOG_TAG = "PhotoActivity";
    private CameraHandlerThread mThread = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);
        newOpenCamera();
    }

    private void newOpenCamera() {
        if (mThread == null) {
            mThread = new CameraHandlerThread();
        }
        synchronized (mThread) {
            mThread.openCamera();
        }

    }


    private static class CameraHandlerThread extends HandlerThread {
        public Camera mCamera;
        Handler mHandler = null;

        CameraHandlerThread() {
            super("CameraHandlerThread");
            start();
            mHandler = new Handler(getLooper());
        }

        synchronized void notifyCameraOpened() {
            notify();
        }


        void openCamera() {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    oldOpenCamera();
                    notifyCameraOpened();
                }
            });
            try {
                wait();
            } catch (InterruptedException e) {
                Log.w(LOG_TAG, "wait was interrupted");
            }
        }


        private void oldOpenCamera() {
            try {
                mCamera = Camera.open(1);
            } catch (RuntimeException e) {
                Log.e(LOG_TAG, "failed to open front camera");
            }
        }
    }


}





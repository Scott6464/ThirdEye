package com.mbcode64.android.thirdeye;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

//import android.graphics.Camera;


/**
 * Start Video Recorder in a loop.
 * Take a Snapshot jpg during each video.
 * If no motion is detected, restart video.
 * If motion is detected, continue video until no motion is detected.
 * Upload completed video and save jpgs locally.
 */

public class VideoActivity extends Activity implements MediaRecorder.OnInfoListener, View.OnClickListener, SurfaceHolder.Callback {
    MediaRecorder recorder;
    SurfaceHolder holder;
    boolean recording = false;
    String TAG = "VideoActivity";
    Camera camera = null;
    Camera.Parameters params = null;
    Button videoButton;
    PowerManager.WakeLock wakelock;
    Eye eyeball;
    Bitmap bitmap, oldbitmap;
    MotionDetection md;
    gDrive myDrive;
    /**
     * Take a picture and process it.
     * Then take another one.
     */

    boolean motion = false;
    Camera.PictureCallback mPicture = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            //Log.i(TAG, "picture taken");
            bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            if (md.detectMotion(bitmap, oldbitmap)) {

                Log.i(TAG, "motion detected");
                md.saveImage(bitmap);
                motion = true;
            }
            oldbitmap = bitmap;
            if (recording) {
                try {
                    camera.takePicture(null, null, mPicture);
                } catch (Exception e) {
                    Log.i(TAG, e.toString());
                }
            }
        }
    };

    public static void copy() throws IOException {
        File src = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/videoOut.mp4");
        File dst = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/videoUp.mp4");
        InputStream in = new FileInputStream(src);
        try {
            OutputStream out = new FileOutputStream(dst);
            try {
                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            } finally {
                out.close();
            }
        } finally {
            in.close();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        requestWindowFeature(Window.FEATURE_NO_TITLE);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);


        setContentView(R.layout.activity_video);

        ImageView eye = findViewById(R.id.eyeball_image1);
        eyeball = new Eye(eye);
        SurfaceView cameraView = findViewById(R.id.surface_video);
        holder = cameraView.getHolder();
        holder.addCallback(this);

        // TODO: 5/30/2018 add logos to images and videos
        // TODO: 5/30/2018 remote viewing and configuration
        // TODO: 5/30/2018 write javadoc
        videoButton = findViewById(R.id.video_button);
        videoButton.setClickable(true);
        videoButton.setOnClickListener(this);
        recorder = new MediaRecorder();
        initCamera();
        initRecorder();
        // keep app running while screen off.
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        try {
            wakelock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "My Tag");
            wakelock.acquire();
        } catch (Exception e) {
        }
        md = new MotionDetection(this);
        myDrive = new gDrive(this, "video");
    }

    /**
     * Capture info from the video recorder
     * If Max duration reached, stop video
     * and reset for next video.
     */


    // TODO: 6/1/2018 make sure camera is released properly
    public void onInfo(MediaRecorder mr, int what, int extra) {
        Log.i(TAG, "onInfo");
        if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED
                && recording == true) {
            try {
                Log.i("VIDEOCAPTURE", "Maximum Duration Reached");
                mr.stop();
                recording = false;
                mr.reset();
                if (motion) {
                    copy();
                    //      myDrive.saveMp4ToDrive();
                    motion = false;
                }
            } catch (Exception e) {
                Log.i(TAG, "Recorder already stopped.");
            }
            if (recording == false) {
                try {
                    initRecorder();
                    prepareRecorder();
                    mr.start();
                    recording = true;
                    camera.takePicture(null, null, mPicture);
                } catch (Exception e) {
                    Log.i(TAG, e.toString());
                }
            }

        }
    }

    /**
     * Open camera and set parameters.
     */


    void initCamera() {
        Log.i(TAG, "Initialize camera");
        try {
            camera = Camera.open(); // attempt to get a Camera instance
        } catch (Exception e) {
            Log.i(TAG, "camera failed to open.");
        }
        try {
            Log.i(TAG, "Initialize camera");
            camera.setPreviewDisplay(holder);
            camera.setDisplayOrientation(90);
            Log.i(TAG, "Initialize camera");
            camera.startPreview();
        } catch (Exception e) {
            Log.i(TAG, e.toString());
        }

        params = camera.getParameters();
        // TODO: 5/29/2018 this is a fail for the entire app
        Log.i("VideoSnapshot supported", Boolean.toString(params.isVideoSnapshotSupported()));

    }

    /**
     * Set the camera preview size,
     * Assign camera and view holder to the recorder.
     * Set the video quality, file type and file location.
     * Set video duration and set listener to handle the duration event.
     */


    private void initRecorder() {

        Log.i(TAG, "Initialize Recorder");
        CamcorderProfile cpHigh = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
        params.setPreviewSize(cpHigh.videoFrameWidth, cpHigh.videoFrameHeight);
        camera.setParameters(params);
        recorder.setPreviewDisplay(holder.getSurface());
        recorder.setCamera(camera);
        camera.unlock();
        recorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
        recorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);
        recorder.setProfile(cpHigh);
        recorder.setOrientationHint(90);
        recorder.setOutputFile(Environment.getExternalStorageDirectory().getAbsolutePath() + "/videoOut.mp4");
        Log.i(TAG, Environment.getExternalStorageDirectory().getAbsolutePath());
        recorder.setMaxDuration(10000); // 10 seconds
        recorder.setOnInfoListener(this);
    }

    /**
     * Set recorder preview display surface.
     * Recorder prepare.
     */


    private void prepareRecorder() {
        recorder.setPreviewDisplay(holder.getSurface());
        try {
            recorder.prepare();
        } catch (Exception e) {
            e.printStackTrace();
            recorder.release();
            finish();
        }
    }

    /**
     * Start and stop motion detection
     */

    public void onClick(View v) {
        Log.i(TAG, "clicked");
        if (videoButton.getText().equals("Stop Motion Detection")) {
            eyeball.stopAnimation();
            try {
                recorder.stop();
                recording = false;
            } catch (Exception e) {
                Log.i(TAG, e.toString());
                recorder.release();
            }
            initRecorder();
            prepareRecorder();
            videoButton.setText("Start Motion Detection");
        } else {
            recording = true;
            try {
                recorder.start();
                camera.takePicture(null, null, mPicture);
            } catch (Exception e) {
                Log.i(TAG, e.toString());
                recorder.release();
            }
            Log.i(TAG, "recording");
            eyeball.startAnimation();
            videoButton.setText("Stop Motion Detection");
        }
    }

    public void surfaceCreated(SurfaceHolder holder) {
        prepareRecorder();
        videoButton.performClick();
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
    }

    public void surfaceDestroyed(SurfaceHolder holder) {

        if (recording) {
            recorder.stop();
            recording = false;
        }
        recorder.release();
        camera.unlock();
        wakelock.release();
        finish();
    }
}

package com.mbcode64.android.thirdeye;

import android.app.Activity;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

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
    /**
     * Take a picture and process it.
     */


    Camera.PictureCallback mPicture = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            Log.i(TAG, "picture taken");
            /*File pictureFile = getOutputMediaFile();
            if (pictureFile == null) {
                return;
            }
            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();
            } catch (FileNotFoundException e) {

            } catch (IOException e) {
            }
*/
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        requestWindowFeature(Window.FEATURE_NO_TITLE);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);


        setContentView(R.layout.activity_video);

        SurfaceView cameraView = findViewById(R.id.surface_video);
        holder = cameraView.getHolder();
        holder.addCallback(this);

        cameraView.setClickable(true);
        cameraView.setOnClickListener(this);
        recorder = new MediaRecorder();
        initCamera();
        initRecorder();
    }

    /**
     * Capture info from the video recorder
     * If Max duration reached, stop video
     * and reset for next video.
     */


    public void onInfo(MediaRecorder mr, int what, int extra) {
        if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED
                && recording == true) {
            try {
                Log.i("VIDEOCAPTURE", "Maximum Duration Reached");
                mr.stop();
                recording = false;
                mr.reset();

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
        try {
            camera = Camera.open(); // attempt to get a Camera instance
        } catch (Exception e) {
            Log.d(TAG, "camera failed to open.");
        }
        try {
            camera.setPreviewDisplay(holder);
            camera.setDisplayOrientation(90);
            camera.startPreview();
        } catch (Exception e) {
            e.toString();
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

        CamcorderProfile cpHigh = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
        params.setPreviewSize(cpHigh.videoFrameWidth, cpHigh.videoFrameHeight);
        camera.setParameters(params);
        recorder.setPreviewDisplay(holder.getSurface());
        recorder.setCamera(camera);
        camera.unlock();
        recorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
        recorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);
        recorder.setProfile(cpHigh);
        recorder.setOutputFile("/sdcard/videocapture_example.mp4");
        recorder.setMaxDuration(5000); // 5 seconds
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
            finish();
        }
    }

    public void onClick(View v) {
        if (recording) {
            try {
                recorder.stop();
                recording = false;
            } catch (Exception e) {
                Log.i(TAG, e.toString());
            }
            initRecorder();
            prepareRecorder();
        } else {
            recording = true;
            try {
                recorder.start();
                camera.takePicture(null, null, mPicture);
            } catch (Exception e) {
                Log.i(TAG, e.toString());
            }
            Log.i(TAG, "recording");
        }
    }

    public void surfaceCreated(SurfaceHolder holder) {
        prepareRecorder();
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
        finish();
    }
}

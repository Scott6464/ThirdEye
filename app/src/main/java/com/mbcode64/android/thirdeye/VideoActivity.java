package com.mbcode64.android.thirdeye;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.ListIterator;

//import android.graphics.Camera;


/**
 * Start Video Recorder in a loop.
 * Take a Snapshot jpg during each video.
 * If no motion is detected, restart video.
 * If motion is detected, continue video until no motion is detected.
 * Upload completed video and save jpgs locally.
 */

public class VideoActivity extends Activity implements
        MediaRecorder.OnInfoListener, View.OnClickListener, SurfaceHolder.Callback {

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
    myPref myPrefs;
    SurfaceView cameraView;


    /**
     * Take a picture and process it.
     * Then take another one.
     */

    boolean motion = false;
    Camera.ErrorCallback CEC = new Camera.ErrorCallback() {
        public void onError(int error, Camera camera) {
            Log.d("CameraDemo", "camera error detected");
            if (error == Camera.CAMERA_ERROR_SERVER_DIED) {
                Log.d("CameraDemo", "attempting to reinstantiate new camera");
                camera.stopPreview();
                camera.setPreviewCallback(null);
                camera.release(); //written in documentation...
                camera = null;
                camera = Camera.open();

            }
        }
    };
    boolean saved = false;
    Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            Log.i(TAG, "picture taken");
            bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            if (md.detectMotion(bitmap, oldbitmap)) {
                Log.i(TAG, "motion detected");
                if (!saved) {
                    md.saveImage(bitmap);
                    saved = true;
                    Toast.makeText(getApplicationContext(), "Motion Detected", Toast.LENGTH_SHORT).show();
                }
                motion = true;
            }
            oldbitmap = bitmap;
            if (recording) {
                try {
                    camera.takePicture(null, null, mPicture);
                } catch (Exception e) {
                    // release the camera if there is an exception
                    Log.i(TAG, "onPictureTaken " + e.toString());
                    try {
                        //recorder.stop();
                        recorder.reset();
                        initRecorder();
                        prepareRecorder();
                        recorder.start();
                        camera.takePicture(null, null, mPicture);
                    } catch (Exception e1) {
                        Log.i(TAG, "camera reset " + e1.toString());
                    }
                    motion = false;
                    saved = false;
                    recording = true;
                }


            }
        }

    };
// TODO: 6/8/2018 keep filming as long as last pic says motion
    // TODO: 6/8/2018 logging on server when the email is sent.

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        requestWindowFeature(Window.FEATURE_NO_TITLE);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);


        setContentView(R.layout.activity_video);
        setMuteAll(true);
        myPrefs = new myPref(this);
        ImageView eye = findViewById(R.id.eyeball_image1);
        eyeball = new Eye(eye);
        cameraView = findViewById(R.id.surface_video);
        holder = cameraView.getHolder();
        holder.addCallback(this);

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

    // TODO: 6/8/2018 camera rotation
    // TODO: 6/8/2018 video quality preference
    // TODO: 6/8/2018 change graphic to yellow light around a white eyeball with red perimiter

    /**
     * Capture info from the video recorder
     * If Max duration reached, stop video
     * and reset for next video.
     */


    // TODO: 6/1/2018 make sure camera is released properly
    public void onInfo(MediaRecorder mr, int what, int extra) {
        Log.i(TAG, "onInfo");
        if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED
                && recording) {
            Log.i("VIDEOCAPTURE", "Maximum Duration Reached");
            recording = false;
            if (motion) {
                Thread t = new Thread() {
                    @Override
                    public void run() {
                        try {
                            copy();
                        } catch (Exception e) {
                            Log.i(TAG, e.toString());
                        }
                        myDrive.saveMp4ToDrive();
                    }
                };
                t.run();
            }
            try {
                mr.stop();
                mr.reset();
                initRecorder();
                prepareRecorder();
                mr.start();
                camera.takePicture(null, null, mPicture);
            } catch (Exception e) {
                Log.i(TAG, e.toString());
            }
            motion = false;
            saved = false;
            recording = true;

        }
    }


    public void copy() throws IOException {
        File src = new File(getFilesDir().getAbsolutePath() + "/videoOut.mp4");
        File dst = new File(getFilesDir().getAbsolutePath() + "/videoUp.mp4");
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


    /**
     * Open camera and set parameters.
     */


    void initCamera() {
        Log.i(TAG, "Initialize camera");
        // release camera if someone else has it.
        //recorder.setCamera(null);
        //if (recorder != null) {
        //    camera.release();
        //    camera = null;
        //}
        try {
            camera = Camera.open(); // attempt to get a Camera instance
        } catch (Exception e) {
            Log.i(TAG, "camera failed to open.");
            //Toast.makeText(this, "Camera is already in use. Reboot phone to reset the camera.", Toast.LENGTH_LONG).show();
            // TODO: 6/10/2018 change this to error page.
            //startActivity(new Intent(getApplicationContext(), MainActivity.class));
        }
        try {
            Log.i(TAG, "Initialize camera");
            camera.setPreviewDisplay(holder);
            camera.setDisplayOrientation(90);
            Log.i(TAG, "Initialize camera");
            camera.startPreview();
            params = camera.getParameters();
        } catch (Exception e) {
            Log.i(TAG, e.toString());
        }
    }


    private Camera.Size getBestFit(List<Camera.Size> sizes, int width, int height) {
        Camera.Size bestFit = null;
        ListIterator<Camera.Size> items = sizes.listIterator();
        while (items.hasNext()) {
            Camera.Size item = items.next();
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


    /**
     * Set the camera preview size,
     * Assign camera and view holder to the recorder.
     * Set the video quality, file type and file location.
     * Set video duration and set listener to handle the duration event.
     */

// TODO: 6/9/2018 video quality preference
    private void initRecorder() {

        Log.i(TAG, "Initialize Recorder");
        CamcorderProfile cpHigh = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
        if (camera != null) {
            params.setPreviewSize(cpHigh.videoFrameWidth, cpHigh.videoFrameHeight);
            camera.setParameters(params);
            recorder.setPreviewDisplay(holder.getSurface());
            recorder.setCamera(camera);
            camera.unlock();
            recorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
            recorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);
            recorder.setProfile(cpHigh);
            recorder.setOrientationHint(90);
            recorder.setOutputFile(getFilesDir().getAbsolutePath() + "/videoOut.mp4");
            Log.i(TAG, getFilesDir().getAbsolutePath());
            recorder.setMaxDuration(myPrefs.video * 1000);
            recorder.setOnInfoListener(this);
        }
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
            //messageText.setText("Detecting");
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
            //messageText.setText("Paused");
        }
    }

    public void surfaceCreated(SurfaceHolder holder) {
        prepareRecorder();
        videoButton.performClick();
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {

        // TODO: 5/29/2018 this is a fail for the entire app
        Log.i("VideoSnapshot supported", Boolean.toString(params.isVideoSnapshotSupported()));
        List<Camera.Size> sizes = params.getSupportedPreviewSizes();
        Camera.Size pickedSize = getBestFit(sizes, width, height);
        if (pickedSize != null) {
            params.setPreviewSize(pickedSize.width, pickedSize.height);
            Log.i(TAG, "Preview size: (" + pickedSize.width + ","
                    + pickedSize.height + ")");
            // even after setting a supported size, the preview size may
            // capture end up just being the surface size (supported or
            // not)
            camera.setParameters(params);
        }

    }


    public void surfaceDestroyed(SurfaceHolder holder) {

        if (recording) {
            try {
                recorder.stop();
            } catch (Exception e) {
                Log.i(TAG, e.toString());
            }
            recording = false;
        }
        try {
            recorder.release();
            camera.unlock();
            camera.release();
            wakelock.release();
        } catch (Exception e) {
            Log.i(TAG, e.toString());
        }
        finish();
    }

    void setMuteAll(boolean mute) {
        AudioManager manager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        int[] streams = new int[]{AudioManager.STREAM_ALARM,
                AudioManager.STREAM_DTMF, AudioManager.STREAM_MUSIC,
                AudioManager.STREAM_RING, AudioManager.STREAM_SYSTEM,
                AudioManager.STREAM_VOICE_CALL};

        for (int stream : streams)
            manager.setStreamMute(stream, mute);
    }


}

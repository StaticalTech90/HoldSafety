package com.example.holdsafety;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.IOException;
import java.util.Date;

public class AutoRecordingActivity extends AppCompatActivity {
    CameraPreview cameraPreview;
    Camera camera;
    MediaRecorder mediaRecorder;
    FrameLayout cameraLayout;
    File recordingFile;
    final String tag = "AUTORECORD";

    FloatingActionButton btnRecord;
    boolean isRecording = false;
    TextView txtIsRecording;

    ProgressBar progressBar;
    String[] permissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private final int RECORDING_REQ_CODE = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auto_recording);

        btnRecord = findViewById(R.id.btnRecord);
        txtIsRecording = findViewById(R.id.txtIsRecording);
        progressBar = findViewById(R.id.progressBar);
        cameraLayout = findViewById(R.id.camera_preview);


        btnRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isRecording){
                    //user is currently recording
                    //stop option
                    mediaRecorder.stop();
                    camera.lock();
                } else {
                    //user is not recording
                    //play option
                    if(prepareVideoRecorder()){
                        mediaRecorder.start();
                    } else {
                        releaseMediaRecorder();
                    }
                }
                isRecording = !isRecording;
                updateButtonUI();
            }
        });
        checkAllPermissions();
    }

    private void checkAllPermissions() {
        if(ActivityCompat.checkSelfPermission(this, permissions[0]) == PackageManager.PERMISSION_DENIED //camera permission
                || ActivityCompat.checkSelfPermission(this, permissions[1]) == PackageManager.PERMISSION_DENIED //audio permission
                || ActivityCompat.checkSelfPermission(this, permissions[2]) == PackageManager.PERMISSION_DENIED){ //storage permission
            ActivityCompat.requestPermissions(this, permissions, RECORDING_REQ_CODE);
            return;
        }

        camera = getCameraInstance();
        cameraPreview = new CameraPreview(this, camera);
        mediaRecorder = new MediaRecorder();
        cameraLayout.addView(cameraPreview);

        new CountDownTimer(1000, 1000){
            @Override
            public void onTick(long l) {
                long timeRemaining = (l/1000)+1;
            }

            @Override
            public void onFinish() {
                btnRecord.setEnabled(true);
                btnRecord.performClick();
            }
        }.start();

        //btnRecord.setEnabled(true);
        //btnRecord.performClick();
    }

    public void updateButtonUI(){
        if(isRecording){
            txtIsRecording.setVisibility(View.VISIBLE);
            txtIsRecording.setText("Recording");
            btnRecord.setImageResource(R.drawable.recording_shape);
        } else{
            //txtIsRecording.setText("Not Recording");
            txtIsRecording.setVisibility(View.INVISIBLE);
            btnRecord.setImageResource(0);
            //TODO: ADD TO FIREBASE
        }
    }

    private boolean prepareVideoRecorder() {
        // Step 1: Unlock and set camera to MediaRecorder
        camera.unlock();
        mediaRecorder.setCamera(camera);

        // Step 2: Set sources
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        // Step 3: Set a CamcorderProfile (requires API Level 8 or higher)
        mediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));

        // Step 4: Set output file
        mediaRecorder.setOutputFile(getOutputMediaFile().toString());

        // Step 5: Set the preview output
        //cameraPreview.getHolder().addCallback();
        mediaRecorder.setPreviewDisplay(cameraPreview.getHolder().getSurface());

        // Step 6: Prepare configured MediaRecorder
        try {
            mediaRecorder.prepare();
        } catch (IllegalStateException e) {
            Log.d("TAG", "IllegalStateException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            Log.d("TAG", "IOException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        }
        return true;
    }

    /** A safe way to get an instance of the Camera object. */
    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
            e.printStackTrace();
        }
        return c; // returns null if camera is unavailable
    }

    private void releaseMediaRecorder(){
        if (mediaRecorder != null) {
            mediaRecorder.reset();   // clear recorder configuration
            mediaRecorder.release(); // release the recorder object
            mediaRecorder = null;
            camera.lock();           // lock camera for later use
        }
    }

    /**
     * Create a File for saving a video
     */
    private File getOutputMediaFile() {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "EmergencyVideo");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        Date date = new Date();
        File mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                "VID_" + date.getTime() + ".mp4");

        recordingFile = mediaFile;
        return mediaFile;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == RECORDING_REQ_CODE){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[0])
                    || ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[1])
                    || ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[2])){
                ActivityCompat.requestPermissions(this, permissions, RECORDING_REQ_CODE);
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[2] == PackageManager.PERMISSION_GRANTED
                    && grantResults[3] == PackageManager.PERMISSION_GRANTED){//returns true
                //all permission granted
                checkAllPermissions();
            } else{
                //user denied
            }
        }
    }
}
package com.example.holdsafety;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class VideoRecordingActivity extends AppCompatActivity {
    CameraPreview cameraPreview;
    Camera camera;
    MediaRecorder mediaRecorder;
    FrameLayout cameraLayout;
    File recordingFile;
    FirebaseAuth mAuth;

    FloatingActionButton btnRecord;
    boolean isRecording = false;
    CardView txtIsRecording;

    ProgressBar progressBar;

    HashMap<String, String> evidenceLinkRequirements;
    String userID, nearestBrgy, reportID;

    StorageReference videoRef;
    String idUri;

    Map<String, Object> docUsers = new HashMap<>();
    FirebaseUser user;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    LogHelper logHelper;
    CountDownTimer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auto_recording);

        btnRecord = findViewById(R.id.btnRecord);
        txtIsRecording = findViewById(R.id.cardIsRecording);
        progressBar = findViewById(R.id.progressBar);
        cameraLayout = findViewById(R.id.camera_preview);
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        logHelper = new LogHelper(this, mAuth, user,this);

        videoRef = FirebaseStorage.getInstance().getReference("emergencyVideos/");

        Intent intent = getIntent();
        evidenceLinkRequirements = (HashMap<String, String>) intent.getSerializableExtra("evidenceLinkRequirements");

        userID = evidenceLinkRequirements.get("userID");
        nearestBrgy = evidenceLinkRequirements.get("nearestBrgy");
        reportID = evidenceLinkRequirements.get("reportID");

        timer = new CountDownTimer(10000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
            }
            @Override
            public void onFinish() {
                btnRecord.performClick();
            }
        };

        btnRecord.setOnClickListener(view -> {
            timer.cancel();
            if(isRecording){
                //user is currently recording
                //stop option
                mediaRecorder.stop();
                camera.lock();

                //add file to db
                addFileToFirebase();
            } else {
                //user is not recording
                //play option
                if(prepareVideoRecorder()){
                    mediaRecorder.start();
                    timer.start();
                } else {
                    releaseMediaRecorder();
                }
            }
            isRecording = !isRecording;
            updateButtonUI();
        });
        setCamera();
    }

    private void addFileToFirebase() {
        //show progress bar
        progressBar.setVisibility(View.VISIBLE);

        //add to firebase
        FirebaseStorage.getInstance()
                .getReference("emergencyVideos")
                .child(mAuth.getCurrentUser().getUid())
                .child(recordingFile.getName())
                .putFile(Uri.fromFile(recordingFile))
                .addOnSuccessListener(taskSnapshot -> {
                    logHelper.saveToFirebase("addFileToFirebase", "SUCCESS",
                            "Upload successful");
                    Toast.makeText(VideoRecordingActivity.this, "Upload successful", Toast.LENGTH_SHORT).show();
                    setHandler();
                    getVideoLink();

                    Intent landingIntent = new Intent(VideoRecordingActivity.this, LandingActivity.class);
                    landingIntent.putExtra("isFromWidget", "false");
                    startActivity(landingIntent);
                    finish(); // return to landing
                })
                .addOnFailureListener(e -> {
                    logHelper.saveToFirebase("addFileToFirebase", "ERROR",
                            e.getLocalizedMessage());
                    Toast.makeText(VideoRecordingActivity.this, "Upload failed", Toast.LENGTH_SHORT).show();
                    setHandler();

                })
                .addOnProgressListener(snapshot -> {
                    double progress = (100.0 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                    progressBar.getProgressDrawable().setColorFilter(ContextCompat.getColor(VideoRecordingActivity.this,R.color.light_blue), PorterDuff.Mode.MULTIPLY);
                    progressBar.setProgress((int) progress);
                });
    }

    private void getVideoLink() {
        //FETCH VIDEO LINK
        videoRef.child(user.getUid() + "/" + recordingFile.getName()).getDownloadUrl()
                .addOnSuccessListener(uri -> {
                    Log.d("Video to Document", "Fetching video URI success");
                    idUri = String.valueOf(uri);
                    docUsers.put("Evidence", idUri);
                    Log.d("Video to Document", idUri);

                    //UPDATE THE "Evidence" FIELD IN REPORT DB (GENERAL)
                    db.collection("reports").document(reportID).update(docUsers)
                            .addOnSuccessListener(unused ->
                                logHelper.saveToFirebase("getVideoLink", "SUCCESS",
                                        uri.toString()))
                            .addOnFailureListener(e -> logHelper.saveToFirebase("getVideoLink", "ERROR",
                                    e.getLocalizedMessage()));
                })
                .addOnFailureListener(e -> Log.d("Video to Document", "Fetching video URI failed. Log: " + e.getMessage()));
    }

    private void setCamera() {
        camera = getCameraInstance();
        cameraPreview = new CameraPreview(this, camera);
        mediaRecorder = new MediaRecorder(); // responsible on passing whatever the user is recording
        cameraLayout.addView(cameraPreview);

        //needs time for camera to process
        new CountDownTimer(1000, 1000){
            @Override
            public void onTick(long l) {
                long timeRemaining = (l/1000)+1;
                Log.d("TAG", "Time Remaining before recording: " + timeRemaining);
            }
            @Override
            public void onFinish() {
                btnRecord.setEnabled(true);
                btnRecord.performClick();
            }
        }.start();
    }

    public void updateButtonUI(){
        if(isRecording){
            txtIsRecording.setVisibility(View.VISIBLE);
            btnRecord.setImageResource(R.drawable.recording_shape);
        } else {
            //txtIsRecording.setText("Not Recording");
            txtIsRecording.setVisibility(View.INVISIBLE);
            btnRecord.setImageResource(0);
        }
    }

    private boolean prepareVideoRecorder() {
        // Step 1: Unlock and set camera to MediaRecorder
        camera.unlock();
        mediaRecorder.setOrientationHint(90);
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

    private File getOutputMediaFile() {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.
        File mediaStorageDir = new File(getFilesDir(), "EmergencyVideo");
        Log.i("Path", "the path is" + mediaStorageDir.getPath());
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("HoldSafetyCamera", "failed to create directory");
                return null;
            }
        }
        Date date = new Date();
        File mediaFile = new File(mediaStorageDir.getPath() + File.separator + "HoldSafety_" + date.getTime() + ".mp4");
        recordingFile = mediaFile;
        return mediaFile;
    }

    private void setHandler() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(View.INVISIBLE);
                progressBar.setProgress(0);
            }
        }, 2000);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mediaRecorder != null) {
            mediaRecorder.release();
            mediaRecorder = null;
        }
    }
}
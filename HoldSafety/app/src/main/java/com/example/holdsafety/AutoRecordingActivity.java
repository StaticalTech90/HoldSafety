package com.example.holdsafety;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.HashMap;
import java.util.Map;

public class AutoRecordingActivity extends AppCompatActivity {
    CameraPreview cameraPreview;
    Camera camera;
    MediaRecorder mediaRecorder;
    FrameLayout cameraLayout;
    File recordingFile;
    final String tag = "AUTORECORD";

    Intent intent = getIntent();
    String userID = intent.getStringExtra("userId");
    String reportID = intent.getStringExtra("reportId");

    FirebaseAuth mAuth;
    FirebaseUser user;

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    DocumentReference userReportDB = db.collection("reportUser").document(userID).collection("reportDetails").document(reportID);

    Map<String, Object> docUsers = new HashMap<>();
    StorageReference videoRef;
    String idUri;

    FloatingActionButton btnRecord;
    boolean isRecording = false;
    CardView txtIsRecording;

    ProgressBar progressBar;
    private final int RECORDING_REQ_CODE = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auto_recording);

        videoRef = FirebaseStorage.getInstance().getReference("emergencyVideos");

        user = mAuth.getCurrentUser();
        btnRecord = findViewById(R.id.btnRecord);
        txtIsRecording = findViewById(R.id.cardIsRecording);
        progressBar = findViewById(R.id.progressBar);
        cameraLayout = findViewById(R.id.camera_preview);
        mAuth = FirebaseAuth.getInstance();


        btnRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
                        //2. SET TIMER (5 SECONDS) - Limit of the recording
                        new CountDownTimer(5000, 1000){
                            @Override
                            public void onTick(long l) {
                                long timeRemaining = (l/1000) + 1;
                                //txtAudioRecording.setText("Recording will stop in " + timeRemaining + " seconds");
                            }

                            @Override
                            public void onFinish() {
                                //3. STOP RECORDING
                                btnRecord.performClick();
                            }

                        }.start();
                    } else {
                        releaseMediaRecorder();
                    }
                }
                isRecording = !isRecording;
                updateButtonUI();
            }
        });
        setCamera();
    }

    private void addFileToFirebase() {
        //show progress bar
        progressBar.setVisibility(View.VISIBLE);

        //add to firebase
        FirebaseStorage.getInstance()
                .getReference("emergencyVideos")
                .child(userID)
                .child(recordingFile.getName())
                .putFile(Uri.fromFile(recordingFile))
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Toast.makeText(AutoRecordingActivity.this, "Upload successful", Toast.LENGTH_SHORT).show();
                        setHandler();

                        videoRef.child(user.getUid()).child(recordingFile.getName()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                idUri = String.valueOf(uri);
                                docUsers.put("Evidence", idUri);
                                Log.i("URI gDUrl()", idUri);

                                userReportDB.set(docUsers)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Toast.makeText(getApplicationContext(),
                                                        "pushed video to document",
                                                        Toast.LENGTH_SHORT).show();
                                                Log.i(TAG, "Video pushed");
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(getApplicationContext(),
                                                        "Error writing document",
                                                        Toast.LENGTH_SHORT).show();
                                                Log.w(TAG, "Error writing document", e);
                                            }
                                        });
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(AutoRecordingActivity.this, "Upload failed: " +e.getMessage(), Toast.LENGTH_SHORT).show();
                        setHandler();

                    }
                })
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                        double progress = (100.0 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                        progressBar.getProgressDrawable().setColorFilter(ContextCompat.getColor(AutoRecordingActivity.this,R.color.light_blue), PorterDuff.Mode.MULTIPLY);
                        progressBar.setProgress((int) progress);
                    }
                });
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
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH:mma", Locale.getDefault());
        String currentDateandTime = sdf.format(new Date());
//        Date date = new Date();

        File mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                "VID_" + currentDateandTime + ".mp4");

        recordingFile = mediaFile;
        return mediaFile;
    }

    private void setHandler() {
        Handler handler = new Handler();
        handler.postDelayed(() -> {
            progressBar.setVisibility(View.INVISIBLE);
            progressBar.setProgress(0);
        }, 2000);
    }
}
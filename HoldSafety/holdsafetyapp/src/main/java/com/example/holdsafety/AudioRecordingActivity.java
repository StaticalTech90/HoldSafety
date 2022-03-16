package com.example.holdsafety;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class AudioRecordingActivity extends AppCompatActivity {
    LogHelper logHelper;
    Button btnAudio;
    boolean isRecording = false;
    MediaRecorder mediaRecorder;
    FirebaseAuth mAuth;
    ProgressBar progressBar;

    TextView txtAudioRecording;
    File recordingFile;

    HashMap<String, String> evidenceLinkRequirements;
    String userID, nearestBrgy, reportID;

    CountDownTimer timer;

    FirebaseUser user;
    StorageReference audioRef;
    String idUri;
    Map<String, Object> docReportDetails = new HashMap<>();
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_recording);


        btnAudio = findViewById(R.id.btnRecordAudio);
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        progressBar = findViewById(R.id.audioProgressBar);
        txtAudioRecording = findViewById(R.id.txtAudioRecord);

        audioRef = FirebaseStorage.getInstance().getReference("emergencyAudios/");
        logHelper = new LogHelper(this, mAuth, user,this);

        Intent intent = getIntent();
        evidenceLinkRequirements = (HashMap<String, String>) intent.getSerializableExtra("evidenceLinkRequirements");

        userID = evidenceLinkRequirements.get("userID");
        nearestBrgy = evidenceLinkRequirements.get("nearestBrgy");
        reportID = evidenceLinkRequirements.get("reportID");

        Toast.makeText(getApplicationContext(), "HasmapReportID: " + reportID , Toast.LENGTH_SHORT).show();

        timer = new CountDownTimer(10000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long timeRemaining = (millisUntilFinished/1000) + 1;
                txtAudioRecording.setText("Recording will stop in " + timeRemaining + " seconds");
            }
            @Override
            public void onFinish() {
                txtAudioRecording.setText("");
                btnAudio.performClick();
            }
        };

        btnAudio.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                timer.cancel();
                if(isRecording){
                    //user is currently recording
                    //stop option
                    stopRecording();
                } else {
                    //user is not recording
                    //start option
                    timer.start();
                    startRecording();

                }
                isRecording = !isRecording;
            }
        });
        recordAudio();
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private void startRecording() {
        try {
            mediaRecorder = new MediaRecorder();
            mediaRecorder.reset();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mediaRecorder.setOutputFile(getOutputMediaFile());
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mediaRecorder.prepare();
            mediaRecorder.start();

            btnAudio.setText("Stop Recording");
            Toast.makeText(AudioRecordingActivity.this, "Audio Recording Started", Toast.LENGTH_SHORT).show();

        } catch (Exception e){
            Toast.makeText(AudioRecordingActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

    }

    private void stopRecording() {
        mediaRecorder.stop();
        mediaRecorder.reset();
        mediaRecorder.release();
        mediaRecorder = null;

        txtAudioRecording.setText("");
        btnAudio.setText("Start Recording");
        logHelper.saveToFirebase("stopRecording", "SUCCESS", "Recording Stopped");
        Toast.makeText(AudioRecordingActivity.this, "Recording Stopped", Toast.LENGTH_SHORT).show();
        addToFirebase();

    }

    private void addToFirebase() {
        //show progress bar
        progressBar.setVisibility(View.VISIBLE);

        //add to firebase
        FirebaseStorage.getInstance()
                .getReference("emergencyAudios")
                .child(Objects.requireNonNull(mAuth.getCurrentUser()).getUid())
                .child(recordingFile.getName())
                .putFile(Uri.fromFile(recordingFile))
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        logHelper.saveToFirebase("addToFirebase", "SUCCESS", "Upload successful");
                        Toast.makeText(AudioRecordingActivity.this, "Upload successful", Toast.LENGTH_SHORT).show();
                        getAudioLink();
                        setHandler();

                        Intent landingIntent = new Intent(AudioRecordingActivity.this, LandingActivity.class);
                        landingIntent.putExtra("isFromWidget", "false");
                        startActivity(landingIntent);
                        finish(); // return to landing
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        logHelper.saveToFirebase("addToFirebase", "ERROR", "Upload failed");

                        Toast.makeText(AudioRecordingActivity.this, "Upload failed.", Toast.LENGTH_SHORT).show();
                        setHandler();
                    }
                })
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                        double progress = (100.0 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                        progressBar.getProgressDrawable().setColorFilter(ContextCompat.getColor(AudioRecordingActivity.this,R.color.light_blue), PorterDuff.Mode.MULTIPLY);
                        progressBar.setProgress((int) progress);
                    }
                });
    }

    private void getAudioLink() {
        //FETCH VIDEO LINK
        audioRef.child(user.getUid() + "/" + recordingFile.getName()).getDownloadUrl()
                .addOnSuccessListener(uri -> {
                    logHelper.saveToFirebase("getAudioLink", "SUCCESS", "Fetching audio URI success");

                    Log.d("Audio to Document", "Fetching audio URI success");
                    idUri = String.valueOf(uri);
                    docReportDetails.put("Evidence", idUri);
                    Log.d("Audio to Document", idUri);

                    //UPDATE THE "Evidence" FIELD IN REPORT DB (GENERAL)
                    db.collection("reports").document(reportID).update(docReportDetails)
                            .addOnSuccessListener(unused ->
                                    logHelper.saveToFirebase("getAudioLink", "SUCCESS",
                                            "Success! pushed to reportGeneral, id " + nearestBrgy + " w/ audio ID " + idUri))
                            .addOnFailureListener(e -> logHelper.saveToFirebase("getAudioLink", "ERROR",
                                    e.getLocalizedMessage()));
                }).addOnFailureListener(e -> logHelper.saveToFirebase("getAudioLink", "ERROR",
                        e.getLocalizedMessage()));
    }

    public void recordAudio(){
        btnAudio.setEnabled(true);
        btnAudio.performClick();
    }

    /**
     * Create a File for saving audio
     */
    private File getOutputMediaFile() {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(getFilesDir(), "EmergencyAudio");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("HoldSafety Audio", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        Date date = new Date();
        File mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                "AUD_" + date.getTime() + ".mp3");

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
}
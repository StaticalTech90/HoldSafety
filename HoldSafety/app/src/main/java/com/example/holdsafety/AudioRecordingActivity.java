package com.example.holdsafety;

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

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AudioRecordingActivity extends AppCompatActivity {

    Button btnAudio;
    boolean isRecording = true;
    MediaRecorder mediaRecorder;

    HashMap<String, String> evidenceLinkRequirements;
    String userID, nearestBrgy, reportID;
    String idUri;

    FirebaseUser user;
    FirebaseAuth mAuth;
    ProgressBar progressBar;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    Map<String, Object> docUsers = new HashMap<>();
    StorageReference audioRef;

    TextView txtAudioRecording;
    File recordingFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_recording);

        audioRef = FirebaseStorage.getInstance().getReference("emergencyAudios/");

        btnAudio = findViewById(R.id.btnRecordAudio);
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        progressBar = findViewById(R.id.audioProgressBar);
        txtAudioRecording = findViewById(R.id.txtAudioRecord);

        Intent intent = getIntent();
        evidenceLinkRequirements = (HashMap<String, String>) intent.getSerializableExtra("evidenceLinkRequirements");

        userID = evidenceLinkRequirements.get("userID");
        nearestBrgy = evidenceLinkRequirements.get("nearestBrgy");
        reportID = evidenceLinkRequirements.get("reportID");

        btnAudio.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                if(isRecording){
                    //user is currently recording
                    //stop option
                    mediaRecorder.stop();
                    addToFirebase();
                } else {
                    if(prepareAudioRecorder()){
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
                                btnAudio.performClick();
                            }
                        }.start();
                    } else {
                        releaseMediaRecorder();
                    }
                    //user is not recording
                    //start option
                    //startRecording();
                }
                onRecord(isRecording);
                isRecording = !isRecording;
            }
        });
        setRecorder();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void onRecord(boolean start) {
        if (start) {
            startRecording();
        } else {
            stopRecording();
        }
    }

    private void stopRecording() {
        mediaRecorder.stop();
        mediaRecorder.release();
        //mediaRecorder = null;
        addToFirebase();
    }

    private void addToFirebase() {
        //show progress bar
        progressBar.setVisibility(View.VISIBLE);

        //add to firebase
        FirebaseStorage.getInstance()
                .getReference("emergencyAudios")
                .child(mAuth.getCurrentUser().getUid())
                .child(recordingFile.getName())
                .putFile(Uri.fromFile(recordingFile))
                .addOnSuccessListener(taskSnapshot -> {
                    Toast.makeText(AudioRecordingActivity.this, "Upload successful", Toast.LENGTH_SHORT).show();
                    setHandler();
                    getAudioLink();
                    finish(); // return to landing
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AudioRecordingActivity.this, "Upload failed: " +e.getMessage(), Toast.LENGTH_SHORT).show();
                    setHandler();
                })
                .addOnProgressListener(snapshot -> {
                    double progress = (100.0 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                    progressBar.getProgressDrawable().setColorFilter(ContextCompat.getColor(AudioRecordingActivity.this,R.color.light_blue), PorterDuff.Mode.MULTIPLY);
                    progressBar.setProgress((int) progress);
                });
    }

    private void getAudioLink() {
        //FETCH VIDEO LINK
        audioRef.child(user.getUid() + "/" + recordingFile.getName()).getDownloadUrl()
                .addOnSuccessListener(uri -> {
                    Log.d("Video to Document", "Fetching video URI success");
                    idUri = String.valueOf(uri);
                    docUsers.put("Evidence", idUri);
                    Log.d("Video to Document", idUri);

                    //UPDATE THE "Evidence" FIELD IN REPORT DB (GENERAL)
                    db.collection("reports").document(reportID).update(docUsers)
                            .addOnSuccessListener(unused -> Log.d("Video to Document", "Success! pushed to reportGeneral, id " + nearestBrgy + " w/vid ID " + idUri))
                            .addOnFailureListener(e -> Log.d("Video to Document", "Failed to save to reportGeneral"));
                })
                .addOnFailureListener(e -> Log.d("Video to Document", "Fetching video URI failed. Log: " + e.getMessage()));
    }

    private void recordAudio(){
        /*
        //START DELAY TIMER
        new CountDownTimer(1000, 1000){

            @Override
            public void onTick(long l) {
                long timeRemaining = (l/1000) + 1;
                txtAudioRecording.setText("Recording in " + timeRemaining + " seconds");
            }

            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onFinish() {
                //TODO SET TO FRONT CAM
                //SET TO 5 SECONDS ONLY
                //1. START RECORDING
                //startRecording();
                btnAudio.setEnabled(true);
                btnAudio.performClick();
            }
        }.start();
         */
        btnAudio.setEnabled(true);
        btnAudio.performClick();
    }

    private void setRecorder(){
        mediaRecorder = new MediaRecorder();

        //needs time for audio recorder to process
        new CountDownTimer(1000, 1000){
            @Override
            public void onTick(long l) {
                long timeRemaining = (l/1000)+1;
                Log.d("TAG", "Time Remaining before recording: " + timeRemaining);
            }
            @Override
            public void onFinish() {
                btnAudio.setEnabled(true);
            }
        }.start();
    }

    private boolean prepareAudioRecorder(){
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

        mediaRecorder.setOutputFile(String.valueOf(getOutputMediaFile()));

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

    private void releaseMediaRecorder(){
        if (mediaRecorder != null) {
            mediaRecorder.release(); // release the recorder object
            mediaRecorder = null;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void startRecording() {
        try {
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

            mediaRecorder.setOutputFile(String.valueOf(getOutputMediaFile()));
            Log.e("tag", String.valueOf(getOutputMediaFile()));

            try {
                mediaRecorder.prepare();
                mediaRecorder.start();
            } catch (IOException e) {
                releaseMediaRecorder();
                Log.e("tag", "prepare() failed");
            }

            btnAudio.setText("Stop Recording");
            Toast.makeText(AudioRecordingActivity.this, "Audio Recording Started", Toast.LENGTH_SHORT).show();

            //2. SET TIMER (5 SECONDS) - Limit of the recording
            new CountDownTimer(5000, 1000){

                @Override
                public void onTick(long l) {
                    long timeRemaining = (l/1000) + 1;
                    txtAudioRecording.setText("Recording will stop in " + timeRemaining + " seconds");
                }

                @Override
                public void onFinish() {
                    //3. STOP RECORDING
                    txtAudioRecording.setText("");
                    stopRecording();
                }
            }.start();

        } catch (Exception e){
            Toast.makeText(AudioRecordingActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private File getOutputMediaFile() {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_MUSIC), "EmergencyAudio");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("HoldSafetyAudio", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        Date date = new Date();
        Timestamp timestamp = new Timestamp(date.getTime());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH:mm", Locale.getDefault());
        String currentDateAndTime = sdf.format(timestamp);

        File mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                "AUD_" + currentDateAndTime + ".mp3");

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

    @Override
    public void onBackPressed() {
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
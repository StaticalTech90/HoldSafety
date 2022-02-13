package com.example.holdsafety;

import static android.content.ContentValues.TAG;

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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.HashMap;
import java.util.Map;

public class AudioRecording extends AppCompatActivity {

    Button btnAudio;
    boolean isRecording = false;
    MediaRecorder mediaRecorder;

    Intent intent = getIntent();
    String userID = intent.getStringExtra("userId");
    String reportID = intent.getStringExtra("reportId");
    String idUri;

    FirebaseUser user;
    FirebaseAuth mAuth;
    ProgressBar progressBar;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    DocumentReference userReportDB = db.collection("reportUser").document(userID).collection("reportDetails").document(reportID);

    Map<String, Object> docUsers = new HashMap<>();
    StorageReference audioRef;

    TextView txtAudioRecording;
    File recordingFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_recording);

        audioRef = FirebaseStorage.getInstance().getReference("emergencyAudios");

        user = mAuth.getCurrentUser();
        btnAudio = findViewById(R.id.btnRecordAudio);
        mAuth = FirebaseAuth.getInstance();
        progressBar = findViewById(R.id.audioProgressBar);
        txtAudioRecording = findViewById(R.id.txtAudioRecord);

        btnAudio.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                if(isRecording){
                    //user is currently recording
                    //stop option
                    stopRecording();
                } else {
                    //user is not recording
                    //start option
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
            Toast.makeText(AudioRecording.this, "Audio Recording Started", Toast.LENGTH_SHORT).show();


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
            Toast.makeText(AudioRecording.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

    }

    private void stopRecording() {
        mediaRecorder.stop();
        mediaRecorder.reset();
        mediaRecorder.release();
        mediaRecorder = null;

        btnAudio.setText("Start Recording");
        Toast.makeText(AudioRecording.this, "Recording Stopped", Toast.LENGTH_SHORT).show();
        addToFirebase();

    }

    private void addToFirebase() {
        //show progress bar
        progressBar.setVisibility(View.VISIBLE);

        //add to firebase
        FirebaseStorage.getInstance()
                .getReference("emergencyAudios")
                .child(userID)
                .child(recordingFile.getName())
                .putFile(Uri.fromFile(recordingFile))
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Toast.makeText(AudioRecording.this, "Upload successful", Toast.LENGTH_SHORT).show();
                        setHandler();

                        audioRef.child(user.getUid()).child(recordingFile.getName()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
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
                                                        "pushed audio to report",
                                                        Toast.LENGTH_SHORT).show();
                                                Log.i(TAG, "Audio pushed");
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
                        Toast.makeText(AudioRecording.this, "Upload failed.", Toast.LENGTH_SHORT).show();
                        setHandler();
                    }
                })
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                        double progress = (100.0 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                        progressBar.getProgressDrawable().setColorFilter(ContextCompat.getColor(AudioRecording.this,R.color.light_blue), PorterDuff.Mode.MULTIPLY);
                        progressBar.setProgress((int) progress);
                    }
                });
    }

    public void recordAudio(){

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

    /**
     * Create a File for saving audio
     */
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
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH:mma", Locale.getDefault());
        String currentDateandTime = sdf.format(new Date());
//        Date date = new Date();

        File mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                "AUD_" + currentDateandTime + ".mp3");

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
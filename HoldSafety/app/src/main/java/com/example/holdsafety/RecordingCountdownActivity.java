package com.example.holdsafety;

import android.Manifest;
import android.content.Intent;
import android.graphics.Camera;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class RecordingCountdownActivity extends AppCompatActivity {
    TextView countdownTimer;
    Button btnCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recording_countdown);

        btnCancel = findViewById(R.id.btnCancel);
        countdownTimer = findViewById(R.id.countdown);

        CountDownTimer timer = new CountDownTimer(3000, 1000)
        {
            public void onTick(long millisUntilFinished) {
                long remainTime = (millisUntilFinished/1000)+1;
                countdownTimer.setText(Long.toString(remainTime));
            }

            public void onFinish() {
                //Toast.makeText(getApplicationContext(), "2 seconds finished", Toast.LENGTH_SHORT).show();
                //countdownTimer.setText("3");
                Toast.makeText(getApplicationContext(), "Done Countdown" , Toast.LENGTH_SHORT).show();
                startActivity(new Intent(RecordingCountdownActivity.this, AutoRecordingActivity.class));
            }

        };

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                timer.cancel();
                Toast.makeText(getApplicationContext(), "Cancel", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(RecordingCountdownActivity.this, LandingActivity.class));
                finish();

            }
        });

        timer.start();
    }
}
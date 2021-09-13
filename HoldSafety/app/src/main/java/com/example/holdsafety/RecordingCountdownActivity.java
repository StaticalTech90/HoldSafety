package com.example.holdsafety;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.TextView;
import android.widget.Toast;

public class RecordingCountdownActivity extends AppCompatActivity {
    TextView countdownTimer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recording_countdown);

        CountDownTimer cTimer = null;
        startTimer(cTimer);
    }

    //start timer function
    public void startTimer(CountDownTimer cTimer) {
        countdownTimer = findViewById(R.id.countdown);
        cTimer = new CountDownTimer(3000, 1000) {
            public void onTick(long millisUntilFinished) {
                long remainTime = millisUntilFinished/1000;
                countdownTimer.setText(Long.toString(remainTime+1));
            }
            public void onFinish() {
                //Toast.makeText(getApplicationContext(), "2 seconds finished", Toast.LENGTH_SHORT).show();
                countdownTimer.setText("3");
            }
        };
        cTimer.start();
    }

    //cancel timer
    public void cancelTimer(CountDownTimer cTimer) {
        if(cTimer!=null){
            Toast.makeText(getApplicationContext(), "Cancel", Toast.LENGTH_SHORT).show();
            cTimer.cancel();
        }
    }
}
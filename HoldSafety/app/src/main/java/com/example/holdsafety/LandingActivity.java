package com.example.holdsafety;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class LandingActivity extends AppCompatActivity {
    Button btnSafetyButton;
    TextView seconds;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);

        btnSafetyButton = findViewById(R.id.btnSafetyButton);
        btnSafetyButton.setOnTouchListener(new View.OnTouchListener() {
            //Declare timer
            CountDownTimer cTimer = null;

            private long firstTouchTS = 0;
            private int timer = 0;
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction()==MotionEvent.ACTION_DOWN) {
                    this.firstTouchTS = System.currentTimeMillis();
                    startTimer(cTimer);
                } else if (event.getAction()==MotionEvent.ACTION_UP) {
                    timer = (int)(System.currentTimeMillis()-this.firstTouchTS)/1000;
                    Toast.makeText(getApplicationContext(), "Time Pressed: " + timer, Toast.LENGTH_SHORT).show();

                }
                return false;
            }
        });
    }

    public void menuRedirect(View view) {
        Intent intent = new Intent(getApplicationContext(), MenuActivity.class);
        startActivity(intent);
    }

    //start timer function
    public void startTimer(CountDownTimer cTimer) {
        seconds = findViewById(R.id.countdown);
        cTimer = new CountDownTimer(2000, 1000) {
            public void onTick(long millisUntilFinished) {
                long remainTime = millisUntilFinished/1000;
                seconds.setText(Long.toString(remainTime));
            }
            public void onFinish() {
                Toast.makeText(getApplicationContext(), "2 seconds finished", Toast.LENGTH_SHORT).show();
                seconds.setText("2");
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
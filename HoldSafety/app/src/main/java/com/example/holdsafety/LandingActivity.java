package com.example.holdsafety;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LandingActivity extends AppCompatActivity {
    Button btnSafetyButton;
    TextView seconds, description;
    private int timer;
    long remainTime;
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);

        Intent intent = new Intent(LandingActivity.this, RecordingCountdownActivity.class);

        seconds = findViewById(R.id.countdown);
        btnSafetyButton = findViewById(R.id.btnSafetyButton);

        //handle method for holdsafety button
        btnSafetyButton.setOnTouchListener(new View.OnTouchListener() {

            //Declare timer instance
            CountDownTimer cTimer = new CountDownTimer(2000, 1000) {

                //wait for timer to countdown
                public void onTick(long millisUntilFinished) {
                    remainTime = millisUntilFinished/1000;
                    seconds.setText(Long.toString(remainTime));
                }

                //timer executes this code once finished
                public void onFinish() {
                    //Toast.makeText(getApplicationContext(), "2 seconds finished", Toast.LENGTH_SHORT).show();
                    //Enable wifi when button is held
                    WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                    wifiManager.setWifiEnabled(true);
                    startActivity(intent);
                }
            };

            private long firstTouchTS = 0;

            //onTouch handles the long press events (press -> cancel; hold<2 -> cancel; hold>2 -> proceed)
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //if button pressed down
                if (event.getAction()==MotionEvent.ACTION_DOWN) {
                    this.firstTouchTS = System.currentTimeMillis();
                    //start
                    cTimer.start();
                } else if (event.getAction()==MotionEvent.ACTION_UP) { //button released
                    int timer = (int) (System.currentTimeMillis() - this.firstTouchTS) / 1000; //2 second timer

                    Toast.makeText(getApplicationContext(), "Time Pressed: " + timer, Toast.LENGTH_SHORT).show(); //for debug

                    cTimer.cancel();    //cancels countdown; invalidates startActivity
                    seconds.setText(Long.toString(remainTime)); //reset seconds
                }
                return false;
            }
        });
    }

    public void menuRedirect(View view) {
        Intent intent = new Intent(getApplicationContext(), MenuActivity.class);
        startActivity(intent);
    }

    //not working right now
    //start timer function
//    public void startTimer(CountDownTimer cTimer) {
//        seconds = findViewById(R.id.countdown); //kunin seconds
//
//        //make initialize the thing
//        cTimer = new CountDownTimer(2000, 1000) {
//            public void onTick(long millisUntilFinished) {
//                long remainTime = millisUntilFinished/1000;
//                seconds.setText(Long.toString(remainTime));
//            }
//            public void onFinish() {
//                //Toast.makeText(getApplicationContext(), "2 seconds finished", Toast.LENGTH_SHORT).show();
//                Intent intent = new Intent(getApplicationContext(), RecordingCountdownActivity.class);
//                startActivity(intent);
//            }
//        };
//
//        //start of timer
//        cTimer.start();
//    }

    //cancel timer
    public void cancelTimer(CountDownTimer cTimer) {
        if(cTimer!=null){
            Toast.makeText(getApplicationContext(), "Cancel", Toast.LENGTH_SHORT).show();
            cTimer.cancel();
        }
    }
}
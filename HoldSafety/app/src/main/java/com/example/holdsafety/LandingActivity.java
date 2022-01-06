package com.example.holdsafety;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Collections;
import java.util.List;

public class LandingActivity extends AppCompatActivity {
    FirebaseAuth mAuth;
    String userID;

    Button btnSafetyButton;
    TextView seconds, description;
    private int timer;
    long remainTime;

    private static final int SEND_SMS_REQ_CODE = 1002;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);

        mAuth = FirebaseAuth.getInstance();
        userID = FirebaseAuth.getInstance().getCurrentUser().getUid();

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

                    sendAlertMessage();
                    //startActivity(intent);
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

                    //Toast.makeText(getApplicationContext(), "Time Pressed: " + timer, Toast.LENGTH_SHORT).show(); //for debug

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


    //cancel timer
    public void cancelTimer(CountDownTimer cTimer) {
        if(cTimer!=null){
            Toast.makeText(getApplicationContext(), "Cancel", Toast.LENGTH_SHORT).show();
            cTimer.cancel();
        }
    }

    private void sendAlertMessage(){
        //SMS Permission
        //TODO: Put to different func
        if (ActivityCompat.checkSelfPermission(LandingActivity.this,
                Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_DENIED) {
            //DENIED LOCATION PERMISSION
            Log.d("location permission", "Please Grant SMS Permission");
            //SHOW PERMISSION
            ActivityCompat.requestPermissions(LandingActivity.this,
                    new String[]{Manifest.permission.SEND_SMS},
                    SEND_SMS_REQ_CODE);
            return;
        }

        //Get user's emergency contacts
        FirebaseFirestore.getInstance()
                .collection("emergencyContacts")
                .document(userID)
                .collection("contacts")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        //Get each contact details
                        for(QueryDocumentSnapshot snapshot : task.getResult()){
                            String contactID = snapshot.getId();

                            String mobileNumber = snapshot.getString("mobileNumber");
                            String firstName = snapshot.getString("firstName");
                            String lastName = snapshot.getString("lastName");
                            String email = snapshot.getString("email");

                            String fullName = firstName + " " + lastName;
                            String message = "Hello " + fullName;

                            //Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();

                            //Send Text Message
                            if (mobileNumber != null) {
                                registerReceiver(new BroadcastReceiver() {
                                    @Override
                                    public void onReceive(Context context, Intent intent) {
                                        switch (getResultCode()) {

                                            case Activity.RESULT_OK:
                                                //MESSAGE SENT
                                                break;
                                            case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                                                break;
                                            case SmsManager.RESULT_ERROR_NO_SERVICE:
                                                break;
                                            case SmsManager.RESULT_ERROR_NULL_PDU:
                                                break;
                                            case SmsManager.RESULT_ERROR_RADIO_OFF:
                                                break;
                                        }
                                    }
                                }, new IntentFilter("SMS_SENT"));


                                //SEND SMS
                                SmsManager manager = SmsManager.getDefault();
                                PendingIntent sentPI = PendingIntent.getBroadcast(LandingActivity.this,
                                        SEND_SMS_REQ_CODE, new Intent("SMS_SENT"), 0);

                                manager.sendTextMessage(mobileNumber, null, message, sentPI, null);

                            }

                            if(email!=null){
                                //Send Email
                                String username = "holdsafety.ph@gmail.com";
                                String password = "HoldSafety@4qmag";
                                String subject = "[EMERGENCY] Alert Message - HoldSafety";

                                List<String> recipients = Collections.singletonList(email);
                                //email of sender, password of sender, list of recipients, email subject, email body
                                new MailTask(LandingActivity.this).execute(username, password, recipients, subject, message);

                                Toast.makeText(getApplicationContext(), "Email Sent", Toast.LENGTH_LONG).show();


                            }
                        }
                    }
                });
    }
}
package com.example.holdsafety;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.holdsafety.databinding.ActivityLandingBinding;
import com.google.android.gms.common.internal.Constants;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wearable.CapabilityClient;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import java.nio.charset.StandardCharsets;
import java.util.Set;

public class LandingActivity extends Activity {
    private TextView mTextView;
    private ActivityLandingBinding binding;
    Button btnSafetyButton;
    TextView instruction, signal, seconds;
    long remainTime;

    private static final String CAPABILITY_PHONE_APP = "send_signal";

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityLandingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mTextView = binding.textView;
        btnSafetyButton = findViewById(R.id.btnSafetyButton);
        instruction = findViewById(R.id.instruction);
        signal = findViewById(R.id.signal);
        seconds = findViewById(R.id.timer);

        //receive message
        Wearable.getMessageClient(LandingActivity.this).addListener(messageEvent -> {
            if(messageEvent.getData() == "1".getBytes(StandardCharsets.UTF_8)) {
                Log.d("SIGNAL", "INSERT REPORT METHOD HERE");
            } else {
                Log.d("SIGNAL", "FUCK");
            }
        });

        //handle method for holdsafety button
        btnSafetyButton.setOnTouchListener(new View.OnTouchListener() {
            //Declare timer instance
            CountDownTimer cTimer = new CountDownTimer(2000, 1000) {
                //wait for timer to countdown
                public void onTick(long millisUntilFinished) {
                    remainTime = millisUntilFinished / 1000;
                    seconds.setText(Long.toString(remainTime+1));
                }

                //send signal to the phone to do the report function
                public void onFinish() {
                    Log.i("STATUS", "Button pressed for 2s, signal phone");
                    //TODO: send signal to phone here
                    //Get available nodes
                    Task<CapabilityInfo> capabilityInfoTask = Wearable.getCapabilityClient(LandingActivity.this)
                            .getCapability(CAPABILITY_PHONE_APP, CapabilityClient.FILTER_REACHABLE);

                    capabilityInfoTask.addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            CapabilityInfo capabilityInfo = task.getResult();
                            Set<Node> nodes = capabilityInfo.getNodes();
                            Log.d("SIGNAL", "Node list: " + nodes);

//                            Node list: [Node{1jI41ZKfhniwBMR2J_801SAAAAABFBbmRyb2lkU2hhcmVfNjA5NA==, id=af51ad9, hops=1, isNearby=true}]

//                            Wearable.getMessageClient(LandingActivity.this).sendMessage(nodes.)

                            if(nodes != null) {
                                for(Node node : nodes) { //send to all nodes?
                                    byte[] message = {1};
                                    Wearable.getMessageClient(LandingActivity.this).sendMessage(
                                            node.getId(), "Sending signal to phone...", message);
                                    Log.d("SIGNAL", "message: " + message[0]);
                                    Log.d("SIGNAL", "Sending signal to connected device: " + node.getId());
                                }
                            }
                        } else {
                            Log.d("SIGNAL", "Capability request failed to return any results.");
                        }
                    });
                }
            };
            private long firstTouchTS = 0;

            //onTouch handles the long press events (press -> cancel; hold<2 -> cancel; hold>2 -> proceed)
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //if button pressed down
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    this.firstTouchTS = System.currentTimeMillis();
                    //start
                    cTimer.start();
                    instruction.setVisibility(View.INVISIBLE);
                    signal.setVisibility(View.VISIBLE);
                    seconds.setVisibility(View.VISIBLE);
                } else if (event.getAction() == MotionEvent.ACTION_UP) { //button released
                    int timer = (int) (System.currentTimeMillis() - this.firstTouchTS) / 1000; //2 second timer

                    //for debug
                    //Toast.makeText(getApplicationContext(), "Time Pressed: " + timer, Toast.LENGTH_SHORT).show();

                    //cancels countdown; invalidates startActivity
                    cTimer.cancel();
                    instruction.setVisibility(View.VISIBLE);
                    signal.setVisibility(View.INVISIBLE);
                    seconds.setVisibility(View.INVISIBLE);

                    //reset seconds
                    seconds.setText("2");
                }
                return false;
            }
        });
    }
}
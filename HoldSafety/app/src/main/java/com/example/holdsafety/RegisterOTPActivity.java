package com.example.holdsafety;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterOTPActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    FirebaseFirestore db;
    FirebaseUser user;
    StorageReference imageRef = FirebaseStorage.getInstance().getReference("id");

    Button btnSendCode;
    EditText etEmail;
    TextView txtTimeRemaining;

    String userEmail, idUri;
    HashMap<String, Object> docUsers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_otp);

        //Firebase
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();

        btnSendCode = findViewById(R.id.btnSendCode);
        etEmail = findViewById(R.id.txtEmail);
        txtTimeRemaining = findViewById(R.id.txtTimeRemaining);

        //Get extras
        userEmail = getIntent().getStringExtra("Email");
        docUsers = (HashMap<String, Object>) getIntent().getSerializableExtra("UserDetails");

        if(userEmail == null) { userEmail = "placeholdertext"; }

        Toast.makeText(this, "Email: " + userEmail, Toast.LENGTH_LONG).show();
        etEmail.setText(userEmail);

        btnSendCode.setOnClickListener(view -> sendCode());
    }

    private void sendVerification(String email, DialogEmailVerify dialog) {
        String hsEmail = "holdsafety.ph@gmail.com";
        String hsPass = "HoldSafety@4qmag";
        String code = randomNumber();

        List<String> recipients = Collections.singletonList(email);
        String subject = "HoldSafety App Verification Code";
        String message = "Please enter the verification code in your HoldSafety app:" +
                "\n\n" + code;

        new MailTask(RegisterOTPActivity.this).execute(hsEmail, hsPass, recipients, subject, message);

        //Dialog Box for entering code
        dialog.btnSubmit.setOnClickListener(view -> {
            if(dialog.etCode.getText() == null || dialog.etCode.length() != 6) {
                dialog.etCode.setError("Invalid verification code.");
                return;
            } else if(!code.equals(dialog.etCode.getText().toString())) {
                dialog.etCode.setError("Invalid verification code.\nPlease check your email.");
            } else if(code.equals(dialog.etCode.getText().toString())) {
                Toast.makeText(this, "Verification Success", Toast.LENGTH_LONG).show();
                //Head to landing page and close dialog box
                dialog.dismissDialog();

                //insert to db with success/failure listeners
                db.collection("users").document(user.getUid()).set(docUsers)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                imageRef.child(user.getUid()).getDownloadUrl().addOnSuccessListener(uri -> {
                                    idUri = String.valueOf(uri);
                                    docUsers.put("imgUri", idUri);
                                    Log.i("URI gDUrl()", idUri);

                                    db.collection("users").document(user.getUid()).update(docUsers)
                                            .addOnSuccessListener(aVoid -> {
                                                Toast.makeText(getApplicationContext(),
                                                        "pushed image to document",
                                                        Toast.LENGTH_SHORT).show();
                                                Log.i(TAG, "Image pushed");
                                            })
                                            .addOnFailureListener(e -> {
                                                Toast.makeText(getApplicationContext(),
                                                        "Error writing document",
                                                        Toast.LENGTH_SHORT).show();
                                                Log.w(TAG, "Error writing document", e);
                                            });
                                });
                            }
                        })
                        .addOnFailureListener(e -> Log.w(TAG, "error", e));

                startActivity(new Intent(this, LandingActivity.class));
                finish();
            }
        });

        dialog.showDialog();

        //Allow code to be resent every 30 seconds
        CountDownTimer timer = new CountDownTimer(30000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) { //ticking timer
                long timeRemaining = millisUntilFinished / 1000;
                dialog.timeRemaining.setOnClickListener(null); // make unclickable while ticking
                dialog.timeRemaining.setText("Resend in " + timeRemaining + " seconds");
            }

            @Override
            public void onFinish() { //finish timer
                dialog.timeRemaining.setText("Resend Code");
                dialog.timeRemaining.setTextColor(Color.BLUE);
                dialog.timeRemaining.setOnClickListener(v -> {
                    sendCode();
                    dialog.timeRemaining.setTextColor(Color.LTGRAY);
                    dialog.dismissDialog();
                });
            }
        };
        timer.start();
    }

    private void sendCode() {
        //Email data validation
        String emailRegex = "^(.+)@(.+)$";
        Pattern emailPattern = Pattern.compile(emailRegex);

        if(etEmail.getText() != null) {
            Matcher emailMatcher = emailPattern.matcher(etEmail.getText());

            if(emailMatcher.matches()) {
                DialogEmailVerify dialog = new DialogEmailVerify(this);
                sendVerification(etEmail.getText().toString(), dialog);
            } else {
                etEmail.setError("Please enter valid email");
            }
        }
    }

    private String randomNumber() {
        String code = "";

        int random  = new Random().nextInt(999999 + 1);
        code = String.valueOf(random);

        while(code.length() != 6) {
            code = "0" + code;
        }

        return code;
    }
}
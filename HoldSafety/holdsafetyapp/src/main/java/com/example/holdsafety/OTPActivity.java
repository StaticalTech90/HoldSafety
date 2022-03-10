package com.example.holdsafety;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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

public class OTPActivity extends AppCompatActivity {
    FirebaseAuth mAuth;
    FirebaseFirestore db;
    FirebaseUser user;
    StorageReference imageRef;
    LogHelper logHelper;

    ImageView btnBack;
    Button btnSendCode;
    EditText etEmail;
    TextView txtTimeRemaining;

    private String intentSource = null;
    String userEmail, userPassword;
    String code = null; //OTP code
    HashMap<String, Object> newEmail = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_otp);

        //Firebase
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        imageRef = FirebaseStorage.getInstance().getReference("id");
        logHelper = new LogHelper(getApplicationContext(), mAuth, user, this);

        etEmail = findViewById(R.id.txtEmail);
        txtTimeRemaining = findViewById(R.id.txtTimeRemaining);
        btnBack = findViewById(R.id.backArrow);
        btnSendCode = findViewById(R.id.btnSendCode);

        //Get extras
        intentSource = getIntent().getStringExtra("Source");
        userEmail = getIntent().getStringExtra("Email");
        userPassword = getIntent().getStringExtra("Password");

        if(userEmail == null) { userEmail = "placeholdertext"; }

        Toast.makeText(this, "Email: " + userEmail, Toast.LENGTH_LONG).show();
        etEmail.setText(userEmail);

        btnBack.setOnClickListener(view -> goBack());
        btnSendCode.setOnClickListener(view -> sendCode());

        Log.d("INTENT", "Intent started by: " + intentSource);
    }

    private void sendCode() {
        //Email data validation
        String emailRegex = "^(.+)@(.+)$";
        Pattern emailPattern = Pattern.compile(emailRegex);

        if(etEmail.getText() != null) {
            Matcher emailMatcher = emailPattern.matcher(etEmail.getText());

            if(emailMatcher.matches()) {
                DialogEmailVerify dialog = new DialogEmailVerify(this);
                logHelper.saveToFirebase("sendCode", "SUCCESS", "Email matches; sending code");
                sendVerification(etEmail.getText().toString(), dialog);
            } else {
                etEmail.setError("Please enter valid email");
            }
        }
    }

    private void sendVerification(String email, DialogEmailVerify dialog) {
        String hsEmail = "holdsafety.ph@gmail.com";
        String hsPass = "HoldSafety@4qmag";
        code = randomNumber();

        List<String> recipients = Collections.singletonList(email);
        String subject = "HoldSafety App Verification Code";
        String message = "Please enter the verification code in your HoldSafety app:" +
                "\n\n" + code;

        new MailTask(OTPActivity.this).execute(hsEmail, hsPass, recipients, subject, message);

        //Dialog Box for entering code
        dialog.btnSubmit.setOnClickListener(view -> {
            if(TextUtils.isEmpty(dialog.etCode.toString())) {
                dialog.etCode.setError("Please enter your code.");
            } else if(code == null){
                dialog.etCode.setError("Your code expired. Please retry.");
                //Toast.makeText(this, "Your code expired. Please retry.", Toast.LENGTH_LONG).show();
            } else if(code.equals(dialog.etCode.getText().toString())) {
                logHelper.saveToFirebase("sendVerification", "SUCCESS", "Verification Success");

                Toast.makeText(this, "Verification Success", Toast.LENGTH_LONG).show();
                //Close dialog box
                dialog.dismissDialog();

                //insert to db depending on which activity started this
                if(intentSource.equals("RegisterActivity")) {
                    Log.i("REGISTRATION", "OTP Registration in progress...");
                    Intent otpResult = new Intent(OTPActivity.this, RegisterActivity.class);
                    setResult(RESULT_OK, otpResult);
                    finish();
                } else if(intentSource.equals("AccountDetailsActivity")) { //update the user's email
                    Log.i("Email", "Changing user's email in progress...");
                    newEmail.put("Email", userEmail);

                    db.collection("users").document(user.getUid()).update(newEmail)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(getApplicationContext(), "Email updated", Toast.LENGTH_SHORT).show();
                                Log.i("CHANGE EMAIL", "Email updated");

                                Log.d("Email", "Sending RESULT_OK back to AccountDetailsActivity...");
                                logHelper.saveToFirebase("sendVerification", "SUCCESS", "user registered");
                                Intent otpResult = new Intent(OTPActivity.this, AccountDetailsActivity.class);
                                setResult(RESULT_OK, otpResult);
                            })
                            .addOnFailureListener(e -> {
                                logHelper.saveToFirebase("sendVerification", "ERROR", e.getLocalizedMessage());

                                Toast.makeText(getApplicationContext(), "Error updating email", Toast.LENGTH_SHORT).show();
                                Log.w(TAG, "Error updating email", e);
                            });
                    finish();
                }
            } else {
                dialog.etCode.setError("Invalid verification code.\nPlease check your email.");
            }
        });
        dialog.showDialog();

        //Allow code to be resent every 60 seconds
        CountDownTimer timer = new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) { //ticking timer
                long timeRemaining = millisUntilFinished / 1000;
                dialog.timeRemaining.setOnClickListener(null); // make unclickable while ticking
                dialog.timeRemaining.setText("Resend in " + timeRemaining + " seconds");
            }

            @Override
            public void onFinish() { //finish timer
                code = null; //code expired. require to resend
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

    private String randomNumber() {
        String code;

        int random  = new Random().nextInt(999999 + 1);
        code = String.valueOf(random);
        while(code.length() != 6) {
            code = "0" + code;
        }
        return code;
    }

    private void goBack() { finish(); }
}
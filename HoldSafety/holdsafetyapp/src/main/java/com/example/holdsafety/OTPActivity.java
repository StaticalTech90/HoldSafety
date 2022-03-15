package com.example.holdsafety;

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
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OTPActivity extends AppCompatActivity {
    FirebaseAuth mAuth;
    FirebaseFirestore db;
    FirebaseUser user;
    StorageReference imageRef;

    ImageView btnBack;
    Button btnSendCode;
    EditText etEmail;
    TextView txtTimeRemaining;

    private int requestCode;
    String userEmail, userPassword, userNumber;
    String code = null; //OTP code

    private static final int OTP_REQUEST_CODE_REGISTER = 2000;
    public static final int OTP_REQUEST_CODE_CHANGE_EMAIL = 5000;
    public static final int OTP_REQUEST_CODE_CHANGE_NUMBER = 5001;
    public static final int OTP_REQUEST_CODE_CHANGE_EMAIL_AND_NUMBER = 5002;
    public static final int OTP_REQUEST_CODE_REMOVE_ACCOUNT = 9000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_otp);

        //Firebase
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        imageRef = FirebaseStorage.getInstance().getReference("id");

        etEmail = findViewById(R.id.txtEmail);
        txtTimeRemaining = findViewById(R.id.txtTimeRemaining);
        btnBack = findViewById(R.id.backArrow);
        btnSendCode = findViewById(R.id.btnSendCode);

        //Get extras
        requestCode = getIntent().getIntExtra("RequestCode", 0);
        userEmail = getIntent().getStringExtra("Email");
        userPassword = getIntent().getStringExtra("Password");
        userNumber = getIntent().getStringExtra("MobileNumber");

        if(userEmail == null) {
            etEmail.setHint("Enter email here");
            userEmail = "";
        }

        Toast.makeText(this, "Email: " + userEmail, Toast.LENGTH_LONG).show();
        etEmail.setText(userEmail);

        btnBack.setOnClickListener(view -> goBack());
        btnSendCode.setOnClickListener(view -> sendCode());

        Log.d("INTENT", "Intent started by: " + requestCode);
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
            } else if(code == null) {
                dialog.etCode.setError("Your code expired. Please retry.");
                //Toast.makeText(this, "Your code expired. Please retry.", Toast.LENGTH_LONG).show();
            } else if(code.equals(dialog.etCode.getText().toString())) {
                Toast.makeText(this, "Verification Success", Toast.LENGTH_LONG).show();
                //Close dialog box
                dialog.dismissDialog();

                //action depends on how this activity was called
                if(requestCode == OTP_REQUEST_CODE_REGISTER) {
                    Log.i("REGISTRATION", "OTP Registration in progress...");

                    Intent otpResult = new Intent(OTPActivity.this, RegisterActivity.class);
                    setResult(RESULT_OK, otpResult);
                    //startActivity(otpResult);
                    finish();
                } else if(requestCode == OTP_REQUEST_CODE_CHANGE_EMAIL) { //update the user's email
                    Log.i("Email", "Changing user's email in progress...");

                    Toast.makeText(getApplicationContext(), "Email verified", Toast.LENGTH_SHORT).show();
                    Log.i("Email", "Email verified");
                    Log.d("Email", "Sending RESULT_OK back to AccountDetailsActivity...");

                    Intent otpResult = new Intent(OTPActivity.this, AccountDetailsActivity.class);
                    otpResult.putExtra("Password", userPassword);
                    setResult(RESULT_OK, otpResult);
                    finish();
                } else if(requestCode == OTP_REQUEST_CODE_CHANGE_NUMBER) {
                    Log.i("MobileNumber", "Changing user's mobile number in progress...");

                    Toast.makeText(getApplicationContext(), "Mobile number verified", Toast.LENGTH_SHORT).show();
                    Log.i("MobileNumber", "Mobile number verified");
                    Log.d("MobileNumber", "Sending RESULT_OK back to AccountDetailsActivity...");

                    Intent otpResult = new Intent(OTPActivity.this, AccountDetailsActivity.class);
                    setResult(RESULT_OK, otpResult);
                    finish();
                } else if(requestCode == OTP_REQUEST_CODE_CHANGE_EMAIL_AND_NUMBER) {
                    Log.i("EmailNum", "Changing user's email and number progress...");

                    Toast.makeText(getApplicationContext(), "Email verified", Toast.LENGTH_SHORT).show();
                    Log.i("EmailNum", "Email verified");
                    Log.d("EmailNum", "Sending RESULT_OK back to AccountDetailsActivity...");

                    Intent otpResult = new Intent(OTPActivity.this, AccountDetailsActivity.class);
                    otpResult.putExtra("Password", userPassword);
                    setResult(RESULT_OK, otpResult);
                    finish();
                } else if(requestCode == OTP_REQUEST_CODE_REMOVE_ACCOUNT) {
                    Log.i("RemoveAccount", "Removing Account in progress...");
                    Log.d("RemoveAccount", "Sending RESULT_OK back to AccountDetailsActivity...");

                    Intent otpResult = new Intent(OTPActivity.this, AccountDetailsActivity.class);
                    setResult(RESULT_OK, otpResult);
                    finish();
                }
            } else {
                dialog.etCode.setError("Invalid verification code.\nPlease check your email.");
            }
        });
        dialog.showDialog();

        //Allow code to be resent every 60 seconds
        CountDownTimer timer = new CountDownTimer(180000, 1000) {
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
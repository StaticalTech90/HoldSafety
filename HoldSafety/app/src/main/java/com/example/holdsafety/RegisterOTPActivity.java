package com.example.holdsafety;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterOTPActivity extends AppCompatActivity {
    Button btnSendCode;
    EditText etEmail;
    TextView txtTimeRemaining;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_otpactivity);

        btnSendCode = findViewById(R.id.btnSendCode);
        etEmail = findViewById(R.id.txtEmail);
        txtTimeRemaining = findViewById(R.id.txtTimeRemaining);

        //Get email from user input in previous activity
        String userEmail;
        userEmail = getIntent().getStringExtra("Email");

        if(userEmail == null) {
            userEmail = "placeholdertext";
        }
        Toast.makeText(this, "Email: " + userEmail, Toast.LENGTH_LONG).show();
        etEmail.setText(userEmail);

        btnSendCode.setOnClickListener(view -> {
            //Email data validation
            String emailRegex = "^(.+)@(.+)$";
            Pattern emailPattern = Pattern.compile(emailRegex);

            if(etEmail.getText() != null) {
                Matcher emailMatcher = emailPattern.matcher(etEmail.getText());

                if(!emailMatcher.matches()) {
                    etEmail.setError("Please enter valid email");
                } else { //User must input code before timer ends
                    DialogEmailVerify dialog = new DialogEmailVerify(this);

                    //Countdown timer
                    new CountDownTimer(60000, 1000) {
                        @Override
                        public void onTick(long millisUntilFinished) {
                            long timeRemaining = millisUntilFinished / 1000;
                            dialog.timeRemaining.setText("Time Remaining: " + timeRemaining);
                        }

                        @Override
                        public void onFinish() {
                            dialog.dismissDialog();
                        }
                    }.start();

                    sendVerification(etEmail.getText().toString(), dialog);
                }
            }
        });
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
                startActivity(new Intent(this, LandingActivity.class));
                finish();
            }
        });

        dialog.showDialog();
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